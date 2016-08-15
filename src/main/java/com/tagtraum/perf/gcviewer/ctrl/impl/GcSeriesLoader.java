package com.tagtraum.perf.gcviewer.ctrl.impl;

import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Allows to load an (unordered) list of {@link GCResource} and treats them as a consecutive series of {@link GCResource}s.
 *
 * @author martin.geldmacher
 */
public class GcSeriesLoader {
    private static final Logger logger = Logger.getLogger(GcSeriesLoader.class.getName());
    private final DataReaderFacade dataReaderFacade;

    public GcSeriesLoader(DataReaderFacade dataReaderFacade) {
        this.dataReaderFacade = dataReaderFacade;
    }

    public GCModel load(GcResourceSeries series) throws DataReaderException {
        if (series == null || series.getResourcesInOrder().size() == 0) {
            throw new IllegalArgumentException("No resources to load!");
        }

        Map<Timestamp, GCModel> startTimeToGcModel = determineStartTimePerGcModel(series);
        List<GCModel> sortedModels = sortResources(startTimeToGcModel);
        GCModel mergedModel = mergeModels(sortedModels);
        return mergedModel;
    }

    private Map<Timestamp, GCModel> determineStartTimePerGcModel(GcResourceSeries series) throws DataReaderException {
        Map<Timestamp, GCModel> startTimeToGcModel = new HashMap<>();
        for (GCResource resource : series.getResourcesInOrder()) {
            Optional<GCModel> model = loadGcModel(resource);
            if (model.isPresent()) {
                Timestamp timeStamp = getCreationDate(model.get());
                startTimeToGcModel.put(timeStamp, model.get());
            }
            else {
                logger.log(Level.WARNING, "Failed to load " + resource + " - ignoring it");
            }
        }
        return startTimeToGcModel;
    }

    protected Timestamp getCreationDate(GCModel model) throws DataReaderException {
        Timestamp timeStamp;
        Optional<Timestamp> firstDateStamp = getFirstDateStampFromModel(model);
        if (firstDateStamp.isPresent()) {
            timeStamp = firstDateStamp.get();
        }
        else {
            Optional<Timestamp> firstTimeStamp = getFirstTimeStampFromModel(model);
            if(firstTimeStamp.isPresent()) {
                timeStamp = firstTimeStamp.get();
            }
            else {
                logger.log(Level.WARNING, "Logfile contains neither date- nor timestamp. Using file creation date as"
                        + " fallback. Consider using -XX:+PrintGCDateStamps to enable logging of dates for GC events.");
                timeStamp = getCreationDateFromFile(model);
            }
        }
        return timeStamp;
    }

    protected Optional<Timestamp> getFirstDateStampFromModel(GCModel model) {
        ZonedDateTime firstDateStamp = model.getFirstDateStamp();
        if(firstDateStamp == null)
            return Optional.empty();
        else
            return Optional.of(new GcDateStamp(firstDateStamp));
    }
    protected Optional<Timestamp> getFirstTimeStampFromModel(GCModel model) {
        Optional<Double> firstTimeStamp = model.getFirstTimeStamp();
        if (firstTimeStamp.isPresent())
            return Optional.of(new GcTimeStamp(firstTimeStamp.get()));
        else
            return Optional.empty();
    }

    protected Timestamp getCreationDateFromFile(GCModel model) {
        ZonedDateTime creationDate = Instant.ofEpochMilli(model.getCreationTime()).atZone(ZoneId.systemDefault());
        return new GcDateStamp(creationDate);
    }

    protected List<GCModel> sortResources(Map<Timestamp, GCModel> startTimeToGcModel) throws DataReaderException {
        try {
            List<GCModel> sortedModels = startTimeToGcModel.entrySet()
                    .stream()
                    .sorted((x, y) -> x.getKey().compareTo(y.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
            return sortedModels;
        }
        catch (Exception ex) {
            throw new DataReaderException("Logfile series has mixed date- and timestamps. Can't determine logfile order", ex);
        }
    }

    private Optional<GCModel> loadGcModel(GCResource resource) {
        try {
            return Optional.of(dataReaderFacade.loadModel(resource));
        }
        catch (DataReaderException ex) {
            logger.log(Level.WARNING, "Failed to read " + resource + ". Reason: " + ex.getMessage());
            logger.log(Level.FINER, "Details: ", ex);
            return Optional.empty();
        }
    }

    private GCModel mergeModels(List<GCModel> models) {
        GCModel mergedModel = models.get(0);
        for (int i = 1; i < models.size(); i++) {
            GCModel model = models.get(i);
            Iterator<AbstractGCEvent<?>> iterator = model.getEvents();
            while (iterator.hasNext()) {
                mergedModel.add(iterator.next());
            }
        }

        // Use URL of last contained file. In case of a refresh this is the only file that can have changed
        mergedModel.setURL(models.get(models.size() - 1).getURL());
        return mergedModel;
    }

    interface Timestamp extends Comparable<Timestamp> {
    }

    /**
     * Datestamp of a GC log.
     * Created when -XX:+PrintGCDateStamps is used
     */
    static class GcDateStamp implements Timestamp {

        private ZonedDateTime time;
        public GcDateStamp(ZonedDateTime time) {
            this.time = time;
        }

        @Override
        public int compareTo(Timestamp o) {
            if(o instanceof GcDateStamp)
                return this.time.compareTo(((GcDateStamp)o).time);
            throw new IllegalArgumentException("Can't compare Datestamp with Timestamp: " +o);
        }

        @Override
        public String toString() {
            return "GcDateStamp{" +
                    "time=" + time +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            GcDateStamp that = (GcDateStamp) o;

            return time != null ? time.equals(that.time) : that.time == null;

        }

        @Override
        public int hashCode() {
            return time != null ? time.hashCode() : 0;
        }
    }

    /**
     * Timestamp of a GC log. Relative to application start
     * Created when -XX:+PrintGCTimeStamps is used
     */
    static class GcTimeStamp implements Timestamp {

        private double time;
        public GcTimeStamp(double time) {
            this.time = time;
        }

        @Override
        public int compareTo(Timestamp o) {
            if(o instanceof GcTimeStamp) {
                return Double.compare(time, ((GcTimeStamp)o).time);
            }
            throw new IllegalArgumentException("Can't compare Timestamp with Datestamp: " +o);
        }

        @Override
        public String toString() {
            return "GcTimeStamp{" +
                    "time=" + time +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            GcTimeStamp that = (GcTimeStamp) o;

            return Double.compare(that.time, time) == 0;

        }

        @Override
        public int hashCode() {
            long temp = Double.doubleToLongBits(time);
            return (int) (temp ^ (temp >>> 32));
        }
    }
}

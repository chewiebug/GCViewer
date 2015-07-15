package com.tagtraum.perf.gcviewer.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tagtraum.perf.gcviewer.math.DoubleData;
import com.tagtraum.perf.gcviewer.math.DoubleDataPercentile;
import com.tagtraum.perf.gcviewer.math.IntData;
import com.tagtraum.perf.gcviewer.math.RegressionLine;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.CollectionType;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Generation;

/**
 * Collection of GCEvents.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class GCModel implements Serializable {

    private static final long serialVersionUID = -6479685723904770990L;

    private static final Logger LOG = Logger.getLogger(GCModel.class.getName());

    private List<AbstractGCEvent<?>> allEvents;
    private List<AbstractGCEvent<?>> stopTheWorldEvents;
    private List<GCEvent> gcEvents;
    private List<AbstractGCEvent<?>> gcPhases;
    private List<AbstractGCEvent<?>> vmOperationEvents;
    private List<ConcurrentGCEvent> concurrentGCEvents;
    private List<GCEvent> currentNoFullGCEvents;
    private List<GCEvent> fullGCEvents;
    private FileInformation fileInformation = new FileInformation();

    private Map<String, DoubleData> fullGcEventPauses; // pause information about all full gc events for detailed output
    private Map<String, DoubleData> gcEventPauses; // pause information about all stw events for detailed output
    private Map<String, DoubleData> gcEventPhases; // pause information about all phases for garbage collection events
    private Map<String, DoubleData> concurrentGcEventPauses; // pause information about all concurrent events
    private Map<String, DoubleData> vmOperationEventPauses; // pause information about vm operations ("application stopped")

    private IntData heapAllocatedSizes; // allocated heap size of every event
    private IntData tenuredAllocatedSizes; // allocated tenured size of every event that has this information
    private IntData youngAllocatedSizes; // allocated young size of every event that has this information
    private IntData permAllocatedSizes; // allocated perm size of every event that has this information
    private IntData heapUsedSizes; // used heap of every event
    private IntData tenuredUsedSizes; // used tenured size of every event that has this information
    private IntData youngUsedSizes; // used young size of every event that has this information
    private IntData permUsedSizes; // used perm size of every event that has this information

    private IntData postConcurrentCycleUsedTenuredSizes; // used tenured heap after concurrent collections
    private IntData postConcurrentCycleUsedHeapSizes; // used heap after concurrent collections

    private IntData promotion; // promotion from young to tenured generation during young collections

    private double firstPauseTimeStamp = Double.MAX_VALUE;
    private double lastPauseTimeStamp = 0;
    private DoubleData totalPause;
    private DoubleData fullGCPause;
    private double lastFullGcPauseTimeStamp = 0;
    private DoubleData fullGcPauseInterval; // interval between two stop the Full GC pauses
    private DoubleDataPercentile gcPause; // not full gc but stop the world pause
    private DoubleData vmOperationPause; // "application stopped"
    private double lastGcPauseTimeStamp = 0;
    private DoubleData pauseInterval; // interval between two stop the world pauses
    private DoubleData initiatingOccupancyFraction; // all concurrent collectors; start of concurrent collection
    private long freedMemory;
    private Format format;
    private IntData postGCUsedMemory;
    private IntData postFullGCUsedHeap;
    private IntData freedMemoryByGC;
    private IntData freedMemoryByFullGC;
    private DoubleData postGCSlope;
    private RegressionLine currentPostGCSlope;
    private RegressionLine currentRelativePostGCIncrease;
    private DoubleData relativePostGCIncrease;
    private RegressionLine postFullGCSlope;
    private RegressionLine relativePostFullGCIncrease;
    private URL url;

    /**
     * Contains information about a file.
     *
     * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
     */
    private static class FileInformation implements Serializable {
        private static final long serialVersionUID = 1L;
        private static final Logger logger = Logger.getLogger(FileInformation.class.getName());

        public long creationTime;
        public long lastModified;
        public long length;

        public FileInformation() {
            creationTime = 0;
            creationTime = 0;
            length = 0;
        }

        public FileInformation(File file) {
            if (file == null)
                throw new IllegalArgumentException("File must not  be null!");

            Optional<BasicFileAttributes> fileAttributes = getFileAttributes(file);
            this.lastModified = file.lastModified();
            this.creationTime = determineCreationDate(file, fileAttributes);
            this.length = file.length();
        }

        private Optional<BasicFileAttributes> getFileAttributes(File file) {
            try {
                return Optional.of(Files.readAttributes(file.toPath(), BasicFileAttributes.class));
            }
            catch (IOException ex) {
                logger.log(Level.WARNING, "Failed to read attributes of file " + file + ". Reason: " + ex.getMessage());
                logger.log(Level.FINER, "Details: ", ex);
            }
            return Optional.empty();
        }

        private long determineCreationDate(File file, Optional<BasicFileAttributes> fileAttributes) {
            if (fileAttributes.isPresent()) {
                return fileAttributes.get().creationTime().toMillis();
            }
            else {
                // Creation date is unavailable on unix based oS
                return file.lastModified();
            }
        }

        public void setFileInformation(FileInformation other) {
            this.creationTime = other.creationTime;
            this.lastModified = other.lastModified;
            this.length = other.length;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null) {
                return false;
            }
            if (!(other instanceof FileInformation)) {
                return false;
            }

            FileInformation fileInfo = (FileInformation) other;

            return fileInfo.lastModified == lastModified && fileInfo.creationTime == creationTime && fileInfo.length == length;
        }

        @Override
        public int hashCode() {
            int result = (int) (creationTime ^ (creationTime >>> 32));
            result = 31 * result + (int) (lastModified ^ (lastModified >>> 32));
            result = 31 * result + (int) (length ^ (length >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return FileInformation.class.toString() + "; lastModified=" + lastModified + ", length=" + length;
        }
    }

    public GCModel() {
        this.allEvents = new ArrayList<AbstractGCEvent<?>>();
        this.stopTheWorldEvents = new ArrayList<AbstractGCEvent<?>>();
        this.gcEvents = new ArrayList<GCEvent>();
        this.vmOperationEvents = new ArrayList<AbstractGCEvent<?>>();
        this.gcPhases = new ArrayList<AbstractGCEvent<?>>();
        this.concurrentGCEvents = new ArrayList<ConcurrentGCEvent>();
        this.fullGCEvents = new ArrayList<GCEvent>();
        this.currentNoFullGCEvents = new ArrayList<GCEvent>();
        this.currentPostGCSlope = new RegressionLine();
        this.postFullGCSlope = new RegressionLine();
        this.postGCSlope = new DoubleData();
        this.freedMemoryByGC = new IntData();
        this.freedMemoryByFullGC = new IntData();
        this.postFullGCUsedHeap = new IntData();

        this.postGCUsedMemory = new IntData();
        this.totalPause = new DoubleData();
        this.fullGCPause = new DoubleData();
        this.fullGcPauseInterval = new DoubleData();
        this.gcPause = new DoubleDataPercentile();
        this.vmOperationPause = new DoubleData();
        this.pauseInterval = new DoubleData();
        this.initiatingOccupancyFraction = new DoubleData();
        this.currentRelativePostGCIncrease = new RegressionLine();
        this.relativePostGCIncrease = new DoubleData();
        this.relativePostFullGCIncrease = new RegressionLine();

        this.fullGcEventPauses = new TreeMap<String, DoubleData>();
        this.gcEventPauses = new TreeMap<String, DoubleData>();
        this.gcEventPhases = new TreeMap<String, DoubleData>();
        this.concurrentGcEventPauses = new TreeMap<String, DoubleData>();
        this.vmOperationEventPauses = new TreeMap<String, DoubleData>();

        this.heapAllocatedSizes = new IntData();
        this.permAllocatedSizes = new IntData();
        this.tenuredAllocatedSizes = new IntData();
        this.youngAllocatedSizes = new IntData();

        this.heapUsedSizes = new IntData();
        this.permUsedSizes = new IntData();
        this.tenuredUsedSizes = new IntData();
        this.youngUsedSizes = new IntData();

        this.postConcurrentCycleUsedTenuredSizes = new IntData();
        this.postConcurrentCycleUsedHeapSizes = new IntData();

        this.promotion = new IntData();
    }

    public long getLastModified() {
        return fileInformation.lastModified;
    }

    public long getCreationTime() {
        return fileInformation.creationTime;
    }

    public URL getURL() {
        return url;
    }

    private void printPauseMap(Map<String, DoubleData> pauseMap) {
        for (Map.Entry<String, DoubleData> entry: pauseMap.entrySet()) {
            System.out.println(entry.getKey() + " [n, avg, sum, min, max, 50th, 75th, 95th, 99th]:\t" + entry.getValue().getN() + "\t" + entry.getValue().average() + "\t" + entry.getValue().getSum() + "\t" + entry.getValue().getMin() + "\t" + entry.getValue().getMax() + "\t" + ((DoubleDataPercentile)entry.getValue()).getPercentile(50)+ "\t" + ((DoubleDataPercentile)entry.getValue()).getPercentile(75)+ "\t" + ((DoubleDataPercentile)entry.getValue()).getPercentile(95)+ "\t" + ((DoubleDataPercentile)entry.getValue()).getPercentile(99));
        }
    }

    private void printIntData(String name, IntData data) {
        try {
            System.out.println(name + " (n, avg, stddev, min, max):\t" + data.getN() + "\t" + data.average() + "\t" + data.standardDeviation() + "\t" + data.getMin() + "\t" + data.getMax());
        } catch (IllegalStateException e) {
            System.out.println(name + "\t" + e.toString());
        }
    }

    private void printDoubleData(String name, DoubleData data) {
        try {
            System.out.println(name + " (n, avg, stddev, min, max):\t" + data.getN() + "\t" + data.average() + "\t" + data.standardDeviation() + "\t" + data.getMin() + "\t" + data.getMax());
        } catch (IllegalStateException e) {
            System.out.println(name + "\t" + e.toString());
        }
    }

    public void printDetailedInformation() {
        // TODO delete
        printPauseMap(gcEventPauses);
        printPauseMap(fullGcEventPauses);
        printPauseMap(concurrentGcEventPauses);
        printPauseMap(vmOperationEventPauses);

        printDoubleData("initiatingOccupancyFraction", initiatingOccupancyFraction);

        printIntData("heap size used", heapUsedSizes);
        printIntData("perm size used", permUsedSizes);
        printIntData("tenured size used", tenuredUsedSizes);
        printIntData("young size used", youngUsedSizes);
    }

    private FileInformation readFileInformation(URL url) {
        FileInformation fileInformation = new FileInformation();
        URLConnection urlConnection;
        try {
            if (url.getProtocol().startsWith("http")) {
                urlConnection = url.openConnection();
                ((HttpURLConnection) urlConnection).setRequestMethod("HEAD");
                try (InputStream inputStream = urlConnection.getInputStream()) {
                    fileInformation.length = urlConnection.getContentLength();
                    fileInformation.lastModified = urlConnection.getLastModified();
                }
            }
            else {
                if (url.getProtocol().startsWith("file")) {
                    File file = new File(url.getFile());
                    if (file.exists()) {
                        fileInformation = new FileInformation(file);
                    }
                }
            }
        }
        catch (IOException e) {
            if (LOG.isLoggable(Level.WARNING))
                LOG.log(Level.WARNING, "Failed to obtain age and length of URL " + url, e);
        }

        return fileInformation;
    }

    public void setURL(URL url) {
        this.url = url;
        this.fileInformation.setFileInformation(readFileInformation(url));
    }

    public boolean isDifferent(File otherFile) {
        // we just ignore the file name for now...
        FileInformation fileInformation = new FileInformation(otherFile);

        return !this.fileInformation.equals(fileInformation);
    }

    public boolean isDifferent(URL otherURL) {
        FileInformation fileInfo = readFileInformation(otherURL);

        return !this.fileInformation.equals(fileInfo);
    }

    /**
     * Returns the event that was last added or <code>null</code> if there is none yet.
     *
     * @return last event or <code>null</code>
     */
    public AbstractGCEvent<?> getLastEventAdded() {
        if (allEvents.size() > 0) {
            return allEvents.get(allEvents.size() - 1);
        }
        else {
            return null;
        }
    }

    /**
     * Returns an iterator to all stop the world events (everything that stops the vm to perfom
     * its action - includes vm operations of present).
     *
     * @return iterator to all stop the world events
     */
    public Iterator<AbstractGCEvent<?>> getStopTheWorldEvents() {
        return stopTheWorldEvents.iterator();
    }

    /**
     * Returns an iterator to all garbage collection events (without full gcs / vm operations).
     *
     * @return iterator to all gc events (without full gcs).
     */
    public Iterator<GCEvent> getGCEvents() {
        return gcEvents.iterator();
    }

    /**
     * Returns an iterator to all vm operation events.
     *
     * @return iterator to all vm operation events
     */
    public Iterator<AbstractGCEvent<?>> getVmOperationsEvents() {
        return vmOperationEvents.iterator();
    }

    /**
     * Returns an iterator to all concurrent gc events.
     *
     * @return iterator to all concurrent gc events.
     */
    public Iterator<ConcurrentGCEvent> getConcurrentGCEvents() {
        return concurrentGCEvents.iterator();
    }

    /**
     * Returns an iterator to all events in the order they were added to the model.
     *
     * @return iterator to all events
     */
    public Iterator<AbstractGCEvent<?>> getEvents() {
        return allEvents.iterator();
    }

    /**
     * Returns an iterator to all full gc events.
     *
     * @return iterator to all full gc events
     */
    public Iterator<GCEvent> getFullGCEvents() {
        return fullGCEvents.iterator();
    }

    private DoubleData getDoubleData(String key, Map<String, DoubleData> eventMap) {
        DoubleData data = eventMap.get(key);
        if (data == null) {
            data = new DoubleDataPercentile();
            eventMap.put(key, data);
        }

        return data;
    }

    public void add(AbstractGCEvent<?> abstractEvent) {
        makeSureHasTimeStamp(abstractEvent);

        allEvents.add(abstractEvent);

        if (abstractEvent.isStopTheWorld()) {
            // totalPause must not be added here yet, because in case of vmOperationEvents, the
            // pause might be adjusted
            stopTheWorldEvents.add(abstractEvent);
        }

        if (abstractEvent instanceof ConcurrentGCEvent) {
            ConcurrentGCEvent concEvent = (ConcurrentGCEvent) abstractEvent;
            addConcurrentGcEvent(concEvent);
        }
        else if (abstractEvent instanceof GCEvent) {
            // collect statistics about all stop the world events
            GCEvent event = (GCEvent) abstractEvent;
            addGcEvent(event);
        }
        else if (abstractEvent instanceof VmOperationEvent) {
            VmOperationEvent vmOperationEvent = (VmOperationEvent) abstractEvent;
            addVmOperationEvent(vmOperationEvent);
        }

        if (size() == 1 || (size() > 1 && abstractEvent.getTimestamp() > 0.0)) {
            // timestamp == 0 is only valid, if it is the first event.
            // sometimes, no timestamp is present, because the line is mixed -> don't count these here
            firstPauseTimeStamp = Math.min(firstPauseTimeStamp, abstractEvent.getTimestamp());
        }
        lastPauseTimeStamp = Math.max(lastPauseTimeStamp, abstractEvent.getTimestamp());
        if (abstractEvent.isStopTheWorld()) {
            // add to total pause here, because then adjusted VmOperationEvents are added correctly
            // as well
            totalPause.add(abstractEvent.getPause());
        }
    }

    private void addConcurrentGcEvent(ConcurrentGCEvent concEvent) {
        concurrentGCEvents.add(concEvent);

        DoubleData pauses = getDoubleData(concEvent.getExtendedType().getName(), concurrentGcEventPauses);
        pauses.add(concEvent.getPause());

        if (concEvent.hasMemoryInformation() && concEvent.isConcurrentCollectionEnd()) {
            // register postConcurrentCycleUsedSizes, if event contains memory information. Otherwise deduce it (see in handling of GCEvent)
            updatePostConcurrentCycleUsedSizes(concEvent);
        }
    }

    private void addGcEvent(GCEvent event) {
        updateHeapSizes(event);

        updateGcPauseInterval(event);

        updatePromotion(event);

        if (event.isInitialMark()) {
            updateInitiatingOccupancyFraction(event);
        }
        if (size() > 1 && allEvents.get(allEvents.size() - 2).isConcurrentCollectionEnd() && !allEvents.get(allEvents.size() - 2).hasMemoryInformation()) {
            // only deduce postConcurrentCycleUsedSizes, if concurrent event itself does not contain memory information
            updatePostConcurrentCycleUsedSizes(event);
        }

        freedMemory += event.getPreUsed() - event.getPostUsed();

        if (!event.isFull()) {
            addGcEventPause(event);
        }
        else {
            addFullGcEventPauses(event);
        }

        if (!event.getPhases().isEmpty()) {
            addGcEventPhases(event);
        }
    }

    private void addGcEventPause(GCEvent event) {
        // make a difference between stop the world events, which only collect from some generations...
        DoubleData pauses = getDoubleData(event.getTypeAsString(), gcEventPauses);
        pauses.add(event.getPause());

        gcEvents.add(event);
        postGCUsedMemory.add(event.getPostUsed());
        freedMemoryByGC.add(event.getPreUsed() - event.getPostUsed());
        currentNoFullGCEvents.add(event);
        currentPostGCSlope.addPoint(event.getTimestamp(), event.getPostUsed());
        currentRelativePostGCIncrease.addPoint(currentRelativePostGCIncrease.getPointCount(), event.getPostUsed());
        gcPause.add(event.getPause());
    }

    private void addFullGcEventPauses(GCEvent event) {
        // ... as opposed to all generations
        DoubleData pauses = getDoubleData(event.getTypeAsString(), fullGcEventPauses);
        pauses.add(event.getPause());

        updateFullGcPauseInterval(event);
        fullGCEvents.add(event);
        postFullGCUsedHeap.add(event.getPostUsed());
        int freed = event.getPreUsed() - event.getPostUsed();
        freedMemoryByFullGC.add(freed);
        fullGCPause.add(event.getPause());
        postFullGCSlope.addPoint(event.getTimestamp(), event.getPostUsed());
        relativePostFullGCIncrease.addPoint(relativePostFullGCIncrease.getPointCount(), event.getPostUsed());

        // process no full-gc run data
        if (currentPostGCSlope.hasPoints()) {
            // make sure we have at least _two_ data points
            if (currentPostGCSlope.isLine()) {
                postGCSlope.add(currentPostGCSlope.slope(), currentPostGCSlope.getPointCount());
                relativePostGCIncrease.add(currentRelativePostGCIncrease.slope(), currentRelativePostGCIncrease.getPointCount());
            }
            currentPostGCSlope.reset();
            currentRelativePostGCIncrease.reset();
        }
    }

    private void addGcEventPhases(GCEvent event) {
        DoubleData phases;
        AbstractGCEvent<?> phaseEvent;

        for (int i = 0; i < event.getPhases().size(); i++) {
            phaseEvent = event.getPhases().get(i);
            phases = getDoubleData(phaseEvent.getTypeAsString(), gcEventPhases);
            phases.add(phaseEvent.getPause());
        }

        gcPhases.addAll(event.getPhases());
    }

    private void addVmOperationEvent(VmOperationEvent vmOperationEvent) {
        adjustPause(vmOperationEvent);
        if (vmOperationEvent.getTimestamp() < 0.000001) {
            setTimeStamp(vmOperationEvent);
        }
        vmOperationPause.add(vmOperationEvent.getPause());
        vmOperationEvents.add(vmOperationEvent);
        DoubleData vmOpPauses = getDoubleData(vmOperationEvent.getTypeAsString(), vmOperationEventPauses);
        vmOpPauses.add(vmOperationEvent.getPause());
    }

    private void makeSureHasTimeStamp(AbstractGCEvent<?> abstractEvent) {
        if (size() >= 1 && abstractEvent.getTimestamp() < 0.000001 && abstractEvent.getDatestamp() != null) {
            // looks like there is no timestamp set -> set one, because a lot depends on the timestamps
            abstractEvent.setTimestamp(ChronoUnit.MILLIS.between(getFirstDateStamp(), abstractEvent.getDatestamp()) / 1000.0);
        }
    }

    private void updatePostConcurrentCycleUsedSizes(ConcurrentGCEvent event) {
        postConcurrentCycleUsedTenuredSizes.add(event.getPreUsed());
        postConcurrentCycleUsedHeapSizes.add(event.getPreUsed());
    }

    private void updatePostConcurrentCycleUsedSizes(GCEvent event) {
            // Most interesting is the size of the life objects immediately after a concurrent cycle.
            // Since the "concurrent-end" events don't have the heap size information, the next event
            // after is taken to get the information. Young generation, that has already filled up
            // again since the concurrent-end should not be counted, so take tenured size, if available.
        GCEvent afterConcurrentEvent = event;
        if (event.hasDetails()) {
            afterConcurrentEvent = event.getTenured();
        }

        postConcurrentCycleUsedTenuredSizes.add(afterConcurrentEvent.getPreUsed());
        postConcurrentCycleUsedHeapSizes.add(event.getPreUsed());
    }

    private void adjustPause(VmOperationEvent vmOpEvent) {
        if (stopTheWorldEvents.size() > 1) {
            AbstractGCEvent<?> previousEvent = stopTheWorldEvents.get(stopTheWorldEvents.size() - 2);

            // if the event directly before this event is also a VM_OPERATION event,
            // it was a VM_OPERATION without gc pause -> whole pause is "overhead"
            if (!previousEvent.getExtendedType().getCollectionType().equals(CollectionType.VM_OPERATION)) {

                // only count overhead of vmOpEvent, not whole pause,
                // because it includes the previous stop the world event
                double adjustedPause = vmOpEvent.getPause() - previousEvent.getPause();
                if (adjustedPause > 0) {
                    vmOpEvent.setPause(adjustedPause);
                    adjustTimeStamp(previousEvent, vmOpEvent);
                }
                else {
                    // this happens if the first VM_OPERATION event after a GCEvent could not be read (mixed with concurrent event)
                    // and the next is used to calculate the overhead
                    LOG.fine("vmOpEvent at " + vmOpEvent.getTimestamp()
                            + " should not have negative pause -> no adjustment made");
                }
            }
        }
    }

    /**
     * Make sure, time / datestamp of <code>vmOpEvent</code> is at least as long later as the
     * pause duration of <code>previousEvent</code>.
     *
     * @param previousEvent event just before <code>vmOpEvent</code>
     * @param vmOpEvent     event to be adjusted
     */
    private void adjustTimeStamp(AbstractGCEvent<?> previousEvent, VmOperationEvent vmOpEvent) {
        if (previousEvent.getTimestamp() + previousEvent.getPause() > vmOpEvent.getTimestamp()) {
            vmOpEvent.setTimestamp(previousEvent.getTimestamp() + previousEvent.getPause());
            if (previousEvent.getDatestamp() != null) {
                Duration adjustment = Duration.ofMinutes((long) Math.rint(previousEvent.getPause() / 60))
                        .plus((long) Math.rint(previousEvent.getPause()), ChronoUnit.SECONDS)
                        .plus((long) Math.rint(previousEvent.getPause() * 1000), ChronoUnit.MILLIS);
                ZonedDateTime adjustedDatestamp = previousEvent.getDatestamp().plus(adjustment);
                vmOpEvent.setDateStamp(adjustedDatestamp);
            }
        }
    }

    private void setTimeStamp(VmOperationEvent vmOpEvent) {
        AbstractGCEvent<?> previousEvent = stopTheWorldEvents.size() > 1
                ? stopTheWorldEvents.get(stopTheWorldEvents.size() - 2)
                : null;

        if (previousEvent != null) {
            adjustTimeStamp(previousEvent, vmOpEvent);
        }
    }

    /**
     * Promotion is the amount of memory that is promoted from young to tenured space during
     * a collection of the young space.
     *
     * @param event
     */
    private void updatePromotion(GCEvent event) {
        if (event.getGeneration().equals(Generation.YOUNG) && event.hasDetails() && !event.isFull()) {

            GCEvent youngEvent = null;
            for (Iterator<GCEvent> i = event.details(); i.hasNext(); ) {
                GCEvent ev = i.next();
                if (ev.getGeneration().equals(Generation.YOUNG)) {
                    youngEvent = ev;
                    break;
                }
            }

            if (youngEvent != null) {
                promotion.add((youngEvent.getPreUsed() - youngEvent.getPostUsed())
                        - (event.getPreUsed() - event.getPostUsed())
                );
            }
        }
    }

    private void updateGcPauseInterval(GCEvent event) {
        if (lastGcPauseTimeStamp > 0) {
            if (!event.isConcurrencyHelper()) {
                // JRockit sometimes has special timestamps that seem to go back in time,
                // omit them here
                if (event.getTimestamp() - lastGcPauseTimeStamp >= 0) {
                    pauseInterval.add(event.getTimestamp() - lastGcPauseTimeStamp);
                }
                lastGcPauseTimeStamp = event.getTimestamp();
            }
        } else {
            // interval between startup of VM and first gc event should be omitted because
            // startup time of VM is included.
            lastGcPauseTimeStamp = event.getTimestamp();
        }
    }

    private void updateFullGcPauseInterval(GCEvent event) {
        if (lastFullGcPauseTimeStamp > 0) {
            if (!event.isConcurrencyHelper()) {
                // JRockit sometimes has special timestamps that seem to go back in time,
                // omit them here
                if (event.getTimestamp() - lastFullGcPauseTimeStamp >= 0) {
                    fullGcPauseInterval.add(event.getTimestamp() - lastFullGcPauseTimeStamp);
                }
                lastFullGcPauseTimeStamp = event.getTimestamp();
            }
        } else {
            // interval between startup of VM and first gc event should be omitted because
            // startup time of VM is included.
            lastFullGcPauseTimeStamp = event.getTimestamp();
        }
    }

    private void updateInitiatingOccupancyFraction(GCEvent event) {
        GCEvent initialMarkEvent = event;

        if (event.hasDetails()) {
            Iterator<GCEvent> i = event.details();
            while (i.hasNext()) {
                GCEvent gcEvent = i.next();
                if (gcEvent.isInitialMark()) {
                    initialMarkEvent = gcEvent;
                    break;
                }
            }
        }

        // getTotal() returns 0 only if just the memory information could not be parsed
        // which can be the case with java 7 G1 algorithm (mixed with concurrent event)
        if (initialMarkEvent != null && initialMarkEvent.getTotal() > 0) {
            initiatingOccupancyFraction.add(initialMarkEvent.getPreUsed() / (double) initialMarkEvent.getTotal());
        }
    }

    private void updateHeapSizes(GCEvent event) {
        // event always contains heap size
        if (event.getTotal() > 0) {
            heapAllocatedSizes.add(event.getTotal());
            heapUsedSizes.add(event.getPreUsed());
        }

        if (event.hasDetails()) {
            // if details are present, young and tenured are always assumed to be present
            // because one can be derived from the other
            GCEvent young = event.getYoung();
            if (young != null) {
                youngAllocatedSizes.add(young.getTotal());
                youngUsedSizes.add(young.getPreUsed());
            }

            GCEvent tenured = event.getTenured();
            if (tenured != null) {
                tenuredAllocatedSizes.add(tenured.getTotal());
                tenuredUsedSizes.add(tenured.getPreUsed());
            }

            GCEvent perm = event.getPerm();
            if (perm != null) {
                permAllocatedSizes.add(perm.getTotal());
                permUsedSizes.add(perm.getPreUsed());
            }
        }
    }

    public int size() {
        return allEvents.size();
    }

    /**
     * Get all types of events in the order they were added to the model.
     *
     * @param index index of event
     * @return event at <code>index</code>
     * @throws IndexOutOfBoundsException if <code>index</code> is out of bounds
     */
    public AbstractGCEvent<?> get(int index) {
        return allEvents.get(index);
    }

    /**
     * @return Statistical data about pauses caused by full garbage collections.
     */
    public DoubleData getFullGCPause() {
        return fullGCPause;
    }

    /**
     * @return Statistical data about pauses interval between full garbage collections.
     */
    public DoubleData getFullGCPauseInterval() {
        return fullGcPauseInterval;
    }

    /**
     * @return Statistical data about pauses caused by garbage collections (full gcs excluded).
     */
    public DoubleData getGCPause() {
        return gcPause;
    }

    /**
     * @return Statistical data about pauses caused by vm operations other than gc pauses ("application stopped").
     */
    public DoubleData getVmOperationPause() {
        return vmOperationPause;
    }

    /**
     * @return Interval between gc pauses (full gcs excluded).
     */
    public DoubleData getPauseInterval() {
        return pauseInterval;
    }

    /**
     * Return statistical data about fraction of tenured heap when concurrent collection cycles
     * are started.
     *
     * @return statistical data about tenured heap occupation at start of concurrent collections
     */
    public DoubleData getCmsInitiatingOccupancyFraction() {
        return initiatingOccupancyFraction;
    }

    /**
     * @return The increase in memory consumption after a full collection in relation to the amount that was
     * used after the previous full collection.
     */
    public RegressionLine getRelativePostFullGCIncrease() {
        return relativePostFullGCIncrease;
    }

    /**
     * @return The increase in memory consumption after a collection in relation to the amount that was
     * used after the previous collection.
     */
    public DoubleData getRelativePostGCIncrease() {
        return relativePostGCIncrease;
    }

    /**
     * @return The average slope of the regression lines of the memory consumption after
     * a garbage collection in between <em>full</em> garbage collections.
     * <p>
     * The unit is kb/s.
     */
    public double getPostGCSlope() {
        return postGCSlope.average();
    }

    public RegressionLine getCurrentPostGCSlope() {
        return currentPostGCSlope;
    }

    public RegressionLine getPostFullGCSlope() {
        return postFullGCSlope;
    }

    /**
     * @return Heap memory freed by a (small) garbage collection.
     */
    public IntData getFreedMemoryByGC() {
        return freedMemoryByGC;
    }

    /**
     * @return Heap memory freed by a <em>full</em> garbage collection.
     */
    public IntData getFreedMemoryByFullGC() {
        return freedMemoryByFullGC;
    }

    /**
     * @return Heap memory consumption after a (small) garbage collection.
     */
    public IntData getFootprintAfterGC() {
        return postGCUsedMemory;
    }

    /**
     * @return Heap memory consumption after a <em>full</em> garbage collection.
     */
    public IntData getFootprintAfterFullGC() {
        return postFullGCUsedHeap;
    }

    /**
     * @return Pause in sec.
     */
    public DoubleData getPause() {
        return totalPause;
    }

    public Map<String, DoubleData> getGcEventPauses() {
        return gcEventPauses;
    }

    public Map<String, DoubleData> getGcEventPhases() {
        return gcEventPhases;
    }

    public Map<String, DoubleData> getFullGcEventPauses() {
        return fullGcEventPauses;
    }

    public Map<String, DoubleData> getVmOperationEventPauses() {
        return vmOperationEventPauses;
    }

    public Map<String, DoubleData> getConcurrentEventPauses() {
        return concurrentGcEventPauses;
    }

    /**
     * @return Throughput in percent.
     */
    public double getThroughput() {
        return 100
                * (getRunningTime() - totalPause.getSum())
                / getRunningTime();
    }

    /**
     * @return max heap allocated for every event
     */
    public IntData getHeapAllocatedSizes() {
        return heapAllocatedSizes;
    }

    /**
     * @return max heap used for every event
     */
    public IntData getHeapUsedSizes() {
        return heapUsedSizes;
    }

    /**
     * @return perm sizes allocated for every event that contained one (only if detailed logging is active and
     * and all spaces were collected)
     */
    public IntData getPermAllocatedSizes() {
        return permAllocatedSizes;
    }

    /**
     * @return perm sizes used for every event that has the information
     */
    public IntData getPermUsedSizes() {
        return permUsedSizes;
    }

    /**
     * @return tenured sizes allocated for every event that contained one (only if detailed logging is active)
     */
    public IntData getTenuredAllocatedSizes() {
        return tenuredAllocatedSizes;
    }

    /**
     * @return tenured sizes used for every event that contained one (only if detailed logging is active)
     */
    public IntData getTenuredUsedSizes() {
        return tenuredUsedSizes;
    }

    /**
     * @return young sizes allocated for every event that contained one (only if detailed logging is active)
     */
    public IntData getYoungAllocatedSizes() {
        return youngAllocatedSizes;
    }

    /**
     * @return young sizes used for every event that contained one (only if detailed logging is active)
     */
    public IntData getYoungUsedSizes() {
        return youngUsedSizes;
    }

    /**
     * @return Sizes of tenured heap (or if not available total heap) immediately after completion of
     * a concurrent cycle.
     */
    public IntData getPostConcurrentCycleTenuredUsedSizes() {
        return postConcurrentCycleUsedTenuredSizes;
    }

    /**
     * @return Sizes of heap immediately after completion of a concurrent cycle.
     */
    public IntData getPostConcurrentCycleHeapUsedSizes() {
        return postConcurrentCycleUsedHeapSizes;
    }

    /**
     * @return promotion information for all young collections (how much memory was promoted to
     * tenured space per young collection?)
     */
    public IntData getPromotion() {
        return promotion;
    }

    /**
     * @return Footprint in KB.
     */
    public long getFootprint() {
        return heapAllocatedSizes.getMax();
    }

    /**
     * @return Running time in sec.
     */
    public double getRunningTime() {
        return lastPauseTimeStamp - firstPauseTimeStamp
                + (stopTheWorldEvents.size() > 0
                ? stopTheWorldEvents.get(stopTheWorldEvents.size() - 1).getPause()
                : 0);
    }

    /**
     * @return The timestamp of the first event in the log (which usually is probably never exactly 0)
     */
    public double getFirstPauseTimeStamp() {
        return firstPauseTimeStamp;
    }

    /**
     * @return The timestamp of the last event in the log
     */
    public double getLastPauseTimeStamp() {
        return lastPauseTimeStamp;
    }

    /**
     * @return Freed memory in KB.
     */
    public long getFreedMemory() {
        return freedMemory;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public boolean hasCorrectTimestamp() {
        return format == Format.IBM_VERBOSE_GC || format == Format.SUN_X_LOG_GC || format == Format.SUN_1_2_2VERBOSE_GC || format == Format.UNIFIED_JVM_LOGGING;
    }

    public boolean hasDateStamp() {
        return allEvents.size() > 0
                ? get(0).getDatestamp() != null
                : false;
    }

    public ZonedDateTime getFirstDateStamp() {
        return allEvents.size() > 0
                ? get(0).getDatestamp()
                : null;
    }

    public Optional<Double> getFirstTimeStamp() {
        if (allEvents.size() > 0)
            return Optional.of(get(0).getTimestamp());
        else
            return Optional.empty();
    }

    /**
     * Best effort calculation of this {@link GCModel}s start date based on the available information
     *
     * @return the most probable start date of this {@link GCModel}
     */
    public ZonedDateTime getStartDate() {
        ZonedDateTime suggestedStartDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(getLastModified()), ZoneId.systemDefault());
        if (hasDateStamp()) {
            suggestedStartDate = getFirstDateStamp();
        } else if (hasCorrectTimestamp()) {
            double runningTimeInSeconds = getRunningTime();
            long runningTimeInMillis = (long) (runningTimeInSeconds * 1000d);
            suggestedStartDate = suggestedStartDate.minus(runningTimeInMillis, ChronoUnit.MILLIS);
        }
        return suggestedStartDate;
    }

    public String toString() {
        return "GCModel[size=" + size() + "]: " + allEvents.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GCModel model = (GCModel) o;
        return Objects.equals(allEvents, model.allEvents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allEvents, fileInformation, fullGcEventPauses, gcEventPauses, gcEventPhases, concurrentGcEventPauses, vmOperationEventPauses, heapAllocatedSizes, tenuredAllocatedSizes, youngAllocatedSizes, permAllocatedSizes, heapUsedSizes, tenuredUsedSizes, youngUsedSizes, permUsedSizes, postConcurrentCycleUsedTenuredSizes, postConcurrentCycleUsedHeapSizes, promotion, firstPauseTimeStamp, lastPauseTimeStamp, totalPause, fullGCPause, lastFullGcPauseTimeStamp, fullGcPauseInterval, gcPause, vmOperationPause, lastGcPauseTimeStamp, pauseInterval, initiatingOccupancyFraction, freedMemory, format, postGCUsedMemory, postFullGCUsedHeap, freedMemoryByGC, freedMemoryByFullGC, postGCSlope, currentPostGCSlope, currentRelativePostGCIncrease, relativePostGCIncrease, postFullGCSlope, relativePostFullGCIncrease, url);
    }

    public static class Format implements Serializable {
        private static final long serialVersionUID = 483615745336894207L;

        private String format;

        private Format(String format) {
            this.format = format;
        }

        public String toString() {
            return format;
        }

        public static final Format SUN_VERBOSE_GC = new Format("Sun -verbose:gc");
        public static final Format SUN_X_LOG_GC = new Format("Sun -Xloggc:<file>");
        public static final Format IBM_VERBOSE_GC = new Format("IBM -verbose:gc");
        public static final Format SUN_1_2_2VERBOSE_GC = new Format("Sun 1.2.2 -verbose:gc");
        public static final Format UNIFIED_JVM_LOGGING = new Format("Unified jvm logging -Xlog:gc");
        public static final Format GO = new Format("Go");
    }
}

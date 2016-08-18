package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.ctrl.impl.GcSeriesLoader;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;
import com.tagtraum.perf.gcviewer.util.BuildInfoReader;
import com.tagtraum.perf.gcviewer.util.HttpUrlConnectionHelper;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DataReaderFacade is a helper class providing a simple interface to read a gc log file
 * including standard error handling.
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 */
public class DataReaderFacade {

    private List<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();

    /**
     * Add propertyChangeListener for underlying MonitoredBufferedInputStreams property "progress".
     *
     * @param listener component requiring to listen to progress changes
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeListeners.add(listener);
    }

    /**
     * Loads a model from a given <code>gcResource</code> logging all exceptions that occur.
     *
     * @param gcResource where to find data to be parsed
     * @return instance of GCModel containing all information that was parsed
     * @throws DataReaderException if any exception occurred, it is logged and added as the cause
     * to this exception
     */
    public GCModel loadModel(GCResource gcResource) throws DataReaderException {
        if (gcResource == null) {
            throw new NullPointerException("gcResource must never be null");
        }
        if (gcResource instanceof  GcResourceSeries) {
            return loadModelFromSeries((GcResourceSeries) gcResource);
        }
        if (!(gcResource instanceof GcResourceFile))
            throw new UnsupportedOperationException("Only supported for files!");

        DataReaderException dataReaderException = new DataReaderException();
        GCModel model = null;
        Logger logger = gcResource.getLogger();

        try {
            logger.info("GCViewer version " + BuildInfoReader.getVersion()
                    + " (" + BuildInfoReader.getBuildDate() + ")");
            model = readModel((GcResourceFile) gcResource);
        }
        catch (RuntimeException | IOException e) {
            dataReaderException.initCause(e);
            logger.warning(LocalisationHelper.getString("fileopen_dialog_read_file_failed")
                    + "\n" + e.toString() + " " + e.getLocalizedMessage());
        }

        if (dataReaderException.getCause() != null) {
            throw dataReaderException;
        }

        return model;
    }

    /**
     * Loads the {@link GCResource}s as a rotated series of logfiles. Takes care of ordering them
     *
     * @param series the {@link GcResourceSeries} to load
     * @return a {@link GCModel} containing all events found in the given {@link GCResource}s that were readable
     * @throws DataReaderException
     */
    protected GCModel loadModelFromSeries(GcResourceSeries series) throws DataReaderException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        GcSeriesLoader seriesLoader = new GcSeriesLoader(this);
        GCModel model = seriesLoader.load(series);

        stopWatch.stop();
        series.getLogger().log(Level.INFO, "Parsing logfile series containing of " + series.getResourcesInOrder().size() + " files took " + getDurationFormatted(stopWatch));
        return model;
    }

    /**
     * Open and parse data designated by <code>gcResource</code>.
     *
     * @param gcResource where to find data to be parsed
     * @return GCModel containing events parsed from <code>gcResource</code>
     * @throws IOException problem reading the data
     */
    private GCModel readModel(GcResourceFile gcResource) throws IOException {
        URL url = gcResource.getResourceNameAsUrl();
        DataReaderFactory factory = new DataReaderFactory();
        long contentLength = 0L;
        InputStream in;
        if (url.getProtocol().startsWith("http")) {
            AtomicLong cl = new AtomicLong();
            URLConnection conn = url.openConnection();
            in = HttpUrlConnectionHelper.openInputStream((HttpURLConnection)conn, HttpUrlConnectionHelper.GZIP, cl);
            contentLength = cl.get();
        }
        else {
            in = url.openStream();
            if (url.getProtocol().startsWith("file")) {
                File file = new File(url.getFile());
                if (file.exists()) {
                contentLength = file.length();
                }
            }
        }
        if (contentLength > 100L) {
            in = new MonitoredBufferedInputStream(in, DataReaderFactory.FOUR_KB, contentLength);
            for (PropertyChangeListener listener : propertyChangeListeners) {
                ((MonitoredBufferedInputStream)in).addPropertyChangeListener(listener);
            }
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        DataReader reader = factory.getDataReader(gcResource, in);
        GCModel model = reader.read();
        model.setURL(url);

        stopWatch.stop();
        gcResource.getLogger().log(Level.INFO, "Parsing logfile " + gcResource.getResourceName() + " took " + getDurationFormatted(stopWatch));

        return model;
    }

    private String getDurationFormatted(StopWatch stopWatch) {
        return DurationFormatUtils.formatDuration(stopWatch.getTime(), "s,SS") + "s";
    }

}

package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.math.DoubleData;
import com.tagtraum.perf.gcviewer.math.IntData;
import com.tagtraum.perf.gcviewer.math.RegressionLine;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;

/**
 * Collection of GCEvents.
 * <p/>
 * Date: Jan 30, 2002
 * Time: 5:01:45 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class GCModel implements Serializable {

    private static Logger LOG = Logger.getLogger(GCModel.class.getName());


    private List allEvents;
    private List events;
    private List gcEvents;
    private List concurrentGCEvents;
    private List currentNoFullGCEvents;
    private List fullGCEvents;
    private long lastModified;
    private long length;

    private long footprint;
    private double runningTime;
    private DoubleData pause;
    private DoubleData fullGCPause;
    private DoubleData gcPause;
    private long freedMemory;
    private Format format;
    private IntData postGCUsedMemory;
    private IntData postFullGCUsedMemory;
    private IntData freedMemoryByGC;
    private IntData freedMemoryByFullGC;
    private DoubleData postGCSlope;
    private RegressionLine currentPostGCSlope;
    private RegressionLine currentRelativePostGCIncrease;
    private DoubleData relativePostGCIncrease;
    private RegressionLine postFullGCSlope;
    private RegressionLine relativePostFullGCIncrease;
    private boolean countTenuredAsFull = true;
    private URL url;

    public GCModel(boolean countTenuredAsFull) {
        this.countTenuredAsFull = countTenuredAsFull;
        this.allEvents = new ArrayList();
        this.events = new ArrayList();
        this.gcEvents = new ArrayList();
        this.concurrentGCEvents = new ArrayList();
        this.fullGCEvents = new ArrayList();
        this.currentNoFullGCEvents = new ArrayList();
        this.currentPostGCSlope = new RegressionLine();
        this.postFullGCSlope = new RegressionLine();
        this.postGCSlope = new DoubleData();
        this.freedMemoryByGC = new IntData();
        this.freedMemoryByFullGC = new IntData();
        this.postFullGCUsedMemory = new IntData();
        this.postGCUsedMemory = new IntData();
        this.pause = new DoubleData();
        this.fullGCPause = new DoubleData();
        this.gcPause = new DoubleData();
        this.currentRelativePostGCIncrease = new RegressionLine();
        this.relativePostGCIncrease = new DoubleData();
        this.relativePostFullGCIncrease = new RegressionLine();
    }

    public boolean isCountTenuredAsFull() {
        return countTenuredAsFull;
    }

    public long getLastModified() {
        return lastModified;
    }

    public URL getURL() {
        return url;
    }

    public void setURL(final URL url) {
        this.url = url;
        if (url.getProtocol().startsWith("http")) {
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("HEAD");
                this.length = urlConnection.getContentLength();
                this.lastModified = urlConnection.getLastModified();
            } catch (IOException e) {
                if (LOG.isLoggable(Level.WARNING)) LOG.log(Level.WARNING, "Failed to obtain age and length of URL " + url, e);
            } finally {
                try {
                    if (urlConnection != null) {
                        final InputStream inputStream = urlConnection.getInputStream();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException ignore) {
                                // ignore
                            }
                        }
                    }
                } catch (IOException ignore) {
                    // ignore
                }
            }
        }
        else {
            URLConnection urlConnection = null;
            try {
                urlConnection = url.openConnection();
                this.length = urlConnection.getContentLength();
                this.lastModified = urlConnection.getLastModified();
            } catch (IOException e) {
                if (LOG.isLoggable(Level.WARNING)) LOG.log(Level.WARNING, "Failed to obtain age and length of URL " + url, e);
            } finally {
                try {
                    if (urlConnection != null) {
                        final InputStream inputStream = urlConnection.getInputStream();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException ignore) {
                                // ignore
                            }
                        }
                    }
                } catch (IOException ignore) {
                    // ignore
                }
            }
        }
    }

    public boolean isDifferent(File otherFile) {
        // we just ignore the file name for now...
        return this.lastModified != otherFile.lastModified() || this.length != otherFile.length();
    }

    public boolean isDifferent(URL otherURL) {
        long otherLength = 0;
        long otherLastModified = 0;
        if (otherURL.getProtocol().startsWith("http")) {
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection)otherURL.openConnection();
                urlConnection.setRequestMethod("HEAD");
                otherLength = urlConnection.getContentLength();
                otherLastModified = urlConnection.getLastModified();
            } catch (IOException e) {
                if (LOG.isLoggable(Level.WARNING)) LOG.log(Level.WARNING, "Failed to obtain age and otherLength of URL " + otherURL, e);
            } finally {
                try {
                    if (urlConnection != null) {
                        final InputStream inputStream = urlConnection.getInputStream();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException ignore) {
                                // ignore
                            }
                        }
                    }
                } catch (IOException ignore) {
                    // ignore
                }
            }
        }
        else {
            URLConnection urlConnection = null;
            try {
                urlConnection = otherURL.openConnection();
                otherLength = urlConnection.getContentLength();
                otherLastModified = urlConnection.getLastModified();
            } catch (IOException e) {
                if (LOG.isLoggable(Level.WARNING)) LOG.log(Level.WARNING, "Failed to obtain age and otherLength of URL " + otherURL, e);
            } finally {
                try {
                    if (urlConnection != null) {
                        final InputStream inputStream = urlConnection.getInputStream();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException ignore) {
                                // ignore
                            }
                        }
                    }
                } catch (IOException ignore) {
                    // ignore
                }
            }
        }

        return this.lastModified != otherLastModified || this.length != otherLength;
    }

    public Iterator getGCEvents() {
        return events.iterator();
    }

    public Iterator getConcurrentGCEvents() {
        return concurrentGCEvents.iterator();
    }

    public Iterator getEvents() {
        return allEvents.iterator();
    }

    public void add(final AbstractGCEvent abstractEvent) {
        allEvents.add(abstractEvent);
        if (abstractEvent instanceof ConcurrentGCEvent) {
            concurrentGCEvents.add(abstractEvent);
        } else if (abstractEvent instanceof GCEvent) {
            events.add(abstractEvent);
            final GCEvent event = (GCEvent) abstractEvent;
            footprint = Math.max(footprint, event.getTotal());
            runningTime = Math.max(runningTime, event.getTimestamp());
            freedMemory += event.getPreUsed() - event.getPostUsed();
            pause.add(event.getPause());
            if (event.getType().getGeneration() != GCEvent.Generation.TENURED && !event.hasTenuredDetail()) {
                gcEvents.add(event);
                postGCUsedMemory.add(event.getPostUsed());
                freedMemoryByGC.add(event.getPreUsed() - event.getPostUsed());
                currentNoFullGCEvents.add(event);
                currentPostGCSlope.addPoint(event.getTimestamp(), event.getPostUsed());
                currentRelativePostGCIncrease.addPoint(currentRelativePostGCIncrease.getPointCount(), event.getPostUsed());
                gcPause.add(event.getPause());
            } else {
                if (event.getType().getGeneration() == GCEvent.Generation.TENURED || event.hasTenuredDetail()) {
                    fullGCEvents.add(event);
                    postFullGCUsedMemory.add(event.getPostUsed());
                    final int freed = event.getPreUsed() - event.getPostUsed();
                    freedMemoryByFullGC.add(freed);
                    fullGCPause.add(event.getPause());
                    postFullGCSlope.addPoint(event.getTimestamp(), event.getPostUsed());
                    relativePostFullGCIncrease.addPoint(relativePostFullGCIncrease.getPointCount(), event.getPostUsed());
                }
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
        }
    }

    public int size() {
        return events.size();
    }

    public AbstractGCEvent get(final int index) {
        return (AbstractGCEvent) events.get(index);
    }


    /**
     * Pauses caused by full garbage collections.
     */
    public DoubleData getFullGCPause() {
        return fullGCPause;
    }

    /**
     * Pauses caused by garbage collections.
     */
    public DoubleData getGCPause() {
        return gcPause;
    }

    /**
     * The increase in memory consumption after a full collection in relation to the amount that was
     * used after the previous full collection.
     */
    public RegressionLine getRelativePostFullGCIncrease() {
        return relativePostFullGCIncrease;
    }

    /**
     * The increase in memory consumption after a collection in relation to the amount that was
     * used after the previous collection.
     */
    public DoubleData getRelativePostGCIncrease() {
        return relativePostGCIncrease;
    }

    /**
     * The average slope of the regression lines of the memory consumption after
     * a garbage collection in between <em>full</em> garbage collections.
     * <p/>
     * The unit is kb/s.
     */
    public double getPostGCSlope() {
        return postGCSlope.average();
    }

    public IntData getPostGCUsedMemory() {
        return postGCUsedMemory;
    }

    public RegressionLine getCurrentPostGCSlope() {
        return currentPostGCSlope;
    }

    public RegressionLine getPostFullGCSlope() {
        return postFullGCSlope;
    }

    public IntData getPostFullGCUsedMemory() {
        return postFullGCUsedMemory;
    }

    /**
     * Heap memory freed by a (small) garbage collection.
     */
    public IntData getFreedMemoryByGC() {
        return freedMemoryByGC;
    }

    /**
     * Heap memory freed by a <em>full</em> garbage collection.
     */
    public IntData getFreedMemoryByFullGC() {
        return freedMemoryByFullGC;
    }

    /**
     * Heap memory consumption after a (small) garbage collection.
     */
    public IntData getFootprintAfterGC() {
        return postGCUsedMemory;
    }

    /**
     * Heap memory consumption after a <em>full</em> garbage collection.
     */
    public IntData getFootprintAfterFullGC() {
        return postFullGCUsedMemory;
    }

    /**
     * Pause in sec.
     */
    public DoubleData getPause() {
        return pause;
    }

    /**
     * Throughput in percent.
     */
    public double getThroughput() {
        return 100 * (runningTime - pause.getSum()) / runningTime;
    }

    /**
     * Footprint in KB.
     */
    public long getFootprint() {
        return footprint;
    }

    /**
     * Running time in sec.
     */
    public double getRunningTime() {
        return runningTime;
    }

    /**
     * Freed memory in KB.
     */
    public long getFreedMemory() {
        return freedMemory;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(final Format format) {
        this.format = format;
    }

    public boolean hasCorrectTimestamp() {
        return format == Format.IBM_VERBOSE_GC || format == Format.SUN_X_LOG_GC || format == Format.SUN_1_2_2VERBOSE_GC;
    }

    public String toString() {
        return events.toString();
    }

    public static class Format implements Serializable {
        private String format;

        private Format(final String format) {
            this.format = format;
        }

        public String toString() {
            return format;
        }

        public static final Format SUN_VERBOSE_GC = new Format("Sun -verbose:gc");
        public static final Format SUN_X_LOG_GC = new Format("Sun -Xloggc:<file>");
        public static final Format IBM_VERBOSE_GC = new Format("IBM -verbose:gc");
        public static final Format SUN_1_2_2VERBOSE_GC = new Format("Sun 1.2.2 -verbose:gc");
    }
}

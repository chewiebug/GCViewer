package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.AbstractGCEvent.Generation;
import com.tagtraum.perf.gcviewer.math.DoubleData;
import com.tagtraum.perf.gcviewer.math.IntData;
import com.tagtraum.perf.gcviewer.math.RegressionLine;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

	private static final long serialVersionUID = -6479685723904770990L;

	private static Logger LOG = Logger.getLogger(GCModel.class.getName());

    private List<AbstractGCEvent> allEvents;
    private List<GCEvent> stopTheWorldEvents;
    private List<GCEvent> gcEvents;
    private List<ConcurrentGCEvent> concurrentGCEvents;
    private List<GCEvent> currentNoFullGCEvents;
    private List<GCEvent> fullGCEvents;
    private long lastModified;
    private long length;

    private Map<String, DoubleData> gcEventPauses; // pause information about all stw events for detailed output
    private Map<String, DoubleData> concurrentGcEventPauses; // pause information about all concurrent events
    
    private IntData heapSizes; // heap size of every event
    private IntData tenuredSizes; // tenured size of every event that has this information
    private IntData youngSizes; // young size of every event that has this information
    private IntData permSizes; // perm size of every event that has this information
    
    private long footprint;
    private double runningTime;
    private DoubleData totalPause;
    private DoubleData fullGCPause;
    private DoubleData gcPause; // not full gc but stop the world pause
    private double lastPauseTimeStamp = -1;
    private DoubleData pauseInterval; // interval between two stop the world pauses
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
        
        this.allEvents = new ArrayList<AbstractGCEvent>();
        this.stopTheWorldEvents = new ArrayList<GCEvent>();
        this.gcEvents = new ArrayList<GCEvent>();
        this.concurrentGCEvents = new ArrayList<ConcurrentGCEvent>();
        this.fullGCEvents = new ArrayList<GCEvent>();
        this.currentNoFullGCEvents = new ArrayList<GCEvent>();
        this.currentPostGCSlope = new RegressionLine();
        this.postFullGCSlope = new RegressionLine();
        this.postGCSlope = new DoubleData();
        this.freedMemoryByGC = new IntData();
        this.freedMemoryByFullGC = new IntData();
        this.postFullGCUsedMemory = new IntData();
        this.postGCUsedMemory = new IntData();
        this.totalPause = new DoubleData();
        this.fullGCPause = new DoubleData();
        this.gcPause = new DoubleData();
        this.pauseInterval = new DoubleData();
        this.currentRelativePostGCIncrease = new RegressionLine();
        this.relativePostGCIncrease = new DoubleData();
        this.relativePostFullGCIncrease = new RegressionLine();
        
        this.gcEventPauses = new TreeMap<String, DoubleData>();
        this.concurrentGcEventPauses = new TreeMap<String, DoubleData>();
        
        this.heapSizes = new IntData();
        this.permSizes = new IntData();
        this.tenuredSizes = new IntData();
        this.youngSizes = new IntData();
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

    private void printPauseMap(Map<String, DoubleData> pauseMap) {
    	for (Map.Entry<String, DoubleData> entry: pauseMap.entrySet()) {
    		System.out.println(entry.getKey() + " [n, avg, sum, min, max]:\t" + entry.getValue().getN() + "\t" + entry.getValue().average() + "\t" + entry.getValue().getSum() + "\t" + entry.getValue().getMin() + "\t" + entry.getValue().getMax());
    	}
    }
    
    private void printIntData(String name, IntData data) {
        try {
            System.out.println(name + " (avg, min, max):\t" + data.average() + "\t" + data.getMin() + "\t" + data.getMax());
        } catch (IllegalStateException e) {
            System.out.println(name + "\t" + e.toString());
        }
    }
    
    public void printPauseMaps() {
    	// TODO delete
    	printPauseMap(gcEventPauses);
    	printPauseMap(concurrentGcEventPauses);
    	System.out.println("sum of pauses\t" + totalPause.getSum());
    	System.out.println("sum of full gc pauses\t" + fullGCPause.getSum());
    	System.out.println("sum of young gc pauses\t" + gcPause.getSum());
    	System.out.println("interval between pauses (avg, min, max)\t" + pauseInterval.average() + "\t" + pauseInterval.getMin() + "\t" + pauseInterval.getMax());
    	
    	printIntData("heap size", heapSizes);
    	printIntData("perm size", permSizes);
    	printIntData("tenured size", tenuredSizes);
    	printIntData("young size", youngSizes);
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

    public Iterator<GCEvent> getGCEvents() {
        return stopTheWorldEvents.iterator();
    }

    public Iterator<ConcurrentGCEvent> getConcurrentGCEvents() {
        return concurrentGCEvents.iterator();
    }

    public Iterator<AbstractGCEvent> getEvents() {
        return allEvents.iterator();
    }
    
    public Iterator<GCEvent> getFullGCEvents() {
    	return fullGCEvents.iterator();
    }

    private DoubleData getDoubleData(String key, Map<String, DoubleData> eventMap) {
    	DoubleData data = eventMap.get(key);
    	if (data == null) {
    		data = new DoubleData();
    		eventMap.put(key, data);
    	}
    	
    	return data;
    }
    
    public void add(final AbstractGCEvent abstractEvent) {
        allEvents.add(abstractEvent);
        if (abstractEvent instanceof ConcurrentGCEvent) {
        	final ConcurrentGCEvent concEvent = (ConcurrentGCEvent)abstractEvent;
            concurrentGCEvents.add(concEvent);
        
            DoubleData pauses = getDoubleData(concEvent.getType().getType(), concurrentGcEventPauses);
            pauses.add(concEvent.getPause());

        } else if (abstractEvent instanceof GCEvent) {
        	
        	// collect statistics about all stop the world events
            final GCEvent event = (GCEvent) abstractEvent;
            
            updateHeapSizes(event);
            
            DoubleData pauses = getDoubleData(event.getTypeAsString(), gcEventPauses);
            pauses.add(event.getPause());
            
            stopTheWorldEvents.add(event);
            footprint = Math.max(footprint, event.getTotal());
            runningTime = Math.max(runningTime, event.getTimestamp());
            freedMemory += event.getPreUsed() - event.getPostUsed();
            totalPause.add(event.getPause());
            
            if (lastPauseTimeStamp > 0) {
                // JRockit sometimes has special timestamps that seem to go back in time,
                // omit them here
                if (event.getTimestamp() - lastPauseTimeStamp >= 0) {
                    pauseInterval.add(event.getTimestamp() - lastPauseTimeStamp);
                }
            }
            lastPauseTimeStamp = event.getTimestamp();
            
            if (!event.isFull()) {
            	// make a difference between stop the world events, which only collect from some generations...
                gcEvents.add(event);
                postGCUsedMemory.add(event.getPostUsed());
                freedMemoryByGC.add(event.getPreUsed() - event.getPostUsed());
                currentNoFullGCEvents.add(event);
                currentPostGCSlope.addPoint(event.getTimestamp(), event.getPostUsed());
                currentRelativePostGCIncrease.addPoint(currentRelativePostGCIncrease.getPointCount(), event.getPostUsed());
                gcPause.add(event.getPause());
            } else {
                if (event.isFull()) {
                	// ... as opposed to all generations
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

    private void updateHeapSizes(GCEvent event) {
        // event contains always heap size
        if (event.getTotal() > 0) {
            heapSizes.add(event.getTotal());
        }
        
        // if there are details, young, tenured and perm sizes can be extracted
        Iterator<AbstractGCEvent> i = event.details();
        while (i.hasNext()) {
            AbstractGCEvent abstractGCEvent = (AbstractGCEvent)i.next();
            if (abstractGCEvent instanceof GCEvent) {
                updateHeapSize((GCEvent)abstractGCEvent);
            }
        }
    }
    
    private void updateHeapSize(GCEvent event) {
        if (event.getTotal() > 0) {
            if (event.getGeneration().equals(Generation.ALL)) {
                heapSizes.add(event.getTotal());
            }
            else if (event.getGeneration().equals(Generation.PERM)) {
                permSizes.add(event.getTotal());
            }
            else if (event.getGeneration().equals(Generation.TENURED)) {
                tenuredSizes.add(event.getTotal());
            }
            else if (event.getGeneration().equals(Generation.YOUNG)) {
                youngSizes.add(event.getTotal());
            }
        }
    }

    public int size() {
        return allEvents.size();
    }

    public AbstractGCEvent get(final int index) {
        return (AbstractGCEvent) allEvents.get(index);
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
     * Interval between all types of stop the world pauses.
     */
    public DoubleData getPauseInterval() {
        return pauseInterval;
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
        return totalPause;
    }

    /**
     * Throughput in percent.
     */
    public double getThroughput() {
        return 100 * (runningTime - totalPause.getSum()) / runningTime;
    }

    /**
     * max heap size for every event
     */
    public IntData getHeapSizes() {
        return heapSizes;
    }

    /**
     * perm sizes for every event that contained one (only if detailed logging is active and
     * and all spaces were collected) 
     */
    public IntData getPermSizes() {
        return permSizes;
    }
    
    /**
     * tenured sizes for every event that contained one (only if detailed logging is active) 
     */
    public IntData getTenuredSizes() {
        return tenuredSizes;
    }
    
    /**
     * young sizes for every event that contained one (only if detailed logging is active) 
     */
    public IntData getYoungSizes() {
        return youngSizes;
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
    
    public boolean hasDateStamp() {
    	return allEvents.size() > 0 
    			? get(0).getDatestamp() != null 
    			: false;
    }
    
    public Date getFirstDateStamp() {
    	return allEvents.size() > 0 
    			? get(0).getDatestamp() 
    			: null;
    }

    public String toString() {
        return allEvents.toString();
    }

    public static class Format implements Serializable {
		private static final long serialVersionUID = 483615745336894207L;
		
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

package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.NumberParser;

/**
 * Parses -verbose:gc output from IBM i5/OS JDK 1.4.2.
 *
 * Date: Feb 12, 2008
 * Time: 13:25:00
 * @author Ruwin Veldwijk
 */
public class DataReaderIBMi5OS1_4_2 extends AbstractDataReader {

    private DateFormat cycleStartGCFormat;

    /**
     * Constructor for the IBM i5/OS GC reader.
     * @param gcResource information about the resource to be parsed
     * @param in InputStream delivering the GC data
     * @throws UnsupportedEncodingException thrown in case the desired encoding is not supported
     */
    public DataReaderIBMi5OS1_4_2(GCResource gcResource, InputStream in) throws UnsupportedEncodingException {
        super(gcResource, in);
    }

    /**
     * Reads the GC data lines and translates them to GCEvents which
     * are collected in the GCModel.
     *
     * @throws IOException When reading the inputstream fails.
     */
    public GCModel read() throws IOException {
        if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reading IBM i5/OS 1.4.2 format...");
        try {
        	// Initialize model
            final GCModel model = new GCModel();
            model.setFormat(GCModel.Format.IBM_VERBOSE_GC);

            // Initialize local variables
            int state = 0;
            String line = null;
            GCEvent event = null;
            int freed = 0;
            int previousCycle = 0;
            int currentCycle = 0;
            long basetime = 0;

            // Initialize date formatter
            cycleStartGCFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

            // Read the GC data lines
            while ((line = in.readLine()) != null && shouldContinue()) {
                final String trimmedLine = line.trim();
                // GC Data line always start with GC
                if (!"".equals(trimmedLine) && !trimmedLine.startsWith("GC")) {
                    if (getLogger().isLoggable(Level.INFO)) getLogger().info("Malformed line (" + in.getLineNumber() + "): " + line);
                    state = 0;
                }
                switch (state) {
                    case 0:
                        if (line.indexOf("GC:") != -1) {
                        	// This is the start of the GC log
                            event = new GCEvent();
                            event.setType(AbstractGCEvent.Type.GC);
                            event.setPreUsed(parseInitialHeap(line));
                            event.setPostUsed(event.getPreUsed());
                            event.setTotal(event.getPreUsed());
                            model.add(event);
                            event = null;
                            // stay in state 0
                            break;
                        }
                        else if (line.indexOf("collection starting") != -1) {
                        	// This is the start of a GC event
                            event = new GCEvent();
                            event.setType(AbstractGCEvent.Type.GC);
                            final long time = parseGCCycleStart(line);
                            if (basetime == 0) basetime = time;
                            event.setTimestamp((time - basetime)/1000.0d);
                            state++;
                            break;
                        }
                        break;
                    case 1:
                    	// Collect data to add to the event
                        if (line.indexOf("current heap(KB) ") != -1) {
                            event.setTotal(parseTotalAfterGC(line));
                            break;
                        }
                        else if (line.indexOf("collect (milliseconds) ") != -1) {
							event.setPause(parsePause(line));
							break;
                        }
                        else if (line.indexOf("collected(KB) ") != -1) {
							freed = parseFreed(line);
							break;
                        }
                        else if (line.indexOf("current cycle allocation(KB) ") != -1) {
							previousCycle = parsePreviousCycle(line);
							currentCycle = parseCurrentCycle(line);
							event.setPreUsed((event.getTotal() - previousCycle - currentCycle) + freed);
							event.setPostUsed((event.getTotal() - previousCycle - currentCycle));
							break;
						}
                        else if (line.indexOf("collection ending") != -1) {
							// End of GC event, store data in the model and reset variables
							model.add(event);
							event = null;
							state = 0;
				            freed = 0;
				            previousCycle = 0;
				            currentCycle = 0;
				            break;
						}
                        break;
                    default:
                }
            }
            return model;
        }
        finally {
            if (getLogger().isLoggable(Level.INFO)) getLogger().info("Done reading.");
        }
    }

    /**
     * Parses the line which holds the GC Cycle start date/time. The
     * date/time is extracted and returned as a long.
     *
     * @param line The line which holds the date
     * @return The date represented as a long
     * @throws IOException When parsing the date fails.
     */
    private long parseGCCycleStart(final String line) throws IOException {
        try {
            final int idx = line.indexOf("collection starting ");
            final Date date = cycleStartGCFormat.parse(line.substring(idx + "collection starting ".length()));
            return date.getTime();
        }
        catch (java.text.ParseException e) {
            throw new ParseException(e.toString());
        }
    }

    /**
     * Parses the line which holds the initial heap size. The size
     * is returned as int.
     *
     * @param line The line which holds the initial heap size
     * @return The initial heap size
     */
    private int parseInitialHeap(final String line) {
        final int start = line.indexOf("initial heap(KB) ") + 17;
        final int end = line.indexOf(';', start);
        return Integer.parseInt(line.substring(start, end));
    }

    /**
     * Parses the line which holds the current heap size and
     * returns the heap size after GC completed.
     *
     * @param line The line which holds the current heap size
     * @return The heap size after GC finished
     */
    private int parseTotalAfterGC(final String line) {
        final int start = line.indexOf("current heap(KB) ") + 17;
        final int end = line.indexOf(';', start);
        return Integer.parseInt(line.substring(start, end));
    }

    /**
     * Parses the number of bytes (KB) collected in this GC event.
     *
     * @param line The line which holds the collected heap
     * @return The number of bytes (KB) collected in this event
     */
    private int parseFreed(final String line) {
        final int start = line.indexOf("collected(KB) ") + 14;
        final int end = line.indexOf('.', start);
        return Integer.parseInt(line.substring(start, end));
    }

    /**
     * Parses the number of bytes (KB) that was allocated in the cycle
     *
     * @param line The line which holds the current allocation in KB
     * @return The number of bytes (KB) allocated in current cycle
     */
    private int parseCurrentCycle(final String line) {
        final int start = line.indexOf("current cycle allocation(KB) ") + 29;
        final int end = line.indexOf(';', start);
        return Integer.parseInt(line.substring(start, end));
    }

    /**
     * Parses the number of bytes (KB) that was allocated in the previous cycle
     *
     * @param line The line which holds the previous allocation in KB
     * @return The number of bytes (KB) allocated in the previous cycle
     */
    private int parsePreviousCycle(final String line) {
        final int start = line.indexOf("previous cycle allocation(KB) ") + 30;
        final int end = line.indexOf('.', start);
        return Integer.parseInt(line.substring(start, end));
    }

    /**
     * Parses the time the GC event lasted in seconds
     *
     * @param line The line which holds the collect time in millis.
     * @return The number of seconds the GC event took.
     */
    private double parsePause(final String line) {
        final int start = line.indexOf("collect (milliseconds) ") + 23;
        final int end = line.indexOf('.', start);
        return NumberParser.parseDouble(line.substring(start, end)) / 1000.0d;
    }
}

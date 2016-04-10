package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Generation;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.NumberParser;

/**
 * DataReaderJRockit1_6_0
 * @see DataReaderJRockit1_4_2
 * @author Rupesh Ramachandran
 */
public class DataReaderJRockit1_6_0 extends AbstractDataReader {
    private static final String MEMORY_MARKER = "[memory ]";
    private static final String NURSERY_SIZE = "nursery size: ";
    private static final String PAUSE_MARKER = "longest pause ";

    public DataReaderJRockit1_6_0(GCResource gcResource, InputStream in) throws UnsupportedEncodingException {
        super(gcResource, in);
    }

    public GCModel read() throws IOException {
        if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reading JRockit 1.6.0 format...");
        boolean gcSummary = false;
        try {
            GCModel model = new GCModel();
            model.setFormat(GCModel.Format.SUN_X_LOG_GC);
            String line = null;
            GCEvent event = null;
            int nurserySize = -1;
            while ((line = in.readLine()) != null && shouldContinue()) {
                // Sample JRockit log entry types to be parsed:
                //
                // [INFO ][memory ] GC mode: Garbage collection optimized for throughput, strategy: Generational Parallel Mark & Sweep
                // [INFO ][memory ] Heap size: 8388608KB, maximal heap size: 8388608KB, nursery size: 4194304KB
                // [INFO ][memory ] <start>-<end>: <type>..
                // [INFO ][memory ] [OC#2] 34.287-34.351: OC 460781KB->214044KB (524288KB), 0.064 s, sum of pauses 5.580 ms, longest pause 4.693 ms.

                final int memoryIndex = line.indexOf(MEMORY_MARKER);
                if (memoryIndex == -1) {
                    if (getLogger().isLoggable(Level.FINE)) getLogger().fine("Ignoring line " + in.getLineNumber() + ". Missing \"[memory ]\" marker: " + line);
                    continue;
                }
                if (line.endsWith(MEMORY_MARKER)) {
                    continue;
                }
                final int startLog = memoryIndex + MEMORY_MARKER.length();
                // Skip "[INFO ][memory ] "

                // print some special JRockit summary statements to the log.
                if (!gcSummary) {
                    gcSummary = line.endsWith("Memory usage report");
                }
                // Log any relevant memory usage reports at INFO level, rest as FINE
                if (gcSummary) {
                    if (getLogger().isLoggable(Level.INFO)) getLogger().info(line.substring(startLog));
                    continue;
                }
                else if (line.indexOf("Prefetch distance") != -1) {
                    if (getLogger().isLoggable(Level.INFO)) getLogger().info(line.substring(startLog));
                    continue;
                }
                else if (line.indexOf("GC mode") != -1) {
                    if (getLogger().isLoggable(Level.INFO)) getLogger().info(line.substring(startLog));
                    continue;
                }
                else if (line.indexOf("GC strategy") != -1) {
                    if (getLogger().isLoggable(Level.INFO)) getLogger().info(line.substring(startLog));
                    continue;
                }
                else if (line.indexOf("OutOfMemory") != -1) {
                    if (getLogger().isLoggable(Level.INFO)) getLogger().warning("GC log contains OutOfMemory error: " + line.substring(startLog));
                    continue;
                }
                else if (line.substring(startLog).startsWith("<")) {
                    // ignore
                    if (getLogger().isLoggable(Level.FINE)) getLogger().fine(line.substring(startLog));
                    continue;
                }
                else if (line.toLowerCase().indexOf("heap size:") != -1) {
                    if (getLogger().isLoggable(Level.INFO)) getLogger().info(line.substring(startLog));
                    final int nurserySizeStart = line.indexOf(NURSERY_SIZE);
                    final int nurserySizeEnd = line.indexOf('K', nurserySizeStart + NURSERY_SIZE.length());
                    if (nurserySizeStart != -1) {
                        nurserySize = Integer.parseInt(line.substring(nurserySizeStart + NURSERY_SIZE.length(), nurserySizeEnd));
                    }
                    continue;
                }
                else if ((line.indexOf("C#") == -1) || (line.indexOf("->") == -1)){
                    // No [YC#] or [OC#] logs which we are interested in
                    if (getLogger().isLoggable(Level.FINE)) getLogger().fine(line.substring(startLog));
                    continue;
                }

                // Assume this is an actual GC log of interest. Look for time string, skip ahead of [OC#2]
                // [memory ] [OC#2] 34.287-34.351: OC 460781KB->214044KB (524288KB), 0.064 s, sum of pauses 5.580 ms, longest pause 4.693 ms.
                //   OR if timestamp logging enabled...
                // [memory ][Sat Oct 27 20:04:38 2012][23355] [OC#2]
                int startGCStats = line.indexOf("C#"); // skip to OC# or YC#
                // Example:
                final int startTimeIndex = line.indexOf(']', startGCStats) + 1; // go to end of "[OC#2]" in above example

                final int colon = line.indexOf(':', startTimeIndex);
                if (colon == -1) {
                    if (getLogger().isLoggable(Level.WARNING)) getLogger().warning("Malformed line (" + in.getLineNumber() + "). Missing colon after start time: " + line);
                    continue;
                }

                event = new GCEvent();

                //34.287-34.351: OC 460781KB->214044KB (524288KB), 0.064 s, sum of pauses 5.580 ms, longest pause 4.693 ms.

                // set timestamp
                final String timestampString = line.substring(startTimeIndex, colon);
                final int minus = timestampString.indexOf('-');
                if (minus == -1) {
                    event.setTimestamp(NumberParser.parseDouble(timestampString));
                }
                else {
                    event.setTimestamp(NumberParser.parseDouble(timestampString.substring(0, minus)));
                }

                // set type
                final int typeStart = skipSpaces(colon+1, line);
                int typeEnd = typeStart;
                while (!Character.isDigit(line.charAt(++typeEnd))) {}
                final AbstractGCEvent.Type type = AbstractGCEvent.Type.lookup("jrockit." + line.substring(typeStart, typeEnd).trim());
                if (type == null) {
                    if (getLogger().isLoggable(Level.INFO)) getLogger().info("Failed to determine type: " + line.substring(startTimeIndex));
                    continue;
                }
                event.setType(type);

                // Parse GC before/after sizes: "460781KB->214044KB (524288KB)"
                // before
                final int startBefore = typeEnd;
                final int endBefore = line.indexOf('K', startBefore);
                event.setPreUsed(Integer.parseInt(line.substring(startBefore, endBefore)));

                // after
                final int startAfter = endBefore+4;
                final int endAfter = line.indexOf('K', startAfter);
                event.setPostUsed(Integer.parseInt(line.substring(startAfter, endAfter)));

                // total
                final int startTotal = line.indexOf('(', endAfter) + 1;
                final int endTotal = line.indexOf('K', startTotal);
                event.setTotal(Integer.parseInt(line.substring(startTotal, endTotal)));

                // pause
                // 7786210KB->3242204KB (8388608KB), 0.911 s, sum of pauses 865.900 ms, longest pause 865.900 ms.
                final int startPause = line.indexOf(PAUSE_MARKER, endTotal) + PAUSE_MARKER.length();
                final int endPause = line.indexOf(' ', startPause);
                event.setPause(NumberParser.parseDouble(line.substring(startPause, endPause)) / 1000.0d);
                model.add(event);

                // add artificial detail events
                if (nurserySize != -1 && event.getExtendedType().getGeneration() == Generation.YOUNG) {
                    GCEvent detailEvent = new GCEvent();
                    detailEvent.setType(event.getExtendedType().getType());
                    detailEvent.setTimestamp(event.getTimestamp());
                    detailEvent.setTotal(nurserySize);
                    event.add(detailEvent);
                }
                if (nurserySize != -1 && event.getExtendedType().getGeneration() == Generation.TENURED) {
                    GCEvent detailEvent = new GCEvent();
                    detailEvent.setType(event.getExtendedType().getType());
                    detailEvent.setTimestamp(event.getTimestamp());
                    detailEvent.setTotal(event.getTotal() - nurserySize);
                    event.add(detailEvent);
                }
            }
            return model;
        }
        finally {
            if (in != null)
                try {
                    in.close();
                }
                catch (IOException ioe) {
                }
            if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reading done.");
        }
    }

    private static int skipSpaces(int start, String line) {
        int i = start;
        while (line.charAt(i) == ' ') {
            i++;
        }
        return i;
    }
}

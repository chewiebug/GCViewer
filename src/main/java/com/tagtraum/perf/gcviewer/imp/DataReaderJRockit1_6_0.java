package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Generation;

/**
 * DataReaderJRockit1_4_2.
 * <p/>
 * Date: Jan 5, 2006
 * Time: 5:31:50 AM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class DataReaderJRockit1_6_0 implements DataReader {
    private static Logger LOG = Logger.getLogger(DataReaderJRockit1_6_0.class.getName());

    private LineNumberReader in;
    private static final String MEMORY_MARKER = "[memory ] ";
    private static final String NURSERY_SIZE = "nursery size: ";
    private static final String PAUSE_MARKER = "longest pause ";

    public DataReaderJRockit1_6_0(InputStream in) {
        this.in = new LineNumberReader(new InputStreamReader(in));
    }

    public GCModel read() throws IOException {
        if (LOG.isLoggable(Level.INFO)) LOG.info("Reading JRockit 1.6.0 format...");
        boolean gcSummary = false;
        try {
            GCModel model = new GCModel(true);
            model.setFormat(GCModel.Format.SUN_X_LOG_GC);
            String line = null;
            GCEvent event = null;
            int nurserySize = -1;
            while ((line = in.readLine()) != null) {
                // Sample JRockit log entry types to be parsed:
                //
                // [INFO ][memory ] GC mode: Garbage collection optimized for throughput, strategy: Generational Parallel Mark & Sweep
                // [INFO ][memory ] Heap size: 8388608KB, maximal heap size: 8388608KB, nursery size: 4194304KB
                // [INFO ][memory ] <start>-<end>: <type>..
                // [INFO ][memory ] [OC#2] 34.287-34.351: OC 460781KB->214044KB (524288KB), 0.064 s, sum of pauses 5.580 ms, longest pause 4.693 ms.
                
                final int memoryIndex = line.indexOf(MEMORY_MARKER);
                if (memoryIndex == -1) {
                    if (LOG.isLoggable(Level.FINE)) LOG.fine("Ignoring line " + in.getLineNumber() + ". Missing \"[memory ]\" marker: " + line);
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
                if (gcSummary) {
                    if (LOG.isLoggable(Level.INFO)) LOG.info(line.substring(startLog));
                    continue;
                }
                else if (line.indexOf("Prefetch distance") != -1) {                    
                    if (LOG.isLoggable(Level.INFO)) LOG.info(line.substring(startLog));
                    continue;
                }
                else if (line.indexOf("GC mode") != -1) {
                    if (LOG.isLoggable(Level.INFO)) LOG.info(line.substring(startLog));
                    continue;
                }
                else if (line.toLowerCase().indexOf("heap size:") != -1) {
                    if (LOG.isLoggable(Level.INFO)) LOG.info(line.substring(startLog));
                    final int nurserySizeStart = line.indexOf(NURSERY_SIZE);
                    final int nurserySizeEnd = line.indexOf('K', nurserySizeStart + NURSERY_SIZE.length());
                    if (nurserySizeStart != -1) {
                        nurserySize = Integer.parseInt(line.substring(nurserySizeStart + NURSERY_SIZE.length(), nurserySizeEnd));
                    }
                    continue;
                }
                else if (line.substring(startLog).startsWith("<")) {
                    // ignore
                    if (LOG.isLoggable(Level.FINE)) LOG.fine(line.substring(startLog));
                    continue;
                }
                else if (line.indexOf("strategy") != -1) {
                    // ignore logs like:
                    // [INFO ][memory ] [OC#3] Changing GC strategy from: genconcon to: genconpar, reason: Emergency parallel sweep requested.
                    if (LOG.isLoggable(Level.FINE)) LOG.fine(line.substring(startLog));
                    continue;
                }
                else if ((line.indexOf("->") == -1) || (line.indexOf(']') == -1)) {
                    // ignore logs like:
                    // [INFO ][memory ]            Run with -Xverbose:gcpause to see individual phases
                    if (LOG.isLoggable(Level.FINE)) LOG.fine(line.substring(startLog));
                    continue;
                }
                // Assume this is an actual GC log. Look for time string, skip ahead of [OC#2]]
                // [OC#2] 34.287-34.351: OC 460781KB->214044KB (524288KB), 0.064 s, sum of pauses 5.580 ms, longest pause 4.693 ms.
                final int startTimeIndex = line.indexOf(']', startLog) + 1; // go to end of "[OC#2]" in above example                                 
                
                final int colon = line.indexOf(':', startTimeIndex);
                if (colon == -1) {
                    if (LOG.isLoggable(Level.WARNING)) LOG.warning("Malformed line (" + in.getLineNumber() + "). Missing colon after start time: " + line);
                    continue;
                }
                event = new GCEvent();
                
                //34.287-34.351: OC 460781KB->214044KB (524288KB), 0.064 s, sum of pauses 5.580 ms, longest pause 4.693 ms.                

                // set timestamp
                final String timestampString = line.substring(startTimeIndex, colon);
                final int minus = timestampString.indexOf('-');
                if (minus == -1) {
                    event.setTimestamp(Double.parseDouble(timestampString));
                }
                else {
                    event.setTimestamp(Double.parseDouble(timestampString.substring(0, minus)));
                }

                // set type
                final int typeStart = skipSpaces(colon+1, line);
                int typeEnd = typeStart;
                while (!Character.isDigit(line.charAt(++typeEnd))) {}
                final AbstractGCEvent.Type type = AbstractGCEvent.Type.parse("jrockit." + line.substring(typeStart, typeEnd).trim());
                if (type == null) {
                    if (LOG.isLoggable(Level.INFO)) LOG.info("Failed to determine type: " + line.substring(startTimeIndex));
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
                event.setPause(Double.parseDouble(line.substring(startPause, endPause)) / 1000.0d);
                model.add(event);

                // add artificial detail events
                if (nurserySize != -1 && event.getType().getGeneration() == Generation.YOUNG) {
                    GCEvent detailEvent = new GCEvent();
                    detailEvent.setType(event.getType());
                    detailEvent.setTimestamp(event.getTimestamp());
                    detailEvent.setTotal(nurserySize);
                    event.add(detailEvent);
                }
                if (nurserySize != -1 && event.getType().getGeneration() == Generation.TENURED) {
                    GCEvent detailEvent = new GCEvent();
                    detailEvent.setType(event.getType());
                    detailEvent.setTimestamp(event.getTimestamp());
                    detailEvent.setTotal(event.getTotal() - nurserySize);
                    event.add(detailEvent);
                }
            }
            return model;
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            if (LOG.isLoggable(Level.INFO)) LOG.info("Reading done.");
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

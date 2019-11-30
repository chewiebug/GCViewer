package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.NumberParser;

/**
 * Parses -verbose:gc output from IBM JDK 1.3.0.
 *
 * Date: Jan 30, 2002
 * Time: 5:15:44 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class DataReaderIBM1_3_1 extends AbstractDataReader {

    private DateFormat cycleStartGCFormat;

    public DataReaderIBM1_3_1(GCResource gcResource, InputStream in) throws UnsupportedEncodingException {
        super(gcResource, in);
    }

    public GCModel read() throws IOException {
        if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reading IBM 1.3.1 format...");
        try {
            final GCModel model = new GCModel();
            model.setFormat(GCModel.Format.IBM_VERBOSE_GC);
            int state = 0;
            String line = null;
            GCEvent lastEvent = new GCEvent();
            GCEvent event = null;
            long basetime = 0;
            cycleStartGCFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US);
            while ((line = in.readLine()) != null && shouldContinue()) {
                final String trimmedLine = line.trim();
                if (!"".equals(trimmedLine) && !trimmedLine.startsWith("<GC: ") && !trimmedLine.startsWith("<")) {
                    System.err.println("Malformed line (" + in.getLineNumber() + "): " + line);
                    state = 0;
                }
                switch (state) {
                    case 0:
                        if (line.indexOf("Allocation Failure.") != -1) {
                            event = new GCEvent();
                            event.setType(AbstractGCEvent.Type.FULL_GC);
                            event.setTimestamp(lastEvent.getTimestamp() + parseTimeSinceLastAF(line));
                            // stay in state 0
                            break;
                        }
                        else if (line.indexOf("GC cycle started") != -1) { // can apparently occur without AF
                            event = new GCEvent();
                            event.setType(AbstractGCEvent.Type.FULL_GC);
                            final long time = parseGCCycleStart(line);
                            if (basetime == 0) basetime = time;
                            event.setTimestamp((time - basetime)/1000.0d);
                            state++;
                            break;
                        }
                        else if (line.indexOf("managing allocation failure, action=3") != -1) {
                            event = new GCEvent();
                            event.setType(AbstractGCEvent.Type.FULL_GC);
                            event.setTimestamp(lastEvent.getTimestamp() + lastEvent.getPause());
                            event.setPreUsed(parsePreUsedAFAction3(line));
                            event.setPostUsed(event.getPreUsed());
                            state = 2;
                            break;
                        }
                        break;
                    case 1:
                        if (line.indexOf("freed") != -1 && line.indexOf("unloaded") == -1) {
                            event.setPreUsed(parsePreUsed(line));
                            event.setPostUsed(parsePostUsed(line));
                            event.setTotal(parseTotalAfterGC(line));
                            event.setPause(parsePause(line));
                            model.add(event);
                            lastEvent = event;
                            event = null;
                            state = 0;
                            break;
                        }
                        break;
                    case 2:
                        if (line.indexOf("expanded heap by ") != -1 || line.indexOf("expanded heap fully by ") != -1) {
                            event.setTotal(parseTotalAfterHeapExpansion(line));
                            state++;
                            break;
                        }
                        break;
                    case 3:
                        if (line.indexOf("completed in ") != -1) {
                            event.setPause(parsePause(line) - lastEvent.getPause());
                            model.add(event);
                            lastEvent = event;
                            event = null;
                            state = 0;
                        }
                        break;
                    default:
                }
            }
            //System.err.println(model);
            return model;
        }
        finally {
            if (getLogger().isLoggable(Level.INFO)) getLogger().info("Done reading.");
        }
    }

    private long parseGCCycleStart(final String line) throws IOException {
        try {
            final int idx = line.indexOf("GC cycle started ");
            final Date date = cycleStartGCFormat.parse(line.substring(idx + "GC cycle started ".length()));
            return date.getTime();
        }
        catch (java.text.ParseException e) {
            throw new com.tagtraum.perf.gcviewer.imp.ParseException(e.toString());
        }
    }

    private double parseTimeSinceLastAF(final String line) {
        final int start = line.indexOf(',') + 2;
        final int end = line.indexOf(' ', start);
        return NumberParser.parseDouble(line.substring(start, end)) / 1000.0d;
    }

    private int parsePreUsed(final String line) {
        int start = line.indexOf("freed ") + "freed ".length();
        int end = line.indexOf(' ', start);
        final long freed = Long.parseLong(line.substring(start, end));

        start = line.indexOf('(', line.indexOf("freed")) + 1;
        end = line.indexOf(')', start);
        final int mid = line.indexOf('/', start);

        final long postFreedFree = Long.parseLong(line.substring(mid + 1, end))
                - Long.parseLong(line.substring(start, mid));
        return (int)((freed+postFreedFree) / 1024L);
    }

    private int parsePreUsedAFAction3(final String line) {
        final int start = line.indexOf('(', line.indexOf("action=3")) + 1;
        final int end = line.indexOf(')', start);
        final int mid = line.indexOf('/', start);
        return (int)((Long.parseLong(line.substring(mid + 1, end))
                - Long.parseLong(line.substring(start, mid))) / 1024L);
    }

    private int parsePostUsed(final String line) {
        final int start = line.indexOf('(', line.indexOf("freed")) + 1;
        final int end = line.indexOf(')', start);
        final int mid = line.indexOf('/', start);
        return (int)((Long.parseLong(line.substring(mid + 1, end))
                - Long.parseLong(line.substring(start, mid))) / 1024L);
    }

    private int parseTotalAfterGC(final String line) {
        final int start = line.indexOf('/', line.indexOf("freed")) + 1;
        final int end = line.indexOf(')', start);
        return (int)(Long.parseLong(line.substring(start, end)) / 1024L);
    }

    private int parseTotalAfterHeapExpansion(final String line) {
        final int start = line.indexOf("to ") + 3;
        final int end = line.indexOf(' ', start);
        return (int)(Long.parseLong(line.substring(start, end)) / 1024L);
    }

    private double parsePause(final String line) {
        final int start = line.indexOf("in ") + 3;
        final int end = line.indexOf(' ', start);
        return NumberParser.parseDouble(line.substring(start, end)) / 1000.0d;
    }
}

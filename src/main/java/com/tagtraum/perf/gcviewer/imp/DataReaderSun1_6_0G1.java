package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tagtraum.perf.gcviewer.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.AbstractGCEvent.Concurrency;
import com.tagtraum.perf.gcviewer.AbstractGCEvent.GcPattern;
import com.tagtraum.perf.gcviewer.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.G1GcEvent;
import com.tagtraum.perf.gcviewer.GCEvent;
import com.tagtraum.perf.gcviewer.GCModel;
import com.tagtraum.perf.gcviewer.util.ParsePosition;

public class DataReaderSun1_6_0G1 extends AbstractDataReaderSun {

    private static Logger LOG = Logger.getLogger(DataReaderSun1_6_0G1.class .getName());

    // the following pattern is specific for G1 with -XX:+PrintGCDetails
    // "0.295: [GC pause (young), 0.00594747 secs]"
    private static final Pattern PATTERN_GC_PAUSE = Pattern.compile("^([0-9.]+)[: \\[]{3}([A-Za-z- ()]+)[, ]+([0-9.]+)[ sec\\]]+$");
    // "   [ 4096K->3936K(16M)]"
    private static final Pattern PATTERN_MEMORY = Pattern.compile("^[ \\[]{5}[0-9]+[KMG].*");

    // G1 log output in 1.6.0_u25 sometimes starts a new line somewhere in line being written
    // the pattern is "...)<timestamp>..."
    // or "...Full GC<timestamp>..."
    private static Pattern PATTERN_LINES_MIXED = Pattern.compile("(.*\\)|.*Full GC)([0-9.]+.*)"); 

    private static final int GC_TIMESTAMP = 1;
    private static final int GC_TYPE = 2;
    private static final int GC_PAUSE = 3;

    public DataReaderSun1_6_0G1(InputStream in) throws UnsupportedEncodingException {
        super(in);
    }

    @Override
    public GCModel read() throws IOException {
        if (LOG.isLoggable(Level.INFO)) LOG.info("Reading Sun 1.6.x G1 format...");

        try {
            final GCModel model = new GCModel(true);
            // TODO what is this for?
            model.setFormat(GCModel.Format.SUN_X_LOG_GC);
            String line;
            final ParsePosition parsePosition = new ParsePosition(0);
            boolean isInDetailedEvent = false;
            Matcher gcPauseMatcher = PATTERN_GC_PAUSE.matcher("");
            Matcher memoryMatcher = PATTERN_MEMORY.matcher("");
            Matcher linesMixedMatcher = PATTERN_LINES_MIXED.matcher("");
            GCEvent gcEvent = null;
            int lineNumber = 0;
            String beginningOfLine = null;
            while ((line = in.readLine()) != null) {
                ++lineNumber;
                parsePosition.setLineNumber(lineNumber);
                if ("".equals(line)) {
                    continue;
                }
                try {
                    if (!isInDetailedEvent) {
                        // if a new timestamp occurs in the middle of a line, that should be treated as a new line
                        // -> the rest of the old line appears on the next line
                        linesMixedMatcher.reset(line); 
                        if (linesMixedMatcher.matches()) {
                            beginningOfLine = linesMixedMatcher.group(1);
                            model.add(parseLine(linesMixedMatcher.group(2), parsePosition));
                            parsePosition.setIndex(0);
                            continue;
                        }
                        else if (beginningOfLine != null) {
                            line = beginningOfLine + line;
                            beginningOfLine = null;
                        }

                        // the following case is special for -XX:+PrintGCDetails and must be treated
                        // different from the other cases occuring in G1 standard mode
                        // 0.356: [GC pause (young), 0.00219944 secs] -> GC_PAUSE pattern but GC_MEMORY_PAUSE 
                        //   event (has extensive details)
                        // all other GC types are the same as in standard G1 mode.
                        gcPauseMatcher.reset(line);
                        if (gcPauseMatcher.matches()) {
                            AbstractGCEvent.Type type = AbstractGCEvent.Type.parse(gcPauseMatcher.group(GC_TYPE));
                            if (type != null && type.getPattern().compareTo(GcPattern.GC_MEMORY_PAUSE) == 0) {
                                // detailed G1 events start with GC_MEMORY pattern, but are of type GC_MEMORY_PAUSE
                                isInDetailedEvent = true;
    
                                gcEvent = new G1GcEvent();
                                gcEvent.setTimestamp(Double.parseDouble(gcPauseMatcher.group(GC_TIMESTAMP)));
                                gcEvent.setType(type);
                                gcEvent.setPause(Double.parseDouble(gcPauseMatcher.group(GC_PAUSE)));
                            }
                            else {
                                model.add(parseLine(line, parsePosition));
                            }
                        }
                        else if (line.indexOf("Times") < 0) {
                            model.add(parseLine(line, parsePosition));
                        }
                    }
                    else {
                        // now we parse details of a pause
                        // currently everything except memory is skipped
                        memoryMatcher.reset(line);
                        if (memoryMatcher.matches()) {
                            setMemory(gcEvent, line, parsePosition);
                        }
                        
                    }
                    if (line.indexOf("Times") >= 0) {
                        // detailed gc description ends with " [Times: user=...]" -> finalize gcEvent
                        // otherwise just ignore 
                        if (gcEvent != null) {
                            model.add(gcEvent);
                            gcEvent = null;
                            isInDetailedEvent = false;
                        }
                    }
                } catch (Exception pe) {
                    if (LOG.isLoggable(Level.WARNING)) LOG.log(Level.WARNING, pe.getMessage());
                    if (LOG.isLoggable(Level.FINE)) LOG.log(Level.FINE, pe.getMessage(), pe);
                }
                parsePosition.setIndex(0);
            }
            return model;
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            if (LOG.isLoggable(Level.INFO))
                LOG.info("Done reading.");
        }
    }

    @Override
    protected AbstractGCEvent parseLine(final String line, final ParsePosition pos) throws ParseException {
        AbstractGCEvent ae = null;
        try {
            // parse timestamp          "double:"
            // parse collection type    "[TYPE"
            // pre-used->post-used, total, time
            final double timestamp = parseTimestamp(line, pos);
            final GCEvent.Type type = parseType(line, pos);
            // special provision for concurrent events
            if (type.getConcurrency() == Concurrency.CONCURRENT) {
                final ConcurrentGCEvent event = new ConcurrentGCEvent();
                if (type.getPattern() == GcPattern.GC) {
                    event.setTimestamp(timestamp);
                    event.setType(type);
                    // nothing more to parse...
                } 
                else {
                    event.setTimestamp(timestamp);
                    event.setType(type);

                    event.setPause(parsePause(line, pos));
                    event.setDuration(event.getPause());
                    // nothing more to parse
                }
                ae = event;
            } else {
                final GCEvent event = new GCEvent();
                event.setTimestamp(timestamp);
                event.setType(type);
                if (event.getType().getPattern() == GcPattern.GC_MEMORY_PAUSE) {
                    setMemoryAndPauses((GCEvent)event, line, pos);
                }
                else {
                    parsePause(event, line, pos);
                }
                ae = event;
            }
            return ae;
        } catch (RuntimeException rte) {
            throw new ParseException(rte.toString(), line, pos);
        }
    }
    
}

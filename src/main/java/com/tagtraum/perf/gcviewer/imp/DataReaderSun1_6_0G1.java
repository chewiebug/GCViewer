package com.tagtraum.perf.gcviewer.imp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Concurrency;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.GcPattern;
import com.tagtraum.perf.gcviewer.model.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.model.G1GcEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.ParsePosition;

/**
 * <p>Parses log output from Sun / Oracle Java 1.6. / 1.7
 * <br>Supports the following gc algorithms:
 * <ul>
 * <li>-XX:+UseG1GC</li>
 * </ul>
 * </p>
 * <p>All other algorithms for 1.6 / 1.7 are supported by {@link DataReaderSun1_6_0G1}
 * </p>
 * <p>Supports the following options:
 * <ul>
 * <li>-XX:+PrintGCDetails</li>
 * <li>-XX:+PrintGCTimeStamps</li>
 * <li>-XX:+PrintHeapAtGC (output ignored)</li>
 * </ul>
 * </p>
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 23.10.2011</p>
 * @see DataReaderSun1_6_0
 */
public class DataReaderSun1_6_0G1 extends AbstractDataReaderSun {

    private static Logger LOG = Logger.getLogger(DataReaderSun1_6_0G1.class .getName());

    private static final String TIMES = "[Times";
    
    private static final String TIMES_ALONE = " " + TIMES;
    private static final String MARK_STACK_IS_FULL = "Mark stack is full.";
    private static final String SETTING_ABORT_IN = "Setting abort in CSMarkOopClosure";
    private static final List<String> EXCLUDE_STRINGS = new LinkedList<String>();

    static {
        EXCLUDE_STRINGS.add(TIMES_ALONE);
        EXCLUDE_STRINGS.add(MARK_STACK_IS_FULL);
        EXCLUDE_STRINGS.add(SETTING_ABORT_IN);
    }
    
    // the following pattern is specific for G1 with -XX:+PrintGCDetails
    // "0.295: [GC pause (young), 0.00594747 secs]"
    private static final Pattern PATTERN_GC_PAUSE = Pattern.compile("^([0-9.]+)[: \\[]{3}([A-Za-z- ().]+)[, ]+([0-9.]+)[ sec\\]]+$");
    // "   [ 4096K->3936K(16M)]"
    private static final Pattern PATTERN_MEMORY = Pattern.compile("^[ \\[]{5}[0-9]+[KMG].*");

    // G1 log output in 1.6.0_u25 sometimes starts a new line somewhere in line being written
    // the pattern is "...)<timestamp>..."
    // or "...Full GC<timestamp>..."
    // or "...)<timestamp>:  (initial-mark)..." (where the timestamp including ":" belongs to a concurrent event and the rest not)
    // or "...)<timestamp> (initial-mark)..." (where only the timestamp belongs to a concurrent event)
    //private static Pattern PATTERN_LINES_MIXED = Pattern.compile("(.*\\)|.*Full GC)([0-9.]+.*)"); 
    private static Pattern PATTERN_LINES_MIXED = Pattern.compile(
            "(.*\\)|.*Full GC)" + // either starts with ")" or "Full GC"
    		"([0-9.]+[:][ ][\\[].*|" + // 1st pattern: then continues either with "<timestamp>: [" where the timestamp is the start of the complete concurrent collection
    		"([0-9.]+[: ]{2})([ ]?[\\(,].*)|" + // 2nd pattern: or with "<timestamp>:  (..." where only the timestamp is part of the concurrent collection
    		"([0-9.]+)([ ][\\(].*))"); // 3rd pattern: or with "<timestamp> (" or "<timestamp> ," where only the timestamp is part of the concurrent collection  

    private static final int GC_TIMESTAMP = 1;
    private static final int GC_TYPE = 2;
    private static final int GC_PAUSE = 3;

    private static final String HEAP_SIZING_START = "Heap";

    private static final List<String> HEAP_STRINGS = new LinkedList<String>();
    static {
        HEAP_STRINGS.add("garbage-first heap");
        HEAP_STRINGS.add("region size");
        HEAP_STRINGS.add("compacting perm gen");
        HEAP_STRINGS.add("the space");
        HEAP_STRINGS.add("No shared spaces configured.");
        HEAP_STRINGS.add("}");
    }
    
    public DataReaderSun1_6_0G1(InputStream in) throws UnsupportedEncodingException {
        super(in);
    }

    @Override
    public GCModel read() throws IOException {
        if (LOG.isLoggable(Level.INFO)) LOG.info("Reading Sun 1.6.x / 1.7.x G1 format...");

        try {
            final GCModel model = new GCModel(true);
            // TODO what is this for?
            model.setFormat(GCModel.Format.SUN_X_LOG_GC);
            String line;
            final ParsePosition parsePosition = new ParsePosition(0);
            Matcher gcPauseMatcher = PATTERN_GC_PAUSE.matcher("");
            Matcher linesMixedMatcher = PATTERN_LINES_MIXED.matcher("");
            GCEvent gcEvent = null;
            int lineNumber = 0;
            String beginningOfLine = null;
            OUTERLOOP:
            while ((line = in.readLine()) != null) {
                ++lineNumber;
                parsePosition.setLineNumber(lineNumber);
                parsePosition.setIndex(0);
                if ("".equals(line)) {
                    continue;
                }
                try {
                    // filter out lines that don't need to be parsed
                    for (String i : EXCLUDE_STRINGS) {
                        if (line.indexOf(i) == 0) continue OUTERLOOP;
                    }
                    // if a new timestamp occurs in the middle of a line, that should be treated as a new line
                    // -> the rest of the old line appears on the next line
                    linesMixedMatcher.reset(line); 
                    if (linesMixedMatcher.matches()) {
                        if (linesMixedMatcher.group(3) == null && linesMixedMatcher.group(5) == null) {
                            // 1st pattern (complete concurrent collection follows)
                            beginningOfLine = linesMixedMatcher.group(1);
                            model.add(parseLine(linesMixedMatcher.group(2), parsePosition));
                            parsePosition.setIndex(0);
                            continue; // rest of collection is on the next line, so continue there
                        }
                        else if (linesMixedMatcher.group(5) == null) {
                            // 2nd pattern; only timestamp is part of concurrent event
                            beginningOfLine = linesMixedMatcher.group(3); // only timestamp
                            line = linesMixedMatcher.group(1) + linesMixedMatcher.group(4);
                        }
                        else if (linesMixedMatcher.group(3) == null) {
                            // 3rd pattern; again only timestamp is part of concurrent event
                            beginningOfLine = linesMixedMatcher.group(5); // only timestamp
                            line = linesMixedMatcher.group(1) + linesMixedMatcher.group(6);
                        }
                    }
                    else if (beginningOfLine != null) {
                        // not detailed log but mixed line
                        line = beginningOfLine + line;
                        beginningOfLine = null;
                    }
                    
                    if (line.endsWith(MARK_STACK_IS_FULL)) {
                        // "Mark stack is full" message is treated as part of the event name
                        beginningOfLine = line;
                        continue;
                    }
                    
                    // the following case is special for -XX:+PrintGCDetails and must be treated
                    // different from the other cases occurring in G1 standard mode
                    // 0.356: [GC pause (young), 0.00219944 secs] -> GC_PAUSE pattern but GC_MEMORY_PAUSE 
                    //   event (has extensive details)
                    // all other GC types are the same as in standard G1 mode.
                    gcPauseMatcher.reset(line);
                    if (gcPauseMatcher.matches()) {
                        AbstractGCEvent.Type type = AbstractGCEvent.Type.parse(gcPauseMatcher.group(GC_TYPE));
                        if (type != null && type.getPattern().compareTo(GcPattern.GC_MEMORY_PAUSE) == 0) {
                            // detailed G1 events start with GC_MEMORY pattern, but are of type GC_MEMORY_PAUSE

                            gcEvent = new G1GcEvent();
                            gcEvent.setTimestamp(Double.parseDouble(gcPauseMatcher.group(GC_TIMESTAMP)));
                            gcEvent.setType(type);
                            gcEvent.setPause(Double.parseDouble(gcPauseMatcher.group(GC_PAUSE)));
                            
                            // now parse the details of this event
                            lineNumber = parseDetails(in, model, parsePosition, lineNumber, gcEvent, beginningOfLine);
                            beginningOfLine = null;
                            continue;
                        }
                        else {
                            // real GC_PAUSE events like some concurrent events
                            model.add(parseLine(line, parsePosition));
                        }
                    }
                    else if (line.indexOf(HEAP_SIZING_START) >= 0) {
                        // the next few lines will be the sizing of the heap
                        lineNumber = skipLines(in, parsePosition, lineNumber, HEAP_STRINGS);
                        continue;
                    }
                    else {
                        model.add(parseLine(line, parsePosition));
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

    /**
     * Parses details of a standard gc collection event (e.g. GC pause (young)) with
     * -XX:+PrintGCDetails
     * 
     * @param in reader reading from log file
     * @param model current model
     * @param pos parsePosition
     * @param lineNumber line number of last line read
     * @param event current event
     * @param beginningOfLine GC_PAUSE lines are sometimes mixed lines; the extracted parts
     * of such a line are stored inside "beginningOfLine"
     * @return line number of last line read in this method
     * @throws ParseException Problem parsing a part of the details
     * @throws IOException problem reading from file
     */
    private int parseDetails(BufferedReader in, 
            GCModel model,
            ParsePosition pos, 
            int lineNumber, 
            GCEvent event, 
            String beginningOfLine)
                    throws ParseException, IOException {
        
        Matcher memoryMatcher = PATTERN_MEMORY.matcher("");

        pos.setIndex(0);
        boolean isInDetailedEvent = true;
        String line;
        while (isInDetailedEvent && (line = in.readLine()) != null) {
            ++lineNumber;
            pos.setLineNumber(lineNumber);
            
            // we might have had a mixed line before; then we just parsed the second part of the mixed line
            if (beginningOfLine != null) {
                line = beginningOfLine + line;
                beginningOfLine = null;
                model.add(parseLine(line, pos));
                pos.setIndex(0);
                continue;
            }
            
            // now we parse details of a pause
            // currently everything except memory is skipped
            memoryMatcher.reset(line);
            if (memoryMatcher.matches()) {
                setMemory(event, line, pos);
                pos.setIndex(0);
            }

            if (line.indexOf(TIMES) >= 0) {
                // detailed gc description ends with " [Times: user=...]" -> stop reading lines
                isInDetailedEvent = false;
            }
        }
        
        model.add(event);

        return lineNumber;
    }
    
    @SuppressWarnings("rawtypes")
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
                
                // simple concurrent events (ending with -start) just are of type GcPattern.GC
                event.setTimestamp(timestamp);
                event.setType(type);
                if (type.getPattern() == GcPattern.GC_PAUSE) {
                    // the -end events contain a pause as well
                    event.setPause(parsePause(line, pos));
                    event.setDuration(event.getPause());
                }
                ae = event;
            } else {
                final GCEvent event = new GCEvent();
                event.setTimestamp(timestamp);
                event.setType(type);
                // Java 7 can have detailed event at this position like this
                // 0.197: [GC remark 0.197: [GC ref-proc, 0.0000070 secs], 0.0005297 secs]
                if (isTimestamp(line, pos)) {
                    event.add((GCEvent) parseLine(line, pos));
                }
                
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

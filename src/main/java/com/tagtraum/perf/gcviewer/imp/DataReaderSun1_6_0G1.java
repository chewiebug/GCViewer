package com.tagtraum.perf.gcviewer.imp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.CollectionType;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Concurrency;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.ExtendedType;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.GcPattern;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.model.G1GcEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.VmOperationEvent;
import com.tagtraum.perf.gcviewer.util.NumberParser;
import com.tagtraum.perf.gcviewer.util.ParseInformation;

/**
 * Parses log output from Sun / Oracle Java 1.6. / 1.7.
 * <p>
 * Supports the following gc algorithms:
 * <ul>
 * <li>-XX:+UseG1GC</li>
 * </ul>
 * All other algorithms for 1.6 / 1.7 are supported by {@link DataReaderSun1_6_0G1}
 * <p>
 * Supports the following options:
 * <ul>
 * <li>-XX:+PrintGCDetails</li>
 * <li>-XX:+PrintGCTimeStamps</li>
 * <li>-XX:+PrintGCDateStamps</li>
 * <li>-XX:+PrintGCCause</li>
 * <li>-XX:+PrintGCApplicationStoppedTime</li>
 * <li>-XX:+PrintHeapAtGC (output ignored)</li>
 * <li>-XX:+PrintTenuringDistribution (output ignored)</li>
 * <li>-XX:+PrintGCApplicationConcurrentTime (output ignored)</li>
 * <li>-XX:+PrintAdaptiveSizePolicy (output ignored)</li>
 * <li>-XX:+PrintReferenceGC (output ignored)</li>
 * </ul>
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * @see DataReaderSun1_6_0
 */
public class DataReaderSun1_6_0G1 extends AbstractDataReaderSun {

    private static final String INCOMPLETE_CONCURRENT_EVENT_INDICATOR = "concurrent-";

    private static final Logger LOG = Logger.getLogger(DataReaderSun1_6_0G1.class .getName());

    private static final String TIMES = "[Times";

    private static final String TIMES_ALONE = " " + TIMES;
    private static final String APPLICATION_TIME = "Application time:"; // -XX:+PrintGCApplicationConcurrentTime
    private static final String DESIRED_SURVIVOR = "Desired survivor"; // -XX:+PrintTenuringDistribution
    private static final String SURVIVOR_AGE = "- age"; // -XX:+PrintTenuringDistribution
    private static final String MARK_STACK_IS_FULL = "Mark stack is full.";
    private static final String SETTING_ABORT_IN = "Setting abort in CSMarkOopClosure";
    private static final String G1_ERGONOMICS = "G1Ergonomics";
    private static final String SOFT_REFERENCE = "SoftReference";
    private static final List<String> EXCLUDE_STRINGS = new LinkedList<String>();

    static {
        EXCLUDE_STRINGS.add(TIMES_ALONE);
        EXCLUDE_STRINGS.add(APPLICATION_TIME);
        EXCLUDE_STRINGS.add(DESIRED_SURVIVOR);
        EXCLUDE_STRINGS.add(SURVIVOR_AGE);
        EXCLUDE_STRINGS.add(MARK_STACK_IS_FULL);
        EXCLUDE_STRINGS.add(SETTING_ABORT_IN);
        EXCLUDE_STRINGS.add("   [Root Region"); // all the details of a G1 event -> filter away, if parsing of introduction was not possible
        EXCLUDE_STRINGS.add("   [Parallel Time");
        EXCLUDE_STRINGS.add("      [GC Worker Start");
        EXCLUDE_STRINGS.add("      [Ext Root Scanning");
        EXCLUDE_STRINGS.add("      [SATB Filtering");
        EXCLUDE_STRINGS.add("      [Update RS");
        EXCLUDE_STRINGS.add("         [Processed Buffers");
        EXCLUDE_STRINGS.add("      [Scan RS");
        EXCLUDE_STRINGS.add("      [Code Root Scanning");
        EXCLUDE_STRINGS.add("      [Object Copy");
        EXCLUDE_STRINGS.add("      [Termination");
        EXCLUDE_STRINGS.add("      [GC Worker");
        EXCLUDE_STRINGS.add("   [Code Root");
        EXCLUDE_STRINGS.add("   [Clear");
        EXCLUDE_STRINGS.add("   [Other:");
        EXCLUDE_STRINGS.add("      [Choose CSet");
        EXCLUDE_STRINGS.add("      [Ref ");
        EXCLUDE_STRINGS.add("      [Redirty Cards");
        EXCLUDE_STRINGS.add("      [Humongous Reclaim");
        EXCLUDE_STRINGS.add("      [Free CSet");
        EXCLUDE_STRINGS.add("/proc/meminfo"); // apple vms seem to print this out in the beginning of the logs
    }

    // the following pattern is specific for G1 with -XX:+PrintGCDetails
    // "[<datestamp>: ]0.295: [GC pause (young), 0.00594747 secs]"
    private static final Pattern PATTERN_GC_PAUSE = Pattern.compile("^([0-9-T:.+]{29})?[ ]?([0-9.,]+)?[: \\[]{2,3}([A-Z0-9a-z- ().]+)[, ]+([0-9.,]+)[ sec\\]]+$");
    private static final int GC_PAUSE_GROUP_DATESTAMP = 1;
    private static final int GC_PAUSE_GROUP_TIMESTAMP = 2;
    private static final int GC_PAUSE_GROUP_TYPE = 3;
    private static final int GC_PAUSE_GROUP_PAUSE = 4;

    // "   [ 4096K->3936K(16M)]"
    private static final Pattern PATTERN_MEMORY = Pattern.compile("^[ \\[]*[0-9]+[BKMG].*");

    private static final String INITIAL_MARK = "(initial-mark)";
    private static final String TO_SPACE_OVERFLOW = "(to-space overflow)";

    // G1 log output in 1.6.0_u25 sometimes starts a new line somewhere in line being written
    // the pattern is "...)<timestamp>..."
    // or "...Full GC<timestamp>..."
    // or "...)<timestamp>:  (initial-mark)..." (where the timestamp including ":" belongs to a concurrent event and the rest not)
    // or "...)<timestamp> (initial-mark)..." (where only the timestamp belongs to a concurrent event)
    private static final Pattern PATTERN_LINES_MIXED = Pattern.compile("(.*\\)|.*Full GC)([0-9.]+.*)");

    private static final Pattern PATTERN_G1_ERGONOMICS = Pattern.compile("(.*)\\W\\d+[\\.,]\\d{3}\\W{2}\\[G1Ergonomics .+\\].*");

    private static final String HEAP_SIZING_START = "Heap";

    private static final List<String> HEAP_STRINGS = new LinkedList<String>();
    static {
        HEAP_STRINGS.add("garbage-first heap");
        HEAP_STRINGS.add("region size");
        HEAP_STRINGS.add("compacting perm gen");
        HEAP_STRINGS.add("the space");
        HEAP_STRINGS.add("No shared spaces configured.");
        HEAP_STRINGS.add("Metaspace"); // java 8
        HEAP_STRINGS.add("class space"); // java 8
        HEAP_STRINGS.add("}");
        HEAP_STRINGS.add("[0x"); // special case of line following one containing a concurrent event mixed with heap information
        HEAP_STRINGS.add("total"); // special case of line following one containing a concurrent event mixed with heap information
    }

    /** is true, if "[Times ..." information is present in the gc log */
    private boolean hasTimes = false;

    public DataReaderSun1_6_0G1(InputStream in, GcLogType gcLogType) throws UnsupportedEncodingException {
        super(in, gcLogType);
    }

    @Override
    public GCModel read() throws IOException {
        if (LOG.isLoggable(Level.INFO)) LOG.info("Reading Sun 1.6.x / 1.7.x G1 format...");

        try (BufferedReader in = this.in) {
            GCModel model = new GCModel();
            // TODO what is this for?
            model.setFormat(GCModel.Format.SUN_X_LOG_GC);
            String line;
            ParseInformation parsePosition = new ParseInformation(0);
            Matcher gcPauseMatcher = PATTERN_GC_PAUSE.matcher("");
            Matcher linesMixedMatcher = PATTERN_LINES_MIXED.matcher("");
            Matcher ergonomicsMatcher = PATTERN_G1_ERGONOMICS.matcher("");
            GCEvent gcEvent = null;
            int lineNumber = 0;
            String beginningOfLine = null;

            while ((line = in.readLine()) != null) {
                ++lineNumber;
                parsePosition.setLineNumber(lineNumber);
                parsePosition.setIndex(0);
                if ("".equals(line)) {
                    continue;
                }
                try {
                    // filter out lines that don't need to be parsed
                    if (startsWith(line, EXCLUDE_STRINGS, false)) {
                        continue;
                    }
                    else if (line.indexOf(APPLICATION_TIME) > 0) {
                        continue;
                    }
                    else if (startsWith(line, LOG_INFORMATION_STRINGS, false)) {
                        LOG.info(line);
                        continue;
                    }

                    // remove G1 ergonomics pieces
                    if (line.indexOf(G1_ERGONOMICS) >= 0) {
                        ergonomicsMatcher.reset(line);
                        if (ergonomicsMatcher.matches()) {
                            String firstMatch = (ergonomicsMatcher.group(1));
                            if (firstMatch.length() > 0 && line.indexOf(SOFT_REFERENCE) < 0) {
                                beginningOfLine = firstMatch;
                            }
                            continue;
                        }
                    }

                    // if a new timestamp occurs in the middle of a line, that should be treated as a new line
                    // -> the rest of the old line appears on the next line
                    linesMixedMatcher.reset(line);
                    if (linesMixedMatcher.matches()) {
                        if (line.indexOf("concurrent") > 0) {
                            // 1st pattern (complete concurrent collection follows)
                          beginningOfLine = linesMixedMatcher.group(1);
                          model.add(parseLine(linesMixedMatcher.group(2), parsePosition));
                          parsePosition.setIndex(0);
                          continue; // rest of collection is on the next line, so continue there
                        }
                        else if (line.indexOf(SOFT_REFERENCE) > 0 && line.indexOf(Type.FULL_GC.getName()) > 0) {
                            // for Full GCs, SoftReference entries are treated as unknown detail events
                            // -> parseLine can do this
                        }
                        else if (line.endsWith("secs]")) {
                            // all other patterns: some timestamps follow that are part of a concurrent collection
                            // but the rest of the line is the rest of the same collection
                            StringBuilder realLine = new StringBuilder();
                            realLine.append(linesMixedMatcher.group(1));
                            int toSpaceIndex = line.indexOf(TO_SPACE_OVERFLOW);
                            int initialMarkIndex = line.indexOf(INITIAL_MARK);
                            if (toSpaceIndex > 0 && realLine.length() < toSpaceIndex) {
                                realLine.append(" ").append(TO_SPACE_OVERFLOW);
                            }
                            if (initialMarkIndex > 0 && realLine.length() < initialMarkIndex) {
                                realLine.append(" ").append(INITIAL_MARK);
                            }
                            realLine.append(line.substring(line.lastIndexOf(",")));
                            line = realLine.toString();
                        }
                        else {
                            throw new ParseException("unexpected mixed line", line, parsePosition);
                        }
                    }
                    else if (beginningOfLine != null) {
                        // filter output of -XX:+PrintReferencePolicy away
                        if (line.indexOf(SOFT_REFERENCE) >= 0) {
                            line = line.substring(line.lastIndexOf(","));
                        }

                        // not detailed log but mixed line
                        line = beginningOfLine + line;
                        beginningOfLine = null;
                    }

                    if (line.endsWith(MARK_STACK_IS_FULL)) {
                        // "Mark stack is full" message is treated as part of the event name
                        beginningOfLine = line;
                        continue;
                    }
                    else if (isPrintTenuringDistribution(line)) {
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
                        ExtendedType type = extractTypeFromParsedString(gcPauseMatcher.group(GC_PAUSE_GROUP_TYPE));

                        if (type != null && type.getPattern().compareTo(GcPattern.GC_MEMORY_PAUSE) == 0) {
                            // detailed G1 events start with GC_MEMORY pattern, but are of type GC_MEMORY_PAUSE

                            gcEvent = new G1GcEvent();
                            ZonedDateTime datestamp = parseDatestamp(gcPauseMatcher.group(GC_PAUSE_GROUP_DATESTAMP), parsePosition);
                            gcEvent.setDateStamp(datestamp);
                            double timestamp = 0;
                            if (gcPauseMatcher.group(GC_PAUSE_GROUP_TIMESTAMP) == null) {
                                timestamp = getTimestamp(line, parsePosition, datestamp);
                            }
                            else {
                                timestamp = NumberParser.parseDouble(gcPauseMatcher.group(GC_PAUSE_GROUP_TIMESTAMP));
                            }
                            gcEvent.setTimestamp(timestamp);
                            gcEvent.setExtendedType(type);
                            gcEvent.setPause(NumberParser.parseDouble(gcPauseMatcher.group(GC_PAUSE_GROUP_PAUSE)));

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
                    else if (line.indexOf(Type.FULL_GC.getName()) > 0) {
                        // since jdk 1.8 full gc events in G1 have detailed heap sizing information on the next line
                        GCEvent fullGcEvent = (GCEvent) parseLine(line, parsePosition);
                        if (!in.markSupported()) {
                            LOG.warning("input stream does not support marking!");
                        }
                        else {
                            in.mark(200);
                            try {
                                line = in.readLine();
                                if (line != null && line.trim().startsWith("[Eden")) {
                                    parseMemoryDetails(fullGcEvent, line, parsePosition);
                                }
                                else {
                                    // push last read line back into stream - it is the next event to be parsed
                                    in.reset();
                                }
                            }
                            catch (IOException e) {
                                throw new ParseException("problem resetting stream (" + e.toString() + ")", line, parsePosition);
                            }
                        }
                        model.add(fullGcEvent);
                    }
                    else if (line.indexOf(HEAP_SIZING_START) >= 0) {
                        // the next few lines will be the sizing of the heap
                        lineNumber = skipLinesRespectingConcurrentEvents(in, model, parsePosition, lineNumber, HEAP_STRINGS);
                        continue;
                    }
                    else if (hasIncompleteConcurrentEvent(line, parsePosition)) {
                        parseIncompleteConcurrentEvent(model, model.getLastEventAdded(), line, parsePosition);
                    }
                    else {
                        model.add(parseLine(line, parsePosition));
                    }
                }
                catch (Exception pe) {
                    if (LOG.isLoggable(Level.WARNING)) LOG.log(Level.WARNING, pe.toString());
                    if (LOG.isLoggable(Level.FINE)) LOG.log(Level.FINE, pe.toString(), pe);
                }
                parsePosition.setIndex(0);
            }
            return model;
        }
        finally {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Done reading.");
            }
        }
    }

    private boolean hasIncompleteConcurrentEvent(String line, ParseInformation paresPosition) {
        return !nextIsTimestamp(line, paresPosition)
                && !nextIsDatestamp(line, paresPosition)
                && line.indexOf(INCOMPLETE_CONCURRENT_EVENT_INDICATOR) >= 0;
    }

    /**
     * Returns true, if <code>line</code> ends with one of the detailed event types.
     *
     * @param line current line
     * @return <code>true</code>, if <code>-XX:+PrintTenuringDistribution</code> was used
     */
    private boolean isPrintTenuringDistribution(String line) {
        return     (line.indexOf("GC pause") >= 0 && line.endsWith(")"))
                || (line.indexOf(Type.FULL_GC.getName()) >= 0 && line.endsWith(")"))
                ;
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
     * @throws IOException problem reading from file
     */
    private int parseDetails(BufferedReader in,
            GCModel model,
            ParseInformation pos,
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
            pos.setIndex(0);

            if (line.length() == 0) {
                continue;
            }

            // we might have had a mixed line before; then we just parsed the second part of the mixed line
            if (beginningOfLine != null) {
                line = beginningOfLine + line;
                beginningOfLine = null;
                model.add(parseLine(line, pos));
                continue;
            }

            // now we parse details of a pause
            // currently everything except memory is skipped
            if (line.indexOf("Eden") >= 0) {
                parseMemoryDetails(event, line, pos);
            }
            else if (line.charAt(0) != ' ' && !hasTimes && (nextIsDatestamp(line, pos) || nextIsTimestamp(line, pos))) {
                // special case for simple logs (marked by missing "[Times..." in the log)
                // since the line starts with a time / datestamp, the detailed event seems to be finished (unexpectedly)
                model.add(parseLine(line, pos));
                isInDetailedEvent = false;
            }
            else if (line.indexOf(INCOMPLETE_CONCURRENT_EVENT_INDICATOR) >= 0) {
                parseIncompleteConcurrentEvent(model, event, line, pos);
            }
            else {
                memoryMatcher.reset(line);
                if (memoryMatcher.matches()) {
                    // it is java 1.7_u1 or earlier (including java 1.6)
                    // memory part looks like
                    //    [ 8192K->8128K(64M)]
                    setMemoryExtended(event, line, pos);
                }
            }

            if (line.indexOf(TIMES) >= 0) {
                // detailed gc description ends with " [Times: user=...]" -> stop reading lines
                isInDetailedEvent = false;
                hasTimes = true;
            }
        }

        if (event.getTotal() == 0) {
            // is currently the case for jdk 1.7.0_02 which changed the memory format
            // as of 1.7.0_25 for "GC cleanup" events, there seem to be rare cases, where this just happens
            // => don't log as warning; just log on debug level
            if (LOG.isLoggable(Level.FINE)) LOG.fine("line " + lineNumber + ": no memory information found (" + event.toString() + ")");
        }
        model.add(event);

        return lineNumber;
    }

    /**
     * Parse detailed memory format of G1 ("[Eden: ... Survivors: ... Heap: ...]")
     *
     * @param event Event to add the detailed head information to
     * @param line line containing the detailed heap information
     * @param pos parseInformation concerning gc log
     * @throws ParseException problem while parsing
     */
    private void parseMemoryDetails(GCEvent event, String line, ParseInformation pos) throws ParseException {
        assert line.indexOf("Eden") > 0 : "String 'Eden' not found in line (" + line + ")";
        // it is java 1.7_u2
        // memory part looks like
        //    [Eden: 8192K(8192K)->0B(8192K) Survivors: 0B->8192K Heap: 8192K(16M)->7895K(16M)]

        // parse Eden
        pos.setIndex(line.indexOf("Eden:"));
        GCEvent youngEvent = new GCEvent();
        youngEvent.setDateStamp(event.getDatestamp());
        youngEvent.setTimestamp(event.getTimestamp());
        youngEvent.setExtendedType(parseType(line, pos));
        setMemoryExtended(youngEvent, line, pos);

        // add survivors
        pos.setIndex(line.indexOf("Survivors:") + "Survivors:".length() + 1);
        GCEvent survivorsEvent = new GCEvent();
        setMemoryExtended(survivorsEvent, line, pos);
        youngEvent.setPreUsed(youngEvent.getPreUsed() + survivorsEvent.getPreUsed());
        youngEvent.setPostUsed(youngEvent.getPostUsed() + survivorsEvent.getPostUsed());
        youngEvent.setTotal(youngEvent.getTotal() + survivorsEvent.getPostUsed());

        event.add(youngEvent);

        // parse heap size
        pos.setIndex(line.indexOf("Heap:") + "Heap:".length() + 1);
        setMemoryExtended(event, line, pos);

        // parse Metaspace
        if (line.indexOf("Metaspace:") > 0) {
            pos.setIndex(line.indexOf("Metaspace:"));
            GCEvent metaSpace = new GCEvent();
            metaSpace.setDateStamp(event.getDatestamp());
            metaSpace.setTimestamp(event.getTimestamp());
            metaSpace.setExtendedType(parseType(line, pos));
            setMemoryExtended(metaSpace, line, pos);

            event.add(metaSpace);
        }

    }

    /**
     * Parses an incomplete line containing a concurrent-mark-start / -end event. The timestamp
     * is taken from the previous event.
     *
     * @param model model where event should be added
     * @param previousEvent last complete event that occurred
     * @param line line containing the incomplete concurrent event
     * @throws ParseException
     */
    private void parseIncompleteConcurrentEvent(GCModel model, AbstractGCEvent<?> previousEvent, String line, ParseInformation pos) throws ParseException {
        // some concurrent event is mixed in -> extract it
        pos.setIndex(line.indexOf("GC conc"));
        ExtendedType type = parseType(line, pos);
        model.add(parseConcurrentEvent(line,
                pos,
                previousEvent != null ? previousEvent.getDatestamp() : null,
                previousEvent != null ? previousEvent.getTimestamp() : 0,
                type));
    }

    @Override
    protected AbstractGCEvent<?> parseLine(String line, ParseInformation pos) throws ParseException {
        AbstractGCEvent<?> ae = null;
        try {
            // parse datestamp          "yyyy-MM-dd'T'hh:mm:ssZ:"
            // parse timestamp          "double:"
            // parse collection type    "[TYPE"
            // pre-used->post-used, total, time
            ZonedDateTime datestamp = parseDatestamp(line, pos);
            double timestamp = getTimestamp(line, pos, datestamp);
            ExtendedType type = parseType(line, pos);
            // special provision for concurrent events
            if (type.getConcurrency() == Concurrency.CONCURRENT) {
                ae = parseConcurrentEvent(line, pos, datestamp, timestamp, type);
            }
            else if (type.getCollectionType().equals(CollectionType.VM_OPERATION)) {
                ae = new VmOperationEvent();
                VmOperationEvent vmOpEvent = (VmOperationEvent) ae;

                vmOpEvent.setDateStamp(datestamp);
                vmOpEvent.setTimestamp(timestamp);
                vmOpEvent.setExtendedType(type);
                vmOpEvent.setPause(parsePause(line, pos));
            }
            else {
                ae = new GCEvent();
                GCEvent event = (GCEvent) ae;
                event.setDateStamp(datestamp);
                event.setTimestamp(timestamp);
                event.setExtendedType(type);
                // Java 7 can have detailed event at this position like this
                // 0.197: [GC remark 0.197: [GC ref-proc, 0.0000070 secs], 0.0005297 secs]
                // or when PrintDateTimeStamps is on like:
                // 2013-09-09T06:45:45.825+0000: 83146.942: [GC remark 2013-09-09T06:45:45.825+0000: 83146.943: [GC ref-proc, 0.0069100 secs], 0.0290090 secs]
                    parseDetailEventsIfExist(line, pos, event);

                if (event.getExtendedType().getPattern() == GcPattern.GC_MEMORY_PAUSE) {
                    setMemoryAndPauses(event, line, pos);
                }
                else {
                    event.setPause(parsePause(line, pos));
                }
            }
            return ae;
        }
        catch (RuntimeException rte) {
            throw new ParseException(rte.toString(), line, pos);
        }
    }

    /**
     * Parse concurrent event
     *
     * @param line line containing concurrent event
     * @param pos position where event starts
     * @param datestamp datestamp
     * @param timestamp timestamp
     * @param type type of event
     * @return complete concurrent event
     * @throws ParseException
     */
    private AbstractGCEvent<?> parseConcurrentEvent(String line,
            ParseInformation pos, ZonedDateTime datestamp,
            double timestamp, final ExtendedType type) throws ParseException {

        ConcurrentGCEvent event = new ConcurrentGCEvent();

        // simple concurrent events (ending with -start) just are of type GcPattern.GC
        event.setDateStamp(datestamp);
        event.setTimestamp(timestamp);
        event.setExtendedType(type);
        if (type.getPattern() == GcPattern.GC_PAUSE) {
            // the -end events contain a pause as well
            event.setPause(parsePause(line, pos));
            event.setDuration(event.getPause());
        }
        return event;
    }

    /**
     * Skips a block of lines containing information like they are generated by
     * -XX:+PrintHeapAtGC or -XX:+PrintAdaptiveSizePolicy.
     *
     * @param in inputStream of the current log to be read
     * @param lineNumber current line number
     * @param lineStartStrings lines starting with these strings should be ignored
     * @return line number including lines read in this method
     * @throws IOException problem with reading from the file
     */
    private int skipLinesRespectingConcurrentEvents(BufferedReader in, GCModel model, ParseInformation pos, int lineNumber, List<String> lineStartStrings) throws IOException {
        String line = "";

        if (!in.markSupported()) {
            LOG.warning("input stream does not support marking!");
        }
        else {
            in.mark(200);
        }

        boolean startsWithString = true;
        while (startsWithString && (line = in.readLine()) != null) {
            ++lineNumber;
            pos.setLineNumber(lineNumber);

            if (line.indexOf(INCOMPLETE_CONCURRENT_EVENT_INDICATOR) >= 0) {
                parseIncompleteConcurrentEvent(model, model.getLastEventAdded(), line, pos);
            }
            else {
                // for now just skip those lines
                startsWithString = startsWith(line, lineStartStrings, true);
                if (startsWithString) {
                    // don't mark any more if line didn't match -> it is the first line that
                    // is of interest after the skipped block
                    if (in.markSupported()) {
                        in.mark(200);
                    }
                }
            }
        }

        // push last read line back into stream - it is the next event to be parsed
        if (in.markSupported()) {
            try {
                in.reset();
            }
            catch (IOException e) {
                throw new ParseException("problem resetting stream (" + e.toString() + ")", line, pos);
            }
        }

        return --lineNumber;
    }

}

package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Concurrency;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.GcPattern;
import com.tagtraum.perf.gcviewer.model.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.NumberParser;

/**
 * DataReaderUnifiedJvmLogging can parse all gc events of unified jvm logs with default decorations.
 * <p>
 * Currently needs the "gc" selector with "info" level and "uptime,level,tags" decorators (Java 9.0.1).
 * <ul>
 * <li>minimum configuration with defaults: <code>-Xlog:gc:file="path-to-file"</code></li>
 * <li>configuration with tags + decorations: <code>-Xlog:gc=info:file="path-to-file":tags,uptime,level</code></li>
 * </ul>
 * Only processes the following information format for Serial, Parallel, CMS, G1 and Shenandoah algorithms, everything else is ignored:
 * <pre>
 * [0.731s][info][gc           ] GC(0) Pause Init Mark 1.021ms
 * [0.735s][info][gc           ] GC(0) Concurrent marking 74M-&gt;74M(128M) 3.688ms
 * [43.948s][info][gc             ] GC(831) Pause Full (Allocation Failure) 7943M-&gt;6013M(8192M) 14289.335ms
 * </pre>
 *
 * <p>
 * For more information about Shenandoah see: <a href="https://wiki.openjdk.java.net/display/shenandoah/Main">Shenandoah Wiki at OpenJDK</a>
 */
public class DataReaderUnifiedJvmLogging extends AbstractDataReader {

    // matches the whole line and extracts decorators from it (decorators always appear between [] and are independent of the gc algorithm being logged)
    // Input: [0.693s][info][gc           ] GC(0) Pause Init Mark 1.070ms
    // Group 1 / time: <empty> (optional group, no full timestamp present)
    // Group 2 / uptime: 0.693 (optional group, present in this example)
    // Group 3 / level: info
    // Group 4 / tags: gc
    // Group 5 / gcnumber: 0
    // Group 6 / tail: Pause Init Mark 1.070ms
    // Regex: ^(?:\[(?<time>[0-9-T:.+]*)])?(?:\[(?<uptime>[^s]*)s])?\[(?<level>[^]]+)]\[(?:(?<tags>[^] ]+)[ ]*)][ ]GC\((?<gcnumber>[0-9]+)\)[ ](?<type>([-.a-zA-Z ()]+|[a-zA-Z1 ()]+))(?:(?:[ ](?<tail>[0-9]{1}.*))|$)
    //   note for the <type> part: easiest would have been to use [^0-9]+, but the G1 events don't fit there, because of the number in their name
    private static final Pattern PATTERN_DECORATORS = Pattern.compile(
            "^(?:\\[(?<time>[0-9-T:.+]*)])?(?:\\[(?<uptime>[^s]*)s])?\\[(?<level>[^]]+)]\\[(?:(?<tags>[^] ]+)[ ]*)][ ]GC\\((?<gcnumber>[0-9]+)\\)[ ](?<type>[-.a-zA-Z ()]+|[a-zA-Z1 ()]+)(?:(?:[ ](?<tail>[0-9]{1}.*))|$)"
    );
    private static final String GROUP_DECORATORS_TIME = "time";
    private static final String GROUP_DECORATORS_UPTIME = "uptime";
    private static final String GROUP_DECORATORS_LEVEL = "level";
    private static final String GROUP_DECORATORS_TAGS = "tags";
    private static final String GROUP_DECORATORS_GC_NUMBER = "gcnumber";
    private static final String GROUP_DECORATORS_GC_TYPE = "type";
    private static final String GROUP_DECORATORS_TAIL = "tail";

    private static final String PATTERN_PAUSE_STRING = "([0-9]+[.,][0-9]+)ms";
    // Input: 1.070ms
    // Group 1: 1.070
    private static final Pattern PATTERN_PAUSE = Pattern.compile("^" + PATTERN_PAUSE_STRING);

    private static final int GROUP_PAUSE = 1;

    // Input: 4848M->4855M(4998M) 2.872ms
    // Group 1: 4848
    // Group 2: M
    // Group 3: 4855
    // Group 4: M
    // Group 5: 4998
    // Group 6: M
    // Group 7: 1.070
    private static final Pattern PATTERN_MEMORY_PAUSE = Pattern.compile("^(([0-9]+)([BKMG])->([0-9]+)([BKMG])\\(([0-9]+)([BKMG])\\)) " + PATTERN_PAUSE_STRING);

    private static final int GROUP_MEMORY = 1;
    private static final int GROUP_MEMORY_BEFORE = 2;
    private static final int GROUP_MEMORY_BEFORE_UNIT = 3;
    private static final int GROUP_MEMORY_AFTER = 4;
    private static final int GROUP_MEMORY_AFTER_UNIT = 5;
    private static final int GROUP_MEMORY_CURRENT_TOTAL = 6;
    private static final int GROUP_MEMORY_CURRENT_TOTAL_UNIT = 7;
    private static final int GROUP_MEMORY_PAUSE = 8;

    /** list of strings, that must be part of the gc log line to be considered for parsing */
    private static final List<String> INCLUDE_STRINGS = Arrays.asList("[gc ", "[gc]");
    /** list of strings, that target gc log lines, that - although part of INCLUDE_STRINGS - are not considered a gc event */
    private static final List<String> EXCLUDE_STRINGS = Arrays.asList("Cancelling concurrent GC", "[debug", "[trace");
    /** list of strings, that are gc log lines, but not a gc event -&gt; should be logged only */
    private static final List<String> LOG_ONLY_STRINGS = Arrays.asList("Using");


    protected DataReaderUnifiedJvmLogging(GCResource gcResource, InputStream in) throws UnsupportedEncodingException {
        super(gcResource, in);
    }

    @Override
    public GCModel read() throws IOException {
        getLogger().info("Reading Oracle / OpenJDK unified jvm logging format...");

        try {
            GCModel model = new GCModel();
            model.setFormat(GCModel.Format.UNIFIED_JVM_LOGGING);

            Stream<String> lines = in.lines();
            lines.filter(this::lineContainsParseableEvent)
                    .map(this::parseEvent)
                    .filter(Objects::nonNull)
                    .forEach(model::add);

            return model;
        } finally {
            if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reading done.");
        }
    }

    private AbstractGCEvent<?> parseEvent(String line) {
        AbstractGCEvent<?> event = null;

        Matcher decoratorsMatcher = PATTERN_DECORATORS.matcher(line);
        try {
            event = createGcEventWithStandardDecorators(decoratorsMatcher, line);
            if (event != null) {
                String tail = decoratorsMatcher.group(GROUP_DECORATORS_TAIL);
                if (event.getExtendedType().getPattern().equals(GcPattern.GC_PAUSE)) {
                    handleGcPauseTail(line, event, tail);
                } else if (event.getExtendedType().getPattern().equals(GcPattern.GC_MEMORY_PAUSE)) {
                    handleGcMemoryPauseTail(line, event, tail);
                } else if (event.getExtendedType().getPattern().equals(GcPattern.GC) || event.getExtendedType().getPattern().equals(GcPattern.GC_PAUSE_DURATION)) {
                    handleGcTail(line, tail);
                }
            }
        } catch (UnknownGcTypeException | NumberFormatException e) {
            // prevent incomplete event from being added to the GCModel
            event = null;
            getLogger().warning(String.format("Failed to parse gc event (%s) on line number %d (line=\"%s\")", e.toString(), in.getLineNumber(), line));
        }

        return event;
    }

    private void handleGcTail(String line, String tail) {
        if (!(tail == null)) {
            getLogger().warning(String.format("Unexpected tail present in the end of line number %d (expected nothing to be present, tail=\"%s\"; line=\"%s\")", in.getLineNumber(), tail, line));
        }
    }

    private void handleGcMemoryPauseTail(String line, AbstractGCEvent<?> event, String tail) {
        Matcher memoryPauseMatcher = PATTERN_MEMORY_PAUSE.matcher(tail);
        if (memoryPauseMatcher.find()) {
            setPause(event, memoryPauseMatcher.group(GROUP_MEMORY_PAUSE));
            setMemory(event, memoryPauseMatcher);
        } else {
            getLogger().warning(String.format("Expected memory and pause in the end of line number %d (line=\"%s\")", in.getLineNumber(), line));
        }
    }

    private void handleGcPauseTail(String line, AbstractGCEvent<?> event, String tail) {
        // G1 and CMS algorithms have "gc" tagged concurrent events, that are actually the start of an event
        // (I'd expect them to be tagged "gc,start", as this seems to be the case for the Shenandoah algorithm)
        // G1: Concurrent Cycle (without pause -> start of event; with pause -> end of event)
        // CMS: all concurrent events

        // this is the reason, why a "null" tail is accepted here
        if (tail != null) {
            Matcher pauseMatcher = PATTERN_PAUSE.matcher(tail);
            if (pauseMatcher.find()) {
                setPause(event, pauseMatcher.group(GROUP_PAUSE));
            } else {
                getLogger().warning(String.format("Expected only pause in the end of line number %d  (line=\"%s\")", in.getLineNumber(), line));
            }
        }
    }

    /**
     * Returns an instance of AbstractGcEvent (GCEvent or ConcurrentGcEvent) with all decorators present filled in
     * or <code>null</code> if the line could not be matched.
     * @param decoratorsMatcher matcher for decorators to be used for GcEvent creation
     * @param line current line to be parsed
     * @return Instance of <code>AbstractGcEvent</code> or <code>null</code> if the line could not be matched.
     */
    private AbstractGCEvent<?> createGcEventWithStandardDecorators(Matcher decoratorsMatcher, String line) throws UnknownGcTypeException {
        if (decoratorsMatcher.find()) {
            AbstractGCEvent.ExtendedType type = getDataReaderTools().parseType(decoratorsMatcher.group(GROUP_DECORATORS_GC_TYPE));

            AbstractGCEvent<?> event = type.getConcurrency().equals(Concurrency.CONCURRENT) ? new ConcurrentGCEvent() : new GCEvent();
            event.setExtendedType(type);
            setDateStampIfPresent(event, decoratorsMatcher.group(GROUP_DECORATORS_TIME));
            setTimeStampIfPresent(event, decoratorsMatcher.group(GROUP_DECORATORS_UPTIME));
            return event;
        } else {
            getLogger().warning(String.format("Failed to parse line number %d (no match; line=\"%s\")", in.getLineNumber(), line));
            return null;
        }
    }

    private void setPause(AbstractGCEvent event, String pauseAsString) {
        // TODO remove code duplication with AbstractDataReaderSun -> move to DataReaderTools
        if (pauseAsString != null && pauseAsString.length() > 0) {
            event.setPause(NumberParser.parseDouble(pauseAsString) / 1000);
        }
    }

    private void setMemory(AbstractGCEvent event, Matcher matcher) {
        // TODO remove code duplication with AbstractDataReaderSun -> move to DataReaderTools
        event.setPreUsed(getDataReaderTools().getMemoryInKiloByte(
                Integer.parseInt(matcher.group(GROUP_MEMORY_BEFORE)), matcher.group(GROUP_MEMORY_BEFORE_UNIT).charAt(0), matcher.group(GROUP_MEMORY)));
        event.setPostUsed(getDataReaderTools().getMemoryInKiloByte(
                Integer.parseInt(matcher.group(GROUP_MEMORY_AFTER)), matcher.group(GROUP_MEMORY_AFTER_UNIT).charAt(0), matcher.group(GROUP_MEMORY)));
        event.setTotal(getDataReaderTools().getMemoryInKiloByte(
                Integer.parseInt(matcher.group(GROUP_MEMORY_CURRENT_TOTAL)), matcher.group(GROUP_MEMORY_CURRENT_TOTAL_UNIT).charAt(0), matcher.group(GROUP_MEMORY)));
        event.setExtendedType(event.getExtendedType());
    }

    private void setDateStampIfPresent(AbstractGCEvent<?> event, String dateStampAsString) {
        // TODO remove code duplication with AbstractDataReaderSun -> move to DataReaderTools
        if (dateStampAsString != null) {
            ZonedDateTime dateTime = ZonedDateTime.parse(dateStampAsString, AbstractDataReaderSun.DATE_TIME_FORMATTER);
            event.setDateStamp(dateTime);
        }
    }

    private void setTimeStampIfPresent(AbstractGCEvent<?> event, String timeStampAsString) {
        if (timeStampAsString != null && timeStampAsString.length() > 0) {
            event.setTimestamp(NumberParser.parseDouble(timeStampAsString));
        }
    }

    private boolean isExcludedLine(String line) {
        return EXCLUDE_STRINGS.stream().anyMatch(line::contains);
    }

    private boolean isCandidateForParseableEvent(String line) {
        return INCLUDE_STRINGS.stream().anyMatch(line::contains);
    }

    private boolean isLogOnlyLine(String line) {
        return LOG_ONLY_STRINGS.stream().anyMatch(line::contains);
    }

    private boolean lineContainsParseableEvent(String line) {
        if (isCandidateForParseableEvent(line) && !isExcludedLine(line)) {
            if (isLogOnlyLine(line)) {
                getLogger().info(line.substring(line.lastIndexOf("]")+1));
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

}
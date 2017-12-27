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
import com.tagtraum.perf.gcviewer.model.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;

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

    // Input: [0.693s][info][gc           ] GC(0) Pause Init Mark 1.070ms
    // Group 1: 0.693
    // Group 2: Pause Init Mark
    // Group 3: 1.070 -> optional
    // Regex: ^\[([^s\]]*)[^\-]*\)[ ]([^\-]*)[ ]([0-9]+[.,][0-9]+)
    private static final Pattern PATTERN_WITHOUT_HEAP = Pattern.compile(
            "^\\[([^s\\]]*)[^\\-]*\\)[ ]([^\\d]*)(([0-9]+[.,][0-9]+)|$)");

    // Input: [13.522s][info][gc            ] GC(708) Concurrent evacuation  4848M->4855M(4998M) 2.872ms
    // Group 1: 13.522
    // Group 2: Concurrent evacuation
    // Group 3: 4848M->4855M(4998M)
    // Group 4: 2.872
    // Regex: ^\[([^s\]]*).*\)[ ](.*)[ ]([0-9]+[BKMG]\-\>[0-9]+[BKMG]\([0-9]+[BKMG]\)) ([0-9]+[.,][0-9]+)
    private static final Pattern PATTERN_WITH_HEAP = Pattern.compile(
            "^\\[([^s\\]]*).*\\)[ ](.*)[ ]([0-9]+[BKMG]\\-\\>[0-9]+[BKMG]\\([0-9]+[BKMG]\\)) ([0-9]+[.,][0-9]+)");

    // Input: 4848M->4855M(4998M)
    // Group 1: 4848
    // Group 2: 4855
    // Group 3: 4998
    // Regex: ([0-9]+)[BKMG]\-\>([0-9]+)[BKMG]\(([0-9]+)[BKMG]\)
    private static final Pattern PATTERN_HEAP_CHANGES = Pattern.compile(
            "([0-9]+)([BKMG])->([0-9]+)([BKMG])\\(([0-9]+)([BKMG])\\)");

    // Input: 2017-08-30T23:22:47.357+0300
    // Regex: ^\d{4}\-\d\d\-\d\d[tT][\d:\.]*?(?:[zZ]|[+\-]\d\d:?\d\d)?$
    private static final Pattern PATTERN_ISO8601_DATE = Pattern.compile(
            "^\\d{4}\\-\\d\\d\\-\\d\\d[tT][\\d:\\.]*?(?:[zZ]|[+\\-]\\d\\d:?\\d\\d)?$");

    private static final int NO_HEAP_TIMESTAMP = 1;
    private static final int NO_HEAP_EVENT_NAME = 2;
    private static final int NO_HEAP_DURATION = 3;
    private static final int WITH_HEAP_TIMESTAMP = 1;
    private static final int WITH_HEAP_EVENT_NAME = 2;
    private static final int WITH_HEAP_MEMORY = 3;
    private static final int WITH_HEAP_DURATION = 4;
    private static final int HEAP_BEFORE = 1;
    private static final int HEAP_BEFORE_UNIT = 2;
    private static final int HEAP_AFTER = 3;
    private static final int HEAP_AFTER_UNIT = 4;
    private static final int HEAP_CURRENT_TOTAL = 5;
    private static final int HEAP_CURRENT_TOTAL_UNIT = 6;

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

        Matcher noHeapMatcher = PATTERN_WITHOUT_HEAP.matcher(line);
        Matcher withHeapMatcher = PATTERN_WITH_HEAP.matcher(line);
        try {
            event = line.contains("Concurrent") ? new ConcurrentGCEvent() : new GCEvent();
            if (noHeapMatcher.find()) {
                AbstractGCEvent.ExtendedType type = getDataReaderTools().parseType(noHeapMatcher.group(NO_HEAP_EVENT_NAME));
                event.setExtendedType(type);
                setPauseAndDateOrTimestamp(event, noHeapMatcher.group(NO_HEAP_TIMESTAMP), noHeapMatcher.groupCount() > 2 ? noHeapMatcher.group(NO_HEAP_DURATION) : null);
            } else if (withHeapMatcher.find()) {
                AbstractGCEvent.ExtendedType type = getDataReaderTools().parseType(withHeapMatcher.group(WITH_HEAP_EVENT_NAME));
                event.setExtendedType(type);
                setPauseAndDateOrTimestamp(event, withHeapMatcher.group(WITH_HEAP_TIMESTAMP), withHeapMatcher.group(WITH_HEAP_DURATION));
                addHeapDetailsToEvent(event, withHeapMatcher.group(WITH_HEAP_MEMORY));
            } else {
                // prevent incomplete event from being added to the GCModel
                event = null;
                getLogger().warning(String.format("Failed to parse line number %d (line=\"%s\")", in.getLineNumber(), line));
            }
        } catch (UnknownGcTypeException | NumberFormatException e) {
            // prevent incomplete event from being added to the GCModel
            event = null;
            getLogger().warning(String.format("Failed to parse gc event (%s) on line number %d (line=\"%s\")", e.toString(), in.getLineNumber(), line));
        }

        return event;
    }

    /**
     * @param event                     GC event to which pause and timestamp information is added
     * @param pauseAsString             Pause information from regex group as string
     * @param dateOrTimeStampAsString   Date- or timestamp information from regex group as string
     */
    private void setPauseAndDateOrTimestamp(AbstractGCEvent<?> event, String dateOrTimeStampAsString, String pauseAsString) {
        if (pauseAsString != null && pauseAsString.length() > 0) {
            double pause = Double.parseDouble(pauseAsString.replace(",", "."));
            event.setPause(pause / 1000);
        }
        if (PATTERN_ISO8601_DATE.matcher(dateOrTimeStampAsString).find()) {
            ZonedDateTime dateTime = ZonedDateTime.parse(dateOrTimeStampAsString, AbstractDataReaderSun.DATE_TIME_FORMATTER);
            event.setDateStamp(dateTime);
        } else {
            double timestamp = Double.parseDouble(dateOrTimeStampAsString.replace(",", "."));
            event.setTimestamp(timestamp);
        }
    }

    /**
     * @param event        GC event to which the heap change information is added
     * @param memoryString Memory changes in format 100M->80M(120M) where 100M-before, 80M-after, 120M-max
     */
    private void addHeapDetailsToEvent(AbstractGCEvent<?> event, String memoryString) {
        Matcher matcher = PATTERN_HEAP_CHANGES.matcher(memoryString);
        if (matcher.find()) {
            event.setPreUsed(getDataReaderTools().getMemoryInKiloByte(
                    Integer.parseInt(matcher.group(HEAP_BEFORE)), matcher.group(HEAP_BEFORE_UNIT).charAt(0), memoryString));
            event.setPostUsed(getDataReaderTools().getMemoryInKiloByte(
                    Integer.parseInt(matcher.group(HEAP_AFTER)), matcher.group(HEAP_AFTER_UNIT).charAt(0), memoryString));
            event.setTotal(getDataReaderTools().getMemoryInKiloByte(
                    Integer.parseInt(matcher.group(HEAP_CURRENT_TOTAL)), matcher.group(HEAP_CURRENT_TOTAL_UNIT).charAt(0), memoryString));
            event.setExtendedType(event.getExtendedType());
        } else {
            getLogger().warning("Failed to find heap details from string: \"" + memoryString + "\"");
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
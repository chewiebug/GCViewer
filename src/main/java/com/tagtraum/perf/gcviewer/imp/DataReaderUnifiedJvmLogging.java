package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Concurrency;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.GcPattern;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCEventUJL;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.VmOperationEvent;
import com.tagtraum.perf.gcviewer.util.DateHelper;
import com.tagtraum.perf.gcviewer.util.NumberParser;

/**
 * DataReaderUnifiedJvmLogging can parse all gc events of unified jvm logs with default decorations.
 * <p>
 * Currently needs the "gc" selector with "info" level and "uptime,level,tags" decorators (Java 9.0.1).
 * Also supports "gc*" selector with "trace" level and "time,uptime,level,tags" decorators, but will ignore some of
 * the debug and all trace level info (evaluates the following tags: "gc", "gc,start", "gc,heap", "gc,metaspace".
 * <ul>
 * <li>minimum configuration with defaults supported: <code>-Xlog:gc:file="path-to-file"</code></li>
 * <li>explicit minimum configuration needed: <code>-Xlog:gc=info:file="path-to-file":tags,uptime,level</code></li>
 * <li>maximum detail configuration this parser is able to work with: <code>-Xlog:gc*=trace:file="path-to-file":tags,time,uptime,level</code></li>
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
    
    // matches log decorators (like time stamp, up time, level, tags and message)
    private static final Pattern PATTERN_DECORATORS = Pattern.compile("^(?:\\[(?<time>[0-9-T:.+]*)])?(?:\\[(?<uptime>[^s]*)s])?\\[(?<level>[^]]+)]\\[(?:(?<tags>[^] ]+)[ ]*)][ ](?<message>.*)");

    // matches gc event
    private static final Pattern PATTERN_GC_MESSAGE = Pattern.compile("^GC\\((?<gcnumber>[0-9]+)\\)[ ](?<type>[-.a-zA-Z: ()]+|[a-zA-Z1 ()]+)(?:(?:[ ](?<tail>[0-9]{1}.*))|$)");

    // matches application stopped time (entries with [safepoint] tag
    private static final Pattern PATTERN_APPLICATION_STOPPED = Pattern.compile("Total time for which application threads were stopped: (?<applicationstopped>[0-9]+,[0-9]+) seconds, Stopping threads took: (?<threadsstopping>[0-9]+,[0-9]+) seconds");
    
    private static final String GROUP_DECORATORS_TIME = "time";
    private static final String GROUP_DECORATORS_UPTIME = "uptime";
    private static final String GROUP_DECORATORS_LEVEL = "level";
    private static final String GROUP_DECORATORS_TAGS = "tags";
    private static final String GROUP_DECORATORS_MESSAGE = "message";
    private static final String GROUP_DECORATORS_GC_NUMBER = "gcnumber";
    private static final String GROUP_DECORATORS_GC_TYPE = "type";
    private static final String GROUP_DECORATORS_TAIL = "tail";

    private static final Pattern PATTERN_HEAP_REGION_SIZE = Pattern.compile("^Heap region size: ([0-9]+)M$");
    private static final int GROUP_HEAP_REGION_SIZE = 1;

    private static final String PATTERN_PAUSE_STRING = "([0-9]+[.,][0-9]+)ms";
    private static final String PATTERN_MEMORY_STRING = "(([0-9]+)([BKMG])->([0-9]+)([BKMG])\\(([0-9]+)([BKMG])\\))";

    // Input: 1.070ms
    // Group 1: 1.070
    private static final Pattern PATTERN_PAUSE = Pattern.compile("^" + PATTERN_PAUSE_STRING);

    private static final int GROUP_PAUSE = 1;

    // Input: 4848M->4855M(4998M)
    // Group 1: 4848
    // Group 2: M
    // Group 3: 4855
    // Group 4: M
    // Group 5: 4998
    // Group 6: M
    private static final Pattern PATTERN_MEMORY = Pattern.compile("^" + PATTERN_MEMORY_STRING);

    // Input: 4848M->4855M(4998M) 2.872ms
    // Group 1: 4848
    // Group 2: M
    // Group 3: 4855
    // Group 4: M
    // Group 5: 4998
    // Group 6: M
    // Group 7: 1.070 (optional group)
    private static final Pattern PATTERN_MEMORY_PAUSE = Pattern.compile("^" + PATTERN_MEMORY_STRING + "(?:(?:[ ]" + PATTERN_PAUSE_STRING + ")|$)");

    private static final int GROUP_MEMORY = 1;
    private static final int GROUP_MEMORY_BEFORE = 2;
    private static final int GROUP_MEMORY_BEFORE_UNIT = 3;
    private static final int GROUP_MEMORY_AFTER = 4;
    private static final int GROUP_MEMORY_AFTER_UNIT = 5;
    private static final int GROUP_MEMORY_CURRENT_TOTAL = 6;
    private static final int GROUP_MEMORY_CURRENT_TOTAL_UNIT = 7;
    private static final int GROUP_MEMORY_PAUSE = 8;

    // Input: 7->3(2)
    // Group 1: 7
    // Group 2: 3
    // Group 3: 2 (optional group)
    private static final Pattern PATTERN_REGION = Pattern.compile("^([0-9]+)->([0-9]+)(?:\\(([0-9]+)\\))?");

    private static final int GROUP_REGION_BEFORE = 1;
    private static final int GROUP_REGION_AFTER = 2;
    private static final int GROUP_REGION_TOTAL = 3;

    private static final String TAG_GC = "gc";
    private static final String TAG_GC_START = "gc,start";
    private static final String TAG_GC_HEAP = "gc,heap";
    private static final String TAG_GC_METASPACE = "gc,metaspace";

    /** list of strings, that must be part of the gc log line to be considered for parsing */
    private static final List<String> INCLUDE_STRINGS = Arrays.asList("[gc ", "[gc]", "[safepoint]","[" + TAG_GC_START, "[" + TAG_GC_HEAP, "[" + TAG_GC_METASPACE);
    /** list of strings, that target gc log lines, that - although part of INCLUDE_STRINGS - are not considered a gc event */
    private static final List<String> EXCLUDE_STRINGS = Arrays.asList("Cancelling concurrent GC", "[debug", "[trace", "gc,heap,coops", "gc,heap,exit", "Application time:");
    /** list of strings, that are gc log lines, but not a gc event -&gt; should be logged only */
    private static final List<String> LOG_ONLY_STRINGS = Arrays.asList("Using", "Heap region size");



    protected DataReaderUnifiedJvmLogging(GCResource gcResource, InputStream in) throws UnsupportedEncodingException {
        super(gcResource, in);
    }

    @Override
    public GCModel read() throws IOException {
        getLogger().info("Reading Oracle / OpenJDK unified jvm logging format...");

        try {
            // some information shared accross several lines of parsing...
            Map<String, AbstractGCEvent<?>> partialEventsMap = new HashMap<>();
            Map<String, Object> infoMap = new HashMap<>();

            GCModel model = new GCModel();
            model.setFormat(GCModel.Format.UNIFIED_JVM_LOGGING);

            Stream<String> lines = in.lines();
            lines.map(line -> new ParseContext(line, partialEventsMap, infoMap))
                    .filter(this::lineContainsParseableEvent)
                    .map(this::parseEvent)
                    .filter(context -> context.getCurrentEvent() != null)
                    .forEach(context -> model.add(context.getCurrentEvent()));

            return model;
        } finally {
            if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reading done.");
        }
    }

    private ParseContext parseEvent(ParseContext context) {
        AbstractGCEvent<?> event = null;
        String line = context.getLine();
        
	// extract log decorators (like time stamp, up time, level, tags and message)
        Matcher eventDecoratorsMatcher = PATTERN_DECORATORS.matcher(line );
        LogEvent logEvent = parseLogEvent(eventDecoratorsMatcher);
        
        if (logEvent==null) {
            getLogger().warning(String.format("Failed to parse line number %d (no match; line=\"%s\")", in.getLineNumber(), line));
        } else {
            String message = logEvent.message;
            try {
        	if("safepoint".equals(logEvent.tags)) {
        	    Matcher applicationStoppedMatcher = PATTERN_APPLICATION_STOPPED.matcher(logEvent.message);
        	    event = parseSafepointEvent(logEvent, applicationStoppedMatcher);        	    
        	} else {
        	    Matcher gcMatcher = PATTERN_GC_MESSAGE.matcher(message);
                    event = createGcEvent(context, logEvent, gcMatcher);        	    
        	}
            } catch (UnknownGcTypeException | NumberFormatException e) {
                // prevent incomplete event from being added to the GCModel
                event = null;
                getLogger().warning(String.format("Failed to parse gc event (%s) on line number %d (line=\"%s\")", e.toString(), in.getLineNumber(), context.getLine()));
            }
        }
        

        context.setCurrentEvent(event);
        return context;
    }

    private AbstractGCEvent<?> parseSafepointEvent(LogEvent logEvent, Matcher applicationStoppedMatcher) { 
	if (applicationStoppedMatcher.find()) {
            AbstractGCEvent<?> event = new VmOperationEvent();
            event.setType(Type.APPLICATION_STOPPED_TIME);
            setPause(event, applicationStoppedMatcher.group("applicationstopped"));
            setDateStampIfPresent(event, logEvent.time);
            setTimeStampIfPresent(event, logEvent.uptime);
            return event;
        } 
	return null;
    }

    private LogEvent parseLogEvent(Matcher eventDecoratorsMatcher) {
        if(eventDecoratorsMatcher.matches()) {
            String time = eventDecoratorsMatcher.group( GROUP_DECORATORS_TIME );
            String uptime = eventDecoratorsMatcher.group( GROUP_DECORATORS_UPTIME );            
            String level = eventDecoratorsMatcher.group( GROUP_DECORATORS_LEVEL );
            String tags = eventDecoratorsMatcher.group( GROUP_DECORATORS_TAGS );
            String message = eventDecoratorsMatcher.group( GROUP_DECORATORS_MESSAGE );
            return new LogEvent(time,uptime,level,tags,message);
        }
        return null;
    }

    private AbstractGCEvent<?> handleTail(ParseContext context, AbstractGCEvent<?> event, String tags, String tail) {
        AbstractGCEvent<?> returnEvent = event;
        switch (tags) {
            case TAG_GC_START:
                // here, the gc type is known, and the partial events will need to be added later
                context.getPartialEventsMap().put(event.getNumber() + "", event);
                returnEvent = null;
                break;
            case TAG_GC_HEAP:
                // fallthrough -> same handling as for METASPACE event
            case TAG_GC_METASPACE:
                event = parseTail(context, event, tail);
                // the UJL "Old" event occurs often after the next STW events have taken place; ignore it for now
                //   size after concurrent collection will be calculated by GCModel#add()
                if (!event.getExtendedType().getType().equals(Type.UJL_CMS_CONCURRENT_OLD)) {
                    updateEventDetails(context, event);
                }
                returnEvent = null;
                break;
            case TAG_GC:
                AbstractGCEvent<?> parentEvent = context.getPartialEventsMap().get(event.getNumber() + "");
                if (parentEvent != null) {
                    if (parentEvent.getExtendedType().equals(returnEvent.getExtendedType())) {
                        // date- and timestamp are always end of event -> adjust the parent event
                        parentEvent.setDateStamp(event.getDatestamp());
                        parentEvent.setTimestamp(event.getTimestamp());
                        returnEvent = parseTail(context, parentEvent, tail);
                        context.partialEventsMap.remove(event.getNumber() + "");
                    } else {
                        // more detail information is provided for the parent event
                        updateEventDetails(context, returnEvent);
                        returnEvent = null;
                    }
                } else {
                    returnEvent = parseTail(context, event, tail);
                }
                break;
            default:
                getLogger().warning(String.format("Unexpected tail present in the end of line number %d (tail=\"%s\"; line=\"%s\")", in.getLineNumber(), tail, context.getLine()));
        }

        return returnEvent;
    }

    private void updateEventDetails(ParseContext context, AbstractGCEvent<?> event) {
        AbstractGCEvent<?> parentEvent = context.getPartialEventsMap().get(event.getNumber() + "");
        if (parentEvent == null) {
            getLogger().warning(String.format("Didn't find parent event for partial event %s (line number %d, line=\"%s\"", event.toString(), in.getLineNumber(), context.getLine()));
        } else {
            if (parentEvent instanceof GCEvent) {
                ((GCEvent)parentEvent).add((GCEvent)event);
            } else {
                getLogger().warning(String.format("Parent (%s) event for %s should be GCEvent (line number %d, line=\"%s\"", parentEvent.toString(), event.toString(), in.getLineNumber(), context.getLine()));
            }
        }
    }

    private AbstractGCEvent<?> parseTail(ParseContext context, AbstractGCEvent<?> event, String tail) {
        if (event.getExtendedType().getPattern().equals(GcPattern.GC_PAUSE)) {
            parseGcPauseTail(context, event, tail);
        } else if (event.getExtendedType().getPattern().equals(GcPattern.GC_MEMORY)) {
            parseGcMemoryTail(context, event, tail);
        } else if (event.getExtendedType().getPattern().equals(GcPattern.GC_REGION)) {
            parseGcRegionTail(context, event, tail);
        } else if (event.getExtendedType().getPattern().equals(GcPattern.GC_MEMORY_PAUSE)) {
            parseGcMemoryPauseTail(context, event, tail);
        } else if (event.getExtendedType().getPattern().equals(GcPattern.GC) || event.getExtendedType().getPattern().equals(GcPattern.GC_PAUSE_DURATION)) {
            parseGcTail(context, tail);
        }

        return event;
    }

    private void parseGcTail(ParseContext context, String tail) {
        if (!(tail == null)) {
            getLogger().warning(String.format("Unexpected tail present in the end of line number %d (expected nothing to be present, tail=\"%s\"; line=\"%s\")", in.getLineNumber(), tail, context.getLine()));
        }
    }

    private void parseGcMemoryTail(ParseContext context, AbstractGCEvent<?> event, String tail) {
        Matcher memoryMatcher = tail != null ? PATTERN_MEMORY.matcher(tail) : null;
        if (memoryMatcher != null && memoryMatcher.find()) {
            setMemory(event, memoryMatcher);
        } else {
            getLogger().warning(String.format("Expected only memory in the end of line number %d (line=\"%s\")", in.getLineNumber(), context.getLine()));
        }
    }

    private void parseGcMemoryPauseTail(ParseContext context, AbstractGCEvent<?> event, String tail) {
        Matcher memoryPauseMatcher = tail != null ? PATTERN_MEMORY_PAUSE.matcher(tail) : null;
        if (memoryPauseMatcher != null && memoryPauseMatcher.find()) {
            setPause(event, memoryPauseMatcher.group(GROUP_MEMORY_PAUSE));
            if (!hasMemory(event)) {
                // if the event already has detail memory information, there is no need to add the high level one as well
                setMemory(event, memoryPauseMatcher);
            }
        } else {
            getLogger().warning(String.format("Expected memory and pause in the end of line number %d (line=\"%s\")", in.getLineNumber(), context.getLine()));
        }
    }

    private void parseGcPauseTail(ParseContext context, AbstractGCEvent<?> event, String tail) {
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
                getLogger().warning(String.format("Expected only pause in the end of line number %d  (line=\"%s\")", in.getLineNumber(), context.getLine()));
            }
        }
    }

    private void parseGcRegionTail(ParseContext context, AbstractGCEvent<?> event, String tail) {
        Matcher regionMatcher = tail != null ? PATTERN_REGION.matcher(tail) : null;
        if (regionMatcher != null && regionMatcher.find()) {
            int regionSize = context.getRegionSize();
            // if the event has regions, but the regionSize is unknown, at the moment, I don't know, how to calculate the size
            // -> store 0 for the size
            // this happens, whenever a G1 log file is parsed and the line
            // [0.018s][info][gc,heap] Heap region size: 1M
            // is missing (only part of log present)
            event.setPreUsed(Integer.parseInt(regionMatcher.group(GROUP_REGION_BEFORE)) * regionSize * 1024);
            event.setPostUsed(Integer.parseInt(regionMatcher.group(GROUP_REGION_AFTER)) * regionSize * 1024);
            if (regionMatcher.group(GROUP_REGION_TOTAL) != null) {
                event.setTotal(Integer.parseInt(regionMatcher.group(GROUP_REGION_TOTAL)) * regionSize * 1024);
            }
        } else {
            getLogger().warning(String.format("Expected region information in the end of line number %d (line=\"%s\")", in.getLineNumber(), context.getLine()));
        }
    }

    /**
     * Returns an instance of AbstractGcEvent (GCEvent or ConcurrentGcEvent) with all decorators present filled in
     * or <code>null</code> if the line could not be matched.
     * @param logEvent 
     * @param decoratorsMatcher matcher for decorators to be used for GcEvent creation
     * @param line current line to be parsed
     * @return Instance of <code>AbstractGcEvent</code> or <code>null</code> if the line could not be matched.
     */
    private AbstractGCEvent<?> createGcEvent(ParseContext context, LogEvent logEvent, Matcher decoratorsMatcher) throws UnknownGcTypeException {
        if (decoratorsMatcher.matches()) {
            AbstractGCEvent.ExtendedType type = getDataReaderTools().parseType(decoratorsMatcher.group(GROUP_DECORATORS_GC_TYPE));

            AbstractGCEvent<?> event = type.getConcurrency().equals(Concurrency.CONCURRENT) ? new ConcurrentGCEvent() : new GCEventUJL();
            event.setExtendedType(type);
            event.setNumber(Integer.parseInt(decoratorsMatcher.group(GROUP_DECORATORS_GC_NUMBER)));
            setDateStampIfPresent(event, logEvent.time);
            setTimeStampIfPresent(event, logEvent.uptime);
            String tags = logEvent.tags;
            String tail = decoratorsMatcher.group(GROUP_DECORATORS_TAIL);
            event = handleTail(context, event, tags, tail);
            return event;
        }
        return null;
    }

    private void setPause(AbstractGCEvent<?> event, String pauseAsString) {
        // TODO remove code duplication with AbstractDataReaderSun -> move to DataReaderTools
        if (pauseAsString != null && pauseAsString.length() > 0) {
            event.setPause(NumberParser.parseDouble(pauseAsString) / 1000);
        }
    }

    private boolean hasMemory(AbstractGCEvent<?> event) {
        return event.getTotal() > 0;
    }

    private void setMemory(AbstractGCEvent<?> event, Matcher matcher) {
        // TODO remove code duplication with AbstractDataReaderSun -> move to DataReaderTools
        event.setPreUsed(getDataReaderTools().getMemoryInKiloByte(
                Integer.parseInt(matcher.group(GROUP_MEMORY_BEFORE)), matcher.group(GROUP_MEMORY_BEFORE_UNIT).charAt(0), matcher.group(GROUP_MEMORY)));
        event.setPostUsed(getDataReaderTools().getMemoryInKiloByte(
                Integer.parseInt(matcher.group(GROUP_MEMORY_AFTER)), matcher.group(GROUP_MEMORY_AFTER_UNIT).charAt(0), matcher.group(GROUP_MEMORY)));
        event.setTotal(getDataReaderTools().getMemoryInKiloByte(
                Integer.parseInt(matcher.group(GROUP_MEMORY_CURRENT_TOTAL)), matcher.group(GROUP_MEMORY_CURRENT_TOTAL_UNIT).charAt(0), matcher.group(GROUP_MEMORY)));
    }

    private void setDateStampIfPresent(AbstractGCEvent<?> event, String dateStampAsString) {
        // TODO remove code duplication with AbstractDataReaderSun -> move to DataReaderTools
        if (dateStampAsString != null) {
            event.setDateStamp(DateHelper.parseDate(dateStampAsString));
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

    private boolean lineContainsParseableEvent(ParseContext context) {
        if (isCandidateForParseableEvent(context.getLine()) && !isExcludedLine(context.getLine())) {
            if (isLogOnlyLine(context.getLine())) {
                String tail = context.getLine().substring(context.getLine().lastIndexOf("]")+1);
                enrichContext(context, tail);
                getLogger().info(tail);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private void enrichContext(ParseContext context, String tail) {
        Matcher regionSizeMatcher = tail != null ? PATTERN_HEAP_REGION_SIZE.matcher(tail.trim()) : null;
        if (regionSizeMatcher != null && regionSizeMatcher.find()) {
            try {
                context.setRegionSize(Integer.parseInt(regionSizeMatcher.group(GROUP_HEAP_REGION_SIZE)));
            } catch (NumberFormatException e) {
                getLogger().warning(String.format("Failed to parse heap region size on line %d (line=%s)", in.getLineNumber(), context.getLine()));
            }
        }
    }

    private static class ParseContext {
        /** G1 has a region size and logs the gc,heap information with # of regions */
        private static final String REGION_SIZE_KEY = "regionSize";
        private Map<String, AbstractGCEvent<?>> partialEventsMap;
        private Map<String, Object> info;
        private String line;
        private AbstractGCEvent<?> currentEvent;

        public ParseContext(String line, Map<String, AbstractGCEvent<?>> partialEventsMap, Map<String, Object> info) {
            this.line = line;
            this.partialEventsMap = partialEventsMap;
            this.info = info;
        }

        public String getLine() {
            return line;
        }

        public Map<String, AbstractGCEvent<?>> getPartialEventsMap() {
            return partialEventsMap;
        }

        public AbstractGCEvent<?> getCurrentEvent() {
            return currentEvent;
        }

        public void setCurrentEvent(AbstractGCEvent<?> currentEvent) {
            this.currentEvent = currentEvent;
        }

        public int getRegionSize() {
            Object regionSize = this.info.get(REGION_SIZE_KEY);
            return regionSize != null ? (Integer)regionSize : 0;
        }

        public void setRegionSize(int regionSize) {
            this.info.put(REGION_SIZE_KEY, regionSize);
        }

        @Override
        public String toString() {
            return line + (getRegionSize() > 0 ? "; regionsSize=" + getRegionSize() : "") + "; partialEventsMap.size()=" + partialEventsMap.size() + "currentEvent=" + getCurrentEvent();
        }

    }
    
    class LogEvent {

        private final String time;
	private final String uptime;
	private final String level;
	private final String tags;
	private final String message;

	LogEvent(String time, String uptime, String level, String tags, String message) {
	    this.time = time;
	    this.uptime = uptime;
	    this.level = level;
	    this.tags = tags;
	    this.message = message;
        }

    }

}
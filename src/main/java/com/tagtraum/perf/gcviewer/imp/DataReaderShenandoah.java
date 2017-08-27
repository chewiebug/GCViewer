package com.tagtraum.perf.gcviewer.imp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;

/**
 * Currently only parsing 5 main messages for GCViewer with default decorations.
 * Initial mark, concurrent mark, final mark, concurrent evacuation
 * <p>
 * Example Format
 * [0.730s][info][gc,start     ] GC(0) Pause Init Mark
 * [0.731s][info][gc           ] GC(0) Pause Init Mark 1.021ms
 * [0.731s][info][gc,start     ] GC(0) Concurrent marking
 * [0.735s][info][gc           ] GC(0) Concurrent marking 74M-&gt;74M(128M) 3.688ms
 * [0.735s][info][gc,start     ] GC(0) Pause Final Mark
 * [0.736s][info][gc           ] GC(0) Pause Final Mark 74M-&gt;76M(128M) 0.811ms
 * [0.736s][info][gc,start     ] GC(0) Concurrent evacuation
 * ...
 * [29.628s][info][gc            ] Cancelling concurrent GC: Allocation Failure
 * ... skipping detailed messages as those aren't parsed yet
 * [43.948s][info][gc             ] GC(831) Pause Full (Allocation Failure) 7943M-&gt;6013M(8192M) 14289.335ms
 */
public class DataReaderShenandoah extends AbstractDataReader {


    // Input: [0.693s][info][gc           ] GC(0) Pause Init Mark 1.070ms
    // Group 1: 0.693
    // Group 2: Pause Init Mark
    // Group 3: 1.070
    // Regex: ^\[([0-9]+[.,][0-9]+)[^\-]*\)[ ]([^\-]*)[ ]([0-9]+[.,][0-9]+)
    private static final Pattern PATTERN_WITHOUT_HEAP = Pattern.compile("^\\[([0-9]+[.,][0-9]+)[^\\-]*\\)[ ]([^\\-]*)[ ]([0-9]+[.,][0-9]+)");

    // Input: [13.522s][info][gc            ] GC(708) Concurrent evacuation  4848M->4855M(4998M) 2.872ms
    // Group 1: 13.522
    // Group 2: Concurrent evacuation
    // Group 3: 4848M->4855M(4998M)
    // Group 4: 2.872
    // Regex: ^\[([0-9]+[.,][0-9]+).*\)[ ](.*)[ ]([0-9]+[BKMG]\-\>[0-9]+[BKMG]\([0-9]+[BKMG]\)) ([0-9]+[.,][0-9]+)
    private static final Pattern PATTERN_WITH_HEAP = Pattern.compile(
            "^\\[([0-9]+[.,][0-9]+).*\\)[ ](.*)[ ]([0-9]+[BKMG]->[0-9]+[BKMG]\\([0-9]+[BKMG]\\)) ([0-9]+[.,][0-9]+)");

    // Input: 4848M->4855M(4998M)
    // Group 1: 4848
    // Group 2: 4855
    // Group 3: 4998
    // Regex: ([0-9]+)[BKMG]\-\>([0-9]+)[BKMG]\(([0-9]+)[BKMG]\)
    private static final Pattern PATTERN_HEAP_CHANGES = Pattern.compile("([0-9]+)([BKMG])->([0-9]+)([BKMG])\\(([0-9]+)([BKMG])\\)");

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

    private static final List<String> EXCLUDE_STRINGS = Arrays.asList("Using Shenandoah", "Cancelling concurrent GC",
            "[gc,start", "[gc,ergo", "[gc,stringtable", "[gc,init", "[gc,heap", "[pagesize", "[class", "[os", "[startuptime",
            "[os,thread", "[gc,heap,exit", "Cancelling concurrent GC: Allocation Failure", "Phase ",
            "[gc,stats", "[biasedlocking", "[logging", "[verification", "[modules,startuptime", "[safepoint", "[stacktrace",
            "[exceptions", "thrown", "at bci", "for thread", "[module,load", "[module,startuptime");

    protected DataReaderShenandoah(GCResource gcResource, InputStream in) throws UnsupportedEncodingException {
        super(gcResource, in);
    }

    @Override
    public GCModel read() throws IOException {
        getLogger().info("Reading Shenandoah format...");

        GCModel model = new GCModel();
        model.setFormat(GCModel.Format.RED_HAT_SHENANDOAH_GC);

        Stream<String> lines = new BufferedReader(in).lines();
        lines.filter(this::lineNotInExcludedStrings)
                .map(this::parseShenandoahEvent)
                .filter(Objects::nonNull)
                .forEach(model::add);
        return model;
    }

    private AbstractGCEvent<?> parseShenandoahEvent(String line) {
        AbstractGCEvent<?> event = null;

        Matcher noHeapMatcher = PATTERN_WITHOUT_HEAP.matcher(line);
        Matcher withHeapMatcher = PATTERN_WITH_HEAP.matcher(line);
        if (noHeapMatcher.find()) {
            event = new GCEvent();
            AbstractGCEvent.Type type = AbstractGCEvent.Type.lookup(noHeapMatcher.group(NO_HEAP_EVENT_NAME));
            event.setType(type);
            setPauseAndTimestamp(event, noHeapMatcher.group(NO_HEAP_DURATION), noHeapMatcher.group(NO_HEAP_TIMESTAMP));
        } else if (withHeapMatcher.find()) {
            event = line.contains("Concurrent") ? new ConcurrentGCEvent() : new GCEvent();
            AbstractGCEvent.Type type = AbstractGCEvent.Type.lookup(withHeapMatcher.group(WITH_HEAP_EVENT_NAME));
            event.setType(type);
            setPauseAndTimestamp(event, withHeapMatcher.group(WITH_HEAP_DURATION), withHeapMatcher.group(WITH_HEAP_TIMESTAMP));
            addHeapDetailsToEvent(event, withHeapMatcher.group(WITH_HEAP_MEMORY));
        } else {
            getLogger().warning("Found line that has no match:" + line);
        }

        return event;
    }

    /**
     * @param event                 GC event to which pause and timestamp information is added
     * @param pauseAsString         Pause information from regex group as string
     * @param timestampAsString     Timestamp information from regex group as string
     */
    private void setPauseAndTimestamp(AbstractGCEvent<?> event, String pauseAsString, String timestampAsString) {
        double pause = Double.parseDouble(pauseAsString.replace(",", "."));
        double timestamp = Double.parseDouble(timestampAsString.replace(",", "."));
        event.setPause(pause / 1000);
        event.setTimestamp(timestamp);
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
            getLogger().warning("Failed to find heap details from line: " + memoryString);
        }
    }

    private boolean lineNotInExcludedStrings(String line) {
        return EXCLUDE_STRINGS.stream().noneMatch(line::contains);
    }

}
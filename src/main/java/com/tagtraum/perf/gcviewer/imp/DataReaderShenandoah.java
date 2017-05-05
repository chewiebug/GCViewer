package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.ShenandoahGCEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Currently only parsing 5 main messages for GCViewer with default decorations.
 * Initial mark, concurrent mark, final mark, concurrent evacuation
 * <p>
 * Example Format
 * [0.730s][info][gc,start     ] GC(0) Pause Init Mark
 * [0.731s][info][gc           ] GC(0) Pause Init Mark 1.021ms
 * [0.731s][info][gc,start     ] GC(0) Concurrent marking
 * [0.735s][info][gc           ] GC(0) Concurrent marking 74M->74M(128M) 3.688ms
 * [0.735s][info][gc,start     ] GC(0) Pause Final Mark
 * [0.736s][info][gc           ] GC(0) Pause Final Mark 74M->76M(128M) 0.811ms
 * [0.736s][info][gc,start     ] GC(0) Concurrent evacuation
 * ...
 * [29.628s][info][gc            ] Cancelling concurrent GC: Allocation Failure
 * ... skipping detailed messages as those aren't parsed yet
 * [43.948s][info][gc             ] GC(831) Pause Full (Allocation Failure) 7943M->6013M(8192M) 14289.335ms
 */
public class DataReaderShenandoah extends AbstractDataReader {


    // Input: [0.693s][info][gc           ] GC(0) Pause Init Mark 1.070ms
    // Group 1: 0.693
    // Group 2: 1.070
    // Regex without breaking: ^\[([0-9]+\.[0-9]+)[^\-]*[ ]([0-9]+\.[0-9]+)
    private static final Pattern PATTERN_WITHOUT_HEAP = Pattern.compile("^\\[([0-9]+\\.[0-9]+)[^\\-]*[ ]([0-9]+\\.[0-9]+)");

    // Input: [13.522s][info][gc            ] GC(708) Concurrent evacuation  4848M->4855M(4998M) 2.872ms
    // Group 1: 13.522
    // Group 2: 4848M->4855M(4998M)
    // Group 3: 2.872
    // Regex without breaking: ^\[([0-9]+\.[0-9]+).*[ ]([0-9]+[BKMG]\-\>[0-9]+[BKMG]\([0-9]+[BKMG]\)) ([0-9]+\.[0-9]+)
    private static final Pattern PATTERN_WITH_HEAP = Pattern.compile("^\\[([0-9]+\\.[0-9]+)" +
            ".*[ ]([0-9]+[BKMG]\\-\\>[0-9]+[BKMG]\\([0-9]+[BKMG]\\)) " +
            "([0-9]+\\.[0-9]+)");

    // Input: 4848M->4855M(4998M)
    // Group 1: 4848
    // Group 2: 4855
    // Group 3: 4998
    // Regex without breaking: ([0-9]+)[BKMG]\-\>([0-9]+)[BKMG]\(([0-9]+)[BKMG]\)
    private static final Pattern PATTERN_HEAP_CHANGES = Pattern.compile("([0-9]+)[BKMG]->([0-9]+)[BKMG]\\(([0-9]+)[BKMG]\\)");

    private static final int NO_HEAP_TIMESTAMP = 1;
    private static final int NO_HEAP_DURATION = 2;
    private static final int WITH_HEAP_TIMESTAMP = 1;
    private static final int WITH_HEAP_MEMORY = 2;
    private static final int WITH_HEAP_DURATION = 3;
    private static final int HEAP_BEFORE = 1;
    private static final int HEAP_AFTER = 2;
    private static final int HEAP_CURRENT_TOTAL = 3;

    private static final List<String> EXCLUDE_STRINGS = Arrays.asList("Using Shenandoah", "Cancelling concurrent GC",
            "[gc,start", "[gc,ergo", "[gc,stringtable", "[gc,init", "[gc,heap", "[pagesize", "[class", "[os", "[startuptime",
            "[os,thread", "[gc,heap,exit", "Cancelling concurrent GC: Allocation Failure", "Phase ",
            "[gc,stats", "[biasedlocking", "[logging", "[verification", "[modules,startuptime", "[safepoint");

    protected DataReaderShenandoah(GCResource gcResource, InputStream in) throws UnsupportedEncodingException {
        super(gcResource, in);
    }

    @Override
    public GCModel read() throws IOException {
        getLogger().info("Reading Shenandoah format...");

        GCModel model = new GCModel();
        model.setFormat(GCModel.Format.RED_HAT_SHENANDOAH_GC);

        Stream<String> lines = new BufferedReader(in).lines();
        lines.filter(this::lineNotInExcludedStrings).forEach(e -> model.add(parseShenandoahEvent(e)));
        return model;
    }

    private AbstractGCEvent<?> parseShenandoahEvent(String line) {
        ShenandoahGCEvent event = new ShenandoahGCEvent();

        Matcher noHeapMatcher = PATTERN_WITHOUT_HEAP.matcher(line);
        Matcher withHeapMatcher = PATTERN_WITH_HEAP.matcher(line);
        if (noHeapMatcher.find()) {
            System.out.println(line);
            if (line.contains("Init Mark")) {
                setEventTypes(event, AbstractGCEvent.Type.SHEN_STW_INIT_MARK);
            } else if (line.contains("Concurrent reset bitmaps")) {
                setEventTypes(event, AbstractGCEvent.Type.SHEN_CONCURRENT_CONC_RESET);
            } else {
                getLogger().warning("Failed to match line with no heap info: " + line);
            }
            event.setPause(Double.parseDouble(noHeapMatcher.group(NO_HEAP_DURATION)));
            event.setTimestamp(Double.parseDouble(noHeapMatcher.group(NO_HEAP_TIMESTAMP)));
        } else if (withHeapMatcher.find()) {
            if (line.contains("Final Mark")) {
                setEventTypes(event, AbstractGCEvent.Type.SHEN_STW_FINAL_MARK);
            } else if (line.contains("Concurrent marking")) {
                setEventTypes(event, AbstractGCEvent.Type.SHEN_CONCURRENT_CONC_MARK);
                event.setConcurrency(true);
            } else if (line.contains("Concurrent evacuation")) {
                setEventTypes(event, AbstractGCEvent.Type.SHEN_CONCURRENT_CONC_EVAC);
                event.setConcurrency(true);
            } else if (line.contains("Pause Full (Allocation Failure)")) {
                setEventTypes(event, AbstractGCEvent.Type.SHEN_STW_ALLOC_FAILURE);
            } else {
                getLogger().warning("Failed to match line with heap info: " + line);
            }
            event.setPause(Double.parseDouble(withHeapMatcher.group(WITH_HEAP_DURATION)));
            event.setTimestamp(Double.parseDouble(withHeapMatcher.group(WITH_HEAP_TIMESTAMP)));
            addHeapDetailsToEvent(event, withHeapMatcher.group(WITH_HEAP_MEMORY));
        } else {
            getLogger().warning("Found line that has no match:" + line);
        }

        return event;
    }

    /**
     * @param event        GC event to which the heap change information is added
     * @param memoryString Memory changes in format 100M->80M(120M) where 100M-before, 80M-after, 120M-max
     */
    private void addHeapDetailsToEvent(ShenandoahGCEvent event, String memoryString) {
        Matcher matcher = PATTERN_HEAP_CHANGES.matcher(memoryString);
        if (matcher.find()) {
            event.setPreUsed(Integer.parseInt(matcher.group(HEAP_BEFORE)));
            event.setPostUsed(Integer.parseInt(matcher.group(HEAP_AFTER)));
            event.setTotal(Integer.parseInt(matcher.group(HEAP_CURRENT_TOTAL)));
            event.setExtendedType(event.getExtendedType());
        } else {
            getLogger().warning("Failed to find heap details from line: " + memoryString);
        }
    }

    private void setEventTypes(ShenandoahGCEvent event, AbstractGCEvent.Type type) {
        event.setType(type);
        event.setExtendedType(AbstractGCEvent.ExtendedType.lookup(type));
    }

    private boolean lineNotInExcludedStrings(String line) {
        return EXCLUDE_STRINGS.stream().noneMatch(line::contains);
    }

}
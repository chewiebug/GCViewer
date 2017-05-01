package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.ShenandoahGCEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataReaderShenandoah extends AbstractDataReader {
    /**
     * Currently only parsing 5 main messages for GCViewer with default decorations.
     * Initial mark, concurrent mark, final mark, concurrent evacuation
     *
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

    // Input: [0.693s][info][gc           ] GC(0) Pause Init Mark 1.070ms
    // Group 1: 0.693
    // Group 2: 1.070
    // Regex without breaking: ^\[([0-9]+\.[0-9]+).*[ ]([0-9]+\.[0-9]+)
    private static final Pattern PATTERN_INIT_MARK = Pattern.compile("^\\[([0-9]+\\.[0-9]+).*[ ]([0-9]+\\.[0-9]+)");

    // Input: [13.522s][info][gc            ] GC(708) Concurrent evacuation  4848M->4855M(4998M) 2.872ms
    // Group 1: 13.522
    // Group 2: 4848M->4855M(4998M)
    // Group 3: 2.872
    // Regex without breaking: ^\[([0-9]+\.[0-9]+).*[ ]([0-9]+[BKMG]\-\>[0-9]+[BKMG]\([0-9]+[BKMG]\)) ([0-9]+\.[0-9]+)
    private static final Pattern PATTERN_GENERIC_LINE = Pattern.compile("^\\[([0-9]+\\.[0-9]+)" +
            ".*[ ]([0-9]+[BKMG]\\-\\>[0-9]+[BKMG]\\([0-9]+[BKMG]\\)) " +
            "([0-9]+\\.[0-9]+)");

    // Input: 4848M->4855M(4998M)
    // Group 1: 4848
    // Group 2: 4855
    // Group 3: 4998
    // Regex without breaking: ([0-9]+)[BKMG]\-\>([0-9]+)[BKMG]\(([0-9]+)[BKMG]\)
    private static final Pattern PATTERN_HEAP_CHANGES = Pattern.compile("([0-9]+)[BKMG]->([0-9]+)[BKMG]\\(([0-9]+)[BKMG]\\)");

    private static final int INIT_MARK_TIMESTAMP = 1;
    private static final int INIT_MARK_DURATION = 2;
    private static final int GENERIC_MARK_TIMESTAMP = 1;
    private static final int GENERIC_MARK_MEMORY = 2;
    private static final int GENERIC_MARK_DURATION = 3;
    private static final int HEAP_BEFORE = 1;
    private static final int HEAP_AFTER = 2;
    private static final int HEAP_CURRENT_TOTAL = 3;

    private static final List<String> EXCLUDE_STRINGS = new LinkedList<>();

    static {
        EXCLUDE_STRINGS.add("Using Shenandoah");
        EXCLUDE_STRINGS.add("Cancelling concurrent GC: Stopping VM");
        EXCLUDE_STRINGS.add("[gc,start");
        EXCLUDE_STRINGS.add("[gc,ergo");
        EXCLUDE_STRINGS.add("[gc,stringtable");
        EXCLUDE_STRINGS.add("[gc,init");
        EXCLUDE_STRINGS.add("[gc,heap");
        EXCLUDE_STRINGS.add("[gc,stats");
        EXCLUDE_STRINGS.add("[pagesize");
        EXCLUDE_STRINGS.add("[class");
        EXCLUDE_STRINGS.add("[os");
        EXCLUDE_STRINGS.add("[startuptime");
        EXCLUDE_STRINGS.add("[os,thread");
        EXCLUDE_STRINGS.add("[gc,heap,exit");
        EXCLUDE_STRINGS.add("Concurrent reset bitmaps");
        EXCLUDE_STRINGS.add("Cancelling concurrent GC: Allocation Failure");
        EXCLUDE_STRINGS.add("Phase "); // Ignore Allocation failure standalone messages, only parse the total
    }

    protected DataReaderShenandoah(GCResource gcResource, InputStream in) throws UnsupportedEncodingException {
        super(gcResource, in);
    }

    @Override
    public GCModel read() throws IOException {
        if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reading Shenandoah format...");

        GCModel model = new GCModel();
        model.setFormat(GCModel.Format.RED_HAT_SHENANDOAH_GC);

        try (LineNumberReader in = this.in) {
            String line;
            while ((line = in.readLine()) != null) {
                if ("".equals(line) || lineInExcludedStrings(line)) continue;
                model.add(parseShenandoahEvent(line));
            }
        }

        return model;
    }

    private AbstractGCEvent<?> parseShenandoahEvent(String line) {
        ShenandoahGCEvent event = new ShenandoahGCEvent();

        if (line.contains("Init Mark")) {
            Matcher matcher = PATTERN_INIT_MARK.matcher(line);
            if (matcher.find()) {
                event.setTimestamp(Double.parseDouble(matcher.group(INIT_MARK_TIMESTAMP)));
                event.setPause(Double.parseDouble(matcher.group(INIT_MARK_DURATION)));
                setEventTypes(event, AbstractGCEvent.Type.SHEN_CONCURRENT_INIT_MARK);
            } else {
                logMessage("Failed to match init mark line: " + line, Level.WARNING);
            }
        } else {
            Matcher matcher = PATTERN_GENERIC_LINE.matcher(line);
            if (matcher.find()) {
                if (line.contains("Final Mark")) {
                    setEventTypes(event, AbstractGCEvent.Type.SHEN_CONCURRENT_FINAL_MARK);
                } else if (line.contains("Concurrent marking")) {
                    setEventTypes(event, AbstractGCEvent.Type.SHEN_CONCURRENT_CONC_MARK);
                    event.setConcurrency(true);
                } else if (line.contains("Concurrent evacuation")) {
                    setEventTypes(event, AbstractGCEvent.Type.SHEN_CONCURRENT_CONC_EVAC);
                    event.setConcurrency(true);
                } else if (line.contains("Pause Full (Allocation Failure)")) {
                    setEventTypes(event, AbstractGCEvent.Type.SHEN_CONCURRENT_ALLOC_FAILURE);
                }
                event.setPause(Double.parseDouble(matcher.group(GENERIC_MARK_DURATION)));
                event.setTimestamp(Double.parseDouble(matcher.group(GENERIC_MARK_TIMESTAMP)));
                addHeapDetailsToEvent(event, matcher.group(GENERIC_MARK_MEMORY));
            } else {
                logMessage("Failed to match generic GC line: " + line, Level.WARNING);
            }
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
            logMessage("Failed to find heap details from line: " + memoryString, Level.WARNING);
        }
    }


    private void setEventTypes(ShenandoahGCEvent event, AbstractGCEvent.Type type) {
        event.setType(type);
        event.setExtendedType(AbstractGCEvent.ExtendedType.lookup(type));
    }

    private boolean lineInExcludedStrings(String line) {
        return EXCLUDE_STRINGS.stream().anyMatch(line::contains);
    }

    private void logMessage(String message, Level level) {
        if (getLogger().isLoggable(level)) {
            getLogger().info(message);
        }
    }
}
package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Generation;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEventUJL;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Test;

/**
 * Tests unified jvm logging parser for cms gc events.
 */
public class TestDataReaderUJLG1 {
    private GCModel getGCModelFromLogFile(String fileName) throws IOException {
        return UnittestHelper.getGCModelFromLogFile(fileName, FOLDER.OPENJDK_UJL, DataReaderUnifiedJvmLogging.class);
    }

    @Test
    public void parseGcDefaults() throws Exception {
        GCModel model = getGCModelFromLogFile("sample-ujl-g1-gc-defaults.txt");
        assertThat("size", model.size(), is(15));
        assertThat("amount of gc event types", model.getGcEventPauses().size(), is(6));
        assertThat("amount of gc events", model.getGCPause().getN(), is(12));
        assertThat("amount of full gc event types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of full gc events", model.getFullGCPause().getN(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(1));

        UnittestHelper.testMemoryPauseEvent(model.get(0),
                "young",
                Type.UJL_PAUSE_YOUNG,
                0.006529,
                1024 * 14, 1024 * 12, 1024 * 128,
                Generation.YOUNG,
                false);

        AbstractGCEvent<?> mixedEvent = model.get(14);
        UnittestHelper.testMemoryPauseEvent(mixedEvent,
                "mixed",
                Type.UJL_G1_PAUSE_MIXED,
                0.002790,
                1024 * 29, 1024 * 28, 1024 * 123,
                Generation.TENURED,
                false);

        AbstractGCEvent<?> initialMarkEvent = model.get(1);
        UnittestHelper.testMemoryPauseEvent(initialMarkEvent,
                "initialMarkEvent",
                Type.UJL_PAUSE_INITIAL_MARK,
                0.006187,
                1024 * 80, 1024 * 80, 1024 * 128,
                Generation.TENURED,
                false);
        assertThat("isInitialMark", initialMarkEvent.isInitialMark(), is(true));

        AbstractGCEvent<?> remarkEvent = model.get(6);
        UnittestHelper.testMemoryPauseEvent(remarkEvent,
                "RemarkEvent",
                Type.UJL_PAUSE_REMARK,
                0.000837,
                1024 * 121, 1024 * 121, 1024 * 128,
                Generation.TENURED,
                false);
        assertThat("isRemark", remarkEvent.isRemark(), is(true));

        AbstractGCEvent<?> fullGcEvent = model.get(11);
        UnittestHelper.testMemoryPauseEvent(fullGcEvent,
                "full",
                Type.UJL_PAUSE_FULL,
                0.014918,
                1024 * 128, 1024 * 8, 1024 * 28,
                Generation.ALL,
                true);

        AbstractGCEvent<?> cleanupEvent = model.get(12);
        UnittestHelper.testMemoryPauseEvent(cleanupEvent,
                "cleanup",
                Type.UJL_G1_PAUSE_CLEANUP,
                0.000001,
                1024 * 9, 1024 * 9, 1024 * 28,
                Generation.TENURED,
                false);

        AbstractGCEvent<?> concurrentCycleBegin = model.get(2);
        assertThat("event is start of concurrent collection",
                concurrentCycleBegin.isConcurrentCollectionStart(),
                is(true));

        AbstractGCEvent<?> concurrentCycleEnd = model.get(13);
        assertThat("event is end of concurrent collection", concurrentCycleEnd.isConcurrentCollectionEnd(), is(true));

    }

    @Test
    public void parseGcAllSafepointOsCpu() throws Exception {
        GCModel model = getGCModelFromLogFile("sample-ujl-g1-gc-all,safepoint,os+cpu.txt");
        assertThat("size", model.size(), is(15));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(4));
        assertThat("amount of STW GC pauses", model.getGCPause().getN(), is(13));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(0));
        assertThat("amount of STW Full GC pauses", model.getFullGCPause().getN(), is(0));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(1));

        AbstractGCEvent<?> event1 = model.get(0);
        UnittestHelper.testMemoryPauseEvent(event1,
                "young",
                Type.UJL_PAUSE_YOUNG,
                0.007033,
                1024 * 14, 1024 * 12, 1024 * 128,
                Generation.YOUNG,
                false);
        assertThat("young heap before", event1.details().next().getPreUsed(), is(1024 * 14));

        // GC(3) Pause Initial Mark
        AbstractGCEvent<?> event2 = model.get(5);
        UnittestHelper.testMemoryPauseEvent(event2,
                "initial mark",
                Type.UJL_PAUSE_INITIAL_MARK,
                0.005011,
                1024 * 80, 1024 * 80, 1024 * 128,
                Generation.TENURED,
                false);
        assertThat("isInitialMark", event2.isInitialMark(), is(true));

        // GC(3) Pause Remark
        AbstractGCEvent<?> remarkEvent = model.get(10);
        UnittestHelper.testMemoryPauseEvent(remarkEvent,
                "remark",
                Type.UJL_PAUSE_REMARK,
                0.000939,
                1024 * 113, 1024 * 113, 1024 * 128,
                Generation.TENURED,
                false);
        assertThat("isRemark", remarkEvent.isRemark(), is(true));

        AbstractGCEvent<?> cleanupEvent = model.get(13);
        UnittestHelper.testMemoryPauseEvent(cleanupEvent,
                "cleanup",
                Type.UJL_G1_PAUSE_CLEANUP,
                0.000367,
                1024 * 114, 1024 * 70, 1024 * 128,
                Generation.TENURED,
                false);

        AbstractGCEvent<?> concurrentCycleBeginEvent = model.get(6);
        assertThat("event is start of concurrent collection",
                concurrentCycleBeginEvent.isConcurrentCollectionStart(),
                is(true));

        AbstractGCEvent<?> concurrentCycleEndEvent = model.get(14);
        assertThat("event is end of concurrent collection",
                concurrentCycleEndEvent.isConcurrentCollectionEnd(),
                is(true));
    }

    @Test
    public void parseGcAllSafepointOsCpuWithToSpaceExhausted() throws Exception {
        GCModel model = getGCModelFromLogFile("sample-ujl-g1-gc-all,safepoint,os+cpu-to-space-exhausted.txt");
        assertThat("size", model.size(), is(1));
        AbstractGCEvent<?> youngEvent = model.get(0);
        UnittestHelper.testMemoryPauseEvent(youngEvent,
                "young",
                Type.UJL_PAUSE_YOUNG,
                0.002717,
                1024 * 116, 1024 * 119, 1024 * 128,
                Generation.YOUNG,
                false);
        assertThat("typeAsString", youngEvent.getTypeAsString(), equalTo("Pause Young (G1 Evacuation Pause); To-space exhausted; Eden regions:; Survivor regions:; Old regions:; Humongous regions:; Metaspace:"));
        Iterator<AbstractGCEvent<?>> iterator = (Iterator<AbstractGCEvent<?>>) youngEvent.details();
        // skip "To-space exhausted"
        iterator.next();
        testHeapSizing(iterator.next(), "eden", 0, 0, 0);
        testHeapSizing(iterator.next(), "survivor", 0, 0, 0);
        testHeapSizing(iterator.next(), "old", 0, 0, 0);
        testHeapSizing(iterator.next(), "humongous", 0, 0, 0);
        testHeapSizing(iterator.next(), "metaspace", 3648, 3648, 1056768);

        GCEventUJL gcEventUJL = (GCEventUJL)youngEvent;
        testHeapSizing(gcEventUJL.getYoung(), "young", 0, 0, 0);
        testHeapSizing(gcEventUJL.getTenured(), "tenured", 0, 0, 0);
        testHeapSizing(gcEventUJL.getPerm(), "metaspace", 3648, 3648, 1056768);
    }

    @Test
    public void testParseGcWithPhases() throws Exception  {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[0.200s][info][gc,start     ] GC(0) Pause Young (G1 Evacuation Pause)\n" +
                "[0.200s][info][gc,task      ] GC(0) Using 4 workers of 4 for evacuation\n" +
                "[0.207s][info][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms\n" +
                "[0.207s][info][gc,phases    ] GC(0)   Evacuate Collection Set: 6.4ms\n" +
                "[0.207s][info][gc,phases    ] GC(0)   Post Evacuate Collection Set: 0.3ms\n" +
                "[0.207s][info][gc,phases    ] GC(0)   Other: 0.3ms\n" +
                "[0.207s][info][gc,heap      ] GC(0) Eden regions: 14->0(8)\n" +
                "[0.207s][info][gc,heap      ] GC(0) Survivor regions: 0->2(2)\n" +
                "[0.207s][info][gc,heap      ] GC(0) Old regions: 0->11\n" +
                "[0.207s][info][gc,heap      ] GC(0) Humongous regions: 0->0\n" +
                "[0.207s][info][gc,metaspace ] GC(0) Metaspace: 3644K->3644K(1056768K)\n" +
                "[0.207s][info][gc           ] GC(0) Pause Young (G1 Evacuation Pause) 14M->12M(128M) 7.033ms\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_PAUSE_YOUNG));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.007033, 0.0000001));

        assertThat("phases", model.getGcEventPhases().size(), is(4));
        assertThat("phase 1", model.getGcEventPhases().get(Type.UJL_G1_PHASE_PRE_EVACUATE_COLLECTION_SET.getName() + ":").getSum(), closeTo(0.0, 0.00001));
        assertThat("phase 2", model.getGcEventPhases().get(Type.UJL_G1_PHASE_EVACUATE_COLLECTION_SET.getName() + ":").getSum(), closeTo(0.0064, 0.00001));
    }

    private void testHeapSizing(AbstractGCEvent<?> event, String testName, int expectedBefore, int expectedAfter, int expectedTotal) {
        assertThat(testName + " before", event.getPreUsed(), is(expectedBefore));
        assertThat(testName + " after", event.getPostUsed(), is(expectedAfter));
        assertThat(testName + " total", event.getTotal(), is(expectedTotal));
    }
}

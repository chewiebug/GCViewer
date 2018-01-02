package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCModel;
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
        assertThat("amount gc events", model.getGcEventPauses().size(), is(6));
        assertThat("amount full gc events", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(1));

        testMemoryPauseEvent(model.get(0),
                "young",
                Type.UJL_PAUSE_YOUNG,
                0.006529,
                1024 * 14, 1024 * 12,1024 * 128);

        AbstractGCEvent<?> mixedEvent = model.get(14);
        testMemoryPauseEvent(mixedEvent,
                "mixed",
                Type.UJL_G1_PAUSE_MIXED,
                0.002790,
                1024 * 29, 1024 * 28, 1024 * 123);

        AbstractGCEvent<?> initialMarkEvent = model.get(1);
        testMemoryPauseEvent(initialMarkEvent,
                "initialMarkEvent",
                Type.UJL_PAUSE_INITIAL_MARK,
                0.006187,
                1024 * 80, 1024 * 80,1024 * 128);
        assertThat("isInitialMark", initialMarkEvent.isInitialMark(), is(true));

        AbstractGCEvent<?> remarkEvent = model.get(6);
        testMemoryPauseEvent(remarkEvent,
                "RemarkEvent",
                Type.UJL_PAUSE_REMARK,
                0.000837,
                1024 * 121, 1024 * 121,1024 * 128);
        assertThat("isRemark", remarkEvent.isRemark(), is(true));

        AbstractGCEvent<?> fullGcEvent = model.get(11);
        testMemoryPauseEvent(fullGcEvent,
                "full",
                Type.UJL_PAUSE_FULL,
                0.014918,
                1024 * 128, 1024 * 8, 1024 * 28);
        assertThat("isFull", fullGcEvent.isFull(), is(true));

        AbstractGCEvent<?> cleanupEvent = model.get(12);
        testMemoryPauseEvent(cleanupEvent,
                "cleanup",
                Type.UJL_G1_PAUSE_CLEANUP,
                0.000001,
                1024 * 9, 1024 * 9, 1024 * 28);

        AbstractGCEvent<?> concurrentCycleBegin = model.get(2);
        assertThat("event is start of concurrent collection", concurrentCycleBegin.isConcurrentCollectionStart(), is(true));

        AbstractGCEvent<?> concurrentCycleEnd = model.get(13);
        assertThat("event is end of concurrent collection", concurrentCycleEnd.isConcurrentCollectionEnd(), is(true));

    }

    @Test
    public void parseGcAllSafepointOsCpu() throws Exception {
        GCModel model = getGCModelFromLogFile("sample-ujl-g1-gc-all,safepoint,os+cpu.txt");
        assertThat("size", model.size(), is(15));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(4));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(0));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(1));

        AbstractGCEvent<?> event1 = model.get(0);
        testMemoryPauseEvent(event1,
                "young",
                Type.UJL_PAUSE_YOUNG,
                0.007033,
                1024 * 14, 1024 * 12, 1024 * 128);

        // GC(3) Pause Initial Mark
        AbstractGCEvent<?> event2 = model.get(5);
        testMemoryPauseEvent(event2,
                "initial mark",
                Type.UJL_PAUSE_INITIAL_MARK,
                0.005011,
                1024 * 80, 1024 * 80, 1024 * 128);
        assertThat("isInitialMark", event2.isInitialMark(), is(true));

        // GC(3) Pause Remark
        AbstractGCEvent<?> remarkEvent = model.get(10);
        testMemoryPauseEvent(remarkEvent,
                "remark",
                Type.UJL_PAUSE_REMARK,
                0.000939,
                1024 * 113, 1024 * 113, 1024 * 128);
        assertThat("isRemark", remarkEvent.isRemark(), is(true));

        AbstractGCEvent<?> cleanupEvent = model.get(13);
        testMemoryPauseEvent(cleanupEvent,
                "cleanup",
                Type.UJL_G1_PAUSE_CLEANUP,
                0.000367,
                1024 * 114, 1024 * 70, 1024 * 128);

        AbstractGCEvent<?> concurrentCycleBeginEvent = model.get(6);
        assertThat("event is start of concurrent collection", concurrentCycleBeginEvent.isConcurrentCollectionStart(), is(true));

        AbstractGCEvent<?> concurrentCycleEndEvent = model.get(14);
        assertThat("event is end of concurrent collection", concurrentCycleEndEvent.isConcurrentCollectionEnd(), is(true));
    }

    private void testMemoryPauseEvent(AbstractGCEvent<?> event,
                                      String eventName,
                                      Type type,
                                      double pause,
                                      int heapBefore,
                                      int heapAfter,
                                      int heapTotal) {

        assertThat(eventName + " type", event.getTypeAsString(), startsWith(type.getName()));
        assertThat(eventName + " pause", event.getPause(), closeTo(pause, 0.00001));
        assertThat(eventName + " heap before", event.getPreUsed(), is(heapBefore));
        assertThat(eventName + " heap after", event.getPostUsed(), is(heapAfter));
        assertThat(eventName + " total heap", event.getTotal(), is(heapTotal));
    }

}

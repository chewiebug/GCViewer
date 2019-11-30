package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Generation;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Test;

/**
 * Tests unified jvm logging parser for cms gc events.
 */
public class TestDataReaderUJLCMS {
    private GCModel getGCModelFromLogFile(String fileName) throws IOException {
        return UnittestHelper.getGCModelFromLogFile(fileName, FOLDER.OPENJDK_UJL, DataReaderUnifiedJvmLogging.class);
    }

    @Test
    public void parseGcDefaults() throws Exception {
        GCModel model = getGCModelFromLogFile("sample-ujl-cms-gc-defaults.txt");
        assertThat("size", model.size(), is(16));
        assertThat("amount of gc event types", model.getGcEventPauses().size(), is(3));
        assertThat("amount of gc events", model.getGCPause().getN(), is(5));
        assertThat("amount of full gc event types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of full gc events", model.getFullGCPause().getN(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(5));

        UnittestHelper.testMemoryPauseEvent(model.get(0),
                "young",
                Type.UJL_PAUSE_YOUNG,
                0.008822,
                1024 * 41, 1024 * 38,1024 * 150,
                Generation.YOUNG,
                false);

        AbstractGCEvent<?> event2 = model.get(1);
        UnittestHelper.testMemoryPauseEvent(event2,
                "initialMarkEvent",
                Type.UJL_PAUSE_INITIAL_MARK,
                0.000136,
                1024 * 61, 1024 * 61,1024 * 150,
                Generation.TENURED,
                false);
        assertThat("isInitialMark", event2.isInitialMark(), is(true));

        AbstractGCEvent<?> event3 = model.get(10);
        UnittestHelper.testMemoryPauseEvent(event3,
                "RemarkEvent",
                Type.UJL_PAUSE_REMARK,
                0.000859,
                1024 * 110, 1024 * 110,1024 * 150,
                Generation.TENURED,
                false);
        assertThat("isRemark", event3.isRemark(), is(true));

        AbstractGCEvent<?> event4 = model.get(15);
        UnittestHelper.testMemoryPauseEvent(event4,
                "full",
                Type.UJL_PAUSE_FULL,
                0.009775,
                1024 * 125, 1024 * 31, 1024 * 150,
                Generation.ALL,
                true);

        AbstractGCEvent<?> concurrentMarkBeginEvent = model.get(2);
        assertThat("event is not start of concurrent collection", concurrentMarkBeginEvent.isConcurrentCollectionStart(), is(false));

        AbstractGCEvent<?> concurrentMarkWithPauseEvent = model.get(3);
        assertThat("event is start of concurrent collection", concurrentMarkWithPauseEvent.isConcurrentCollectionStart(), is(true));

        AbstractGCEvent<?> concurrentResetBeginEvent = model.get(13);
        assertThat("event is not end of concurrent collection", concurrentResetBeginEvent.isConcurrentCollectionEnd(), is(false));

        AbstractGCEvent<?> concurrentResetEvent = model.get(14);
        assertThat("event is end of concurrent collection", concurrentResetEvent.isConcurrentCollectionEnd(), is(true));

    }

    @Test
    public void parseGcAllSafepointOsCpu() throws Exception {
        GCModel model = getGCModelFromLogFile("sample-ujl-cms-gc-all,safepoint,os+cpu.txt");
        assertThat("size", model.size(), is(26));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(3));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(4));

        AbstractGCEvent<?> event1 = model.get(0);
        UnittestHelper.testMemoryPauseEvent(event1,
                "young",
                Type.UJL_PAUSE_YOUNG,
                0.009618,
                1024 * 41, 1024 * 38, 1024 * 150,
                Generation.YOUNG,
                false);

        // GC(3) Pause Initial Mark
        AbstractGCEvent<?> event2 = model.get(3);
        UnittestHelper.testMemoryPauseEvent(event2,
                "initial mark",
                Type.UJL_PAUSE_INITIAL_MARK,
                0.000165,
                91 * 1024, 91 * 1024, 150 * 1024,
                Generation.TENURED,
                false);
        assertThat("isInitialMark", event2.isInitialMark(), is(true));

        // GC(3) Pause Remark
        AbstractGCEvent<?> remarkEvent = model.get(10);
        UnittestHelper.testMemoryPauseEvent(remarkEvent,
                "remark",
                Type.UJL_PAUSE_REMARK,
                0.000908,
                130 * 1024, 130 * 1024, 150 * 1024,
                Generation.TENURED,
                false);
        assertThat("isRemark", remarkEvent.isRemark(), is(true));

        // GC(5) Pause Full
        AbstractGCEvent<?> fullGcEvent = model.get(13);
        UnittestHelper.testMemoryPauseEvent(fullGcEvent,
                "full gc ",
                Type.UJL_PAUSE_FULL,
                0.009509,
                119 * 1024, 33 * 1024, 150 * 1024,
                Generation.ALL,
                true);

        AbstractGCEvent<?> concurrentMarkBeginEvent = model.get(4);
        assertThat("event is not start of concurrent collection", concurrentMarkBeginEvent.isConcurrentCollectionStart(), is(false));

        AbstractGCEvent<?> concurrentMarkWithPauseEvent = model.get(5);
        assertThat("event is start of concurrent collection", concurrentMarkWithPauseEvent.isConcurrentCollectionStart(), is(true));
    }

    @Test
    public void testParseFullGcWithPhases() throws Exception  {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[0.315s][info][gc,start     ] GC(5) Pause Full (Allocation Failure)\n" +
                "[0.315s][info][gc           ] GC(3) Concurrent Sweep 0.817ms\n" +
                "[0.315s][info][gc,cpu       ] GC(3) User=0.00s Sys=0.00s Real=0.00s\n" +
                "[0.315s][info][gc,phases,start] GC(5) Phase 1: Mark live objects\n" +
                "[0.316s][info][gc,phases      ] GC(5) Phase 1: Mark live objects 1.154ms\n" +
                "[0.316s][info][gc,phases,start] GC(5) Phase 2: Compute new object addresses\n" +
                "[0.317s][info][gc,phases      ] GC(5) Phase 2: Compute new object addresses 0.391ms\n" +
                "[0.317s][info][gc,phases,start] GC(5) Phase 3: Adjust pointers\n" +
                "[0.317s][info][gc,phases      ] GC(5) Phase 3: Adjust pointers 0.575ms\n" +
                "[0.317s][info][gc,phases,start] GC(5) Phase 4: Move objects\n" +
                "[0.324s][info][gc,phases      ] GC(5) Phase 4: Move objects 6.493ms\n" +
                "[0.324s][info][gc             ] GC(5) Pause Full (Allocation Failure) 119M->33M(150M) 9.509ms\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(2));
        assertThat("event type", model.get(1).getExtendedType().getType(), is(Type.UJL_PAUSE_FULL));
        assertThat("event pause", model.get(1).getPause(), closeTo(0.009509, 0.0000001));

        assertThat("phases", model.getGcEventPhases().size(), is(4));
        assertThat("phase 1", model.getGcEventPhases().get(Type.UJL_SERIAL_PHASE_MARK_LIFE_OBJECTS.getName()).getSum(), closeTo(0.001154, 0.0000001));
    }

}

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
 * Tests unified jvm logging parser for parallel gc events.
 */
public class TestDataReaderUJLParallel {
    private GCModel getGCModelFromLogFile(String fileName) throws IOException {
        return UnittestHelper.getGCModelFromLogFile(fileName, FOLDER.OPENJDK_UJL, DataReaderUnifiedJvmLogging.class);
    }

    @Test
    public void parseGcDefaults() throws Exception {
        GCModel model = getGCModelFromLogFile("sample-ujl-parallel-gc-defaults.txt");
        assertThat("size", model.size(), is(11));
        assertThat("amount of gc event types", model.getGcEventPauses().size(), is(1));
        assertThat("amount of gc events", model.getGCPause().getN(), is(5));
        assertThat("amount of full gc event types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of full gc events", model.getFullGCPause().getN(), is(6));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));

        AbstractGCEvent<?> event1 = model.get(0);
        UnittestHelper.testMemoryPauseEvent(event1,
                "pause young",
                Type.UJL_PAUSE_YOUNG,
                0.006868,
                1024 * 32, 1024 * 29, 1024 * 123,
                Generation.YOUNG,
                false);

        AbstractGCEvent<?> event2 = model.get(2);
        UnittestHelper.testMemoryPauseEvent(event2,
                "pause full",
                Type.UJL_PAUSE_FULL,
                0.013765,
                1024 * 62, 1024 * 61, 1024 * 123,
                Generation.ALL,
                true);
    }

    @Test
    public void parseGcAllSafepointOsCpu() throws Exception {
        GCModel model = getGCModelFromLogFile("sample-ujl-parallel-gc-all,safepoint,os+cpu.txt");
        assertThat("size", model.size(), is(8));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(1));
        assertThat("amount of gc events", model.getGCPause().getN(), is(4));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of full gc events", model.getFullGCPause().getN(), is(4));
        assertThat("amount of phase types", model.getGcEventPhases().size(), is(5));
        assertThat("amount of events for phase 1 types", model.getGcEventPhases().get(Type.UJL_PARALLEL_PHASE_MARKING.getName()).getN(), is(4));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));

        AbstractGCEvent<?> event1 = model.get(0);
        UnittestHelper.testMemoryPauseEvent(event1,
                "pause young",
            Type.UJL_PAUSE_YOUNG,
            0.008112,
            1024 * 32, 1024 * 29, 1024 * 123,
            Generation.YOUNG,
            false);

        // GC(6) Pause Full (Ergonomics)
        AbstractGCEvent<?> event2 = model.get(6);
        UnittestHelper.testMemoryPauseEvent(event2,
                "pause full",
                Type.UJL_PAUSE_FULL,
                0.008792,
                1024 * 95, 1024 * 31, 1024 * 123,
                Generation.ALL,
                true);

        // GC(7) Pause Young (Allocation Failure)
        AbstractGCEvent<?> event3 = model.get(7);
        UnittestHelper.testMemoryPauseEvent(event3,
                "pause young 2",
                Type.UJL_PAUSE_YOUNG,
                0.005794,
                1024 * 63, 1024 * 63, 1024 * 123,
                Generation.YOUNG,
                false);
    }

    @Test
    public void testParseFullGcWithPhases() throws Exception  {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[1.499s][info][gc,start     ] GC(4) Pause Full (Ergonomics)\n" +
                "[1.499s][info][gc,phases,start] GC(4) Marking Phase\n" +
                "[1.504s][info][gc,phases      ] GC(4) Marking Phase 4.370ms\n" +
                "[1.504s][info][gc,phases,start] GC(4) Summary Phase\n" +
                "[1.504s][info][gc,phases      ] GC(4) Summary Phase 0.039ms\n" +
                "[1.504s][info][gc,phases,start] GC(4) Adjust Roots\n" +
                "[1.507s][info][gc,phases      ] GC(4) Adjust Roots 2.971ms\n" +
                "[1.507s][info][gc,phases,start] GC(4) Compaction Phase\n" +
                "[1.520s][info][gc,phases      ] GC(4) Compaction Phase 13.004ms\n" +
                "[1.520s][info][gc,phases,start] GC(4) Post Compact\n" +
                "[1.522s][info][gc,phases      ] GC(4) Post Compact 2.222ms\n" +
                "[1.522s][info][gc,heap        ] GC(4) PSYoungGen: 5105K->0K(38400K)\n" +
                "[1.522s][info][gc,heap        ] GC(4) ParOldGen: 79662K->45521K(87552K)\n" +
                "[1.522s][info][gc,metaspace   ] GC(4) Metaspace: 15031K->15031K(1062912K)\n" +
                "[1.522s][info][gc             ] GC(4) Pause Full (Ergonomics) 82M->44M(123M) 23.271ms\n" +
                "[1.523s][info][gc,cpu         ] GC(4) User=0.05s Sys=0.00s Real=0.02s\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_PAUSE_FULL));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.023271, 0.0000001));

        assertThat("phases", model.getGcEventPhases().size(), is(5));
        assertThat("phase 1", model.getGcEventPhases().get(Type.UJL_PARALLEL_PHASE_MARKING.getName()).getSum(), closeTo(0.00437, 0.0000001));
    }


}

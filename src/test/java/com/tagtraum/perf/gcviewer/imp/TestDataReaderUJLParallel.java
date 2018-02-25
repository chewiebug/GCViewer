package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Generation;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCModel;
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

}

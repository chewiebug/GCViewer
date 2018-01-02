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
        assertThat("amount gc events", model.getGcEventPauses().size(), is(1));
        assertThat("amount full gc events", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));

        AbstractGCEvent<?> event1 = model.get(0);
        assertThat("event1 type", event1.getTypeAsString(), startsWith(Type.UJL_PAUSE_YOUNG.getName()));
        assertThat("event1 pause", event1.getPause(), closeTo(0.006868, 0.00001));
        assertThat("event1 heap before", event1.getPreUsed(), is(1024 * 32));
        assertThat("event1 heap after", event1.getPostUsed(), is(1024 * 29));
        assertThat("event1 total heap", event1.getTotal(), is(1024 * 123));

        AbstractGCEvent<?> event2 = model.get(2);
        assertThat("event2 type", event2.getTypeAsString(), startsWith(Type.UJL_PAUSE_FULL.getName()));
        assertThat("event2 pause", event2.getPause(), closeTo(0.013765, 0.00001));
        assertThat("event2 heap before", event2.getPreUsed(), is(1024 * 62));
        assertThat("event2 heap after", event2.getPostUsed(), is(1024 * 61));
        assertThat("event2 total heap", event2.getTotal(), is(1024 * 123));
    }

    @Test
    public void parseGcAllSafepointOsCpu() throws Exception {
        GCModel model = getGCModelFromLogFile("sample-ujl-parallel-gc-all,safepoint,os+cpu.txt");
        assertThat("size", model.size(), is(8));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(1));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));

        AbstractGCEvent<?> event1 = model.get(0);
        assertThat("event1 type", event1.getTypeAsString(), startsWith(Type.UJL_PAUSE_YOUNG.getName()));
        assertThat("event1 pause", event1.getPause(), closeTo(0.008112, 0.00001));
        assertThat("event1 heap before", event1.getPreUsed(), is(1024 * 32));
        assertThat("event1 heap after", event1.getPostUsed(), is(1024 * 29));
        assertThat("event1 total heap", event1.getTotal(), is(1024 * 123));

        // GC(6) Pause Full (Ergonomics)
        AbstractGCEvent<?> event2 = model.get(6);
        assertThat("event2 type", event2.getTypeAsString(), startsWith(Type.UJL_PAUSE_FULL.getName()));
        assertThat("event2 pause", event2.getPause(), closeTo(0.008792, 0.00001));
        assertThat("event2 timestamp", event2.getTimestamp(), closeTo(0.321, 0.0001));

        // GC(7) Pause Young (Allocation Failure)
        AbstractGCEvent<?> event3 = model.get(7);
        assertThat("event3 type", event3.getTypeAsString(), startsWith(Type.UJL_PAUSE_YOUNG.getName()));
        assertThat("event3 pause", event3.getPause(), closeTo(0.005794, 0.00001));
        assertThat("event3 timestamp", event3.getTimestamp(), closeTo(0.330, 0.0001));
    }

}

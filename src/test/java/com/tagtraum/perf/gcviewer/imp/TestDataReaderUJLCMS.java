package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Test;

/**
 * Tests unified jvm logging parser for cms gc events.
 */
public class TestDataReaderUJLCMS {
    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_OPENJDK_UJL, fileName);
    }

    @Test
    public void parseGcDefaults() throws Exception {
        GCModel model = getGCModelFromLogFile("sample-ujl-cms-gc-defaults.txt");
        assertThat("size", model.size(), is(16));
        assertThat("amount gc events", model.getGcEventPauses().size(), is(3));
        assertThat("amount full gc events", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(5));

        testMemoryPauseEvent(model.get(0),
                "young",
                Type.UJL_PAUSE_YOUNG,
                0.008822,
                1024 * 41, 1024 * 38,1024 * 150);

        AbstractGCEvent<?> event2 = model.get(1);
        testMemoryPauseEvent(event2,
                "initialMarkEvent",
                Type.UJL_PAUSE_INITIAL_MARK,
                0.000136,
                1024 * 61, 1024 * 61,1024 * 150);
        assertThat("isInitialMark", event2.isInitialMark(), is(true));

        AbstractGCEvent<?> event3 = model.get(10);
        testMemoryPauseEvent(event3,
                "RemarkEvent",
                Type.UJL_PAUSE_REMARK,
                0.000859,
                1024 * 110, 1024 * 110,1024 * 150);
        assertThat("isRemark", event3.isRemark(), is(true));

        AbstractGCEvent<?> event4 = model.get(15);
        testMemoryPauseEvent(event4,
                "full",
                Type.UJL_PAUSE_FULL,
                0.009775,
                1024 * 125, 1024 * 31, 1024 * 150);

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
        testMemoryPauseEvent(event1,
                "young",
                Type.UJL_PAUSE_YOUNG,
                0.009618,
                1024 * 41, 1024 * 38, 1024 * 150);

        // GC(3) Pause Initial Mark
        AbstractGCEvent<?> event2 = model.get(3);
        testMemoryPauseEvent(event2,
                "initial mark",
                Type.UJL_PAUSE_INITIAL_MARK,
                0.000165,
                91 * 1024, 91 * 1024, 150 * 1024);
        assertThat("isInitialMark", event2.isInitialMark(), is(true));

        // GC(3) Pause Remark
        AbstractGCEvent<?> remarkEvent = model.get(10);
        testMemoryPauseEvent(remarkEvent,
                "remark",
                Type.UJL_PAUSE_REMARK,
                0.000908,
                130 * 1024, 130 * 1024, 150 * 1024);
        assertThat("isRemark", remarkEvent.isRemark(), is(true));

        // GC(5) Pause Full
        AbstractGCEvent<?> fullGcEvent = model.get(13);
        testMemoryPauseEvent(fullGcEvent,
                "full gc ",
                Type.UJL_PAUSE_FULL,
                0.009509,
                119 * 1024, 33 * 1024, 150 * 1024);
        assertThat("isFull", fullGcEvent.isFull(), is(true));

        AbstractGCEvent<?> concurrentMarkBeginEvent = model.get(4);
        assertThat("event is not start of concurrent collection", concurrentMarkBeginEvent.isConcurrentCollectionStart(), is(false));

        AbstractGCEvent<?> concurrentMarkWithPauseEvent = model.get(5);
        assertThat("event is start of concurrent collection", concurrentMarkWithPauseEvent.isConcurrentCollectionStart(), is(true));
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

    private GCModel getGCModelFromLogFile(String fileName) throws IOException {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile(fileName);
        gcResource.getLogger().addHandler(handler);

        try (InputStream in = getInputStream(gcResource.getResourceName())) {
            DataReader reader = new DataReaderFactory().getDataReader(gcResource, in);
            assertThat("reader from factory", reader.getClass().getCanonicalName(), is(DataReaderUnifiedJvmLogging.class.getCanonicalName()));

            GCModel model = reader.read();
            assertThat("model format", model.getFormat(), is(GCModel.Format.UNIFIED_JVM_LOGGING));
            assertThat("number of errors", handler.getCount(), is(0));
            return model;
        }
    }
}

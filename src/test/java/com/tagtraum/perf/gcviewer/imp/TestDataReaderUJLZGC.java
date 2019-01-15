package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Test;

/**
 * Test unified java logging ZGC algorithm in OpenJDK 11
 */
public class TestDataReaderUJLZGC {
    private GCModel getGCModelFromLogFile(String fileName) throws IOException {
        return UnittestHelper.getGCModelFromLogFile(fileName, UnittestHelper.FOLDER.OPENJDK_UJL, DataReaderUnifiedJvmLogging.class);
    }

    @Test
    public void testGcAll() throws Exception {
//        GCModel model = getGCModelFromLogFile("sample-ujl-zgc-gc-all.txt");
//        assertThat("size", model.size(), is(22));
//        assertThat("amount of gc event types", model.getGcEventPauses().size(), is(3));
//        assertThat("amount of gc events", model.getGCPause().getN(), is(6));
//        assertThat("amount of full gc event types", model.getFullGcEventPauses().size(), is(2));
//        assertThat("amount of full gc events", model.getFullGCPause().getN(), is(2));
//        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(7));
//
//        UnittestHelper.testMemoryPauseEvent(model.get(0),
//                "young",
//                AbstractGCEvent.Type.UJL_ZGC_PAUSE_MARK_START,
//                0.001279,
//                0, 0, 0,
//                AbstractGCEvent.Generation.TENURED,
//                false);
//        AbstractGCEvent<?> initialMarkEvent = model.get(0);
//        assertThat("isInitialMark", initialMarkEvent.isInitialMark(), is(true));
//
//        AbstractGCEvent<?> finalMarkEvent = model.get(2);
//        assertThat("isRemark", finalMarkEvent.isRemark(), is(true));
//
//        AbstractGCEvent<?> concurrentMarkingEvent = model.get(1);
//        assertThat("event is start of concurrent collection", concurrentMarkingEvent.isConcurrentCollectionStart(), is(true));
//
//        AbstractGCEvent<?> concurrentResetEvent = model.get(4);
//        assertThat("event is end of concurrent collection", concurrentResetEvent.isConcurrentCollectionEnd(), is(true));
    }

    @Test
    public void testGcDefault() throws Exception {
        GCModel model = getGCModelFromLogFile("sample-ujl-zgc-gc-default.txt");
        assertThat("size", model.size(), is(5));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(0));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(0));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(5));

        // Default gc log gives no pause time or total heap size
        AbstractGCEvent<?> metadataGcThresholdEvent = model.get(0);
        UnittestHelper.testMemoryPauseEvent(metadataGcThresholdEvent,
                "Metadata GC Threshold heap",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION_METADATA_GC_THRESHOLD,
                0,
                1024 * 106, 1024 * 88, 0,
                AbstractGCEvent.Generation.ALL,
                true);

        AbstractGCEvent<?> warmupEvent = model.get(1);
        UnittestHelper.testMemoryPauseEvent(warmupEvent,
                "Warmup heap",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION_WARMUP,
                0,
                1024 * 208, 1024 * 164, 0,
                AbstractGCEvent.Generation.ALL,
                true);

        AbstractGCEvent<?> proactiveEvent = model.get(2);
        UnittestHelper.testMemoryPauseEvent(proactiveEvent,
                "Proactive heap",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION_PROACTIVE,
                0,
                1024 * 19804, 1024 * 20212, 0,
                AbstractGCEvent.Generation.ALL,
                true);

        AbstractGCEvent<?> allocationRateEvent = model.get(3);
        UnittestHelper.testMemoryPauseEvent(allocationRateEvent,
                "Allocation Rate heap",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION_ALLOCATION_RATE,
                0,
                1024 * 502, 1024 * 716, 0,
                AbstractGCEvent.Generation.ALL,
                true);

        AbstractGCEvent<?> systemGcEvent = model.get(4);
        UnittestHelper.testMemoryPauseEvent(systemGcEvent,
                "System.gc() heap",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION_SYSTEM_GC,
                0,
                1024 * 10124, 1024 * 5020, 0,
                AbstractGCEvent.Generation.ALL,
                true);
    }
}
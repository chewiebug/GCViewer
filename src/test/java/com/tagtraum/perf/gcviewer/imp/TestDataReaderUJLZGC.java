package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test unified java logging ZGC algorithm in OpenJDK 11
 */
public class TestDataReaderUJLZGC {
    private GCModel gcAllModel;
    private GCModel gcDefaultModel;

    private GCModel getGCModelFromLogFile(String fileName) throws IOException {
        return UnittestHelper.getGCModelFromLogFile(fileName, UnittestHelper.FOLDER.OPENJDK_UJL, DataReaderUnifiedJvmLogging.class);
    }

    @Before
    public void setUp() throws Exception {
        gcAllModel = getGCModelFromLogFile("sample-ujl-zgc-gc-all.txt");
        gcDefaultModel = getGCModelFromLogFile("sample-ujl-zgc-gc-default.txt");
    }

    @After
    public void tearDown() {
        gcAllModel = null;
        gcDefaultModel = null;
    }

    @Test
    public void testGcAll() {
        assertThat("size", gcAllModel.size(), is(11));
        assertThat("amount of gc event types", gcAllModel.getGcEventPauses().size(), is(3));
        assertThat("amount of gc events", gcAllModel.getGCPause().getN(), is(3));
        assertThat("amount of full gc event types", gcAllModel.getFullGcEventPauses().size(), is(0));
        assertThat("amount of full gc events", gcAllModel.getFullGCPause().getN(), is(0));
        assertThat("amount of concurrent pause types", gcAllModel.getConcurrentEventPauses().size(), is(7));
    }

    @Test
    public void testGcAllPauseMarkStart() {
        AbstractGCEvent<?> pauseMarkStartEvent = gcAllModel.get(0);
        UnittestHelper.testMemoryPauseEvent(pauseMarkStartEvent,
                "Pause Mark Start",
                AbstractGCEvent.Type.UJL_ZGC_PAUSE_MARK_START,
                0.001279,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("Pause Mark Start preused heap percentage", pauseMarkStartEvent.getPreUsedPercent(), is(0));
        assertThat("Pause Mark Start postused heap percentage", pauseMarkStartEvent.getPostUsedPercent(), is(0));
    }

    @Test
    public void testGcAllConcurrentMark() {
        AbstractGCEvent<?> concurrentMarkEvent = gcAllModel.get(1);
        UnittestHelper.testMemoryPauseEvent(concurrentMarkEvent,
                "Concurrent Mark",
                AbstractGCEvent.Type.UJL_ZGC_CONCURRENT_MARK,
                0.005216,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("Concurrent Mark preused heap percentage", concurrentMarkEvent.getPreUsedPercent(), is(0));
        assertThat("Concurrent Mark postused heap percentage", concurrentMarkEvent.getPostUsedPercent(), is(0));
    }

    @Test
    public void testGcAllPauseMarkEnd() {
        AbstractGCEvent<?> pauseMarkEndEvent = gcAllModel.get(2);
        UnittestHelper.testMemoryPauseEvent(pauseMarkEndEvent,
                "Pause Mark End",
                AbstractGCEvent.Type.UJL_ZGC_PAUSE_MARK_END,
                0.000695,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("Pause Mark End preused heap percentage", pauseMarkEndEvent.getPreUsedPercent(), is(0));
        assertThat("Pause Mark End postused heap percentage", pauseMarkEndEvent.getPostUsedPercent(), is(0));
    }

    @Test
    public void testGcAllConcurrentNonref() {
        AbstractGCEvent<?> concurrentNonrefEvent = gcAllModel.get(3);
        UnittestHelper.testMemoryPauseEvent(concurrentNonrefEvent,
                "Concurrent Process Non-Strong References",
                AbstractGCEvent.Type.UJL_ZGC_CONCURRENT_NONREF,
                0.000258,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("Concurrent Nonref preused heap percentage", concurrentNonrefEvent.getPreUsedPercent(), is(0));
        assertThat("Concurrent Nonref postused heap percentage", concurrentNonrefEvent.getPostUsedPercent(), is(0));
    }

    @Test
    public void testGcAllConcurrentResetRelocSet() {
        AbstractGCEvent<?> concurrentResetRelocSetEvent = gcAllModel.get(4);
        UnittestHelper.testMemoryPauseEvent(concurrentResetRelocSetEvent,
                "Concurrent Reset Relocation Set",
                AbstractGCEvent.Type.UJL_ZGC_CONCURRENT_RESET_RELOC_SET,
                0.000001,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("Concurrent Reset Relocation preused heap percentage", concurrentResetRelocSetEvent.getPreUsedPercent(), is(0));
        assertThat("Concurrent Reset Relocation postused heap percentage", concurrentResetRelocSetEvent.getPostUsedPercent(), is(0));
    }

    @Test
    public void testGcAllConcurrentDetachedPages() {
        AbstractGCEvent<?> concurrentDetachedPagesEvent = gcAllModel.get(5);
        UnittestHelper.testMemoryPauseEvent(concurrentDetachedPagesEvent,
                "Concurrent Destroy Detached Pages",
                AbstractGCEvent.Type.UJL_ZGC_CONCURRENT_DETATCHED_PAGES,
                0.000001,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("Concurrent Detached Pages preused heap percentage", concurrentDetachedPagesEvent.getPreUsedPercent(), is(0));
        assertThat("Concurrent Detached Pages postused heap percentage", concurrentDetachedPagesEvent.getPostUsedPercent(), is(0));
    }

    @Test
    public void testGcAllConcurrentSelectRelocSet() {
        AbstractGCEvent<?> concurrentSelectRelocSetEvent = gcAllModel.get(6);
        UnittestHelper.testMemoryPauseEvent(concurrentSelectRelocSetEvent,
                "Concurrent Select Relocation Set",
                AbstractGCEvent.Type.UJL_ZGC_CONCURRENT_SELECT_RELOC_SET,
                0.003822,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("Concurrent Select Relocation Set preused heap percentage", concurrentSelectRelocSetEvent.getPreUsedPercent(), is(0));
        assertThat("Concurrent Select Relocation Set postused heap percentage", concurrentSelectRelocSetEvent.getPostUsedPercent(), is(0));
    }

    @Test
    public void testGcAllConcurrentPrepareRelocSet() {
        AbstractGCEvent<?> concurrentPrepareRelocSetEvent = gcAllModel.get(7);
        UnittestHelper.testMemoryPauseEvent(concurrentPrepareRelocSetEvent,
                "Concurrent Prepare Relocation Set",
                AbstractGCEvent.Type.UJL_ZGC_CONCURRENT_PREPARE_RELOC_SET,
                0.000865,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("Concurrent Prepare Relocation Set preused heap percentage", concurrentPrepareRelocSetEvent.getPreUsedPercent(), is(0));
        assertThat("Concurrent Prepare Relocation Set postused heap percentage", concurrentPrepareRelocSetEvent.getPostUsedPercent(), is(0));
    }

    @Test
    public void testGcAllPauseRelocateStart() {
        AbstractGCEvent<?> pauseRelocateStartEvent = gcAllModel.get(8);
        UnittestHelper.testMemoryPauseEvent(pauseRelocateStartEvent,
                "Pause Relocate Start",
                AbstractGCEvent.Type.UJL_ZGC_PAUSE_RELOCATE_START,
                0.000679,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("Pause Relocate Start preused heap percentage", pauseRelocateStartEvent.getPreUsedPercent(), is(0));
        assertThat("Pause Relocate Start postused heap percentage", pauseRelocateStartEvent.getPostUsedPercent(), is(0));
    }

    @Test
    public void testGcAllConcurrentRelocate() {
        AbstractGCEvent<?> concurrentRelocateEvent = gcAllModel.get(9);
        UnittestHelper.testMemoryPauseEvent(concurrentRelocateEvent,
                "Concurrent Relocate",
                AbstractGCEvent.Type.UJL_ZGC_CONCURRENT_RELOCATE,
                0.002846,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("Concurrent Relocate preused heap percentage", concurrentRelocateEvent.getPreUsedPercent(), is(0));
        assertThat("Concurrent Relocate postused heap percentage", concurrentRelocateEvent.getPostUsedPercent(), is(0));
    }

    @Test
    public void testGcAllGarbageCollection() {
        AbstractGCEvent<?> garbageCollectionEvent = gcAllModel.get(10);
        UnittestHelper.testMemoryPauseEvent(garbageCollectionEvent,
                "Garbage Collection",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION,
                0,
                1024 * 10620, 1024 * 8800, 1024 * 194560,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("Garbage Collection preused heap percentage", garbageCollectionEvent.getPreUsedPercent(), is(5));
        assertThat("Garbage Collection postused heap percentage", garbageCollectionEvent.getPostUsedPercent(), is(4));
    }


    @Test
    public void testGcDefault() {
        assertThat("size", gcDefaultModel.size(), is(5));
        assertThat("amount of STW GC pause types", gcDefaultModel.getGcEventPauses().size(), is(0));
        assertThat("amount of STW Full GC pause types", gcDefaultModel.getFullGcEventPauses().size(), is(0));
        assertThat("amount of concurrent pause types", gcDefaultModel.getConcurrentEventPauses().size(), is(0));
    }

    @Test
    public void testGcDefaultMetadataGcThreshold() {
        // Default gc log gives no pause time or total heap size
        AbstractGCEvent<?> metadataGcThresholdEvent = gcDefaultModel.get(0);
        UnittestHelper.testMemoryPauseEvent(metadataGcThresholdEvent,
                "Metadata GC Threshold heap",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION,
                0,
                1024 * 106, 1024 * 88, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("Metadata GC preused heap percentage", metadataGcThresholdEvent.getPreUsedPercent(), is(0));
        assertThat("Metadata GC postused heap percentage", metadataGcThresholdEvent.getPostUsedPercent(), is(0));
    }

    @Test
    public void testGcDefaultWarmup() {
        AbstractGCEvent<?> warmupEvent = gcDefaultModel.get(1);
        UnittestHelper.testMemoryPauseEvent(warmupEvent,
                "Warmup heap",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION,
                0,
                1024 * 208, 1024 * 164, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("Warmup preused heap percentage", warmupEvent.getPreUsedPercent(), is(20));
        assertThat("Warmup GC postused heap percentage", warmupEvent.getPostUsedPercent(), is(16));
    }

    @Test
    public void testGcDefaultProactive() {
        AbstractGCEvent<?> proactiveEvent = gcDefaultModel.get(2);
        UnittestHelper.testMemoryPauseEvent(proactiveEvent,
                "Proactive heap",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION,
                0,
                1024 * 19804, 1024 * 20212, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("Proactive preused heap percentage", proactiveEvent.getPreUsedPercent(), is(10));
        assertThat("Proactive postused heap percentage", proactiveEvent.getPostUsedPercent(), is(10));
    }

    @Test
    public void testGcDefaultAllocationRate() {
        AbstractGCEvent<?> allocationRateEvent = gcDefaultModel.get(3);
        UnittestHelper.testMemoryPauseEvent(allocationRateEvent,
                "Allocation Rate heap",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION,
                0,
                1024 * 502, 1024 * 716, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("Allocation preused heap percentage", allocationRateEvent.getPreUsedPercent(), is(49));
        assertThat("Allocation postused heap percentage", allocationRateEvent.getPostUsedPercent(), is(70));
    }

    @Test
    public void testDefaultGcSystemGc() {
        AbstractGCEvent<?> systemGcEvent = gcDefaultModel.get(4);
        UnittestHelper.testMemoryPauseEvent(systemGcEvent,
                "System.gc() heap",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION,
                0,
                1024 * 10124, 1024 * 5020, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
        assertThat("System.gc() preused heap percentage", systemGcEvent.getPreUsedPercent(), is(10));
        assertThat("System.gc() postused heap percentage", systemGcEvent.getPostUsedPercent(), is(5));
    }
}
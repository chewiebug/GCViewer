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
    private static final int CONCURRENT_MARK_INDEX = 0;
    private static final int CONCURRENT_PROCESS_REFERENCES_INDEX = 1;
    private static final int CONCURRENT_RESET_RELOCATION_SET_INDEX = 2;
    private static final int CONCURRENT_DESTROY_DETACHED_PAGES_INDEX = 3;
    private static final int CONCURRENT_SELECT_RELOCATION_SET_INDEX = 4;
    private static final int CONCURRENT_PREPARE_RELOCATION_SET_INDEX = 5;
    private static final int CONCURRENT_RELOCATE_INDEX = 6;


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
        assertThat("size", gcAllModel.size(), is(8));
        assertThat("amount of gc event types", gcAllModel.getGcEventPauses().size(), is(1));
        assertThat("amount of gc events", gcAllModel.getGCPause().getN(), is(1));
        assertThat("amount of full gc event types", gcAllModel.getFullGcEventPauses().size(), is(0));
        assertThat("amount of gc phases event types", gcAllModel.getGcEventPhases().size(), is(3));
        assertThat("amount of full gc events", gcAllModel.getFullGCPause().getN(), is(0));
        assertThat("amount of concurrent pause types", gcAllModel.getConcurrentEventPauses().size(), is(7));
    }

    @Test
    public void testGcAllGarbageCollection() {
        AbstractGCEvent<?> garbageCollectionEvent = gcAllModel.get(gcAllModel.size()-1);
        UnittestHelper.testMemoryPauseEvent(garbageCollectionEvent,
                "Garbage Collection",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION,
                0.002653,
                1024 * 10620, 1024 * 8800, 1024 * 194560,
                AbstractGCEvent.Generation.TENURED,
                false);
    }

    @Test
    public void testGcAllPauseMarkStart() {
        AbstractGCEvent<?> pauseMarkStartEvent = gcAllModel.getGCEvents().next().getPhases().get(0);
        UnittestHelper.testMemoryPauseEvent(pauseMarkStartEvent,
                "Pause Mark Start",
                AbstractGCEvent.Type.UJL_ZGC_PAUSE_MARK_START,
                0.001279,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
    }

    @Test
    public void testGcAllConcurrentMark() {
        AbstractGCEvent<?> concurrentMarkEvent = gcAllModel.get(CONCURRENT_MARK_INDEX);
        UnittestHelper.testMemoryPauseEvent(concurrentMarkEvent,
                "Concurrent Mark",
                AbstractGCEvent.Type.UJL_ZGC_CONCURRENT_MARK,
                0.005216,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
    }

    @Test
    public void testGcAllPauseMarkEnd() {
        AbstractGCEvent<?> pauseMarkEndEvent = gcAllModel.getGCEvents().next().getPhases().get(1);
        UnittestHelper.testMemoryPauseEvent(pauseMarkEndEvent,
                "Pause Mark End",
                AbstractGCEvent.Type.UJL_ZGC_PAUSE_MARK_END,
                0.000695,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
    }

    @Test
    public void testGcAllConcurrentNonref() {
        AbstractGCEvent<?> concurrentNonrefEvent = gcAllModel.get(CONCURRENT_PROCESS_REFERENCES_INDEX);
        UnittestHelper.testMemoryPauseEvent(concurrentNonrefEvent,
                "Concurrent Process Non-Strong References",
                AbstractGCEvent.Type.UJL_ZGC_CONCURRENT_NONREF,
                0.000258,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
    }

    @Test
    public void testGcAllConcurrentResetRelocSet() {
        AbstractGCEvent<?> concurrentResetRelocSetEvent = gcAllModel.get(CONCURRENT_RESET_RELOCATION_SET_INDEX);
        UnittestHelper.testMemoryPauseEvent(concurrentResetRelocSetEvent,
                "Concurrent Reset Relocation Set",
                AbstractGCEvent.Type.UJL_ZGC_CONCURRENT_RESET_RELOC_SET,
                0.000001,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
    }

    @Test
    public void testGcAllConcurrentDetachedPages() {
        AbstractGCEvent<?> concurrentDetachedPagesEvent = gcAllModel.get(CONCURRENT_DESTROY_DETACHED_PAGES_INDEX);
        UnittestHelper.testMemoryPauseEvent(concurrentDetachedPagesEvent,
                "Concurrent Destroy Detached Pages",
                AbstractGCEvent.Type.UJL_ZGC_CONCURRENT_DETATCHED_PAGES,
                0.000001,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
    }

    @Test
    public void testGcAllConcurrentSelectRelocSet() {
        AbstractGCEvent<?> concurrentSelectRelocSetEvent = gcAllModel.get(CONCURRENT_SELECT_RELOCATION_SET_INDEX);
        UnittestHelper.testMemoryPauseEvent(concurrentSelectRelocSetEvent,
                "Concurrent Select Relocation Set",
                AbstractGCEvent.Type.UJL_ZGC_CONCURRENT_SELECT_RELOC_SET,
                0.003822,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
    }

    @Test
    public void testGcAllConcurrentPrepareRelocSet() {
        AbstractGCEvent<?> concurrentPrepareRelocSetEvent = gcAllModel.get(CONCURRENT_PREPARE_RELOCATION_SET_INDEX);
        UnittestHelper.testMemoryPauseEvent(concurrentPrepareRelocSetEvent,
                "Concurrent Prepare Relocation Set",
                AbstractGCEvent.Type.UJL_ZGC_CONCURRENT_PREPARE_RELOC_SET,
                0.000865,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
    }

    @Test
    public void testGcAllPauseRelocateStart() {
        AbstractGCEvent<?> pauseRelocateStartEvent = gcAllModel.getGCEvents().next().getPhases().get(2);
        UnittestHelper.testMemoryPauseEvent(pauseRelocateStartEvent,
                "Pause Relocate Start",
                AbstractGCEvent.Type.UJL_ZGC_PAUSE_RELOCATE_START,
                0.000679,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
    }

    @Test
    public void testGcAllConcurrentRelocate() {
        AbstractGCEvent<?> concurrentRelocateEvent = gcAllModel.get(CONCURRENT_RELOCATE_INDEX);
        UnittestHelper.testMemoryPauseEvent(concurrentRelocateEvent,
                "Concurrent Relocate",
                AbstractGCEvent.Type.UJL_ZGC_CONCURRENT_RELOCATE,
                0.002846,
                0, 0, 0,
                AbstractGCEvent.Generation.TENURED,
                false);
    }

    @Test
    public void testGcDefault() {
        assertThat("size", gcDefaultModel.size(), is(5));
        assertThat("amount of STW GC pause types", gcDefaultModel.getGcEventPauses().size(), is(5));
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
    }

    @Test
    public void testGcDefaultWarmup() {
        AbstractGCEvent<?> warmupEvent = gcDefaultModel.get(1);
        UnittestHelper.testMemoryPauseEvent(warmupEvent,
                "Warmup heap",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION,
                0,
                1024 * 208, 1024 * 164, 1024 * 164 / 16 * 100,
                AbstractGCEvent.Generation.TENURED,
                false);
    }

    @Test
    public void testGcDefaultProactive() {
        AbstractGCEvent<?> proactiveEvent = gcDefaultModel.get(2);
        UnittestHelper.testMemoryPauseEvent(proactiveEvent,
                "Proactive heap",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION,
                0,
                1024 * 19804, 1024 * 20212, 20212 * 1024 / 10 * 100,
                AbstractGCEvent.Generation.TENURED,
                false);
    }

    @Test
    public void testGcDefaultAllocationRate() {
        AbstractGCEvent<?> allocationRateEvent = gcDefaultModel.get(3);
        UnittestHelper.testMemoryPauseEvent(allocationRateEvent,
                "Allocation Rate heap",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION,
                0,
                1024 * 502, 1024 * 716, 716 * 1024 / 70 * 100,
                AbstractGCEvent.Generation.TENURED,
                false);
    }

    @Test
    public void testDefaultGcSystemGc() {
        AbstractGCEvent<?> systemGcEvent = gcDefaultModel.get(4);
        UnittestHelper.testMemoryPauseEvent(systemGcEvent,
                "System.gc() heap",
                AbstractGCEvent.Type.UJL_ZGC_GARBAGE_COLLECTION,
                0,
                1024 * 10124, 1024 * 5020, 5020 * 1024 / 5 * 100,
                AbstractGCEvent.Generation.TENURED,
                false);
    }

}
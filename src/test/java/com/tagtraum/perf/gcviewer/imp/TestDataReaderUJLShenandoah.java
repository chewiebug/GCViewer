package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.util.DateHelper;
import org.junit.Test;

/**
 * Created by Mart on 10/05/2017.
 */
public class TestDataReaderUJLShenandoah {
    private GCModel getGCModelFromLogFile(String fileName) throws IOException {
        return UnittestHelper.getGCModelFromLogFile(fileName, FOLDER.OPENJDK_UJL, DataReaderUnifiedJvmLogging.class);
    }

    @Test
    public void parseAllocationFailure() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahAllocationFailure.txt");
        assertThat("size", model.size(), is(1));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(0));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));
        assertThat("total pause time", model.getPause().getSum(), closeTo(14.289335, 0.000001));
        assertThat("gc pause time", model.getGCPause().getSum(), is(0.0));
        assertThat("full gc pause time", model.getFullGCPause().getSum(), closeTo(14.289335, 0.000001));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("type", event.getTypeAsString(), startsWith(Type.UJL_PAUSE_FULL.toString()));
        assertThat("preUsed heap size", event.getPreUsed(), is(7943 * 1024));
        assertThat("postUsed heap size", event.getPostUsed(), is(6013 * 1024));
        assertThat("total heap size", event.getTotal(), is(8192 * 1024));
        assertThat("timestamp", event.getTimestamp(), closeTo(43.948, 0.001));
        assertThat("generation", event.getGeneration(), is(AbstractGCEvent.Generation.ALL));
    }

    @Test
    public void parsePassiveHeuristics() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahPassiveHeuristics.txt");
        assertThat("size", model.size(), is(0));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(0));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(0));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));
    }


    @Test
    public void parseSingleSystemGCEvent() throws Exception {
        // this kind of system.gc event logging might have been removed in jdk1.8_232
        GCModel model = getGCModelFromLogFile("SampleShenandoahSingleSystemGC.txt");
        assertThat("size", model.size(), is(1));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(0));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("type", event.getTypeAsString(), equalTo(Type.UJL_PAUSE_FULL + " (System.gc())"));
        assertThat("is system gc", event.isSystem(), is(true));
        assertThat("preUsed heap size", event.getPreUsed(), is(10 * 1024));
        assertThat("postUsed heap size", event.getPostUsed(), is(1 * 1024));
        assertThat("total heap size", event.getTotal(), is(128 * 1024));
        assertThat("timestamp", event.getTimestamp(), closeTo(1.481, 0.001));
        assertThat("generation", event.getGeneration(), is(AbstractGCEvent.Generation.ALL));
    }

    @Test
    public void parseSeveralSystemGCEvents() throws Exception {
        GCModel model = getGCModelFromLogFile("SampleShenandoahSeveralSystemGC.txt");
        assertThat("size", model.size(), is(438));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(0));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("type", event.getTypeAsString(), equalTo(Type.UJL_PAUSE_FULL + " (System.gc())"));
        assertThat("is system gc", event.isSystem(), is(true));
        assertThat("preUsed heap size", event.getPreUsed(), is(10 * 1024));
        assertThat("postUsed heap size", event.getPostUsed(), is(1 * 1024));
        assertThat("total heap size", event.getTotal(), is(128 * 1024));
        assertThat("generation", event.getGeneration(), is(AbstractGCEvent.Generation.ALL));
        assertThat("timestamp", event.getTimestamp(), closeTo(1.303, 0.001));
    }

    @Test
    public void parseJdk11Beginning() throws Exception {
        // the main purpose if this test is to make sure, that no warnings are printed, when parsing the beginning of a shenandoah gc log file
        GCModel model = getGCModelFromLogFile("Sample-ujl-shenandoah-jdk11-beginning.txt");
        assertThat("size", model.size(), is(1));
    }

    @Test
    public void testConcurrentReset() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2019-09-28T14:32:51.558+0000][0.277s][info][gc           ] Trigger: Learning 1 of 5. Free (88M) is below initial threshold (89M)\n" +
                        "[2019-09-28T14:32:51.558+0000][0.277s][info][gc,ergo      ] Free: 88M (44 regions), Max regular: 2048K, Max humongous: 88064K, External frag: 3%, Internal frag: 0%\n" +
                        "[2019-09-28T14:32:51.558+0000][0.277s][info][gc,ergo      ] Evacuation Reserve: 8M (4 regions), Max regular: 2048K\n" +
                        "[2019-09-28T14:32:51.558+0000][0.277s][info][gc,start     ] GC(0) Concurrent reset\n" +
                        "[2019-09-28T14:32:51.558+0000][0.277s][info][gc,task      ] GC(0) Using 1 of 1 workers for concurrent reset\n" +
                        "[2019-09-28T14:32:51.558+0000][0.277s][info][gc           ] GC(0) Concurrent reset 32M->32M(32M) 0.081ms\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_SHEN_CONCURRENT_RESET));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.000081, 0.0000001));
        assertThat("event preUsed", model.get(0).getPreUsed(), is(32 * 1024));
        assertThat("begin of concurrent collection", model.get(0).isConcurrentCollectionStart(), is(true));
    }

    @Test
    public void testPauseInitMark() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2019-09-28T14:32:51.558+0000][0.277s][info][safepoint    ] Application time: 0.1149181 seconds\n" +
                        "[2019-09-28T14:32:51.558+0000][0.278s][info][safepoint    ] Entering safepoint region: ShenandoahInitMark\n" +
                        "[2019-09-28T14:32:51.559+0000][0.278s][info][gc,start     ] GC(0) Pause Init Mark (process weakrefs)\n" +
                        "[2019-09-28T14:32:51.559+0000][0.278s][info][gc,task      ] GC(0) Using 1 of 1 workers for init marking\n" +
                        "[2019-09-28T14:32:51.560+0000][0.279s][info][gc,ergo      ] GC(0) Pacer for Mark. Expected Live: 12M, Free: 88M, Non-Taxable: 8M, Alloc Tax Rate: 0.5x\n" +
                        "[2019-09-28T14:32:51.560+0000][0.279s][info][gc           ] GC(0) Pause Init Mark (process weakrefs) 1.275ms\n" +
                        "[2019-09-28T14:32:51.560+0000][0.279s][info][safepoint    ] Leaving safepoint region\n" +
                        "[2019-09-28T14:32:51.560+0000][0.279s][info][safepoint    ] Total time for which application threads were stopped: 0.0021273 seconds, Stopping threads took: 0.0005609 seconds\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type as string", model.get(0).getTypeAsString(), is("Pause Init Mark (process weakrefs)"));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_SHEN_INIT_MARK));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.001275, 0.0000001));
        assertThat("event preUsed", model.get(0).getPreUsed(), is(0));
        assertThat("datestamp", model.get(0).getDatestamp(), is(DateHelper.parseDate("2019-09-28T14:32:51.560+0000")));
        assertThat("is initial mark", model.get(0).isInitialMark(), is(true));
    }

    @Test
    public void testConcurrentMark() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2019-09-28T14:32:51.560+0000][0.279s][info][gc,start     ] GC(0) Concurrent marking (process weakrefs)\n" +
                        "[2019-09-28T14:32:51.560+0000][0.279s][info][gc,task      ] GC(0) Using 1 of 1 workers for concurrent marking\n" +
                        "[2019-09-28T14:32:51.561+0000][0.281s][info][gc           ] GC(0) Concurrent marking (process weakrefs) 32M->36M(36M) 1.385ms\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type as string", model.get(0).getTypeAsString(), is("Concurrent marking (process weakrefs)"));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_SHEN_CONCURRENT_CONC_MARK));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.001385, 0.0000001));
        assertThat("event preUsed", model.get(0).getPreUsed(), is(32 * 1024));
        assertThat("event postUsed", model.get(0).getPostUsed(), is(36 * 1024));
    }

    @Test
    public void testConcurrentPrecleaning() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2019-09-28T14:32:51.561+0000][0.281s][info][gc,start     ] GC(0) Concurrent precleaning\n" +
                        "[2019-09-28T14:32:51.561+0000][0.281s][info][gc,task      ] GC(0) Using 1 of 1 workers for concurrent preclean\n" +
                        "[2019-09-28T14:32:51.561+0000][0.281s][info][gc           ] GC(0) Concurrent precleaning 36M->36M(36M) 0.029ms\n" +
                        "[2019-09-28T14:32:51.562+0000][0.281s][info][safepoint    ] Application time: 0.0015217 seconds\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getTypeAsString(), is("Concurrent precleaning"));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.000029, 0.0000001));
        assertThat("event preUsed", model.get(0).getPreUsed(), is(36 * 1024));
        assertThat("event postUsed", model.get(0).getPostUsed(), is(36 * 1024));
    }

    @Test
    public void testPauseFinalMark() throws Exception {
        // in its early implementation (2017), Shenandoah had memory information in this event, which was dropped later (2019)
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2019-09-28T14:32:51.562+0000][0.281s][info][safepoint    ] Entering safepoint region: ShenandoahFinalMarkStartEvac\n" +
                        "[2019-09-28T14:32:51.562+0000][0.281s][info][gc,start     ] GC(0) Pause Final Mark (process weakrefs)\n" +
                        "[2019-09-28T14:32:51.562+0000][0.281s][info][gc,task      ] GC(0) Using 1 of 1 workers for final marking\n" +
                        "[2019-09-28T14:32:51.563+0000][0.282s][info][gc,ergo      ] GC(0) Adaptive CSet Selection. Target Free: 12M, Actual Free: 92M, Max CSet: 5M, Min Garbage: 0M\n" +
                        "[2019-09-28T14:32:51.563+0000][0.282s][info][gc,ergo      ] GC(0) Collectable Garbage: 9M (96% of total), 0M CSet, 5 CSet regions\n" +
                        "[2019-09-28T14:32:51.563+0000][0.282s][info][gc,ergo      ] GC(0) Immediate Garbage: 0M (0% of total), 0 regions\n" +
                        "[2019-09-28T14:32:51.563+0000][0.283s][info][gc,ergo      ] GC(0) Pacer for Evacuation. Used CSet: 10M, Free: 84M, Non-Taxable: 8M, Alloc Tax Rate: 1.1x\n" +
                        "[2019-09-28T14:32:51.563+0000][0.283s][info][gc           ] GC(0) Pause Final Mark (process weakrefs) 1.404ms\n" +
                        "[2019-09-28T14:32:51.563+0000][0.283s][info][safepoint    ] Leaving safepoint region\n" +
                        "[2019-09-28T14:32:51.563+0000][0.283s][info][safepoint    ] Total time for which application threads were stopped: 0.0018727 seconds, Stopping threads took: 0.0002023 seconds\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getTypeAsString(), is("Pause Final Mark (process weakrefs)"));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.001404, 0.0000001));
        assertThat("event preUsed", model.get(0).getPreUsed(), is(0));
        assertThat("is final mark", model.get(0).isRemark(), is(true));
    }

    @Test
    public void testConcurrentCleanup() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2019-09-28T14:32:51.563+0000][0.283s][info][gc,start     ] GC(0) Concurrent cleanup\n" +
                        "[2019-09-28T14:32:51.563+0000][0.283s][info][gc           ] GC(0) Concurrent cleanup 36M->36M(38M) 0.014ms\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getTypeAsString(), is("Concurrent cleanup"));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.000014, 0.0000001));
        assertThat("event preUsed", model.get(0).getPreUsed(), is(36 * 1024));
        assertThat("event total", model.get(0).getTotal(), is(38 * 1024));
        assertThat("is concurrent collection end", model.get(0).isConcurrentCollectionEnd(), is(true));
    }

    @Test
    public void testConcurrentEvacuation() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2019-09-28T14:32:51.563+0000][0.283s][info][gc,ergo      ] GC(0) Free: 84M (42 regions), Max regular: 2048K, Max humongous: 83968K, External frag: 3%, Internal frag: 0%\n" +
                        "[2019-09-28T14:32:51.563+0000][0.283s][info][gc,ergo      ] GC(0) Evacuation Reserve: 7M (4 regions), Max regular: 2048K\n" +
                        "[2019-09-28T14:32:51.563+0000][0.283s][info][gc,start     ] GC(0) Concurrent evacuation\n" +
                        "[2019-09-28T14:32:51.563+0000][0.283s][info][gc,task      ] GC(0) Using 1 of 1 workers for concurrent evacuation\n" +
                        "[2019-09-28T14:32:51.564+0000][0.283s][info][gc           ] GC(0) Concurrent evacuation 36M->39M(40M) 0.573ms\n" +
                        "[2019-09-28T14:32:51.564+0000][0.283s][info][safepoint    ] Application time: 0.0006846 seconds\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getTypeAsString(), is("Concurrent evacuation"));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.000573, 0.0000001));
        assertThat("event preUsed", model.get(0).getPreUsed(), is(36 * 1024));
        assertThat("event postUsed", model.get(0).getPostUsed(), is(39 * 1024));
        assertThat("event total", model.get(0).getTotal(), is(40 * 1024));
    }

    @Test
    public void testPauseInitUpdateRefs() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2019-09-28T14:32:51.564+0000][0.284s][info][safepoint    ] Entering safepoint region: ShenandoahInitUpdateRefs\n" +
                        "[2019-09-28T14:32:51.564+0000][0.284s][info][gc,start     ] GC(0) Pause Init Update Refs\n" +
                        "[2019-09-28T14:32:51.564+0000][0.284s][info][gc,ergo      ] GC(0) Pacer for Update Refs. Used: 39M, Free: 82M, Non-Taxable: 8M, Alloc Tax Rate: 1.1x\n" +
                        "[2019-09-28T14:32:51.564+0000][0.284s][info][gc           ] GC(0) Pause Init Update Refs 0.027ms\n" +
                        "[2019-09-28T14:32:51.565+0000][0.284s][info][safepoint    ] Leaving safepoint region\n" +
                        "[2019-09-28T14:32:51.565+0000][0.284s][info][safepoint    ] Total time for which application threads were stopped: 0.0004592 seconds, Stopping threads took: 0.0001897 seconds\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getTypeAsString(), is("Pause Init Update Refs"));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.000027, 0.0000001));
        assertThat("event preUsed", model.get(0).getPreUsed(), is(0));
    }

    @Test
    public void testConcurrentUpdateRefs() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2019-09-28T14:32:51.565+0000][0.284s][info][gc,start     ] GC(0) Concurrent update references\n" +
                        "[2019-09-28T14:32:51.565+0000][0.284s][info][gc,task      ] GC(0) Using 1 of 1 workers for concurrent reference update\n" +
                        "[2019-09-28T14:32:51.565+0000][0.284s][info][gc           ] GC(0) Concurrent update references 39M->41M(42M) 0.578ms\n" +
                        "[2019-09-28T14:32:51.565+0000][0.284s][info][safepoint    ] Application time: 0.0006526 seconds\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getTypeAsString(), is("Concurrent update references"));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.000578, 0.0000001));
        assertThat("event preUsed", model.get(0).getPreUsed(), is(39 * 1024));
    }

    @Test
    public void testPauseFinalUpdateRefs() throws Exception {
        // in its early implementation (2017), Shenandoah had memory information in this event, which was dropped later (2019)
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2019-09-28T14:32:51.565+0000][0.285s][info][safepoint    ] Entering safepoint region: ShenandoahFinalUpdateRefs\n" +
                        "[2019-09-28T14:32:51.566+0000][0.285s][info][gc,start     ] GC(0) Pause Final Update Refs\n" +
                        "[2019-09-28T14:32:51.566+0000][0.285s][info][gc,task      ] GC(0) Using 1 of 1 workers for final reference update\n" +
                        "[2019-09-28T14:32:51.566+0000][0.285s][info][gc           ] GC(0) Pause Final Update Refs 0.203ms\n" +
                        "[2019-09-28T14:32:51.566+0000][0.285s][info][safepoint    ] Leaving safepoint region\n" +
                        "[2019-09-28T14:32:51.566+0000][0.285s][info][safepoint    ] Total time for which application threads were stopped: 0.0006525 seconds, Stopping threads took: 0.0001846 seconds\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getTypeAsString(), is("Pause Final Update Refs"));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.000203, 0.0000001));
        assertThat("event preUsed", model.get(0).getPreUsed(), is(0));
    }

    @Test
    public void testConcurrentUncommit() throws Exception {
        // in the sequence of gc events, there seem to be two "concurrent cleanup" events - one somewhere in the middle, the other the last
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2019-09-28T14:32:52.483+0000][1.203s][info][gc,start      ] Concurrent uncommit\n" +
                        "[2019-09-28T14:32:52.496+0000][1.215s][info][gc            ] Concurrent uncommit 3M->3M(16M) 12.181ms\n" +
                        "[2019-09-28T14:32:52.565+0000][1.284s][info][gc            ] Trigger: Learning 2 of 5. Free (88M) is below initial threshold (89M)\n" +
                        "[2019-09-28T14:32:52.565+0000][1.284s][info][gc,ergo       ] Free: 88M (47 regions), Max regular: 2048K, Max humongous: 77824K, External frag: 15%, Internal frag: 5%\n" +
                        "[2019-09-28T14:32:52.565+0000][1.284s][info][gc,ergo       ] Evacuation Reserve: 8M (4 regions), Max regular: 2048K\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getTypeAsString(), is("Concurrent uncommit"));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.012181, 0.0000001));
        assertThat("event preUsed", model.get(0).getPreUsed(), is(3 * 1024));
    }

    @Test
    public void testPauseDegeneratedGc() throws Exception {
        GCModel model = getGCModelFromLogFile("Sample-ujl-shenandoah-jdk11-PauseDegeneratedGc.txt");

        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getTypeAsString(), is("Pause Degenerated GC (Outside of Cycle)"));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.011373, 0.0000001));
        assertThat("event preUsed", model.get(0).getPreUsed(), is(119 * 1024));
        assertThat("event postUsed", model.get(0).getPostUsed(), is(56 * 1024));
        assertThat("event total", model.get(0).getTotal(), is(124 * 1024));
    }

    @Test
    public void testParseFullGcWithPhases() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[1,331s][info][gc,start          ] GC(0) Pause Full (System.gc())\n" +
                "[1,332s][info][gc,phases,start   ] GC(0) Phase 1: Mark live objects\n" +
                "[1,334s][info][gc,stringtable    ] GC(0) Cleaned string and symbol table, strings: 1755 processed, 19 removed, symbols: 23807 processed, 0 removed\n" +
                "[1,334s][info][gc,phases         ] GC(0) Phase 1: Mark live objects 2,911ms\n" +
                "[1,334s][info][gc,phases,start   ] GC(0) Phase 2: Compute new object addresses\n" +
                "[1,335s][info][gc,phases         ] GC(0) Phase 2: Compute new object addresses 0,501ms\n" +
                "[1,335s][info][gc,phases,start   ] GC(0) Phase 3: Adjust pointers\n" +
                "[1,336s][info][gc,phases         ] GC(0) Phase 3: Adjust pointers 1,430ms\n" +
                "[1,336s][info][gc,phases,start   ] GC(0) Phase 4: Move objects\n" +
                "[1,337s][info][gc,phases         ] GC(0) Phase 4: Move objects 0,619ms\n" +
                "[1,337s][info][gc                ] GC(0) Pause Full (System.gc()) 10M->1M(128M) 5,636ms\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_PAUSE_FULL));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.005636, 0.0000001));

        assertThat("phases", model.getGcEventPhases().size(), is(4));
        assertThat("phase 1",
                model.getGcEventPhases().get(Type.UJL_SERIAL_PHASE_MARK_LIFE_OBJECTS.getName()).getSum(),
                closeTo(0.002911, 0.0000001));
    }

}

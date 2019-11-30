package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Test;

/**
 * Test logs generated specifically by java 1.8.
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 25.09.2013</p>
 */
public class TestDataReaderSun1_8_0 {

    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(FOLDER.OPENJDK, fileName);
    }

    private DataReader getDataReader(GCResource gcResource) throws IOException {
        return new DataReaderSun1_6_0(gcResource, getInputStream(gcResource.getResourceName()), GcLogType.SUN1_8);
    }

    @Test
    public void parallelPrintHeapAtGC() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleSun1_8_0ParallelPrintHeapAtGC.txt");
        gcResource.getLogger().addHandler(handler);
        
        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc pause sum", model.getPause().getSum(), closeTo(0.0103603, 0.000000001));

        assertThat("number of errors", handler.getCount(), is(0));
    }

    @Test
    public void scavengeBeforeRemarkPrintHeapAtGC_YGOccupancy() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleSun1_8_0CMS_ScavengeBeforeRemark_HeapAtGc.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(2));
        assertThat("scavenge before remark event", model.get(0).getPause(), closeTo(0.0000778, 0.000000001));
        assertThat("remark event", model.get(1).getPause(), closeTo(0.0019970 - 0.0000778, 0.000000001));

        assertEquals("number of errors", 0, handler.getCount());
    }

    @Test
    public void scavengeBeforeRemark_HeapAtGC_PrintTenuringDistribution_PrintFLSStats() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleSun1_8_0CMS_ScavengeBR_HeapAtGC_TenuringDist_PrintFLS.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(2));
        assertThat("scavenge before remark event", model.get(0).getPause(), closeTo(0.1306264, 0.000000001));
        assertThat("remark event", model.get(1).getPause(), closeTo(0.1787717 - 0.1306264, 0.000000001));

        assertEquals("number of errors", 0, handler.getCount());
    }

    @Test
    public void parallelPrintTenuringGcCause() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleSun1_8_0Parallel_Tenuring_PrintGCCause.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(5));
        assertThat("gc name", model.get(0).getTypeAsString(), equalTo("GC (Allocation Failure); PSYoungGen"));
        assertThat("pause", model.get(0).getPause(), closeTo(0.0199218, 0.000000001));

        assertEquals("number of errors", 0, handler.getCount());
    }

    @Test
    public void parallelApple() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleSun1_8_0Parallel_Apple.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(6));

        assertEquals("number of errors", 0, handler.getCount());
    }

    @Test
    public void cmsPrintHeapBeforeFullGc() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleSun1_8_0CMS_HeadDumpBeforeFullGc.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(2));
        assertThat("gc name concurrent", model.get(0).getTypeAsString(), equalTo("CMS-concurrent-mark"));

        assertThat("gc name full gc", model.get(1).getTypeAsString(), equalTo("Full GC (GCLocker Initiated GC); CMS (concurrent mode failure); Metaspace"));
        assertThat("pause", model.get(1).getPause(), closeTo(218.6928810, 0.000000001));

        assertEquals("number of errors", 0, handler.getCount());
    }

    @Test
    public void shenandoahPauseMark() throws Exception {
        // in its early implementation (jdk8u171), Shenandoah had memory information in the "Pause Final Mark" event, which was dropped later (jdk8u232)
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("0.234: [Pause Init Mark (process weakrefs), start]\n" +
                        "    Using 2 of 2 workers for init marking\n" +
                        "    Pacer for Mark. Expected Live: 12M, Free: 87M, Non-Taxable: 8M, Alloc Tax Rate: 0.5x\n" +
                        "0.236: [Pause Init Mark (process weakrefs), 1.983 ms]\n" +
                        "0.239: [Pause Final Mark (process weakrefs), start]\n" +
                        "    Using 2 of 2 workers for final marking\n" +
                        "    Adaptive CSet Selection. Target Free: 12M, Actual Free: 92M, Max CSet: 5M, Min Garbage: 0M\n" +
                        "    Collectable Garbage: 0M (0% of total), 0M CSet, 0 CSet regions\n" +
                        "    Immediate Garbage: 0M (0% of total), 0 regions\n" +
                        "0.242: [Pause Final Mark (process weakrefs), 2.472 ms]\n")
                        .getBytes());

        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_8);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(2));
        assertThat("warnings", handler.getCount(), is(0));

        AbstractGCEvent<?> initMarkEvent = model.get(0);
        assertThat("Pause init mark: name", initMarkEvent.getTypeAsString(), startsWith("Pause Init Mark"));
        assertThat("Pause init mark: type", initMarkEvent.getExtendedType().getType(), is(Type.UJL_SHEN_INIT_MARK));
        assertThat("Pause init mark: duration", initMarkEvent.getPause(), closeTo(0.001983, 0.00001));

        AbstractGCEvent<?> finalMarkEvent = model.get(1);
        assertThat("Pause final mark: name", finalMarkEvent.getTypeAsString(), startsWith("Pause Final Mark"));
        assertThat("Pause final mark: type", finalMarkEvent.getExtendedType().getType(), is(Type.UJL_SHEN_FINAL_MARK));
        assertThat("Pause final mark: duration", finalMarkEvent.getPause(), closeTo(0.002472, 0.00001));
    }

    @Test
    public void shenandoahPauseUpdateRefs() throws Exception {
        // in its early implementation (jdk8u171), Shenandoah had memory information in the "Pause Final Update Refs" event, which was dropped later (jdk8u232)
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("0.500: [Pause Init Update Refs, start]\n" +
                        "    Pacer for Update Refs. Used: 43M, Free: 76M, Non-Taxable: 7M, Alloc Tax Rate: 1.1x\n" +
                        "0.500: [Pause Init Update Refs, 0.015 ms]\n" +
                        "0.501: [Pause Final Update Refs, start]\n" +
                        "    Using 2 of 2 workers for final reference update\n" +
                        "0.501: [Pause Final Update Refs, 0.291 ms]\n")
                        .getBytes());

        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_8);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(2));
        assertThat("warnings", handler.getCount(), is(0));

        AbstractGCEvent<?> initUpdateRefsEvent = model.get(0);
        assertThat("Pause init update refs: name", initUpdateRefsEvent.getTypeAsString(), equalTo("Pause Init Update Refs"));
        assertThat("Pause init update refs: duration", initUpdateRefsEvent.getPause(), closeTo(0.000015, 0.0000001));

        AbstractGCEvent<?> finalUpdateRefsEvent = model.get(1);
        assertThat("Pause Final Update Refs: name", finalUpdateRefsEvent.getTypeAsString(), equalTo("Pause Final Update Refs"));
        assertThat("Pause Final Update Refs: duration", finalUpdateRefsEvent.getPause(), closeTo(0.000291, 0.00001));
    }

    @Test
    public void shehandoahConcurrentEventsjsk8u171() throws Exception {
        // probably jdk8u171
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("13.979: [Concurrent marking 1435M->1447M(2048M), 12.576 ms]" +
                        "\n13.994: [Concurrent evacuation 684M->712M(2048M), 6.041 ms]" +
                        "\n14.001: [Concurrent update references  713M->726M(2048M), 14.718 ms]" +
                        "\n14.017: [Concurrent reset bitmaps 60M->62M(2048M), 0.294 ms]" +
                        "\n626.259: [Cancel concurrent mark, 0.056 ms]\n")
                        .getBytes());

        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_8);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(5));
        assertThat("warnings", handler.getCount(), is(0));

        AbstractGCEvent<?> concurrentMarking = model.get(0);
        assertThat("Concurrent Marking: name", concurrentMarking.getTypeAsString(), equalTo("Concurrent marking"));
        assertThat("Concurrent Marking: duration", concurrentMarking.getPause(), closeTo(0.012576, 0.0000001));
        assertThat("Concurrent Marking: before", concurrentMarking.getPreUsed(), is(1435 * 1024));
        assertThat("Concurrent Marking: after", concurrentMarking.getPostUsed(), is(1447 * 1024));
        assertThat("Concurrent Marking: total", concurrentMarking.getTotal(), is(2048 * 1024));
    }

    @Test
    public void shehandoahConcurrentEventsjsk8u232() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("0.233: [Concurrent reset, start]\n" +
                        "    Using 2 of 2 workers for concurrent reset\n" +
                        "0.234: [Concurrent reset 32854K->32983K(34816K), 0.401 ms]\n" +
                        "0.237: [Concurrent marking (process weakrefs), start]\n" +
                        "    Using 2 of 2 workers for concurrent marking\n" +
                        "0.238: [Concurrent marking (process weakrefs) 32983K->34327K(36864K), 1.159 ms]\n" +
                        "0.238: [Concurrent precleaning, start]\n" +
                        "    Using 1 of 2 workers for concurrent preclean\n" +
                        "0.238: [Concurrent precleaning 34359K->34423K(36864K), 0.053 ms]\n" +
                        "0.242: [Concurrent cleanup, start]\n" +
                        "0.242: [Concurrent cleanup 34807K->34840K(36864K), 0.019 ms]\n" +
                        "Free: 78M (56 regions), Max regular: 2048K, Max humongous: 61440K, External frag: 24%, Internal frag: 29%\n" +
                        "Evacuation Reserve: 7M (4 regions), Max regular: 2048K\n" +
                        "0.298: [Concurrent evacuation, start]\n" +
                        "    Using 2 of 2 workers for concurrent evacuation\n" +
                        "0.299: [Concurrent evacuation 42458K->46621K(112M), 0.538 ms]\n" +
                        "0.299: [Concurrent update references, start]\n" +
                        "    Using 2 of 2 workers for concurrent reference update\n" +
                        "0.299: [Concurrent update references 46813K->49951K(112M), 0.481 ms]\n" +
                        "Free: 118M (60 regions), Max regular: 2048K, Max humongous: 110592K, External frag: 9%, Internal frag: 1%\n" +
                        "Evacuation Reserve: 8M (4 regions), Max regular: 2048K\n" +
                        "Pacer for Idle. Initial: 2M, Alloc Tax Rate: 1.0x\n" +
                        "1.115: [Concurrent uncommit, start]\n" +
                        "1.129: [Concurrent uncommit 1986K->1986K(10240K), 13.524 ms]\n")
                        .getBytes());

        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_8);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(7));
        assertThat("warnings", handler.getCount(), is(0));

        AbstractGCEvent<?> concurrentMarking = model.get(0);
        assertThat("Concurrent reset: name", concurrentMarking.getTypeAsString(), is("Concurrent reset"));
        assertThat("Concurrent reset: duration", concurrentMarking.getPause(), closeTo(0.000401, 0.0000001));
        assertThat("Concurrent reset: before", concurrentMarking.getPreUsed(), is(32854));
        assertThat("Concurrent reset: after", concurrentMarking.getPostUsed(), is(32983));
        assertThat("Concurrent reset: total", concurrentMarking.getTotal(), is(34816));
    }

    @Test
    public void shenandoahIgnoredLines() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("Uncommitted 87M. Heap: 2048M reserved, 1961M committed, 992M used" +
                        "\nCancelling concurrent GC: Allocation Failure")
                        .getBytes());

        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_8);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(0));
        assertThat("warnings", handler.getCount(), is(0));
    }

    @Test
    public void shenandoaPauseInitMarkDetails() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("Capacity: 262144M, Peak Occupancy: 222063M, Lowest Free: 40080M, Free Threshold: 7864M\n" +
                        "Uncommitted 1184M. Heap: 262144M reserved, 223616M committed, 213270M used\n" +
                        "Periodic GC triggered. Time since last GC: 300004 ms, Guaranteed Interval: 300000 ms\n" +
                        "347584.988: [Pause Init Mark, 3.942 ms]\n")
                        .getBytes());

        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_8);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(1));
        assertThat("warnings", handler.getCount(), is(0));

        AbstractGCEvent<?> initMarkEvent = model.get(0);
        assertThat("Pause init mark: duration", initMarkEvent.getPause(), closeTo(0.003942, 0.00001));
    }

    @Test
    public void shenandoahConcurrentCleanup() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("0.242: [Concurrent cleanup 34807K->34840K(36864K), 0.019 ms]\n" +
                        "Free: 85M (43 regions), Max regular: 2048K, Max humongous: 86016K, External frag: 2%, Internal frag: 0%\n" +
                        "Evacuation Reserve: 8M (4 regions), Max regular: 2048K\n" +
                        "Free: 85M (43 regions), Max regular: 2048K, Max humongous: 86016K, External frag: 2%, Internal frag: 0%\n" +
                        "Evacuation Reserve: 8M (4 regions), Max regular: 2048K\n" +
                        "Pacer for Idle. Initial: 2M, Alloc Tax Rate: 1.0x\n" +
                        "Trigger: Learning 2 of 5. Free (83M) is below initial threshold (89M)\n" +
                        "Free: 83M (42 regions), Max regular: 2048K, Max humongous: 83968K, External frag: 2%, Internal frag: 0%\n" +
                        "Evacuation Reserve: 8M (4 regions), Max regular: 2048K\n")
                        .getBytes());

        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_8);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(1));
        assertThat("warnings", handler.getCount(), is(0));

        AbstractGCEvent<?> concurrentCleanupEvent = model.get(0);
        assertThat("name", concurrentCleanupEvent.getTypeAsString(), equalTo("Concurrent cleanup"));
        assertThat("duration", concurrentCleanupEvent.getPause(), closeTo(0.000019, 0.0000001));
        assertThat("before", concurrentCleanupEvent.getPreUsed(), is(34807));
    }

    @Test
    public void shenandoahDetailsShutdown()  throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.INFO);
        GCResource gcResource = new GcResourceFile("SampleOpenJdk1_8_0ShenandoahDetailsShutdown.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(0));
        assertThat("number of errors",
                handler.getLogRecords().stream().filter(logRecord -> !logRecord.getLevel().equals(Level.INFO)).count(),
                is(0L));
        assertThat("contains GC STATISTICS",
                handler.getLogRecords().stream().filter(logRecord -> logRecord.getMessage().startsWith("GC STATISTICS")).count(),
                is(1L));
    }

    @Test
    public void shenandoah_171_Beginning()  throws Exception {
        // in its early implementation (jdk8u171), Shenandoah had memory information in the "Pause Final Mark" event, which was dropped later (jdk8u232)
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.INFO);
        GCResource gcResource = new GcResourceFile("SampleOpenJdk1_8_0-171-ShenandoahBeginning.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(9));
        assertThat("number of errors",
                handler.getLogRecords().stream().filter(logRecord -> !logRecord.getLevel().equals(Level.INFO)).count(),
                is(2L));
        assertThat("contains 'Initialize Shenandoah heap'",
                handler.getLogRecords().stream().filter(logRecord -> logRecord.getMessage().startsWith("Initialize Shenandoah heap")).count(),
                is(1L));
    }

    @Test
    public void shenandoah_232_Beginning()  throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.INFO);
        GCResource gcResource = new GcResourceFile("SampleOpenJdk1_8_0-232-ShenandoahBeginning.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(1));
        assertThat("number of errors",
                handler.getLogRecords().stream().filter(logRecord -> !logRecord.getLevel().equals(Level.INFO)).count(),
                is(0L));
        assertThat("contains 'Shenandoah heuristics'",
                handler.getLogRecords().stream().filter(logRecord -> logRecord.getMessage().startsWith("Shenandoah heuristics")).count(),
                is(1L));
    }

}

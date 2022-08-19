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
import com.tagtraum.perf.gcviewer.util.DateHelper;
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

    private GCModel getGCModelFromLogString(String logString) throws IOException {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        ByteArrayInputStream in = new ByteArrayInputStream(logString.getBytes());

        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_8);
        GCModel model = reader.read();
        assertThat("number of errors", handler.getCount(), is(0));
        return model;
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
        assertThat("Pause init mark: type", initMarkEvent.getExtendedType().getType(), is(Type.UJL_SHEN_PAUSE_INIT_MARK));
        assertThat("Pause init mark: duration", initMarkEvent.getPause(), closeTo(0.001983, 0.00001));

        AbstractGCEvent<?> finalMarkEvent = model.get(1);
        assertThat("Pause final mark: name", finalMarkEvent.getTypeAsString(), startsWith("Pause Final Mark"));
        assertThat("Pause final mark: type", finalMarkEvent.getExtendedType().getType(), is(Type.UJL_SHEN_PAUSE_FINAL_MARK));
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

// FIX ME!  Only "Concurrent cleanup", "Concurrent uncommit", "Pause Degenerated GC", "Pause Full" event types have memory info
//    @Test
//    public void shehandoahConcurrentEventsjsk8u171() throws Exception {
//        // FIX ME! I think the test is overtime and need removed, alert the log string for build success.
//        // probably jdk8u171
//        TestLogHandler handler = new TestLogHandler();
//        handler.setLevel(Level.WARNING);
//        GCResource gcResource = new GcResourceFile("byteArray");
//        gcResource.getLogger().addHandler(handler);
//
//        ByteArrayInputStream in = new ByteArrayInputStream(
//                ("13.979: [Concurrent marking 1435M->1447M(2048M), 12.576 ms]" +
//                        "\n13.994: [Concurrent evacuation 684M->712M(2048M), 6.041 ms]" +
//                        "\n14.001: [Concurrent update references  713M->726M(2048M), 14.718 ms]" +
//                        "\n14.017: [Concurrent reset bitmaps 60M->62M(2048M), 0.294 ms]" +
//                        "\n626.259: [Cancel concurrent mark, 0.056 ms]\n")
//                        .getBytes());
//
//        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_8);
//        GCModel model = reader.read();
//
//        assertThat("gc count", model.size(), is(5));
//        assertThat("warnings", handler.getCount(), is(0));
//
//        AbstractGCEvent<?> concurrentMarking = model.get(0);
//        assertThat("Concurrent Marking: name", concurrentMarking.getTypeAsString(), equalTo("Concurrent marking"));
//        assertThat("Concurrent Marking: duration", concurrentMarking.getPause(), closeTo(0.012576, 0.0000001));
//        assertThat("Concurrent Marking: before", concurrentMarking.getPreUsed(), is(1435 * 1024));
//        assertThat("Concurrent Marking: after", concurrentMarking.getPostUsed(), is(1447 * 1024));
//        assertThat("Concurrent Marking: total", concurrentMarking.getTotal(), is(2048 * 1024));
//    }
//
//    @Test
//    public void shehandoahConcurrentEventsjsk8u232() throws Exception {
//        TestLogHandler handler = new TestLogHandler();
//        handler.setLevel(Level.WARNING);
//        GCResource gcResource = new GcResourceFile("byteArray");
//        gcResource.getLogger().addHandler(handler);
//
//        ByteArrayInputStream in = new ByteArrayInputStream(
//                ("0.233: [Concurrent reset, start]\n" +
//                        "    Using 2 of 2 workers for concurrent reset\n" +
//                        "0.234: [Concurrent reset 32854K->32983K(34816K), 0.401 ms]\n" +
//                        "0.237: [Concurrent marking (process weakrefs), start]\n" +
//                        "    Using 2 of 2 workers for concurrent marking\n" +
//                        "0.238: [Concurrent marking (process weakrefs) 32983K->34327K(36864K), 1.159 ms]\n" +
//                        "0.238: [Concurrent precleaning, start]\n" +
//                        "    Using 1 of 2 workers for concurrent preclean\n" +
//                        "0.238: [Concurrent precleaning 34359K->34423K(36864K), 0.053 ms]\n" +
//                        "0.242: [Concurrent cleanup, start]\n" +
//                        "0.242: [Concurrent cleanup 34807K->34840K(36864K), 0.019 ms]\n" +
//                        "Free: 78M (56 regions), Max regular: 2048K, Max humongous: 61440K, External frag: 24%, Internal frag: 29%\n" +
//                        "Evacuation Reserve: 7M (4 regions), Max regular: 2048K\n" +
//                        "0.298: [Concurrent evacuation, start]\n" +
//                        "    Using 2 of 2 workers for concurrent evacuation\n" +
//                        "0.299: [Concurrent evacuation 42458K->46621K(112M), 0.538 ms]\n" +
//                        "0.299: [Concurrent update references, start]\n" +
//                        "    Using 2 of 2 workers for concurrent reference update\n" +
//                        "0.299: [Concurrent update references 46813K->49951K(112M), 0.481 ms]\n" +
//                        "Free: 118M (60 regions), Max regular: 2048K, Max humongous: 110592K, External frag: 9%, Internal frag: 1%\n" +
//                        "Evacuation Reserve: 8M (4 regions), Max regular: 2048K\n" +
//                        "Pacer for Idle. Initial: 2M, Alloc Tax Rate: 1.0x\n" +
//                        "1.115: [Concurrent uncommit, start]\n" +
//                        "1.129: [Concurrent uncommit 1986K->1986K(10240K), 13.524 ms]\n")
//                        .getBytes());
//
//        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_8);
//        GCModel model = reader.read();
//
//        assertThat("gc count", model.size(), is(7));
//        assertThat("warnings", handler.getCount(), is(0));
//
//        AbstractGCEvent<?> concurrentMarking = model.get(0);
//        assertThat("Concurrent reset: name", concurrentMarking.getTypeAsString(), is("Concurrent reset"));
//        assertThat("Concurrent reset: duration", concurrentMarking.getPause(), closeTo(0.000401, 0.0000001));
//        assertThat("Concurrent reset: before", concurrentMarking.getPreUsed(), is(32854));
//        assertThat("Concurrent reset: after", concurrentMarking.getPostUsed(), is(32983));
//        assertThat("Concurrent reset: total", concurrentMarking.getTotal(), is(34816));
//    }

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

// FIX ME!  Only "Concurrent cleanup", "Concurrent uncommit", "Pause Degenerated GC", "Pause Full" event types have memory info
//    @Test
//    public void shenandoah_171_Beginning()  throws Exception {
//        // in its early implementation (jdk8u171), Shenandoah had memory information in the "Pause Final Mark" event, which was dropped later (jdk8u232)
//        TestLogHandler handler = new TestLogHandler();
//        handler.setLevel(Level.INFO);
//        GCResource gcResource = new GcResourceFile("SampleOpenJdk1_8_0-171-ShenandoahBeginning.txt");
//        gcResource.getLogger().addHandler(handler);
//
//        DataReader reader = getDataReader(gcResource);
//        GCModel model = reader.read();
//
//        assertThat("gc count", model.size(), is(3));
//        assertThat("number of errors",
//                handler.getLogRecords().stream().filter(logRecord -> !logRecord.getLevel().equals(Level.INFO)).count(),
//                is(2L));
//        assertThat("contains 'Initialize Shenandoah heap'",
//                handler.getLogRecords().stream().filter(logRecord -> logRecord.getMessage().startsWith("Initialize Shenandoah heap")).count(),
//                is(1L));
//    }
//
//    @Test
//    public void shenandoah_232_Beginning()  throws Exception {
//        TestLogHandler handler = new TestLogHandler();
//        handler.setLevel(Level.INFO);
//        GCResource gcResource = new GcResourceFile("SampleOpenJdk1_8_0-232-ShenandoahBeginning.txt");
//        gcResource.getLogger().addHandler(handler);
//
//        DataReader reader = getDataReader(gcResource);
//        GCModel model = reader.read();
//
//        assertThat("gc count", model.size(), is(0));
//        assertThat("number of errors",
//                handler.getLogRecords().stream().filter(logRecord -> !logRecord.getLevel().equals(Level.INFO)).count(),
//                is(0L));
//        assertThat("contains 'Shenandoah heuristics'",
//                handler.getLogRecords().stream().filter(logRecord -> logRecord.getMessage().startsWith("Shenandoah heuristics")).count(),
//                is(1L));
//    }
 
    @Test
    public void shenandoah_8u332()  throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.INFO);
        GCResource gcResource = new GcResourceFile("SampleOpenJdk1_8_0-332.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("number of errors",
                handler.getLogRecords().stream().filter(logRecord -> !logRecord.getLevel().equals(Level.INFO)).count(),
                is(0L));
        assertThat("number of warnings",
                handler.getLogRecords().stream().filter(logRecord -> logRecord.getLevel().equals(Level.WARNING)).count(),
                is(0L));
        assertThat("contains 'Shenandoah GC mode'",
                handler.getLogRecords().stream().filter(logRecord -> logRecord.getMessage().startsWith("Shenandoah heuristics")).count(),
                is(1L));
        assertThat("contains 'Soft Max Heap Size'",
                handler.getLogRecords().stream().filter(logRecord -> logRecord.getMessage().startsWith("Shenandoah heuristics")).count(),
                is(1L));
        assertThat("contains GC STATISTICS",
                handler.getLogRecords().stream().filter(logRecord -> logRecord.getMessage().startsWith("GC STATISTICS")).count(),
                is(1L));

        assertThat("gc count", model.size(), is(20));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(7));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(0));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(7));
        assertThat("total pause time", model.getPause().getSum(), closeTo(0.197536, 0.000001));
        assertThat("total heap", model.getHeapAllocatedSizes().getMax(), is(3966 * 1024));
    }

    @Test
    public void shenandoahConcurrentReset() throws Exception {
        String logString = "Trigger: Learning 1 of 5. Free (2766M) is below initial threshold (2766M)\n" +
                "Free: 2767M, Max: 1024K regular, 2767M humongous, Frag: 0% external, 0% internal; Reserve: 198M, Max: 1024K\n" +
                "2022-08-13T14:42:27.131+0800: 1.011: [Concurrent reset, start]\n" +
                "    Using 3 of 6 workers for concurrent reset\n" +
                "    Pacer for Reset. Non-Taxable: 3953M\n" +
                "2022-08-13T14:42:27.134+0800: 1.014: [Concurrent reset, 3.204 ms]";

        GCModel model = getGCModelFromLogString(logString);

        assertThat("number of events", model.size(), is(1));
        assertThat("event type as string", model.get(0).getTypeAsString(), is("Concurrent reset"));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_SHEN_CONCURRENT_RESET));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.003204, 0.0000001));
        assertThat("datestamp", model.get(0).getDatestamp(), is(DateHelper.parseDate("2022-08-13T14:42:27.134+0800")));
    }

    @Test
    public void shenandoahPauseInitMark() throws Exception {
        String logString = "2022-08-13T14:42:27.135+0800: 1.015: [Pause Init Mark (process weakrefs), start]\n" +
                "    Using 6 of 6 workers for init marking\n" +
                "    Pacer for Mark. Expected Live: 395M, Free: 2762M, Non-Taxable: 276M, Alloc Tax Rate: 0.2x\n" +
                "2022-08-13T14:42:27.137+0800: 1.016: [Pause Init Mark (process weakrefs), 1.868 ms]";

        GCModel model = getGCModelFromLogString(logString);

        assertThat("number of events", model.size(), is(1));
        assertThat("event type as string", model.get(0).getTypeAsString(), is("Pause Init Mark (process weakrefs)"));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_SHEN_PAUSE_INIT_MARK));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.001868, 0.0000001));
        assertThat("is initial mark", model.get(0).isInitialMark(), is(true));
    }

    @Test
    public void shenandoahConcurrentMarking() throws Exception {
        String logString = "2022-08-13T14:42:27.137+0800: 1.017: [Concurrent marking (process weakrefs), start]\n" +
                "    Using 3 of 6 workers for concurrent marking\n" +
                "2022-08-13T14:42:27.175+0800: 1.055: [Concurrent marking (process weakrefs), 38.059 ms]";

        GCModel model = getGCModelFromLogString(logString);

        assertThat("number of events", model.size(), is(1));
        assertThat("event type as string", model.get(0).getTypeAsString(), is("Concurrent marking (process weakrefs)"));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_SHEN_CONCURRENT_MARKING));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.038059, 0.0000001));
    }

    @Test
    public void shenandoahConcurrentPrecleaning() throws Exception {
        String logString = "2022-08-13T14:42:27.175+0800: 1.055: [Concurrent precleaning, start]\n" +
                "    Using 1 of 6 workers for concurrent preclean\n" +
                "    Pacer for Precleaning. Non-Taxable: 3953M\n" +
                "2022-08-13T14:42:27.175+0800: 1.055: [Concurrent precleaning, 0.237 ms]";

        GCModel model = getGCModelFromLogString(logString);

        assertThat("number of events", model.size(), is(1));
        assertThat("event type as string", model.get(0).getTypeAsString(), is("Concurrent precleaning"));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_SHEN_CONCURRENT_PRECLEANING));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.000237, 0.0000001));
    }

    @Test
    public void shenandoahPauseFinalMark() throws Exception {
        String logString = "2022-08-17T16:20:48.556+0800: 3.340: [Pause Final Mark (process weakrefs), start]\n" +
                "    Using 2 of 2 workers for final marking\n" +
                "    Adaptive CSet Selection. Target Free: 565M, Actual Free: 2610M, Max CSet: 166M, Min Garbage: 0B\n" +
                "    Collectable Garbage: 442M (87%), Immediate: 254M (50%), CSet: 188M (37%)\n" +
                "    Pacer for Evacuation. Used CSet: 279M, Free: 2416M, Non-Taxable: 241M, Alloc Tax Rate: 1.1x\n" +
                "2022-08-17T16:20:48.561+0800: 3.344: [Pause Final Mark (process weakrefs), 4.423 ms]";

        GCModel model = getGCModelFromLogString(logString);

        assertThat("number of events", model.size(), is(1));
        assertThat("event type as string", model.get(0).getTypeAsString(), is("Pause Final Mark (process weakrefs)"));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_SHEN_PAUSE_FINAL_MARK));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.004423, 0.0000001));
        assertThat("is final mark", model.get(0).isRemark(), is(true));
    }

    @Test
    public void shenandoahConcurrentCleanup_332() throws Exception {
        String logString = "2022-08-13T14:42:27.177+0800: 1.057: [Concurrent cleanup, start]\n" +
                "2022-08-13T14:42:27.280+0800: 1.160: [Concurrent cleanup 934M->151M(1036M), 102.802 ms]";

        GCModel model = getGCModelFromLogString(logString);

        assertThat("number of events", model.size(), is(1));
        assertThat("event type as string", model.get(0).getTypeAsString(), is("Concurrent cleanup"));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_SHEN_CONCURRENT_CLEANUP));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.102802, 0.0000001));
        assertThat("event preUsed", model.get(0).getPreUsed(), is(934 * 1024));
        assertThat("event total", model.get(0).getTotal(), is(1036 * 1024));
        assertThat("is concurrent collection end", model.get(0).isConcurrentCollectionEnd(), is(true));
    }

    @Test
    public void shenandoahConcurrentEvacuation() throws Exception {
        String logString = "2022-08-17T16:06:31.518+0800: 3.349: [Concurrent evacuation, start]\n" +
                "    Using 3 of 6 workers for concurrent evacuation\n" +
                "2022-08-17T16:06:31.564+0800: 3.395: [Concurrent evacuation, 46.014 ms]";

        GCModel model = getGCModelFromLogString(logString);

        assertThat("number of events", model.size(), is(1));
        assertThat("event type as string", model.get(0).getTypeAsString(), is("Concurrent evacuation"));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_SHEN_CONCURRENT_EVACUATION));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.046014, 0.0000001));
    }

    @Test
    public void shenandoahPauseInitUpdateRefs() throws Exception {
        String logString = "2022-08-17T16:06:31.564+0800: 3.395: [Pause Init Update Refs, start]\n" +
                "    Pacer for Update Refs. Used: 1412M, Free: 2437M, Non-Taxable: 243M, Alloc Tax Rate: 1.1x\n" +
                "2022-08-17T16:06:31.564+0800: 3.395: [Pause Init Update Refs, 0.020 ms]";

        GCModel model = getGCModelFromLogString(logString);

        assertThat("number of events", model.size(), is(1));
        assertThat("event type as string", model.get(0).getTypeAsString(), is("Pause Init Update Refs"));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_SHEN_PAUSE_INIT_UPDATE_REFS));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.000020, 0.0000001));
    }

    @Test
    public void shenandoahConcurrentUpdateRefs() throws Exception {
        String logString = "2022-08-17T16:25:39.399+0800: 294.183: [Concurrent update references, start] \n" + 
                "    Using 1 of 2 workers for concurrent reference update\n" + 
                "    Failed to allocate TLAB, 128K\n" + 
                "    Cancelling GC: Allocation Failure\n" + 
                "2022-08-17T16:25:40.250+0800: 295.033: [Concurrent update references, 850.437 ms]";

        GCModel model = getGCModelFromLogString(logString);

        assertThat("number of events", model.size(), is(1));
        assertThat("event type as string", model.get(0).getTypeAsString(), is("Concurrent update references"));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_SHEN_CONCURRENT_UPDATE_REFS));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.850437, 0.0000001));
    }

    @Test
    public void shenandoahPauseFinalUpdateRefs() throws Exception {
        String logString = "2022-08-17T16:06:31.780+0800: 3.612: [Pause Final Update Refs, start]\n" +
                "    Using 6 of 6 workers for final reference update\n" +
                "2022-08-17T16:06:31.781+0800: 3.613: [Pause Final Update Refs, 0.711 ms]";

        GCModel model = getGCModelFromLogString(logString);

        assertThat("number of events", model.size(), is(1));
        assertThat("event type as string", model.get(0).getTypeAsString(), is("Pause Final Update Refs"));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_SHEN_PAUSE_FINAL_UPDATE_REFS));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.000711, 0.0000001));
    }

    @Test
    public void shenandoahConcurrentUncommit() throws Exception {
        String logString = "2022-08-17T16:06:30.746+0800: 2.578: [Concurrent uncommit, start]\n" +
                "2022-08-17T16:06:30.798+0800: 2.629: [Concurrent uncommit 136M->136M(141M), 51.650 ms]";

        GCModel model = getGCModelFromLogString(logString);

        assertThat("number of events", model.size(), is(1));
        assertThat("event type as string", model.get(0).getTypeAsString(), is("Concurrent uncommit"));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_SHEN_CONCURRENT_UNCOMMIT));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.051650, 0.0000001));
        assertThat("event preUsed", model.get(0).getPreUsed(), is(136 * 1024));
        assertThat("event total", model.get(0).getTotal(), is(141 * 1024));
    }

    @Test
    public void shenandoahPauseDegeneratedGc() throws Exception {
        String logString = "Trigger: Handle Allocation Failure\n" + 
                "Free: 0B, Max: 0B regular, 0B humongous, Frag: 0% external, 0% internal; Reserve: 26368K, Max: 1024K\n" + 
                "2022-08-17T16:25:40.252+0800: 295.036: [Pause Degenerated GC (Update Refs), start]\n" + 
                "    Using 2 of 2 workers for stw degenerated gc\n" + 
                "    Good progress for free space: 2044M, need 40867K\n" + 
                "    Good progress for used space: 2208M, need 1024K\n" + 
                "2022-08-17T16:25:40.436+0800: 295.220: [Pause Degenerated GC (Update Refs) 3941M->1733M(3966M), 184.489 ms]\n" + 
                "Free: 2044M, Max: 1024K regular, 317M humongous, Frag: 85% external, 2% internal; Reserve: 200M, Max: 1024K";

        GCModel model = getGCModelFromLogString(logString);

        assertThat("number of events", model.size(), is(1));
        assertThat("event type as string", model.get(0).getTypeAsString(), is("Pause Degenerated GC (Update Refs)"));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_SHEN_PAUSE_DEGENERATED_GC));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.184489, 0.0000001));
        assertThat("event preUsed", model.get(0).getPreUsed(), is(3941 * 1024));
        assertThat("event postUsed", model.get(0).getPostUsed(), is(1733 * 1024));
        assertThat("event total", model.get(0).getTotal(), is(3966 * 1024));
    }

    @Test
    public void serialPrintGCID() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2022-08-01T17:14:32.660+0000: 0.177: #1: [GC (Allocation Failure) 2022-08-01T17:14:32.661+0000: 0.178: #1: [DefNew: 9766K->1056K(9792K), 0.0057621 secs] 16728K->16694K(31680K), 0.0073601 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] \n" +
                        "2022-08-01T17:14:32.671+0000: 0.188: #2: [GC (Allocation Failure) 2022-08-01T17:14:32.672+0000: 0.189: #2: [DefNew: 9746K->1056K(9792K), 0.0073505 secs]2022-08-01T17:14:32.680+0000: 0.197: #3: [Tenured: 24314K->24378K(24384K), 0.0036336 secs] 25384K->25370K(34176K), [Metaspace: 2712K->2712K(1056768K)], 0.0134215 secs] [Times: user=0.01 sys=0.00, real=0.02 secs] \n")
                        .getBytes());

        DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_8);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(2));
        assertThat("warnings", handler.getCount(), is(0));

        AbstractGCEvent<?> secondEvent = model.get(1);
        assertThat("name", secondEvent.getTypeAsString(), equalTo("GC (Allocation Failure); DefNew; Tenured; Metaspace"));
        assertThat("duration", secondEvent.getPause(), closeTo(0.0134215, 0.0000001));
        assertThat("before", secondEvent.getPreUsed(), is(25384));
    }

    @Test
    public void parallelPrintGCID() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleSun1_8_0ParallelPrintGCID.txt");
        gcResource.getLogger().addHandler(handler);
        
        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(5));

        assertEquals("number of errors", 0, handler.getCount());
    }

    @Test
    public void cmsPrintGCID() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleSun1_8_0CmsPrintGcId.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(18));
        assertThat("warnings", handler.getCount(), is(0));

        AbstractGCEvent<?> parnew = model.get(0);
        assertThat("name", parnew.getTypeAsString(), equalTo("GC (Allocation Failure); ParNew"));
        assertThat("duration", parnew.getPause(), closeTo(0.0106548, 0.0000001));
        assertThat("before", parnew.getPreUsed(), is(8678));

    }

}

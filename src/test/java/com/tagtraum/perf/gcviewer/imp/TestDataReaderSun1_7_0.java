package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Tests for logs generated specifically by jdk 1.7.0.
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 13.09.2013</p>
 */
public class TestDataReaderSun1_7_0 {

    private static final Logger IMP_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.imp");
    private static final Logger DATA_READER_FACTORY_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.DataReaderFactory");

    private final DateTimeFormatter dateTimeFormatter = AbstractDataReaderSun.DATE_TIME_FORMATTER;

    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_OPENJDK, fileName);
    }

    @Test
    public void testPrintGCCause() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                "111.080: [GC (Allocation Failure)111.080: [ParNew: 140365K->605K(157248K), 0.0034070 secs] 190158K->50399K(506816K), 0.0035370 secs] [Times: user=0.02 sys=0.00, real=0.00 secs]"
                       .getBytes());
        final DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("GC pause", 0.0035370, model.getGCPause().getMax(), 0.0000001);
        assertEquals("GC timestamp", 111.080, model.get(0).getTimestamp(), 0.000001);
        assertEquals("GC (Allocation Failure); ParNew", model.get(0).getTypeAsString());
    }

    @Test
    public void testAdaptiveSizePolicyPrintGCCause() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2013-05-25T17:02:46.238+0200: 0.194: [GC (Allocation Failure) AdaptiveSizePolicy::compute_survivor_space_size_and_thresh:  survived: 2720928  promoted: 13416848  overflow: trueAdaptiveSizeStart: 0.205 collection: 1"
                + "\nPSAdaptiveSizePolicy::compute_eden_space_size: costs minor_time: 0.054157 major_cost: 0.000000 mutator_cost: 0.945843 throughput_goal: 0.990000 live_space: 271156384 free_space: 33685504 old_eden_size: 16842752 desired_eden_size: 33685504"
                + "\nAdaptiveSizePolicy::survivor space sizes: collection: 1 (2752512, 2752512) -> (2752512, 2752512)"
                + "\nAdaptiveSizeStop: collection: 1"
                + "\n[PSYoungGen: 16430K->2657K(19136K)] 16430K->15759K(62848K), 0.0109373 secs] [Times: user=0.05 sys=0.02, real=0.02 secs]"
                       ).getBytes());
        final DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_6);
                GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("GC pause", 0.0109373, model.getGCPause().getMax(), 0.0000001);
        assertEquals("GC timestamp", 0.194, model.get(0).getTimestamp(), 0.000001);
        assertEquals("GC (Allocation Failure); PSYoungGen", model.get(0).getTypeAsString());
    }

    @Test
    public void testPrintWithoutUseAdaptiveSizePolicy() throws Exception {
        // -XX:+PrintAdaptiveSizePolicy
        // -XX:-UseAdaptiveSizePolicy
        // -XX:+PrintGCCause

        ByteArrayInputStream in = new ByteArrayInputStream(
                "2013-09-13T17:05:22.809+0200: 0.191: [GC (Allocation Failure) AdaptiveSizePolicy::compute_survivor_space_size_and_thresh:  survived: 2720928  promoted: 13416848  overflow: true[PSYoungGen: 16430K->2657K(19136K)] 16430K->15759K(62848K), 0.0115757 secs] [Times: user=0.05 sys=0.06, real=0.02 secs]"
                        .getBytes());

        final DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("GC pause", 0.0115757, model.getGCPause().getMax(), 0.00000001);
    }

    @Test
    public void testPrintWithoutUseAdaptiveSizePolicyFullGc() throws Exception {
        // -XX:+PrintAdaptiveSizePolicy
        // -XX:-UseAdaptiveSizePolicy
        // -XX:+PrintGCCause

        ByteArrayInputStream in = new ByteArrayInputStream(
                "2013-09-13T17:05:22.879+0200: 0.256: [Full GC (Ergonomics) [PSYoungGen: 16438K->16424K(19136K)] [ParOldGen: 43690K->43690K(43712K)] 60128K->60114K(62848K), [Metaspace: 2595K->4490K(110592K)], 0.0087315 secs] [Times: user=0.06 sys=0.00, real=0.01 secs]"
                        .getBytes());

        final DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("GC pause", 0.0087315, model.getFullGCPause().getMax(), 0.00000001);
    }

    @Test
    public void cmsRemarkWithTimestamps() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                "2013-09-11T23:03:44.987+0200: 1518.733: [GC[YG occupancy: 3247177 K (4718592 K)]2013-09-11T23:03:45.231+0200: 1518.977: [Rescan (parallel) , 0.0941360 secs]2013-09-11T23:03:45.325+0200: 1519.071: [weak refs processing, 0.0006010 secs]2013-09-11T23:03:45.325+0200: 1519.071: [scrub string table, 0.0028480 secs] [1 CMS-remark: 4246484K(8388608K)] 4557930K(13107200K), 0.3410220 secs] [Times: user=2.48 sys=0.01, real=0.34 secs]"
                        .getBytes());

        final DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("type name", "GC; CMS-remark", model.get(0).getTypeAsString());
        assertEquals("GC pause", 0.3410220, model.getPause().getMax(), 0.00000001);
    }

    @Test
    public void cmsWithoutTimestamps() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                "2013-12-19T17:52:49.323+0100: [GC2013-12-19T17:52:49.323+0100: [ParNew: 4872K->480K(4928K), 0.0031563 secs] 102791K->102785K(140892K), 0.0032042 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]"
                        .getBytes());

        final DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("type name", "GC; ParNew", model.get(0).getTypeAsString());
        assertEquals("GC pause", 0.0032042, model.getPause().getMax(), 0.00000001);
    }

    /**
     * Test output of -XX:+PrintAdaptiveSizePolicy -XX:+UseAdaptiveSizePolicy -XX:+PrintReferenceGC
     */
    @Test
    public void parallelPrintUseAdaptiveSizeReference() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        final InputStream in = getInputStream("SampleSun1_7_0ParallelAdaptiveSizeReference.txt");
        final DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("count", model.size(), is(1));
        GCEvent event = (GCEvent) model.get(0);
        assertThat("type name", event.getTypeAsString(), equalTo("GC (Allocation Failure); PSYoungGen"));
        assertThat("gc pause", event.getPause(), closeTo(0.0134562, 0.00000001));
        assertThat("error count", handler.getCount(), is(0));
    }

    /**
     * Test output of -XX:+PrintAdaptiveSizePolicy -XX:-UseAdaptiveSizePolicy -XX:+PrintReferenceGC
     */
    @Test
    public void parallelAdaptiveReference() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                "2013-10-13T09:54:00.664+0200: 0.180: [GC (Allocation Failure)2013-10-13T09:54:00.680+0200: 0.191: [SoftReference, 0 refs, 0.0001032 secs]2013-10-13T09:54:00.680+0200: 0.192: [WeakReference, 5 refs, 0.0000311 secs]2013-10-13T09:54:00.680+0200: 0.192: [FinalReference, 10 refs, 0.0000389 secs]2013-10-13T09:54:00.680+0200: 0.192: [PhantomReference, 0 refs, 0.0000283 secs]2013-10-13T09:54:00.680+0200: 0.192: [JNI Weak Reference, 0.0000340 secs]AdaptiveSizePolicy::compute_survivor_space_size_and_thresh:  survived: 2589792  promoted: 13949568  overflow: true [PSYoungGen: 16865K->2529K(19456K)] 16865K->16151K(62976K), 0.0117505 secs] [Times: user=0.00 sys=0.06, real=0.02 secs]"
                        .getBytes());

        final DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("GC count", model.size(), is(1));
        assertThat("type name", model.get(0).getTypeAsString(), equalTo("GC (Allocation Failure); PSYoungGen"));
        assertThat("GC pause", model.getPause().getMax(), closeTo(0.0117505, 0.00000001));
    }

    /**
     * Test output of -XX:-PrintAdaptiveSizePolicy -XX:-UseAdaptiveSizePolicy -XX:+PrintReferenceGC
     */
    @Test
    public void parallelReference() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                "2013-10-13T10:32:07.669+0200: 0.182: [GC (Allocation Failure)2013-10-13T10:32:07.685+0200: 0.195: [SoftReference, 0 refs, 0.0001086 secs]2013-10-13T10:32:07.685+0200: 0.195: [WeakReference, 5 refs, 0.0000311 secs]2013-10-13T10:32:07.685+0200: 0.195: [FinalReference, 10 refs, 0.0000377 secs]2013-10-13T10:32:07.685+0200: 0.195: [PhantomReference, 0 refs, 0.0000283 secs]2013-10-13T10:32:07.685+0200: 0.195: [JNI Weak Reference, 0.0000328 secs] [PSYoungGen: 16865K->2529K(19456K)] 16865K->16151K(62976K), 0.0137921 secs] [Times: user=0.03 sys=0.02, real=0.02 secs]"
                        .getBytes());

        final DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("GC count", model.size(), is(1));
        assertThat("type name", model.get(0).getTypeAsString(), equalTo("GC (Allocation Failure); PSYoungGen"));
        assertThat("GC pause", model.getPause().getMax(), closeTo(0.0137921, 0.00000001));
    }

    @Test
    public void serialPrintReferenceGC() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                "2013-10-13T09:52:30.164+0200: 0.189: [GC (Allocation Failure)2013-10-13T09:52:30.164+0200: 0.189: [DefNew2013-10-13T09:52:30.180+0200: 0.205: [SoftReference, 0 refs, 0.0001004 secs]2013-10-13T09:52:30.180+0200: 0.205: [WeakReference, 0 refs, 0.0000287 secs]2013-10-13T09:52:30.180+0200: 0.205: [FinalReference, 0 refs, 0.0000283 secs]2013-10-13T09:52:30.180+0200: 0.205: [PhantomReference, 0 refs, 0.0000279 secs]2013-10-13T09:52:30.180+0200: 0.205: [JNI Weak Reference, 0.0000332 secs]: 17472K->2176K(19648K), 0.0159181 secs] 17472K->16957K(63360K), 0.0161033 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]"
                        .getBytes());

        final DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("GC count", model.size(), is(1));
        assertThat("type name", model.get(0).getTypeAsString(), equalTo("GC (Allocation Failure); DefNew"));
        assertThat("GC pause", model.getPause().getMax(), closeTo(0.0161033, 0.00000001));
        assertThat("heap size", model.getHeapAllocatedSizes().getMax(), is(63360));
        assertThat("young size", model.getYoungAllocatedSizes().getMax(), is(19648));
    }

    @Test
    public void printCMSInitiationStatistics() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2013-11-17T15:09:50.633+0100: 0.687: [GC2013-11-17T15:09:50.633+0100: 0.687: [ParNew: 21468K->2347K(21504K), 0.0037203 secs] 72759K->58184K(140992K), 0.0038018 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]"
                        + "\nCMSCollector shouldConcurrentCollect: 0.691"
                        + "\ntime_until_cms_gen_full 0.0483719"
                        + "\nfree=65178472"
                        + "\ncontiguous_available=27934992"
                        + "\npromotion_rate=8.02999e+008"
                        + "\ncms_allocation_rate=0"
                        + "\noccupancy=0.4673034"
                        + "\ninitiatingOccupancy=0.9200000"
                        + "\ninitiatingPermOccupancy=0.9200000"
                        + "\n2013-11-17T15:09:50.633+0100: 0.691: [GC [1 CMS-initial-mark: 55837K(119488K)] 58378K(140992K), 0.0001819 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]")
                        .getBytes());

        final DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("GC count", model.size(), is(2));
        Iterator<GCEvent> eventIterator = model.getGCEvents();
        GCEvent event1 = eventIterator.next();
        GCEvent event2 = eventIterator.next();
        assertThat("type name [1]", event1.getTypeAsString(), equalTo("GC; ParNew"));
        assertThat("GC pause [1]", event1.getPause(), closeTo(0.0038018, 0.00000001));
        assertThat("type name [2]", event2.getTypeAsString(), equalTo("GC; CMS-initial-mark"));
        assertThat("GC pause [2]", event2.getPause(), closeTo(0.0001819, 0.00000001));

        assertThat("number of parse problems", handler.getCount(), is(0));
    }

    @Test
    public void printTenuringDistributionCMSInitiationStatistics() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        final InputStream in = getInputStream("SampleSun1_7_0CMSTenuringDistributionInitiationStatistics.txt");
        final DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("GC count", model.size(), is(1));
        assertThat("type name", model.get(0).getTypeAsString(), equalTo("GC; ParNew (promotion failed); CMS; CMS Perm"));
        assertThat("GC pause", model.getPause().getMax(), closeTo(21.5649460, 0.00000001));

        assertThat("number of parse problems", handler.getCount(), is(0));
    }

    @Test
    public void parallelPrintGCApplicationStoppedTime() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2014-04-08T22:04:36.018+0200: 0.254: Application time: 0.1310290 seconds"
                 + "\n2014-04-08T22:04:36.018+0200: 0.254: [GC [PSYoungGen: 16865K->2529K(19456K)] 16865K->16175K(62976K), 0.0114994 secs] [Times: user=0.06 sys=0.00, real=0.01 secs]"
                 + "\n2014-04-08T22:04:36.030+0200: 0.266: Total time for which application threads were stopped: 0.0117633 seconds")
                        .getBytes());

        final DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("GC count", model.size(), is(2));
        assertThat("type name (0)", model.get(0).getTypeAsString(), equalTo("GC; PSYoungGen"));
        assertThat("GC pause (0)", model.get(0).getPause(), closeTo(0.0114994, 0.00000001));
        assertThat("type name (1)", model.get(1).getTypeAsString(), equalTo("Total time for which application threads were stopped"));
        assertThat("GC pause (1)", model.get(1).getPause(), closeTo(0.0117633 - 0.0114994, 0.00000001));
        assertThat("timestamp (1)", model.get(1).getTimestamp(), closeTo(0.266, 0.0000001));

        assertThat("total pause", model.getPause().getSum(), closeTo(0.0117633, 0.00000001));
        assertThat("throughput", model.getThroughput(), closeTo(4.081898907, 0.00000001));

        assertThat("number of parse problems", handler.getCount(), is(0));
    }

    @Test
    /**
     * The only difference, between this test and {@link TestDataReaderSun1_8_0#parallelPrintTenuringGcCause()}
     * is the missing space character in the end of the line before the TenuringDistribution information.
     */
    public void parallelPrintTenuringGcCause() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        InputStream in = getInputStream("SampleSun1_7_0Parallel_Tenuring_PrintGCCause.txt");
        DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(2));
        assertThat("gc name", model.get(0).getTypeAsString(), equalTo("GC (Allocation Failure); PSYoungGen"));
        assertThat("pause", model.get(0).getPause(), closeTo(0.0278950, 0.000000001));

        assertEquals("number of errors", 0, handler.getCount());
    }

    @Test
    public void cmsPrintGCApplicationStopped() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        InputStream in = getInputStream("SampleSun1_7_0_51_CMS_PrintApplStoppedTime.txt");
        DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("GC count", model.size(), is(14));
        assertThat("application stopped count", model.getVmOperationPause().getN(), is(2));

        assertThat("type name (0)", model.get(0).getTypeAsString(), equalTo("GC; CMS-initial-mark"));
        assertThat("GC pause (0)", model.get(0).getPause(), closeTo(0.0002081, 0.00000001));
        assertThat("type name (1)", model.get(1).getTypeAsString(), equalTo("Total time for which application threads were stopped"));
        assertThat("GC pause (1)", model.get(1).getPause(), closeTo(0.0003502 - 0.0002081, 0.00000001));

        assertThat("total pause", model.getPause().getSum(), closeTo(0.0015562, 0.0000001));
        assertThat("throughput", model.getThroughput(), closeTo(89.731645035, 0.00001));

        assertThat("number of parse problems", handler.getCount(), is(0));
    }

    @Test
    public void cmsPrintGCApplicationStoppedTimeTenuringDist() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        InputStream in = getInputStream("SampleSun1_7_0_51_CMS_PrintApplStoppedTime_TenuringDist.txt");
        DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("GC count", model.size(), is(19));
        assertThat("application stopped count", model.getVmOperationPause().getN(), is(4));

        assertThat("type name (0)", model.get(0).getTypeAsString(), equalTo("GC; ParNew"));
        assertThat("GC pause (0)", model.get(0).getPause(), closeTo(0.0318639, 0.00000001));

        assertThat("type name (1)", model.get(1).getTypeAsString(), equalTo("Total time for which application threads were stopped"));
        assertThat("GC pause (1)", model.get(1).getPause(), closeTo(0.0320233 - 0.0318639, 0.00000001));

        assertThat("total pause", model.getPause().getSum(), closeTo(0.0488497, 0.00000001));
        assertThat("throughput", model.getThroughput(), closeTo(29.66965410503, 0.00000000001));

        assertThat("number of parse problems", handler.getCount(), is(0));

        assertThat("post concurrent cycle tenured size", model.getPostConcurrentCycleTenuredUsedSizes().getMax(), is(84508 - 42951));
        assertThat("post concurrent cycle size", model.getPostConcurrentCycleHeapUsedSizes().getMax(), is(84508));
    }

    /**
     * Pre 1.7.0_u50 -XX:+PrintGCApplicationStoppedTime wrote it's data without a timestamp.
     * Test here, that they are still added to the {@link GCModel}.
     */
    @Test
    public void cmsPrintGCApplicationStoppedTimeWithoutTimestampsTenuringDist() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("2012-04-26T23:59:50.899+0400: 33395.153: [GC 33395.153: [ParNew"
                 + "\nDesired survivor size 32768 bytes, new threshold 0 (max 0)"
                 + "\n: 130944K->0K(131008K), 0.0158820 secs] 1078066K->949934K(4194240K), 0.1120380 secs] [Times: user=0.07 sys=0.00, real=0.02 secs]"
                 + "\nTotal time for which application threads were stopped: 0.2174010 seconds")
                        .getBytes());

        final DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("GC count", model.size(), is(2));
        assertThat("ParNew timestamp", model.get(0).getTimestamp(), closeTo(33395.153, 0.00001));
        assertThat("is 'Total time...'",
                model.get(1).getExtendedType().getName(),
                equalTo(AbstractGCEvent.Type.APPLICATION_STOPPED_TIME.getName()));
        assertThat("Application Stopped timestamp",
                model.get(1).getTimestamp(),
                closeTo(33395.153 + 0.1120380, 0.0000001));
        assertThat("Application Stopped datestamp",
                dateTimeFormatter.format(model.get(1).getDatestamp()),
                equalTo("2012-04-26T23:59:51.011+0400"));
        assertThat("first timestamp", model.getFirstPauseTimeStamp(), closeTo(33395.153, 0.00001));
    }

    @Test
    public void cmsPrintFlsStatistics1() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        InputStream in = getInputStream("SampleSun1_7_0CmsPrintFlsStats1.txt");
        DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("GC count", model.size(), is(3));
        assertThat("event 1 pause", model.get(0).getPause(), closeTo(0.0030039, 0.00000001));
        assertThat("event 2", model.get(1).isConcurrent(), is(true));
        assertThat("event 3", model.get(2).isFull(), is(true));
        assertThat("event 3 pause", model.get(2).getPause(), closeTo(0.0339164, 0.00000001));
        assertThat("number of parse problems", handler.getCount(), is(0));

    }

    @Test
    public void cmsPrintFlsStatistics2() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        InputStream in = getInputStream("SampleSun1_7_0CmsPrintFlsStats2.txt");
        DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("GC count", model.size(), is(5));
        assertThat("event 1 pause", model.get(0).getPause(), closeTo(0.0054252, 0.00000001));
        assertThat("event 2", model.get(1).isConcurrent(), is(true));
        assertThat("event 3", model.get(2).isFull(), is(true));
        assertThat("event 3 pause", model.get(2).getPause(), closeTo(0.0356364, 0.00000001));
        assertThat("event 4", model.get(3).isConcurrent(), is(true));
        assertThat("event 5", model.get(4).isFull(), is(true));
        assertThat("event 5 pause", model.get(4).getPause(), closeTo(0.0264843, 0.00000001));
        assertThat("number of parse problems", handler.getCount(), is(0));

    }

    @Test
    public void cmsPrintFlsStatistics3() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        InputStream in = getInputStream("SampleSun1_7_0CmsPrintFlsStats3.txt");
        DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("GC count", model.size(), is(4));
        assertThat("event 1 pause", model.get(1).getPause(), closeTo(0.0252550, 0.00000001));
        assertThat("event 1 name", model.get(1).getTypeAsString(), equalTo("GC; ParNew"));
        assertThat("number of parse problems", handler.getCount(), is(0));

    }

    @Test
    public void cmsPrintPromotionFailureTenuringDistribution() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        InputStream in = getInputStream("SampleSun1_7_0CMS_PrintTenuringDistributionPromotionFailure.txt");
        DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("GC count", model.size(), is(7));
        assertThat("pause", model.get(4).getPause(), closeTo(129.9468220, 0.00000001));
        assertThat("warning count", handler.getCount(), is(0));
    }

    @Test
    public void psAdaptiveSizePolicyTenuringDistributionApplicationStopped() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        InputStream in = getInputStream("SampleSun1_7_0PS_Adaptive_Tenuring_AppStopped.txt");
        DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("GC count", model.size(), is(3));
        assertThat("pause", model.get(1).getPause(), closeTo(0.0082230, 0.00000001));
        assertThat("warning count", handler.getCount(), is(0));
    }
}

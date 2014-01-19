package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;

/**
 * Tests for logs generated specifically by jdk 1.7.0.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 13.09.2013</p>
 */
public class TestDataReaderSun1_7_0 {

    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_OPENJDK, fileName);
    }
    
    @Test
    public void testPrintGCCause() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                "111.080: [GC (Allocation Failure)111.080: [ParNew: 140365K->605K(157248K), 0.0034070 secs] 190158K->50399K(506816K), 0.0035370 secs] [Times: user=0.02 sys=0.00, real=0.00 secs]"
                       .getBytes());
        final DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
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
        final DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
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
         
        final DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
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
         
        final DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("GC pause", 0.0087315, model.getFullGCPause().getMax(), 0.00000001);
    }
    
    @Test
    public void CmsRemarkWithTimestamps() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(
                "2013-09-11T23:03:44.987+0200: 1518.733: [GC[YG occupancy: 3247177 K (4718592 K)]2013-09-11T23:03:45.231+0200: 1518.977: [Rescan (parallel) , 0.0941360 secs]2013-09-11T23:03:45.325+0200: 1519.071: [weak refs processing, 0.0006010 secs]2013-09-11T23:03:45.325+0200: 1519.071: [scrub string table, 0.0028480 secs] [1 CMS-remark: 4246484K(8388608K)] 4557930K(13107200K), 0.3410220 secs] [Times: user=2.48 sys=0.01, real=0.34 secs]"
                        .getBytes());
         
        final DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();

        assertEquals("GC count", 1, model.size());
        assertEquals("type name", "GC; CMS-remark", model.get(0).getTypeAsString());
        assertEquals("GC pause", 0.3410220, model.getPause().getMax(), 0.00000001);
    }    

    /**
     * Test output of -XX:+PrintAdaptiveSizePolicy -XX:+UseAdaptiveSizePolicy -XX:+PrintReferenceGC
     */
    @Test
    public void parallelPrintUseAdaptiveSizeReference() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("SampleSun1_7_0ParallelAdaptiveSizeReference.txt");
        gcResource.getLogger().addHandler(handler);
        
        final InputStream in = getInputStream(gcResource.getResourceName());
        final DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_7);

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
         
        final DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_7);
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
         
        final DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_7);
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
         
        final DataReader reader = new DataReaderSun1_6_0(new GCResource("byteArray"), in, GcLogType.SUN1_7);
        GCModel model = reader.read();

        assertThat("GC count", model.size(), is(1));
        assertThat("type name", model.get(0).getTypeAsString(), equalTo("GC (Allocation Failure); DefNew"));
        assertThat("GC pause", model.getPause().getMax(), closeTo(0.0161033, 0.00000001));
        assertThat("heap size", model.getHeapAllocatedSizes().getMax(), is(63360));
        assertThat("young size", model.getYoungAllocatedSizes().getMax(), is(19648));
    }    

     
    
        
}

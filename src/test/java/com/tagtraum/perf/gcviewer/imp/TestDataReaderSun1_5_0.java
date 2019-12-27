package com.tagtraum.perf.gcviewer.imp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests some cases for java 1.5 (using DataReaderSun1_6_0).
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
class TestDataReaderSun1_5_0 {

    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(FOLDER.OPENJDK, fileName);
    }
    
    /**
     * Test output for -XX:+PrintAdaptiveSizePolicy 
     */
    @Test
    void testAdaptiveSizePolicy() throws Exception {
        String fileName = "SampleSun1_5_0AdaptiveSizePolicy.txt";
        final InputStream in = getInputStream(fileName);
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile(fileName), in, GcLogType.SUN1_5);
        GCModel model = reader.read();
        
        assertEquals(6, model.getPause().getN(), "number of events");
        assertEquals(1, model.getFullGCPause().getN(), "number of full gcs");
        assertEquals(5, model.getGCPause().getN(), "number of gcs");
        assertEquals(0.1024222, model.getPause().getSum(), 0.000001, "total pause");
        assertEquals(0.0583435, model.getFullGCPause().getSum(), 0.000001, "full gc pause");
        assertEquals(0.0440787, model.getGCPause().getSum(), 0.000001, "gc pause");
    }
    
    @Test
    void testCMSPrintGCDetails() throws Exception {
        String fileName = "SampleSun1_5_0CMS_PrintGCDetails.txt";
        final InputStream in = getInputStream(fileName);
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile(fileName), in, GcLogType.SUN1_5);
        GCModel model = reader.read();
        
        assertEquals(515, model.size(), "size");
        assertEquals(88.2823289184, model.getThroughput(), 0.00000001, "throughput");
        assertEquals(model.getPause().getSum(), model.getFullGCPause().getSum() + model.getGCPause().getSum(), 0.0000001, "sum of pauses");
        assertEquals(9.1337492, model.getPause().getSum(), 0.0000001, "total pause");
        assertEquals(7.4672903, model.getFullGCPause().getSum(), 0.00000001, "full gc pause");
    }

    @Test
    void testParallelOldGC() throws Exception {
        String fileName = "SampleSun1_5_0ParallelOldGC.txt";
        final InputStream in = getInputStream(fileName);
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile(fileName), in, GcLogType.SUN1_5);
        GCModel model = reader.read();
        
        assertEquals(1, model.size(), "size");
        assertEquals(27.0696262, model.getFullGCPause().getMax(), 0.000001, "gc pause");
    }

    @Test
    void testCMSIncrementalPacing() throws Exception {
        String fileName = "SampleSun1_5_0CMS_IncrementalPacing.txt";
        final InputStream in = getInputStream(fileName);
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile(fileName), in, GcLogType.SUN1_5);
        GCModel model = reader.read();

        assertEquals(810, model.size(), "size");
        assertEquals(94.181240109114, model.getThroughput(), 0.00000001, "throughput");
        assertEquals(2.3410947, model.getPause().getSum(), 0.000000001, "total gc pause");
        assertEquals(2.3410947, model.getGCPause().getSum(), 0.000000001, "gc pause");
        assertEquals(0.0, model.getFullGCPause().getSum(), 0.01, "full gc paus");
    }

    @Test
    void testPromotionFailure() throws Exception {
        String fileName = "SampleSun1_5_0PromotionFailure.txt";
        final InputStream in = getInputStream(fileName);
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile(fileName), in, GcLogType.SUN1_5);
        GCModel model = reader.read();
        
        assertEquals(6, model.size(), "size");
        assertEquals(98.0937624615, model.getThroughput(), 0.00000001, "throughput");
        assertEquals(8.413616, model.getPause().getSum(), 0.000001, "gc pause");
    }

    @Test
    void testCMSConcurrentModeFailure() throws Exception {
        String fileName = "SampleSun1_5_0ConcurrentModeFailure.txt";
        final InputStream in = getInputStream(fileName);
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile(fileName), in, GcLogType.SUN1_5);
        GCModel model = reader.read();
        
        assertEquals(3417, model.size(), "size");
        assertEquals(78.558339113, model.getThroughput(), 0.00000001, "throughput");
        assertEquals(181.8116798, model.getPause().getSum(), 0.000000001, "gc pause");
    }
    
    @Test
    void testCmsScavengeBeforeRemark() throws Exception {
        final ByteArrayInputStream in = new ByteArrayInputStream(
                ("0.871: [GC[YG occupancy: 16241 K (16320 K)]0.871: [Scavenge-Before-Remark0.871: [Full GC 0.871: [ParNew: 16241K->0K(16320K), 0.0311294 secs] 374465K->374455K(524224K), 0.0312110 secs]" +
                 "\n, 0.0312323 secs]" +
                 "\n0.902: [Rescan (parallel) , 0.0310561 secs]0.933: [weak refs processing, 0.0000152 secs] [1 CMS-remark: 374455K(507904K)] 374455K(524224K), 0.0624207 secs]")
                        .getBytes());

        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile("byteArray"), in, GcLogType.SUN1_5);
        GCModel model = reader.read();

        assertEquals(2, model.size(), "gc count");
        assertEquals(0.0312110, model.getFullGCPause().getMax(), 0.000001, "full gc pause");
        assertEquals(0.0624207 - 0.0312110, model.getGCPause().getMax(), 0.000000001, "remark pause");
    }

}

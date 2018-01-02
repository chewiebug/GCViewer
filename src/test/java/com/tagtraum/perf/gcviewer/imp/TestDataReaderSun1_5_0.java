package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Test;

/**
 * Tests some cases for java 1.5 (using DataReaderSun1_6_0).
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestDataReaderSun1_5_0 {

    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(FOLDER.OPENJDK, fileName);
    }
    
    /**
     * Test output for -XX:+PrintAdaptiveSizePolicy 
     */
    @Test
    public void testAdaptiveSizePolicy() throws Exception {
        String fileName = "SampleSun1_5_0AdaptiveSizePolicy.txt";
        final InputStream in = getInputStream(fileName);
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile(fileName), in, GcLogType.SUN1_5);
        GCModel model = reader.read();
        
        assertEquals("number of events", 6, model.getPause().getN());
        assertEquals("number of full gcs", 1, model.getFullGCPause().getN());
        assertEquals("number of gcs", 5, model.getGCPause().getN());
        assertEquals("total pause", 0.1024222, model.getPause().getSum(), 0.000001);
        assertEquals("full gc pause", 0.0583435, model.getFullGCPause().getSum(), 0.000001);
        assertEquals("gc pause", 0.0440787, model.getGCPause().getSum(), 0.000001);
    }
    
    @Test
    public void testCMSPrintGCDetails() throws Exception {
        String fileName = "SampleSun1_5_0CMS_PrintGCDetails.txt";
        final InputStream in = getInputStream(fileName);
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile(fileName), in, GcLogType.SUN1_5);
        GCModel model = reader.read();
        
        assertEquals("size", 515, model.size());
        assertEquals("throughput", 88.2823289184, model.getThroughput(), 0.00000001);
        assertEquals("sum of pauses", model.getPause().getSum(), model.getFullGCPause().getSum() + model.getGCPause().getSum(), 0.0000001);
        assertEquals("total pause", 9.1337492, model.getPause().getSum(), 0.0000001);
        assertEquals("full gc pause", 7.4672903, model.getFullGCPause().getSum(), 0.00000001);
    }

    @Test
    public void testParallelOldGC() throws Exception {
        String fileName = "SampleSun1_5_0ParallelOldGC.txt";
        final InputStream in = getInputStream(fileName);
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile(fileName), in, GcLogType.SUN1_5);
        GCModel model = reader.read();
        
        assertEquals("size", 1, model.size());
        assertEquals("gc pause", 27.0696262, model.getFullGCPause().getMax(), 0.000001);
    }

    @Test
    public void testCMSIncrementalPacing() throws Exception {
        String fileName = "SampleSun1_5_0CMS_IncrementalPacing.txt";
        final InputStream in = getInputStream(fileName);
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile(fileName), in, GcLogType.SUN1_5);
        GCModel model = reader.read();

        assertEquals("size", 810, model.size());
        assertEquals("throughput", 94.181240109114, model.getThroughput(), 0.00000001);
        assertEquals("total gc pause", 2.3410947, model.getPause().getSum(), 0.000000001);
        assertEquals("gc pause", 2.3410947, model.getGCPause().getSum(), 0.000000001);
        assertEquals("full gc paus", 0.0, model.getFullGCPause().getSum(), 0.01);
    }

    @Test
    public void testPromotionFailure() throws Exception {
        String fileName = "SampleSun1_5_0PromotionFailure.txt";
        final InputStream in = getInputStream(fileName);
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile(fileName), in, GcLogType.SUN1_5);
        GCModel model = reader.read();
        
        assertEquals("size", 6, model.size());
        assertEquals("throughput", 98.0937624615, model.getThroughput(), 0.00000001);
        assertEquals("gc pause", 8.413616, model.getPause().getSum(), 0.000001);
    }

    @Test
    public void testCMSConcurrentModeFailure() throws Exception {
        String fileName = "SampleSun1_5_0ConcurrentModeFailure.txt";
        final InputStream in = getInputStream(fileName);
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile(fileName), in, GcLogType.SUN1_5);
        GCModel model = reader.read();
        
        assertEquals("size", 3417, model.size());
        assertEquals("throughput", 78.558339113, model.getThroughput(), 0.00000001);
        assertEquals("gc pause", 181.8116798, model.getPause().getSum(), 0.000000001);
    }
    
    @Test
    public void testCmsScavengeBeforeRemark() throws Exception {
        final ByteArrayInputStream in = new ByteArrayInputStream(
                ("0.871: [GC[YG occupancy: 16241 K (16320 K)]0.871: [Scavenge-Before-Remark0.871: [Full GC 0.871: [ParNew: 16241K->0K(16320K), 0.0311294 secs] 374465K->374455K(524224K), 0.0312110 secs]" +
                 "\n, 0.0312323 secs]" +
                 "\n0.902: [Rescan (parallel) , 0.0310561 secs]0.933: [weak refs processing, 0.0000152 secs] [1 CMS-remark: 374455K(507904K)] 374455K(524224K), 0.0624207 secs]")
                        .getBytes());

        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile("byteArray"), in, GcLogType.SUN1_5);
        GCModel model = reader.read();

        assertEquals("gc count", 2, model.size());
        assertEquals("full gc pause", 0.0312110, model.getFullGCPause().getMax(), 0.000001);
        assertEquals("remark pause", 0.0624207 - 0.0312110, model.getGCPause().getMax(), 0.000000001);
    }

}

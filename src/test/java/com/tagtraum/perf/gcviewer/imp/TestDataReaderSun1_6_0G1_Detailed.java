package com.tagtraum.perf.gcviewer.imp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import com.tagtraum.perf.gcviewer.DataReader;
import com.tagtraum.perf.gcviewer.GCModel;

public class TestDataReaderSun1_6_0G1_Detailed extends TestCase {

    public void testYoungCollection() throws Exception {
        // parse one detailed event
        final InputStream in = getClass().getResourceAsStream("SampleSun1_6_0G1_Detailed-young.txt");
        final DataReader reader = new DataReaderSun1_6_0G1_Detailed(in);
        GCModel model = reader.read();
        
        assertEquals("number of pauses", 1, model.getPause().getN());
        assertEquals("gc pause sum", 0.00594747, model.getPause().getSum(), 0.000000001);
        assertEquals("gc memory", 4096 - 3936, model.getFreedMemoryByGC().getMax());
    }
    
    public void testGcPattern() throws Exception {
        final InputStream in = new ByteArrayInputStream(("0.452: [GC concurrent-count-start]").getBytes());
        
        final DataReader reader = new DataReaderSun1_6_0G1_Detailed(in);
        GCModel model = reader.read();

        assertEquals("count", 1, model.size());
        assertEquals("full gc pause", 0, model.getFullGCPause().getN());
        assertEquals("gc pause", 0, model.getGCPause().getN());
    }

    public void testGcPausePattern() throws Exception {
        final InputStream in = new ByteArrayInputStream(("0.360: [GC concurrent-count-end, 0.0242674]").getBytes());
        
        final DataReader reader = new DataReaderSun1_6_0G1_Detailed(in);
        GCModel model = reader.read();

        assertEquals("count", 1, model.size());
        assertEquals("full gc pause", 0, model.getFullGCPause().getN());
    }

    public void testGcMemoryPausePattern() throws Exception {
        final InputStream in = new ByteArrayInputStream(("0.360: [GC cleanup 19M->19M(36M), 0.0007889 secs]").getBytes());
        
        final DataReader reader = new DataReaderSun1_6_0G1_Detailed(in);
        GCModel model = reader.read();

        assertEquals("count", 1, model.size());
        assertEquals("full gc pause", 0, model.getFullGCPause().getN());
        assertEquals("gc pause", 0.0007889, model.getGCPause().getMax(), 0.0000001);
        assertEquals("memory", 0, model.getFreedMemoryByGC().getMax());
    }

    public void testInitialMark() throws Exception {
        final InputStream in = new ByteArrayInputStream(
                ("0.319: [GC pause (young) (initial-mark), 0.00188271 secs]" +
                		"\n [Times: user=0.00 sys=0.00, real=0.00 secs] ").getBytes());
        
        final DataReader reader = new DataReaderSun1_6_0G1_Detailed(in);
        GCModel model = reader.read();

        assertEquals("count", 1, model.size());
        assertEquals("full gc pause", 0, model.getFullGCPause().getN());
        assertEquals("gc pause", 0.00188271, model.getGCPause().getMax(), 0.0000001);
        assertEquals("memory", 0, model.getFreedMemoryByGC().getMax());
    }

    public void testRemark() throws Exception {
        final InputStream in = new ByteArrayInputStream(
                ("0.334: [GC remark, 0.0009506 secs]" +
                        "\n [Times: user=0.00 sys=0.00, real=0.00 secs] ").getBytes());
        
        final DataReader reader = new DataReaderSun1_6_0G1_Detailed(in);
        GCModel model = reader.read();

        assertEquals("count", 1, model.size());
        assertEquals("full gc pause", 0, model.getFullGCPause().getN());
        assertEquals("gc pause", 0.0009506, model.getGCPause().getMax(), 0.0000001);
        assertEquals("memory", 0, model.getFreedMemoryByGC().getMax());
    }

          

}

package com.tagtraum.perf.gcviewer.imp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.tagtraum.perf.gcviewer.DataReader;
import com.tagtraum.perf.gcviewer.GCModel;

public class TestDataReaderSun1_6_0G1 extends TestCase {
    private static final Logger IMP_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.imp");
    private static final Logger DATA_READER_FACTORY_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.DataReaderFactory");

    /**
     * Test G1 parser with a gc verbose file (not -XX:+PrintGCDetails)
     */
    public void testG1GcVerbose() throws Exception {
    	TestLogHandler handler = new TestLogHandler();
    	IMP_LOGGER.addHandler(handler);
    	DATA_READER_FACTORY_LOGGER.addHandler(handler);
    	
        final InputStream in = getClass().getResourceAsStream("SampleSun1_6_0G1_gc_verbose.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();
        
        assertEquals("gc pause sum", 62.5911287, model.getPause().getSum(), 0.000000001);
        assertEquals("throughput", 47.93401098, model.getThroughput(), 0.000000001);
        assertEquals("longest pause", 0.1581177, model.getPause().getMax(), 0.000001);
        assertEquals("total runtime", 120.215, model.getRunningTime(), 0.000001);
        assertEquals("number of errors", 6, handler.getCount());
        
        assertEquals("max interval", 0.211, model.getPauseInterval().getMax(), 0.000001);
        assertEquals("avg interval", 0.048291297, model.getPauseInterval().average(), 0.0000001);
    }
    
    public void testG1FullGcSystemGc() throws Exception {
    	final InputStream in = new ByteArrayInputStream(
				("9.978: [Full GC (System.gc()) 597M->1142K(7168K), 0.1604955 secs]")
				.getBytes());
    	
		final DataReader reader = new DataReaderSun1_6_0G1(in);
		GCModel model = reader.read();

		assertEquals("count", 1, model.size());
		assertEquals("full gc pause", 0.1604955, model.getFullGCPause().getMax(), 0.000001);

    }
    
    public void testG1MixedLine() throws Exception {
        final InputStream in = new ByteArrayInputStream(
                ("0.388: [GC pause (young) (initial-mark) 10080K->10080K(16M)0.390: [GC concurrent-mark-start]" +
                		"\n, 0.0013065 secs]")
                .getBytes());
        
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();

        assertEquals("count", 2, model.size());
        assertEquals("gc pause", 0.0013065, model.getGCPause().getMax(), 0.000001);
    }

    public void testDetailedYoungCollection() throws Exception {
        // parse one detailed event
        final InputStream in = getClass().getResourceAsStream("SampleSun1_6_0G1_Detailed-young.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();
        
        assertEquals("number of pauses", 1, model.getPause().getN());
        assertEquals("gc pause sum", 0.00594747, model.getPause().getSum(), 0.000000001);
        assertEquals("gc memory", 4096 - 3936, model.getFreedMemoryByGC().getMax());
    }
    
    public void testDetailedYoungCollectionMixed() throws Exception {
        // parse one detailed event with a mixed line (concurrent event starts in the middle of an stw collection)
        final InputStream in = getClass().getResourceAsStream("SampleSun1_6_0G1_Detailed-young-mixedLine.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();
        
        assertEquals("nummber of events", 2, model.size());
        assertEquals("number of pauses", 1, model.getPause().getN());
        assertEquals("gc pause sum", 0.00831998, model.getPause().getSum(), 0.000000001);
        assertEquals("gc memory", 169*1024 - 162*1024, model.getFreedMemoryByGC().getMax());
    }
    
    public void testFullGcMixed() throws Exception {
        final InputStream in = new ByteArrayInputStream(("107.222: [Full GC107.248: [GC concurrent-mark-end, 0.0437048 sec]" +
        		"\n 254M->59M(199M), 0.0687356 secs]" +
        		"\n [Times: user=0.06 sys=0.02, real=0.07 secs]").getBytes());
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();

        assertEquals("number of events", 2, model.size());
        assertEquals("number of full gc pauses", 1, model.getFullGCPause().getN());
        assertEquals("full gc pause sum", 0.0687356, model.getFullGCPause().getSum(), 0.0000001);
    }
    
    public void testYoungToSpaceOverflow() throws Exception {
        // special type of GC: 0.838: "[GC pause (young) (to-space overflow)..."
        final InputStream in = getClass().getResourceAsStream("SampleSun1_6_0G1_young_toSpaceOverflow.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();
        
        assertEquals("nummber of events", 1, model.size());
        assertEquals("number of pauses", 1, model.getPause().getN());
        assertEquals("gc pause sum", 0.04674512, model.getPause().getSum(), 0.000000001);
        assertEquals("gc memory", 228*1024 - 102*1024, model.getFreedMemoryByGC().getMax());
        assertEquals("max memory", 256*1024, model.getFootprint());
    }
    
    public void testPartialToSpaceOverflow() throws Exception {
        // special type of GC: 0.838: "[GC pause (young) (to-space overflow)..."
        final InputStream in = getClass().getResourceAsStream("SampleSun1_6_0G1_partial_toSpaceOverflow.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();
        
        assertEquals("nummber of events", 1, model.size());
        assertEquals("number of pauses", 1, model.getPause().getN());
        assertEquals("gc pause sum", 0.00271976, model.getPause().getSum(), 0.000000001);
        assertEquals("gc memory", 255*1024 - 181*1024, model.getFreedMemoryByGC().getMax());
        assertEquals("max memory", 256*1024, model.getFootprint());
    }
    
    public void testYoungToSpaceOverflowInitialMark() throws Exception {
        // special type of GC: 0.838: "[GC pause (young) (to-space overflow) (initial-mark)..."
        final InputStream in = getClass().getResourceAsStream("SampleSun1_6_0G1_young_initialMarkToSpaceOverflow.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();
        
        assertEquals("nummber of events", 1, model.size());
        assertEquals("number of pauses", 1, model.getPause().getN());
        assertEquals("gc pause sum", 0.00316185, model.getPause().getSum(), 0.000000001);
        assertEquals("gc memory", 254*1024 - 170*1024, model.getFreedMemoryByGC().getMax());
        assertEquals("max memory", 256*1024, model.getFootprint());
    }

    public void testPartialToSpaceOverflowInitialMark() throws Exception {
        // special type of GC: 0.838: "[GC pause (partial) (to-space overflow) (initial-mark)..."
        final InputStream in = getClass().getResourceAsStream("SampleSun1_6_0G1_partial_initialMarkToSpaceOverflow.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();
        
        assertEquals("nummber of events", 1, model.size());
        assertEquals("number of pauses", 1, model.getPause().getN());
        assertEquals("gc pause sum", 0.00588343, model.getPause().getSum(), 0.000000001);
        assertEquals("gc memory", 255*1024 - 197*1024, model.getFreedMemoryByGC().getMax());
        assertEquals("max memory", 256*1024, model.getFootprint());
    }

    public void testGcPattern() throws Exception {
        final InputStream in = new ByteArrayInputStream(("0.452: [GC concurrent-count-start]").getBytes());
        
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();

        assertEquals("count", 1, model.size());
        assertEquals("full gc pause", 0, model.getFullGCPause().getN());
        assertEquals("gc pause", 0, model.getGCPause().getN());
    }

    public void testGcPausePattern() throws Exception {
        final InputStream in = new ByteArrayInputStream(("0.360: [GC concurrent-count-end, 0.0242674]").getBytes());
        
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();

        assertEquals("count", 1, model.size());
        assertEquals("full gc pause", 0, model.getFullGCPause().getN());
    }

    public void testGcMemoryPausePattern() throws Exception {
        final InputStream in = new ByteArrayInputStream(("0.360: [GC cleanup 19M->19M(36M), 0.0007889 secs]").getBytes());
        
        final DataReader reader = new DataReaderSun1_6_0G1(in);
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
        
        final DataReader reader = new DataReaderSun1_6_0G1(in);
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
        
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();

        assertEquals("count", 1, model.size());
        assertEquals("full gc pause", 0, model.getFullGCPause().getN());
        assertEquals("gc pause", 0.0009506, model.getGCPause().getMax(), 0.0000001);
        assertEquals("memory", 0, model.getFreedMemoryByGC().getMax());
    }

}

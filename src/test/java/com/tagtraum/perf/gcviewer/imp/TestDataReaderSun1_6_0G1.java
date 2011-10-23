package com.tagtraum.perf.gcviewer.imp;

import java.io.InputStream;
import java.util.logging.Logger;

import junit.framework.TestCase;

import com.tagtraum.perf.gcviewer.DataReader;
import com.tagtraum.perf.gcviewer.GCModel;

public class TestDataReaderSun1_6_0G1 extends TestCase {
    private static final Logger IMP_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.imp");
    private static final Logger DATA_READER_FACTORY_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.DataReaderFactory");

    public void testG2GcVerbose() throws Exception {
    	TestLogHandler handler = new TestLogHandler();
    	IMP_LOGGER.addHandler(handler);
    	DATA_READER_FACTORY_LOGGER.addHandler(handler);
    	
        final InputStream in = getClass().getResourceAsStream("SampleSun1_6_0G1-gc_verbose.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();
        
        assertEquals("gc pause sum", 62.5911287, model.getPause().getSum(), 0.000000001);
        assertEquals("throughput", 47.93401098, model.getThroughput(), 0.000000001);
        assertEquals("longest pause", 0.1581177, model.getPause().getMax(), 0.000001);
        assertEquals("total runtime", 120.215, model.getRunningTime(), 0.000001);
        assertEquals("number of errors", 10, handler.getCount());
    }

}

package com.tagtraum.perf.gcviewer.imp;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tagtraum.perf.gcviewer.model.GCModel;

import junit.framework.TestCase;

public class TestDataReaderSun1_7_0G1 extends TestCase {

    private static final Logger IMP_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.imp");
    private static final Logger DATA_READER_FACTORY_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.DataReaderFactory");

    public void testYoungJ7() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);
        
        final InputStream in = getClass().getResourceAsStream("SampleSun1_7_0G1_young.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();
        
        assertEquals("gc pause", 0.00631825, model.getPause().getMax(), 0.000000001);
        assertEquals("heap", 64*1024, model.getHeapAllocatedSizes().getMax());
        assertEquals("number of errors", 0, handler.getCount());
    }
    
}

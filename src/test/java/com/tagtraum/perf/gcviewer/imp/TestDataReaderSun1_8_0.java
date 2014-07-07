package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Test logs generated specifically by java 1.8.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 25.09.2013</p>
 */
public class TestDataReaderSun1_8_0 {
    private static final Logger IMP_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.imp");
    private static final Logger DATA_READER_FACTORY_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.DataReaderFactory");

    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_OPENJDK, fileName);
    }
    
    @Test
    public void parallelPrintHeapAtGC() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);
        
        final InputStream in = getInputStream("SampleSun1_8_0ParallelPrintHeapAtGC.txt");
        final DataReader reader = new DataReaderSun1_6_0(in, GcLogType.SUN1_7);
        GCModel model = reader.read();
        
        assertEquals("gc pause sum", 0.0103603, model.getPause().getSum(), 0.000000001);
        
        assertEquals("number of errors", 0, handler.getCount());
    }
}

package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Test logs generated specifically by JDK 1.8 G1 algorithm. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 22.07.2014</p>
 */
public class TestDataReaderSun1_8_0G1 {
    private static final Logger IMP_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.imp");
    private static final Logger DATA_READER_FACTORY_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.DataReaderFactory");

    private DataReader getDataReader(String fileName) throws IOException {
        return new DataReaderSun1_6_0G1(
                UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_OPENJDK, fileName),
                GcLogType.SUN1_8G1
                );
    }

    @Test
    public void printGCCauseTenuringDistribution() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);
        
        DataReader reader = getDataReader("SampleSun1_8_0G1PrintGCCausePrintTenuringDistribution.txt");
        GCModel model = reader.read();
        
        assertEquals("gc pause sum", 16.7578613, model.getPause().getSum(), 0.000000001);
        
        assertEquals("number of errors", 0, handler.getCount());
    }
}

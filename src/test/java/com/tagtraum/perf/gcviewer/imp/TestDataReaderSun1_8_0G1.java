package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;

/**
 * Test logs generated specifically by JDK 1.8 G1 algorithm. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 22.07.2014</p>
 */
public class TestDataReaderSun1_8_0G1 {

    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_OPENJDK, fileName);
    }
    
    private DataReader getDataReader(GCResource gcResource) throws UnsupportedEncodingException, IOException {
        return new DataReaderSun1_6_0G1(gcResource, getInputStream(gcResource.getResourceName()), GcLogType.SUN1_8G1);
    }
    
    @Test
    public void printGCCauseTenuringDistribution() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("SampleSun1_8_0G1PrintGCCausePrintTenuringDistribution.txt");
        gcResource.getLogger().addHandler(handler);
        
        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();
        
        assertEquals("gc pause sum", 16.7578613, model.getPause().getSum(), 0.000000001);
        
        assertEquals("number of errors", 0, handler.getCount());
    }
}

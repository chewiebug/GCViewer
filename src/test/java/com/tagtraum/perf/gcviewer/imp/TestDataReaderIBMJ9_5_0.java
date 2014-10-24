package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;

/**
 * Tests the implementation of {@link TestDataReaderIBMJ9_5_0} and {@link IBMJ9SAXHandler}. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 18.02.2013</p>
 */
public class TestDataReaderIBMJ9_5_0 {

    private InputStream getInputStream(String filename) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_IBM, filename);
    }
    
    private DataReader getDataReader(String fileName) throws UnsupportedEncodingException, IOException {
        return new DataReaderIBM_J9_5_0(new GCResource(fileName), getInputStream(fileName));
    }
    
    @Test
    public void afTenuredGlobal() throws Exception {
        final DataReader reader = getDataReader("SampleIBMJ9_5_0af-global-200811_07.txt");
        GCModel model = reader.read();

        assertEquals("count", 1, model.size());
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals("pause", 0.035912, event.getPause(), 0.0000001);

        assertEquals("timestamp", 0, event.getTimestamp(), 0.000001);
        assertEquals("name", Type.FULL_GC.getName(), event.getExtendedType().getName());
        assertEquals("before", (52428800 - 2621440) / 1024, event.getPreUsed());
        assertEquals("after", (52428800 - 40481192) / 1024, event.getPostUsed());
        assertEquals("total", 52428800 / 1024, event.getTotal());
    }
    
    @Test
    public void afTenuredGlobal_20090417_AA() throws Exception {
        final DataReader reader = getDataReader("SampleIBMJ9_5_0af-global-20090417_AA.txt");
        GCModel model = reader.read();

        assertEquals("count", 1, model.size());
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals("pause", 0.837024, event.getPause(), 0.0000001);

        assertEquals("timestamp", 0, event.getTimestamp(), 0.000001);
        assertEquals("name", Type.FULL_GC.getName(), event.getExtendedType().getName());
        assertEquals("before", (12884901888L - 4626919608L) / 1024, event.getPreUsed());
        assertEquals("after", (12884901888L - 10933557088L) / 1024, event.getPostUsed());
        assertEquals("total", 12884901888L / 1024, event.getTotal());
    }
}

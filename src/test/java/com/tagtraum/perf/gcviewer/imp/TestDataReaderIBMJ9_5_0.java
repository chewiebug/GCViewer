package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the implementation of {@link TestDataReaderIBMJ9_5_0} and {@link IBMJ9SAXHandler}. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 18.02.2013</p>
 */
class TestDataReaderIBMJ9_5_0 {

    private InputStream getInputStream(String filename) throws IOException {
        return UnittestHelper.getResourceAsStream(FOLDER.IBM, filename);
    }
    
    private DataReader getDataReader(String fileName) throws UnsupportedEncodingException, IOException {
        return new DataReaderIBM_J9_5_0(new GcResourceFile(fileName), getInputStream(fileName));
    }
    
    @Test
    void afTenuredGlobal() throws Exception {
        final DataReader reader = getDataReader("SampleIBMJ9_5_0af-global-200811_07.txt");
        GCModel model = reader.read();

        assertEquals(1, model.size(), "count");
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals(0.035912, event.getPause(), 0.0000001, "pause");

        assertEquals(0, event.getTimestamp(), 0.000001, "timestamp");
        assertEquals(Type.FULL_GC.getName(), event.getExtendedType().getName(), "name");
        assertEquals((52428800 - 2621440) / 1024, event.getPreUsed(), "before");
        assertEquals((52428800 - 40481192) / 1024, event.getPostUsed(), "after");
        assertEquals(52428800 / 1024, event.getTotal(), "total");
    }
    
    @Test
    void afTenuredGlobal_20090417_AA() throws Exception {
        final DataReader reader = getDataReader("SampleIBMJ9_5_0af-global-20090417_AA.txt");
        GCModel model = reader.read();

        assertEquals(1, model.size(), "count");
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals(0.837024, event.getPause(), 0.0000001, "pause");

        assertEquals(0, event.getTimestamp(), 0.000001, "timestamp");
        assertEquals(Type.FULL_GC.getName(), event.getExtendedType().getName(), "name");
        assertEquals((12884901888L - 4626919608L) / 1024, event.getPreUsed(), "before");
        assertEquals((12884901888L - 10933557088L) / 1024, event.getPostUsed(), "after");
        assertEquals(12884901888L / 1024, event.getTotal(), "total");
    }
}

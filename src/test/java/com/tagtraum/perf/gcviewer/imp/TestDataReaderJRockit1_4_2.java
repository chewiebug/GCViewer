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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
class TestDataReaderJRockit1_4_2 {

    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(FOLDER.JROCKIT, fileName);
    }
    
    private DataReader getDataReader1_4(String fileName) throws UnsupportedEncodingException, IOException {
        return new DataReaderJRockit1_4_2(new GcResourceFile(fileName), getInputStream(fileName));
    }
    
    private DataReader getDataReader1_5(String fileName) throws UnsupportedEncodingException, IOException {
        return new DataReaderJRockit1_5_0(new GcResourceFile(fileName), getInputStream(fileName));
    }
    
    @Test
    void testParseGenCon() throws Exception {
        // TODO refactor JRockit DataReader
        DataReader reader = getDataReader1_5("SampleJRockit1_4_2gencon.txt");
        GCModel model = reader.read();
        
        assertEquals(123, model.size(), "count");
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals(77.737, event.getTimestamp(), 0.000001, "timestamp");
        assertEquals(Type.JROCKIT_NURSERY_GC.getName(), event.getExtendedType().getName(), "name");
        assertEquals(630435, event.getPreUsed(), "before");
        assertEquals(183741, event.getPostUsed(), "after");
        assertEquals(1048576, event.getTotal(), "total");
        assertEquals(0.566158, event.getPause(), 0.0000001, "pause");
    }

    @Test
    void testParseGenConBig() throws Exception {
        // TODO refactor JRockit DataReader
        DataReader reader = getDataReader1_5("SampleJRockit1_4_2gencon-big.txt");
        GCModel model = reader.read();
        
        assertEquals(32420, model.size(), "count");
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals(9.385, event.getTimestamp(), 0.000001, "timestamp");
        assertEquals(Type.JROCKIT_NURSERY_GC.getName(), event.getExtendedType().getName(), "name");
        assertEquals(123930, event.getPreUsed(), "before");
        assertEquals(27087, event.getPostUsed(), "after");
        assertEquals(1048576, event.getTotal(), "total");
        assertEquals(0.053417, event.getPause(), 0.0000001, "pause");
    }

    @Test
    void testParseParallel() throws Exception {
        // TODO refactor JRockit DataReader
        DataReader reader = getDataReader1_5("SampleJRockit1_4_2parallel.txt");
        GCModel model = reader.read();
        
        assertEquals(92, model.size(), "count");
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals(226.002, event.getTimestamp(), 0.000001, "timestamp");
        assertEquals(Type.JROCKIT_GC.getName(), event.getExtendedType().getName(), "name");
        assertEquals(1048576, event.getPreUsed(), "before");
        assertEquals(133204, event.getPostUsed(), "after");
        assertEquals(1048576, event.getTotal(), "total");
        assertEquals(0.544511, event.getPause(), 0.0000001, "pause");
    }

    @Test
    void testParsePrioPauseTime() throws Exception {
        // TODO refactor JRockit DataReader
        DataReader reader = getDataReader1_5("SampleJRockit1_4_2priopausetime.txt");
        GCModel model = reader.read();
        
        assertEquals(1867, model.size(), "count");
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals(12.622, event.getTimestamp(), 0.000001, "timestamp");
        assertEquals(Type.JROCKIT_GC.getName(), event.getExtendedType().getName(), "name");
        assertEquals(320728, event.getPreUsed(), "before");
        assertEquals(130908, event.getPostUsed(), "after");
        assertEquals(358400, event.getTotal(), "total");
        assertEquals(0.025921, event.getPause(), 0.0000001, "pause");
    }

    @Test
    void testParseTsGCReportGencon() throws Exception {
        DataReader reader = getDataReader1_4("SampleJRockit1_4_2ts-gcreport-gencon.txt");
        GCModel model = reader.read();
        
        assertEquals(63, model.size(), "count");
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals(13.594, event.getTimestamp(), 0.000001, "timestamp");
        assertEquals(Type.JROCKIT_NURSERY_GC.getName(), event.getExtendedType().getName(), "name");
        assertEquals(13824, event.getPreUsed(), "before");
        assertEquals(4553, event.getPostUsed(), "after");
        assertEquals(32768, event.getTotal(), "total");
        assertEquals(0.028308, event.getPause(), 0.0000001, "pause");
    }

    @Test
    void testParseTsGCReportParallel() throws Exception {
        DataReader reader = getDataReader1_4("SampleJRockit1_4_2ts-gcreport-parallel.txt");
        GCModel model = reader.read();
        
        assertEquals(31, model.size(), "count");
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals(20.547, event.getTimestamp(), 0.000001, "timestamp");
        assertEquals(Type.JROCKIT_GC.getName(), event.getExtendedType().getName(), "name");
        assertEquals(32768, event.getPreUsed(), "before");
        assertEquals(5552, event.getPostUsed(), "after");
        assertEquals(32768, event.getTotal(), "total");
        assertEquals(0.072, event.getPause(), 0.0000001, "pause");
    }

    @Test
    void testParseTsGCReportPrioPauseTime() throws Exception {
        String fileName = "SampleJRockit1_4_2ts-gcreport-gcpriopausetime.txt";
        InputStream in = getInputStream(fileName);
        DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
        
        assertTrue(reader instanceof DataReaderJRockit1_4_2, "should be DataReaderJRockit1_4_2 (but was " + reader.toString() + ")");

        GCModel model = reader.read();
        
        assertEquals(64, model.size(), "count");
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals(18.785, event.getTimestamp(), 0.000001, "timestamp");
        assertEquals(Type.JROCKIT_GC.getName(), event.getExtendedType().getName(), "name");
        assertEquals(32260, event.getPreUsed(), "before");
        assertEquals(4028, event.getPostUsed(), "after");
        assertEquals(32768, event.getTotal(), "total");
        assertEquals(0.024491, event.getPause(), 0.0000001, "pause");
    }

    @Test
    void testParseTsGCReportPrioThroughput() throws Exception {
        DataReader reader = getDataReader1_4("SampleJRockit1_4_2ts-gcreport-gcpriothroughput.txt");
        GCModel model = reader.read();
        
        assertEquals(70, model.size(), "count");
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals(20.021, event.getTimestamp(), 0.000001, "timestamp");
        assertEquals(Type.JROCKIT_GC.getName(), event.getExtendedType().getName(), "name");
        assertEquals(32768, event.getPreUsed(), "before");
        assertEquals(5561, event.getPostUsed(), "after");
        assertEquals(32768, event.getTotal(), "total");
        assertEquals(0.061, event.getPause(), 0.0000001, "pause");
    }

    @Test
    void testParseTsGCReportSinglecon() throws Exception {
        DataReader reader = getDataReader1_4("SampleJRockit1_4_2ts-gcreport-singlecon.txt");
        GCModel model = reader.read();
        
        assertEquals(41, model.size(), "count");
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals(18.906, event.getTimestamp(), 0.000001, "timestamp");
        assertEquals(Type.JROCKIT_GC.getName(), event.getExtendedType().getName(), "name");
        assertEquals(32260, event.getPreUsed(), "before");
        assertEquals(3997, event.getPostUsed(), "after");
        assertEquals(32768, event.getTotal(), "total");
        assertEquals(0.020149, event.getPause(), 0.0000001, "pause");
    }
}

package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestDataReaderJRockit1_4_2 {

    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_JROCKIT, fileName);
    }
    
    @Test
    public void testParseGenCon() throws Exception {
        // TODO refactor JRockit DataReader
        InputStream in = getInputStream("SampleJRockit1_4_2gencon.txt");
        DataReader reader = new DataReaderJRockit1_5_0(in);
        GCModel model = reader.read();
        
        assertEquals("count", 123, model.size());
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals("timestamp", 77.737, event.getTimestamp(), 0.000001);
        assertEquals("name", Type.JROCKIT_NURSERY_GC.getName(), event.getExtendedType().getName());
        assertEquals("before", 630435, event.getPreUsed());
        assertEquals("after", 183741, event.getPostUsed());
        assertEquals("total", 1048576, event.getTotal());
        assertEquals("pause", 0.566158, event.getPause(), 0.0000001);
    }

    @Test
    public void testParseGenConBig() throws Exception {
        // TODO refactor JRockit DataReader
        InputStream in = getInputStream("SampleJRockit1_4_2gencon-big.txt");
        DataReader reader = new DataReaderJRockit1_5_0(in);
        GCModel model = reader.read();
        
        assertEquals("count", 32420, model.size());
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals("timestamp", 9.385, event.getTimestamp(), 0.000001);
        assertEquals("name", Type.JROCKIT_NURSERY_GC.getName(), event.getExtendedType().getName());
        assertEquals("before", 123930, event.getPreUsed());
        assertEquals("after", 27087, event.getPostUsed());
        assertEquals("total", 1048576, event.getTotal());
        assertEquals("pause", 0.053417, event.getPause(), 0.0000001);
    }

    @Test
    public void testParseParallel() throws Exception {
        // TODO refactor JRockit DataReader
        InputStream in = getInputStream("SampleJRockit1_4_2parallel.txt");
        DataReader reader = new DataReaderJRockit1_5_0(in);
        GCModel model = reader.read();
        
        assertEquals("count", 92, model.size());
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals("timestamp", 226.002, event.getTimestamp(), 0.000001);
        assertEquals("name", Type.JROCKIT_GC.getName(), event.getExtendedType().getName());
        assertEquals("before", 1048576, event.getPreUsed());
        assertEquals("after", 133204, event.getPostUsed());
        assertEquals("total", 1048576, event.getTotal());
        assertEquals("pause", 0.544511, event.getPause(), 0.0000001);
    }

    @Test
    public void testParsePrioPauseTime() throws Exception {
        // TODO refactor JRockit DataReader
        InputStream in = getInputStream("SampleJRockit1_4_2priopausetime.txt");
        DataReader reader = new DataReaderJRockit1_5_0(in);
        GCModel model = reader.read();
        
        assertEquals("count", 1867, model.size());
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals("timestamp", 12.622, event.getTimestamp(), 0.000001);
        assertEquals("name", Type.JROCKIT_GC.getName(), event.getExtendedType().getName());
        assertEquals("before", 320728, event.getPreUsed());
        assertEquals("after", 130908, event.getPostUsed());
        assertEquals("total", 358400, event.getTotal());
        assertEquals("pause", 0.025921, event.getPause(), 0.0000001);
    }

    @Test
    public void testParseTsGCReportGencon() throws Exception {
        InputStream in = getInputStream("SampleJRockit1_4_2ts-gcreport-gencon.txt");
        DataReader reader = new DataReaderJRockit1_4_2(in);
        GCModel model = reader.read();
        
        assertEquals("count", 63, model.size());
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals("timestamp", 13.594, event.getTimestamp(), 0.000001);
        assertEquals("name", Type.JROCKIT_NURSERY_GC.getName(), event.getExtendedType().getName());
        assertEquals("before", 13824, event.getPreUsed());
        assertEquals("after", 4553, event.getPostUsed());
        assertEquals("total", 32768, event.getTotal());
        assertEquals("pause", 0.028308, event.getPause(), 0.0000001);
    }

    @Test
    public void testParseTsGCReportParallel() throws Exception {
        InputStream in = getInputStream("SampleJRockit1_4_2ts-gcreport-parallel.txt");
        DataReader reader = new DataReaderJRockit1_4_2(in);
        GCModel model = reader.read();
        
        assertEquals("count", 31, model.size());
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals("timestamp", 20.547, event.getTimestamp(), 0.000001);
        assertEquals("name", Type.JROCKIT_GC.getName(), event.getExtendedType().getName());
        assertEquals("before", 32768, event.getPreUsed());
        assertEquals("after", 5552, event.getPostUsed());
        assertEquals("total", 32768, event.getTotal());
        assertEquals("pause", 0.072, event.getPause(), 0.0000001);
    }

    @Test
    public void testParseTsGCReportPrioPauseTime() throws Exception {
        InputStream in = getInputStream("SampleJRockit1_4_2ts-gcreport-gcpriopausetime.txt");
        DataReader reader = new DataReaderFactory().getDataReader(in);
        
        assertTrue("should be DataReaderJRockit1_4_2 (but was " + reader.toString() + ")", reader instanceof DataReaderJRockit1_4_2);

        GCModel model = reader.read();
        
        assertEquals("count", 64, model.size());
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals("timestamp", 18.785, event.getTimestamp(), 0.000001);
        assertEquals("name", Type.JROCKIT_GC.getName(), event.getExtendedType().getName());
        assertEquals("before", 32260, event.getPreUsed());
        assertEquals("after", 4028, event.getPostUsed());
        assertEquals("total", 32768, event.getTotal());
        assertEquals("pause", 0.024491, event.getPause(), 0.0000001);
    }

    @Test
    public void testParseTsGCReportPrioThroughput() throws Exception {
        InputStream in = getInputStream("SampleJRockit1_4_2ts-gcreport-gcpriothroughput.txt");
        DataReader reader = new DataReaderJRockit1_4_2(in);
        GCModel model = reader.read();
        
        assertEquals("count", 70, model.size());
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals("timestamp", 20.021, event.getTimestamp(), 0.000001);
        assertEquals("name", Type.JROCKIT_GC.getName(), event.getExtendedType().getName());
        assertEquals("before", 32768, event.getPreUsed());
        assertEquals("after", 5561, event.getPostUsed());
        assertEquals("total", 32768, event.getTotal());
        assertEquals("pause", 0.061, event.getPause(), 0.0000001);
    }

    @Test
    public void testParseTsGCReportSinglecon() throws Exception {
        InputStream in = getInputStream("SampleJRockit1_4_2ts-gcreport-singlecon.txt");
        DataReader reader = new DataReaderJRockit1_4_2(in);
        GCModel model = reader.read();
        
        assertEquals("count", 41, model.size());
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals("timestamp", 18.906, event.getTimestamp(), 0.000001);
        assertEquals("name", Type.JROCKIT_GC.getName(), event.getExtendedType().getName());
        assertEquals("before", 32260, event.getPreUsed());
        assertEquals("after", 3997, event.getPostUsed());
        assertEquals("total", 32768, event.getTotal());
        assertEquals("pause", 0.020149, event.getPause(), 0.0000001);
    }
}

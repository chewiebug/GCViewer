package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.math.DoubleData;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

public class TestDataReaderSun1_7_0G1 {

    private static final Logger IMP_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.imp");
    private static final Logger DATA_READER_FACTORY_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.DataReaderFactory");

    @Test
    public void youngPause_u1() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);
        
        final InputStream in = getClass().getResourceAsStream("SampleSun1_7_0-01_G1_young.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();
        
        assertEquals("gc pause", 0.00631825, model.getPause().getMax(), 0.000000001);
        assertEquals("heap", 64*1024, model.getHeapAllocatedSizes().getMax());
        assertEquals("number of errors", 0, handler.getCount());
    }
    

    /**
     * Parse memory format of java 1.7.0_u2.
     */
    @Test
    public void youngPause_u2() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);
        
        final InputStream in = getClass().getResourceAsStream("SampleSun1_7_0-02_G1_young.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();
        
        assertEquals("gc pause", 0.00623837, model.getPause().getMax(), 0.000000001);
        GCEvent heap = (GCEvent) model.getEvents().next();
        assertEquals("heap", 8192, heap.getPreUsed());
        assertEquals("heap", 7895, heap.getPostUsed());
        assertEquals("heap", 16*1024, heap.getTotal());
        
        
        // TODO 1.7.0_02: parse all information
//        GCEvent young = model.getGCEvents().next().getYoung();
//        assertEquals("young before", 8192, young.getPreUsed());
//        assertEquals("young after", 0, young.getPostUsed());
//        assertEquals("young total", 8192, young.getTotal());
//        
//        GCEvent tenured = model.getGCEvents().next().getYoung();
//        assertEquals("tenured before", 0, tenured.getPreUsed());
//        assertEquals("tenured after", 7895, tenured.getPostUsed());
//        assertEquals("tenured total", 7895, tenured.getTotal());
//        
//        assertEquals("number of errors", 0, handler.getCount());
    }
    
    @Test
    public void youngPauseDateStamp_u2() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);
        
        final InputStream in = getClass().getResourceAsStream("SampleSun1_7_0_02_G1_young_datestamp.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();
        
        assertEquals("gc pause", 0.14482200, model.getPause().getMax(), 0.000000001);
        GCEvent heap = (GCEvent) model.getEvents().next();
        assertEquals("heap", 1105*1024, heap.getPreUsed());
        assertEquals("heap", 380*1024, heap.getPostUsed());
        assertEquals("heap", 2048*1024, heap.getTotal());
        
    }
    
    @Test
    public void eventNoMemory() throws Exception {
        // there are (rarely) events, where the memory information could not be parsed,
        // because the line with the memory information was mixed with another event
        // looks like this:    [251.448:  213M->174M(256M)[GC concurrent-mark-start]
        // (produced using -XX:+PrintGcDetails -XX:+PrintHeapAtGC)

        GCEvent event = new GCEvent();
        event.setType(Type.G1_YOUNG_INITIAL_MARK);
        event.setTimestamp(0.5);
        event.setPause(0.2);
        // but no memory information -> all values zero there
        
        GCModel model = new GCModel(false);
        model.add(event);
        
        DoubleData initiatingOccupancyFraction = model.getCmsInitiatingOccupancyFraction();
        assertEquals("fraction", 0, initiatingOccupancyFraction.getSum(), 0.1);
    }
    
    @Test
    public void GcRemark() throws Exception {
        final InputStream in = new ByteArrayInputStream(
                ("0.197: [GC remark 0.197: [GC ref-proc, 0.0000070 secs], 0.0005297 secs]" +
                		"\n [Times: user=0.00 sys=0.00, real=0.00 secs]")
                .getBytes());
        
        final DataReader reader = new DataReaderSun1_6_0G1(in);
        GCModel model = reader.read();

        assertEquals("count", 1, model.size());
        assertEquals("gc pause", 0.0005297, model.getGCPause().getMax(), 0.000001);
    }
    
}

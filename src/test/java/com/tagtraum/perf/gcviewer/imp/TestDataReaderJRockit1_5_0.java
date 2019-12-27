/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.perf.gcviewer.imp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.math.DoubleData;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Test DataReaderJRockit1_5_0 implementation.</p>
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
class TestDataReaderJRockit1_5_0 {

    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(FOLDER.JROCKIT, fileName);
    }
    
    private DataReader getDataReader1_5(GCResource gcResource) throws UnsupportedEncodingException, IOException {
        return new DataReaderJRockit1_5_0(gcResource, getInputStream(gcResource.getResourceName()));
    }
    
    private DataReader getDataReader1_6(GCResource gcResource) throws UnsupportedEncodingException, IOException {
        return new DataReaderJRockit1_6_0(gcResource, getInputStream(gcResource.getResourceName()));
    }
    
    @Test
    void testGcPrioPausetime() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleJRockit1_5_12_gcpriopausetime.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader1_5(gcResource);
        GCModel model = reader.read();
        
        assertEquals(10, model.size(), "count");
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals(6.290, event.getTimestamp(), 0.000001, "timestamp");
        assertEquals(Type.JROCKIT_GC.getName(), event.getExtendedType().getName(), "name");
        assertEquals(3128161, event.getPreUsed(), "before");
        assertEquals(296406, event.getPostUsed(), "after");
        assertEquals(3145728, event.getTotal(), "total");
        assertEquals(0.059084, event.getPause(), 0.0000001, "pause");

        assertEquals(6, handler.getCount(), "number of warnings");
    }
    
    @Test
    void testGcPrioThroughput() throws Exception {
        DataReader reader = getDataReader1_5(new GcResourceFile("SampleJRockit1_5_12_gcpriothroughput.txt"));
        GCModel model = reader.read();
        
        assertEquals(8, model.size(), "count");
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals(4.817, event.getTimestamp(), 0.000001, "timestamp");
        assertEquals(Type.JROCKIT_GC.getName(), event.getExtendedType().getName(), "name");
        assertEquals(1641728, event.getPreUsed(), "before");
        assertEquals(148365, event.getPostUsed(), "after");
        assertEquals(3145728, event.getTotal(), "total");
        assertEquals(0.039959, event.getPause(), 0.0000001, "pause");
    }
    
    @Test
    void testGenCon() throws Exception {
        DataReader reader = getDataReader1_5(new GcResourceFile("SampleJRockit1_5_12_gencon.txt"));
        GCModel model = reader.read();
        
        assertEquals(8, model.size(), "count");
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals(6.038, event.getTimestamp(), 0.000001, "timestamp");
        assertEquals(Type.JROCKIT_GC.getName(), event.getExtendedType().getName(), "name");
        assertEquals(3089328, event.getPreUsed(), "before");
        assertEquals(352551, event.getPostUsed(), "after");
        assertEquals(3145728, event.getTotal(), "total");
        assertEquals(0.1186, event.getPause(), 0.0000001, "pause");
    }
    
    /**
     * This log file sample contains much more information about concurrent events
     * than is currently parsed. Still the parser must be able to extract the information
     * it can parse.
     */
    @Test
    void testGenConMemstats() throws Exception {
        // although this log file was written with JRockit 1.5 VM, it has the same structure
        // as a JRockit 1.6 gc log file.
        // TODO refactor JRockit DataReader
        DataReader reader = getDataReader1_6(new GcResourceFile("SampleJRockit1_5_20_memstats2.txt"));
        GCModel model = reader.read();
        
        assertEquals(11, model.size(), "count");
    }
    
    @Test
    void testGenPar() throws Exception {
        DataReader reader = getDataReader1_5(new GcResourceFile("SampleJRockit1_5_12_genpar.txt"));
        GCModel model = reader.read();
        
        assertEquals(17, model.size(), "count");

        // 2 types of events excpected: "GC" and "parallel nursery GC"
        Map<String, DoubleData> gcEventPauses = model.getGcEventPauses();
        assertEquals(2, gcEventPauses.entrySet().size(), "2 types of events found");
    }
    
    /**
     * Test parsing of a malformed type. The test just expects an INFO to be logged - nothing else.
     */
    @Test
    void testMalformedType() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.INFO);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("[memory ][Thu Feb 21 15:06:38 2013][11844] 6.290-6.424: GC-malformed 3128161K->296406K (3145728K), sum of pauses 59.084 ms")
                       .getBytes());
        
        DataReader reader = new DataReaderJRockit1_5_0(gcResource, in);
        reader.read();
        
        // 3 INFO events:
        // Reading JRockit ... format
        // Failed to determine type ...
        // Reading done.
        assertEquals(3, handler.getCount(), "number of infos");
        
        List<LogRecord> logRecords = handler.getLogRecords();
        assertEquals(0, logRecords.get(1).getMessage().indexOf("Failed to determine type"), "should start with 'Failed to determine type'");
    }
    
    @Test
    void testSimpleOpts() throws Exception {
        DataReader reader = getDataReader1_5(new GcResourceFile("SampleJRockit1_5_12-gcreport-simpleopts-singlecon.txt"));
        GCModel model = reader.read();
        
        assertEquals(5, model.size(), "count");
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals(6.771, event.getTimestamp(), 0.000001, "timestamp");
        assertEquals(Type.JROCKIT_GC.getName(), event.getExtendedType().getName(), "name");
        assertEquals(3145728, event.getPreUsed(), "before");
        assertEquals(296406, event.getPostUsed(), "after");
        assertEquals(3145728, event.getTotal(), "total");
        assertEquals(0.066, event.getPause(), 0.0000001, "pause");
    }
    
}

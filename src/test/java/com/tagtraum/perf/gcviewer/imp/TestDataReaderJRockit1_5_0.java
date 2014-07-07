/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.math.DoubleData;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * <p>Test DataReaderJRockit1_5_0 implementation.</p>
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestDataReaderJRockit1_5_0 {
    private static final Logger IMP_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.imp");
    private static final Logger DATA_READER_FACTORY_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.DataReaderFactory");

    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_JROCKIT, fileName);
    }
    
    @Test
    public void testGcPrioPausetime() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        InputStream in = getInputStream("SampleJRockit1_5_12_gcpriopausetime.txt");
        DataReader reader = new DataReaderJRockit1_5_0(in);
        GCModel model = reader.read();
        
        assertEquals("count", 10, model.size());
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals("timestamp", 6.290, event.getTimestamp(), 0.000001);
        assertEquals("name", Type.JROCKIT_GC.getName(), event.getExtendedType().getName());
        assertEquals("before", 3128161, event.getPreUsed());
        assertEquals("after", 296406, event.getPostUsed());
        assertEquals("total", 3145728, event.getTotal());
        assertEquals("pause", 0.059084, event.getPause(), 0.0000001);

        assertEquals("number of warnings", 6, handler.getCount());
    }
    
    @Test
    public void testGcPrioThroughput() throws Exception {
        InputStream in = getInputStream("SampleJRockit1_5_12_gcpriothroughput.txt");
        DataReader reader = new DataReaderJRockit1_5_0(in);
        GCModel model = reader.read();
        
        assertEquals("count", 8, model.size());
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals("timestamp", 4.817, event.getTimestamp(), 0.000001);
        assertEquals("name", Type.JROCKIT_GC.getName(), event.getExtendedType().getName());
        assertEquals("before", 1641728, event.getPreUsed());
        assertEquals("after", 148365, event.getPostUsed());
        assertEquals("total", 3145728, event.getTotal());
        assertEquals("pause", 0.039959, event.getPause(), 0.0000001);
    }
    
    @Test
    public void testGenCon() throws Exception {
        InputStream in = getInputStream("SampleJRockit1_5_12_gencon.txt");
        DataReader reader = new DataReaderJRockit1_5_0(in);
        GCModel model = reader.read();
        
        assertEquals("count", 8, model.size());
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals("timestamp", 6.038, event.getTimestamp(), 0.000001);
        assertEquals("name", Type.JROCKIT_GC.getName(), event.getExtendedType().getName());
        assertEquals("before", 3089328, event.getPreUsed());
        assertEquals("after", 352551, event.getPostUsed());
        assertEquals("total", 3145728, event.getTotal());
        assertEquals("pause", 0.1186, event.getPause(), 0.0000001);
    }
    
    /**
     * This log file sample contains much more information about concurrent events
     * than is currently parsed. Still the parser must be able to extract the information
     * it can parse.
     */
    @Test
    public void testGenConMemstats() throws Exception {
        // allthough this log file was written with JRockit 1.5 VM, it has the same structure
        // as a JRockit 1.6 gc log file.
        // TODO refactor JRockit DataReader
        InputStream in = getInputStream("SampleJRockit1_5_20_memstats2.txt");
        DataReader reader = new DataReaderJRockit1_6_0(in);
        GCModel model = reader.read();
        
        assertEquals("count", 11, model.size());
    }
    
    @Test
    public void testGenPar() throws Exception {
        InputStream in = getInputStream("SampleJRockit1_5_12_genpar.txt");
        DataReader reader = new DataReaderJRockit1_5_0(in);
        GCModel model = reader.read();
        
        assertEquals("count", 17, model.size());

        // 2 types of events excpected: "GC" and "parallel nursery GC"
        Map<String, DoubleData> gcEventPauses = model.getGcEventPauses();
        assertEquals("2 types of events found", 2, gcEventPauses.entrySet().size());
    }
    
    /**
     * Test parsing of a malformed type. The test just expects an INFO to be logged - nothing else.
     */
    @Test
    public void testMalformedType() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.INFO);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        ByteArrayInputStream in = new ByteArrayInputStream(
                ("[memory ][Thu Feb 21 15:06:38 2013][11844] 6.290-6.424: GC-malformed 3128161K->296406K (3145728K), sum of pauses 59.084 ms")
                       .getBytes());
        
        DataReader reader = new DataReaderJRockit1_5_0(in);
        reader.read();
        
        // 3 INFO events:
        // Reading JRockit ... format
        // Failed to determine type ...
        // Reading done.
        assertEquals("number of infos", 3, handler.getCount());
        
        List<LogRecord> logRecords = handler.getLogRecords();
        assertEquals("should start with 'Failed to determine type'", 0, logRecords.get(1).getMessage().indexOf("Failed to determine type"));
    }
    
    @Test
    public void testSimpleOpts() throws Exception {
        InputStream in = getInputStream("SampleJRockit1_5_12-gcreport-simpleopts-singlecon.txt");
        DataReader reader = new DataReaderJRockit1_5_0(in);
        GCModel model = reader.read();
        
        assertEquals("count", 5, model.size());
        
        GCEvent event = (GCEvent) model.get(0);
        assertEquals("timestamp", 6.771, event.getTimestamp(), 0.000001);
        assertEquals("name", Type.JROCKIT_GC.getName(), event.getExtendedType().getName());
        assertEquals("before", 3145728, event.getPreUsed());
        assertEquals("after", 296406, event.getPostUsed());
        assertEquals("total", 3145728, event.getTotal());
        assertEquals("pause", 0.066, event.getPause(), 0.0000001);
        
    }
    
}

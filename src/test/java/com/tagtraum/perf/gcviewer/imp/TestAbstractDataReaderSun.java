package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.ParseInformation;

public class TestAbstractDataReaderSun {

    private AbstractDataReaderSunSub dataReader;
    
    @Before
    public void setUp() throws UnsupportedEncodingException {
        dataReader = new AbstractDataReaderSunSub(new GCResource("empty"), new ByteArrayInputStream(new byte[0]), GcLogType.SUN1_6);
    }
    
    /**
     * Tests parsing of memory information like 8192K(16M)->7895K(16M)
     */
    @Test
    public void setMemorySimplePreUsed_heap_postUsed_heap() throws ParseException {
        String line = "   [Eden: 1000K(2000K)->0B(3000K) Survivors: 1024B->4000K Heap: 5000K(16M)->6000K(16M)]";
        
        ParseInformation pos = new ParseInformation(0);
        pos.setIndex(line.indexOf("Heap:") + "Heap:".length() + 1);
        
        GCEvent event = new GCEvent();
        dataReader.setMemoryExtended(event, line, pos);
        
        assertEquals("heap before", 5000, event.getPreUsed());
        assertEquals("heap after", 6000, event.getPostUsed());
        assertEquals("heap total", 16*1024, event.getTotal());
    }
    
    /**
     * Tests parsing of memory information like 8192K->7895K(16M)
     */
    @Test
    public void setMemorySimplePreUsed_postUsed_heap() throws ParseException {
        String line = "   [ 8192K->8128K(64M)]";
        
        ParseInformation pos = new ParseInformation(0);
        pos.setIndex(line.indexOf("[") + 1);
        
        GCEvent event = new GCEvent();
        dataReader.setMemoryExtended(event, line, pos);
        
        assertEquals("heap before", 8192, event.getPreUsed());
        assertEquals("heap after", 8128, event.getPostUsed());
        assertEquals("heap total", 64*1024, event.getTotal());
    }
    
    /**
     * Tests parsing of memory information like 8192K->7895K (usually found in G1 Survivors block)
     */
    @Test
    public void setMemorySimplePreHeap_postHeap() throws ParseException {
        String line = "   [Eden: 1000K(2000K)->0B(3000K) Survivors: 1024B->4000K Heap: 5000K(16M)->6000K(16M)]";
        
        ParseInformation pos = new ParseInformation(0);
        pos.setIndex(line.indexOf("Survivors:") + "Survivors:".length() + 1);
        
        GCEvent event = new GCEvent();
        dataReader.setMemoryExtended(event, line, pos);
        
        assertEquals("heap before", 1, event.getPreUsed());
        assertEquals("heap after", 4000, event.getPostUsed());
    }
    
    /**
     * Tests parsing of memory information like "118.5M(118.0M)->128.4K(112.0M)" (notice the dots).
     */
    @Test
    public void setExtendedMemoryFloatingPointPreEden_postEden() throws ParseException {
        String line = "   [Eden: 118.5M(118.0M)->128.4K(112.0M) Survivors: 10.0M->16.0M Heap: 548.6M(640.0M)->440.6M(640.0M)]";
        
        ParseInformation pos = new ParseInformation(0);
        pos.setIndex(line.indexOf("Eden:") + "Eden:".length() + 1);
        
        GCEvent event = new GCEvent();
        dataReader.setMemoryExtended(event, line, pos);
        
        assertEquals("heap before", 121344, event.getPreUsed());
        assertEquals("heap after", 128, event.getPostUsed());
    }
    
    /**
     * Subclass of {@link AbstractDataReaderSun} which makes those methods public, I want to test here.
     * 
     * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
     * <p>created on: 28.02.2012</p>
     */
    private class AbstractDataReaderSunSub extends AbstractDataReaderSun {
        
        public AbstractDataReaderSunSub(GCResource gcResource, InputStream in, GcLogType gcLogType) throws UnsupportedEncodingException {
            super(gcResource, in, gcLogType);
        }
    
        @Override
        public void setMemoryExtended(GCEvent event, String line, ParseInformation pos) throws ParseException {
            super.setMemoryExtended(event, line, pos);
        }
        
        @Override
        public GCModel read() throws IOException {
            return null;
        }
    
        @Override
        protected AbstractGCEvent<?> parseLine(String line, ParseInformation pos) throws ParseException {
            return null;
        }

    }
}

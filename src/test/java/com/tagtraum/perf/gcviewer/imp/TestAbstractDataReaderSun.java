package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.model.*;
import com.tagtraum.perf.gcviewer.util.ParseInformation;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class TestAbstractDataReaderSun {

    private AbstractDataReaderSunSub dataReader;
    
    @Before
    public void setUp() throws UnsupportedEncodingException {
        dataReader = new AbstractDataReaderSunSub(new GcResourceFile("empty"), new ByteArrayInputStream(new byte[0]), GcLogType.SUN1_6);
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

    @Test
    public void testParseDatestamp() throws Exception {
        String line =
                "2016-04-14T22:37:55.315+0200: 467.260: [GC (Allocation Failure) 467.260: [ParNew: 226563K->6586K(245760K), 0.0044323 secs] 385679K->165875K(791936K), 0.0045438 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] ";
        ParseInformation pos = new ParseInformation(0);
        ZonedDateTime time = dataReader.parseDatestamp(line, pos);
        assertThat(time, is(ZonedDateTime.of(2016, 4, 14, 22, 37, 55, 315000000, ZoneOffset.ofHours(2))));
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

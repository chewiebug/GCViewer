package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.util.ParseInformation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestAbstractDataReaderSun {

    private AbstractDataReaderSunSub dataReader;
    
    @BeforeEach
    public void setUp() throws UnsupportedEncodingException {
        dataReader = new AbstractDataReaderSunSub(new GcResourceFile("empty"), new ByteArrayInputStream(new byte[0]), GcLogType.SUN1_6);
    }
    
    /**
     * Tests parsing of memory information like 8192K(16M)->7895K(16M)
     */
    @Test
    void setMemorySimplePreUsed_heap_postUsed_heap() throws ParseException {
        String line = "   [Eden: 1000K(2000K)->0B(3000K) Survivors: 1024B->4000K Heap: 5000K(16M)->6000K(16M)]";
        
        ParseInformation pos = new ParseInformation(0);
        pos.setIndex(line.indexOf("Heap:") + "Heap:".length() + 1);
        
        GCEvent event = new GCEvent();
        dataReader.setMemoryExtended(event, line, pos);
        
        assertEquals(5000, event.getPreUsed(), "heap before");
        assertEquals(6000, event.getPostUsed(), "heap after");
        assertEquals(16 * 1024, event.getTotal(), "heap total");
    }
    
    /**
     * Tests parsing of memory information like 8192K->7895K(16M)
     */
    @Test
    void setMemorySimplePreUsed_postUsed_heap() throws ParseException {
        String line = "   [ 8192K->8128K(64M)]";
        
        ParseInformation pos = new ParseInformation(0);
        pos.setIndex(line.indexOf("[") + 1);
        
        GCEvent event = new GCEvent();
        dataReader.setMemoryExtended(event, line, pos);
        
        assertEquals(8192, event.getPreUsed(), "heap before");
        assertEquals(8128, event.getPostUsed(), "heap after");
        assertEquals(64 * 1024, event.getTotal(), "heap total");
    }
    
    /**
     * Tests parsing of memory information like 8192K->7895K (usually found in G1 Survivors block)
     */
    @Test
    void setMemorySimplePreHeap_postHeap() throws ParseException {
        String line = "   [Eden: 1000K(2000K)->0B(3000K) Survivors: 1024B->4000K Heap: 5000K(16M)->6000K(16M)]";
        
        ParseInformation pos = new ParseInformation(0);
        pos.setIndex(line.indexOf("Survivors:") + "Survivors:".length() + 1);
        
        GCEvent event = new GCEvent();
        dataReader.setMemoryExtended(event, line, pos);
        
        assertEquals(1, event.getPreUsed(), "heap before");
        assertEquals(4000, event.getPostUsed(), "heap after");
    }
    
    /**
     * Tests parsing of memory information like "118.5M(118.0M)->128.4K(112.0M)" (notice the dots).
     */
    @Test
    void setExtendedMemoryFloatingPointPreEden_postEden() throws ParseException {
        String line = "   [Eden: 118.5M(118.0M)->128.4K(112.0M) Survivors: 10.0M->16.0M Heap: 548.6M(640.0M)->440.6M(640.0M)]";
        
        ParseInformation pos = new ParseInformation(0);
        pos.setIndex(line.indexOf("Eden:") + "Eden:".length() + 1);
        
        GCEvent event = new GCEvent();
        dataReader.setMemoryExtended(event, line, pos);
        
        assertEquals(121344, event.getPreUsed(), "heap before");
        assertEquals(128, event.getPostUsed(), "heap after");
    }

    @Test
    void contains() {
        String line = "0.233: [Concurrent reset, start]\n";
        List<String> containsStrings = Arrays.asList(", start", "blabla");
        assertThat("should detect string", dataReader.contains(line, containsStrings, false), is(true));
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

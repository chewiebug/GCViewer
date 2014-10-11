package com.tagtraum.perf.gcviewer.exp;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.tagtraum.perf.gcviewer.exp.impl.SimpleGcWriter;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Tests the export format of {@link SimpleGcWriter}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 08.12.2012</p>
 */
public class SimpleGcWriterTest {
    private GCModel gcModel;
    
    @Before
    public void setUp() throws Exception {
//      0.677: [GC 0.677: [ParNew: 118010K->13046K(118016K), 0.0299506 secs] 175499K->104936K(249088K), 0.0300629 secs] [Times: user=0.06 sys=0.06, real=0.03 secs] 
//      0.708: [GC [1 CMS-initial-mark: 91890K(131072K)] 109176K(249088K), 0.0004006 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
//      0.708: [CMS-concurrent-mark-start]
//      0.714: [CMS-concurrent-mark: 0.006/0.006 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 
//      0.714: [CMS-concurrent-preclean-start]
//      0.715: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
//      0.715: [CMS-concurrent-abortable-preclean-start]
//      0.769: [GC 0.769: [ParNew: 118006K->118006K(118016K), 0.0000176 secs]0.769: [CMS0.769: [CMS-concurrent-abortable-preclean: 0.001/0.055 secs] [Times: user=0.02 sys=0.00, real=0.06 secs] 
//       (concurrent mode failure): 91890K->20323K(131072K), 0.0186232 secs] 209896K->20323K(249088K), [CMS Perm : 2561K->2560K(21248K)], 0.0187293 secs] [Times: user=0.01 sys=0.00, real=0.02 secs] 
//      0.877: [GC 0.877: [ParNew: 104960K->13040K(118016K), 0.0256080 secs] 125283K->78706K(249088K), 0.0256748 secs] [Times: user=0.08 sys=0.00, real=0.03 secs] 

        GCEvent gcEvent = new GCEvent(0.677, 175499, 104936, 249088, 0.0300629, AbstractGCEvent.Type.GC);
        GCEvent parNew = new GCEvent(0.677, 118010, 13046, 118016, 0.0299506, AbstractGCEvent.Type.PAR_NEW);
        gcEvent.add(parNew);

        GCEvent gcEvent2 = new GCEvent(0.708, 109176, 109176, 249088, 0.0004006, AbstractGCEvent.Type.GC);
        GCEvent initialMark = new GCEvent(0.708, 91890, 91890, 131072, 0, AbstractGCEvent.Type.CMS_INITIAL_MARK);
        gcEvent2.add(initialMark);
        
        ConcurrentGCEvent concGCEvent = new ConcurrentGCEvent();
        concGCEvent.setTimestamp(0.708);
        concGCEvent.setType(AbstractGCEvent.Type.CMS_CONCURRENT_MARK_START);
        
        gcModel = new GCModel();
        gcModel.add(gcEvent);
        gcModel.add(gcEvent2);
        gcModel.add(concGCEvent);
    }
    
    @Test
    public void exportLocaleDe() throws Exception {
        Locale.setDefault(new Locale("de", "ch"));
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                SimpleGcWriter writer = new SimpleGcWriter(outputStream)) {
            
            writer.write(gcModel);
            
            String[] lines = outputStream.toString().split(System.getProperty("line.separator"));
            assertEquals("line count", 2, lines.length);
            
            String[] firstLine = lines[0].split(" ");
            assertEquals("number of parts in line 1", 3, firstLine.length);
            assertEquals("name of event", "YoungGC", firstLine[0]);
            assertEquals("timestamp", "0.677000", firstLine[1]);
            assertEquals("duration", "0.030063", firstLine[2]);
            
            String[] secondLine = lines[1].split(" ");
            assertEquals("number of parts in line 2", 3, secondLine.length);
            assertEquals("name of event 2", "InitialMarkGC", secondLine[0]);
        }
    }

    @Test
    public void exportLocaleSv() throws Exception {
        Locale.setDefault(new Locale("sv", "se"));
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                SimpleGcWriter writer = new SimpleGcWriter(outputStream)) {
        
            writer.write(gcModel);
            
            String[] lines = outputStream.toString().split(System.getProperty("line.separator"));
            assertEquals("line count", 2, lines.length);
            
            String[] firstLine = lines[0].split(" ");
            assertEquals("number of parts in line 1", 3, firstLine.length);
            assertEquals("timestamp", "0.677000", firstLine[1]);
        }
    }
}

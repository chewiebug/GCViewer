package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestDataReaderSun1_2_2 {

    @Test
    public void testParse1() throws Exception {
        AbstractGCEvent<GCEvent> event1 = new GCEvent(0, 817, 187, 819, 0.008, AbstractGCEvent.Type.GC);
        event1.getGeneration();
        AbstractGCEvent<GCEvent> event2 = new GCEvent(0.02, 775, 188, 819, 0.005, AbstractGCEvent.Type.GC);
        event2.getGeneration();
        AbstractGCEvent<GCEvent> event3 = new GCEvent(0.741, 1213, 1213, 1639, 0.0, AbstractGCEvent.Type.GC);
        event3.getGeneration();
        ByteArrayInputStream in = new ByteArrayInputStream(("<GC: 0 milliseconds since last GC>\n" +
                "<GC: freed 2807 objects, 645224 bytes in 8 ms, 77% free (646672/838856)>\n" +
                "  <GC: init&scan: 0 ms, scan handles: 7 ms, sweep: 1 ms, compact: 0 ms>\n" +
                "  <GC: 0 register-marked objects, 4 stack-marked objects>\n" +
                "  <GC: 1 register-marked handles, 42 stack-marked handles>\n" +
                "  <GC: refs: soft 0 (age >= 32), weak 0, final 2, phantom 0>\n" +
                "<GC: managing allocation failure: need 2128 bytes, type=1, action=1>\n" +
                "<GC: 20 milliseconds since last GC>\n" +
                "<GC: freed 672 objects, 601032 bytes in 5 ms, 77% free (646040/838856)>\n" +
                "<GC: 721 milliseconds since last GC>\n" +
                "<GC: expanded object space by 839680 to 1678536 bytes, 74% free>\n").getBytes());
        DataReader reader = new DataReaderSun1_2_2(new GcResourceFile("byteArray"), in);
        GCModel model = reader.read();
        assertEquals(3, model.size());
        Iterator<GCEvent> i = model.getGCEvents();
        GCEvent event = i.next();
        System.err.println(event.toString());
        assertEquals(event1, event);
        event = i.next();
        System.err.println(event.toString());
        assertEquals(event2, event);
        event = i.next();
        System.err.println(event.toString());
        assertEquals(event3, event);
    }

}

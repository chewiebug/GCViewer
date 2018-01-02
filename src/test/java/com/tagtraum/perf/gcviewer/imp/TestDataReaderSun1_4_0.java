package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Test;

/**
 * Tests some cases for java 1.4 (using DataReaderSun1_6_0).
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestDataReaderSun1_4_0 {
    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(FOLDER.OPENJDK, fileName);
    }

   /**
     * Test output for -XX:+PrintAdaptiveSizePolicy
     */
    @Test
    public void testAdaptiveSizePolicy() throws Exception {
        String fileName = "SampleSun1_4_0AdaptiveSizePolicy.txt";
        InputStream in = getInputStream(fileName);
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile(fileName), in, GcLogType.SUN1_4);
        GCModel model = reader.read();

        assertEquals("number of events", 9, model.getPause().getN());
        assertEquals("number of full gcs", 3, model.getFullGCPause().getN());
        assertEquals("number of gcs", 6, model.getGCPause().getN());
        assertEquals("total pause", 0.1978746, model.getPause().getSum(), 0.000001);
        assertEquals("full gc pause", 0.026889, model.getFullGCPause().getSum(), 0.000001);
        assertEquals("gc pause", 0.1709856, model.getGCPause().getSum(), 0.000001);
    }

    @Test
    public void testParse1() throws Exception {
        // original testcase was written with timestamp "2.23492e-006d" as first timestamp
        // I have never seen a timestamp writte in scientific format in the logfiles, so
        // I assume, that was some experiment here in the unittest
        AbstractGCEvent<GCEvent> event1 = new GCEvent(0, 8968, 8230, 10912, 0.0037192d, AbstractGCEvent.Type.GC);
        event1.getGeneration();
        AbstractGCEvent<GCEvent> event2 = new GCEvent(1, 8968, 8230, 10912, 0.0037192d, AbstractGCEvent.Type.GC);
        event2.getGeneration();
        AbstractGCEvent<GCEvent> event3 = new GCEvent(2, 8968, 8230, 10912, 0.0037192d, AbstractGCEvent.Type.GC);
        event3.getGeneration();
        AbstractGCEvent<GCEvent> event4 = new GCEvent(3, 10753, 6046, 10912, 0.3146707d, AbstractGCEvent.Type.FULL_GC);
        event4.getGeneration();
        AbstractGCEvent<GCEvent> event5 = new GCEvent(4, 10753, 6046, 10912, 0.3146707d, AbstractGCEvent.Type.INC_GC);
        event5.getGeneration();
        AbstractGCEvent<GCEvent> event6 = new GCEvent(5, 52471, 22991, 75776, 1.0754938d, AbstractGCEvent.Type.GC);
        event6.getGeneration();
        ByteArrayInputStream in = new ByteArrayInputStream("0.0: [GC 8968K->8230K(10912K), 0.0037192 secs]\r\n1.0: [GC 8968K->8230K(10912K), 0.0037192 secs]\r\n2.0: [GC 8968K->8230K(10912K), 0.0037192 secs]\r\n3.0: [Full GC 10753K->6046K(10912K), 0.3146707 secs]\r\n4.0: [Inc GC 10753K->6046K(10912K), 0.3146707 secs]\r\n5.0: [GC Desired survivor size 3342336 bytes, new threshold 1 (max 32) - age   1:  6684672 bytes,  6684672 total 52471K->22991K(75776K), 1.0754938 secs]".getBytes());
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile("byteArray"), in, GcLogType.SUN1_4);
        GCModel model = reader.read();
        assertEquals("model size", 6, model.size());
        Iterator<AbstractGCEvent<?>> i = model.getStopTheWorldEvents();
        AbstractGCEvent<?> event = i.next();
        assertEquals("event 1", event, event1);
        event = i.next();
        assertEquals("event 2", event, event2);
        event = i.next();
        assertEquals("event 3", event, event3);
        event = i.next();
        assertEquals("event 4", event, event4);
        event = i.next();
        assertEquals("event 5", event, event5);
        event = i.next();
        assertEquals("event 6", event, event6);

        assertEquals("running time", 5 + 1.0754938, model.getRunningTime(), 0.0001);
        assertEquals("throughput", 71.75550076275, model.getThroughput(), 0.0000001);
    }

    @Test
    public void testNoFullGC() throws Exception {
        String fileName = "SampleSun1_4_2NoFullGC.txt";
        InputStream in = getInputStream(fileName);
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile(fileName), in, GcLogType.SUN1_4);
        GCModel model = reader.read();
        // we just look at the first six...
        /*
        0.000: [GC 511K->180K(1984K), 0.0095672 secs]
        0.691: [GC 433K->233K(1984K), 0.0056869 secs]
        1.030: [GC 745K->242K(1984K), 0.0043429 secs]
        1.378: [GC 753K->452K(1984K), 0.0094429 secs]
        2.499: [GC 964K->690K(1984K), 0.0108058 secs]
        2.831: [GC 1202K->856K(1984K), 0.0122599 secs]
        */
        AbstractGCEvent<GCEvent> event1 = new GCEvent(0.0d, 511, 180, 1984, 0.0095672d, AbstractGCEvent.Type.GC);
        event1.getGeneration();
        AbstractGCEvent<GCEvent> event2 = new GCEvent(0.691d, 433, 233, 1984, 0.0056869d, AbstractGCEvent.Type.GC);
        event2.getGeneration();
        AbstractGCEvent<GCEvent> event3 = new GCEvent(1.030d, 745, 242, 1984, 0.0043429d, AbstractGCEvent.Type.GC);
        event3.getGeneration();
        AbstractGCEvent<GCEvent> event4 = new GCEvent(1.378d, 753, 452, 1984, 0.0094429d, AbstractGCEvent.Type.GC);
        event4.getGeneration();
        AbstractGCEvent<GCEvent> event5 = new GCEvent(2.499d, 964, 690, 1984, 0.0108058d, AbstractGCEvent.Type.GC);
        event5.getGeneration();
        AbstractGCEvent<GCEvent> event6 = new GCEvent(2.831d, 1202, 856, 1984, 0.0122599d, AbstractGCEvent.Type.GC);
        event6.getGeneration();

        assertEquals("model size", 12, model.size());
        Iterator<GCEvent> i = model.getGCEvents();
        AbstractGCEvent<GCEvent> event = i.next();
        assertEquals("event 1", event, event1);
        event = i.next();
        assertEquals("event 2", event, event2);
        event = i.next();
        assertEquals("event 3", event, event3);
        event = i.next();
        assertEquals("event 4", event, event4);
        event = i.next();
        assertEquals("event 5", event, event5);
        event = i.next();
        assertEquals("event 6", event, event6);

        assertEquals("throughput", 98.928592417159, model.getThroughput(), 0.00000000001);
    }

    @Test
    public void testPrintGCDetails() throws Exception {
        String fileName = "SampleSun1_4_2PrintGCDetails.txt";
        InputStream in = getInputStream(fileName);
        final DataReader reader = new DataReaderSun1_6_0(new GcResourceFile(fileName), in, GcLogType.SUN1_4);
        GCModel model = reader.read();
        /*
        0.000: [GC 0.000: [DefNew: 1534K->128K(1664K), 0.0082759 secs] 1534K->276K(16256K), 0.0084272 secs]
        11.653: [Full GC 11.653: [Tenured: 5634K->5492K(14592K), 0.2856516 secs] 6560K->5492K(16256K), 0.2858561 secs]
        22.879: [GC 22.879: [DefNew: 1855K->125K(1856K), 0.0099038 secs]22.889: [Tenured: 14638K->9916K(14720K), 0.8038262 secs] 16358K->9916K(16576K), 0.8142078 secs]
        31.788: [Full GC 31.788: [Tenured: 16141K->13914K(16528K), 0.8032950 secs] 17881K->13914K(18640K), 0.8036514 secs]
        */
        GCEvent event1 = new GCEvent(0.0d, 1534, 276, 16256, 0.0084272d, AbstractGCEvent.Type.GC);
        GCEvent childEvent1 = new GCEvent(0.0d, 1534, 128, 1664, 0.0082759d, AbstractGCEvent.Type.DEF_NEW);
        childEvent1.getGeneration();
        event1.add(childEvent1);
        event1.getGeneration();

        GCEvent event2 = new GCEvent(11.653d, 6560, 5492, 16256, 0.2858561d, AbstractGCEvent.Type.FULL_GC);
        GCEvent childEvent2 = new GCEvent(11.653d, 5634, 5492, 14592, 0.2856516d, AbstractGCEvent.Type.TENURED);
        event2.add(childEvent2);
        event2.getGeneration();

        GCEvent event3 = new GCEvent(22.879d, 16358, 9916, 16576, 0.8142078d, AbstractGCEvent.Type.GC);
        GCEvent childEvent3a = new GCEvent(22.879d, 1855, 125, 1856, 0.0099038d, AbstractGCEvent.Type.DEF_NEW);
        event3.add(childEvent3a);
        GCEvent childEvent3b = new GCEvent(22.889d, 14638, 9916, 14720, 0.8038262d, AbstractGCEvent.Type.TENURED);
        event3.add(childEvent3b);
        event3.getGeneration();

        GCEvent event4 = new GCEvent(31.788d, 17881, 13914, 18640, 0.8036514d, AbstractGCEvent.Type.FULL_GC);
        GCEvent childEvent4 = new GCEvent(31.788d, 16141, 13914, 16528, 0.8032950d, AbstractGCEvent.Type.TENURED);
        event4.add(childEvent4);
        event4.getGeneration();

        assertEquals("model.size()", 4, model.size());
        Iterator<AbstractGCEvent<?>> i = model.getStopTheWorldEvents();
        AbstractGCEvent<?> event = i.next();
        assertEquals("event 1", event1, event);
        event = i.next();
        assertEquals("event 2", event2, event);
        event = i.next();
        assertEquals("event 3", event3, event);
        event = i.next();
        assertEquals("event 4", event4, event);

        assertEquals("throughput", 94.133029724, model.getThroughput(), 0.000001);
    }

    @Test
    public void testPrintHeapAtGC() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleSun1_4_0PSPrintHeapAtGC.txt");
        gcResource.getLogger().addHandler(handler);

        InputStream in = getInputStream(gcResource.getResourceName());
        final DataReader reader = new DataReaderSun1_6_0(gcResource, in, GcLogType.SUN1_4);
        GCModel model = reader.read();

        assertEquals("GC count", 2, model.size());
        assertEquals("GC pause", 0.0083579, model.getGCPause().getMax(), 0.00000001);
        assertEquals("Full GC pause", 0.0299536, model.getFullGCPause().getMax(), 0.00000001);
        assertEquals("number of errors", 0, handler.getCount());
    }


}

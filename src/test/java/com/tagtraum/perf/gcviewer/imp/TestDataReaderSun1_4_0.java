package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.DataReader;
import com.tagtraum.perf.gcviewer.GCEvent;
import com.tagtraum.perf.gcviewer.GCModel;
import com.tagtraum.perf.gcviewer.AbstractGCEvent;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class TestDataReaderSun1_4_0 extends TestCase {

    public TestDataReaderSun1_4_0(String name) {
        super(name);
    }

    public void testParse1() throws Exception {
        AbstractGCEvent event1 = new GCEvent(2.23492e-006d, 8968, 8230, 10912, 0.0037192d, GCEvent.Type.GC);
        AbstractGCEvent event2 = new GCEvent(1, 8968, 8230, 10912, 0.0037192d, GCEvent.Type.GC);
        AbstractGCEvent event3 = new GCEvent(2, 8968, 8230, 10912, 0.0037192d, GCEvent.Type.GC);
        AbstractGCEvent event4 = new GCEvent(3, 10753, 6046, 10912, 0.3146707d, GCEvent.Type.FULL_GC);
        AbstractGCEvent event5 = new GCEvent(4, 10753, 6046, 10912, 0.3146707d, GCEvent.Type.INC_GC);
        AbstractGCEvent event6 = new GCEvent(5, 52471, 22991, 75776, 1.0754938d, GCEvent.Type.GC);
        ByteArrayInputStream in = new ByteArrayInputStream("2.23492e-006: [GC 8968K->8230K(10912K), 0.0037192 secs]\r\n1.0: [GC 8968K->8230K(10912K), 0.0037192 secs]\r\n2.0: [GC 8968K->8230K(10912K), 0.0037192 secs]\r\n3.0: [Full GC 10753K->6046K(10912K), 0.3146707 secs]\r\n4.0: [Inc GC 10753K->6046K(10912K), 0.3146707 secs]\r\n5.0: [GC Desired survivor size 3342336 bytes, new threshold 1 (max 32) - age   1:  6684672 bytes,  6684672 total 52471K->22991K(75776K), 1.0754938 secs]".getBytes());
        DataReader reader = new DataReaderSun1_4_0(in);
        GCModel model = reader.read();
        assertTrue(model.size() == 6);
        Iterator i = model.getGCEvents();
        AbstractGCEvent event = (AbstractGCEvent) i.next();
        assertEquals(event, event1);
        event = (AbstractGCEvent) i.next();
        assertEquals(event, event2);
        event = (AbstractGCEvent) i.next();
        assertEquals(event, event3);
        event = (AbstractGCEvent) i.next();
        assertEquals(event, event4);
        event = (AbstractGCEvent) i.next();
        assertEquals(event, event5);
        event = (AbstractGCEvent) i.next();
        assertEquals(event, event6);
        
        assertEquals("throughput", 65.680144, model.getThroughput(), 0.0000001);
    }

    public void testNoFullGC() throws Exception {
        InputStream in = getClass().getResourceAsStream("SampleSun1_4_2NoFullGC.txt");
        DataReader reader = new DataReaderSun1_4_0(in);
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
        AbstractGCEvent event1 = new GCEvent(0.0d, 511, 180, 1984, 0.0095672d, GCEvent.Type.GC);
        AbstractGCEvent event2 = new GCEvent(0.691d, 433, 233, 1984, 0.0056869d, GCEvent.Type.GC);
        AbstractGCEvent event3 = new GCEvent(1.030d, 745, 242, 1984, 0.0043429d, GCEvent.Type.GC);
        AbstractGCEvent event4 = new GCEvent(1.378d, 753, 452, 1984, 0.0094429d, GCEvent.Type.GC);
        AbstractGCEvent event5 = new GCEvent(2.499d, 964, 690, 1984, 0.0108058d, GCEvent.Type.GC);
        AbstractGCEvent event6 = new GCEvent(2.831d, 1202, 856, 1984, 0.0122599d, GCEvent.Type.GC);

        assertTrue(model.size() == 12);
        Iterator i = model.getGCEvents();
        AbstractGCEvent event = (AbstractGCEvent) i.next();
        assertEquals(event, event1);
        event = (AbstractGCEvent) i.next();
        assertEquals(event, event2);
        event = (AbstractGCEvent) i.next();
        assertEquals(event, event3);
        event = (AbstractGCEvent) i.next();
        assertEquals(event, event4);
        event = (AbstractGCEvent) i.next();
        assertEquals(event, event5);
        event = (AbstractGCEvent) i.next();
        assertEquals(event, event6);
        
        assertEquals("throughput", 98.92780024997158, model.getThroughput(), 0.00000000001);
    }


    public void testPrintGCDetails() throws Exception {
        InputStream in = getClass().getResourceAsStream("SampleSun1_4_2PrintGCDetails.txt");
        DataReader reader = new DataReaderSun1_4_0(in);
        GCModel model = reader.read();
        /*
        0.000: [GC 0.000: [DefNew: 1534K->128K(1664K), 0.0082759 secs] 1534K->276K(16256K), 0.0084272 secs]
        11.653: [Full GC 11.653: [Tenured: 5634K->5492K(14592K), 0.2856516 secs] 6560K->5492K(16256K), 0.2858561 secs]
        22.879: [GC 22.879: [DefNew: 1855K->125K(1856K), 0.0099038 secs]22.889: [Tenured: 14638K->9916K(14720K), 0.8038262 secs] 16358K->9916K(16576K), 0.8142078 secs]
        31.788: [Full GC 31.788: [Tenured: 16141K->13914K(16528K), 0.8032950 secs] 17881K->13914K(18640K), 0.8036514 secs]
        */
        AbstractGCEvent event1 = new GCEvent(0.0d, 1534, 276, 16256, 0.0084272d, GCEvent.Type.GC);
        event1.add(new GCEvent(0.0d, 1534, 128, 1664, 0.0082759d, GCEvent.Type.DEF_NEW));
        AbstractGCEvent event2 = new GCEvent(11.653d, 6560, 5492, 16256, 0.2858561d, GCEvent.Type.FULL_GC);
        event2.add(new GCEvent(11.653d, 5634, 5492, 14592, 0.2856516d, GCEvent.Type.TENURED));
        AbstractGCEvent event3 = new GCEvent(22.879d, 16358, 9916, 16576, 0.8142078d, GCEvent.Type.GC);
        event3.add(new GCEvent(22.879d, 1855, 125, 1856, 0.0099038d, GCEvent.Type.DEF_NEW));
        event3.add(new GCEvent(22.889d, 14638, 9916, 14720, 0.8038262d, GCEvent.Type.TENURED));
        AbstractGCEvent event4 = new GCEvent(31.788d, 17881, 13914, 18640, 0.8036514d, GCEvent.Type.FULL_GC);
        event4.add(new GCEvent(31.788d, 16141, 13914, 16528, 0.8032950d, GCEvent.Type.TENURED));

        assertEquals("model.size()", 4, model.size());
        Iterator i = model.getGCEvents();
        AbstractGCEvent event = (AbstractGCEvent) i.next();
        assertEquals(event1, event);
        event = (AbstractGCEvent) i.next();
        assertEquals(event2, event);
        event = (AbstractGCEvent) i.next();
        assertEquals(event3, event);
        event = (AbstractGCEvent) i.next();
        assertEquals(event4, event);

        assertEquals("throughput", 93.984703347, model.getThroughput(), 0.000001);
    }

    public static TestSuite suite() {
        return new TestSuite(TestDataReaderSun1_4_0.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

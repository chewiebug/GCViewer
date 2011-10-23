package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.DataReader;
import com.tagtraum.perf.gcviewer.GCEvent;
import com.tagtraum.perf.gcviewer.GCModel;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class TestDataReaderSun1_3_1 extends TestCase {

    public TestDataReaderSun1_3_1(String name) {
        super(name);
    }

    public void testParse1() throws Exception {
        AbstractGCEvent event1 = new GCEvent(0, 8968, 8230, 10912, 0.0037192d, GCEvent.Type.GC);
        AbstractGCEvent event2 = new GCEvent(1, 8968, 8230, 10912, 0.0037192d, GCEvent.Type.GC);
        AbstractGCEvent event3 = new GCEvent(2, 8968, 8230, 10912, 0.0037192d, GCEvent.Type.GC);
        AbstractGCEvent event4 = new GCEvent(3, 10753, 6046, 10912, 0.3146707d, GCEvent.Type.FULL_GC);
        ByteArrayInputStream in = new ByteArrayInputStream("[GC 8968K->8230K(10912K), 0.0037192 secs]\r\n[GC 8968K->8230K(10[GC 8968K->8230K(10912K), 0.0037192 secs]912K), 0.0037192 secs]\r\n[Full GC 10753K->6046K(10912K), 0.3146707 secs]".getBytes());
        DataReader reader = new DataReaderSun1_3_1(in);
        GCModel model = reader.read();
        assertTrue(model.size() == 4);
        Iterator i = model.getGCEvents();
        AbstractGCEvent event = (AbstractGCEvent) i.next();
        assertEquals(event, event1);
        event = (AbstractGCEvent) i.next();
        assertEquals(event, event2);
        event = (AbstractGCEvent) i.next();
        assertEquals(event, event3);
        event = (AbstractGCEvent) i.next();
        assertEquals(event, event4);

        assertEquals("throughput", 89.13905666, model.getThroughput(), 0.00000001);
    }

    public static TestSuite suite() {
        return new TestSuite(TestDataReaderSun1_3_1.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

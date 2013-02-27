package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

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
        AbstractGCEvent<GCEvent> event1 = new GCEvent(0, 8968, 8230, 10912, 0.0037192d, AbstractGCEvent.Type.GC);
        AbstractGCEvent<GCEvent> event2 = new GCEvent(1, 8968, 8230, 10912, 0.0037192d, AbstractGCEvent.Type.GC);
        AbstractGCEvent<GCEvent> event3 = new GCEvent(2, 8968, 8230, 10912, 0.0037192d, AbstractGCEvent.Type.GC);
        AbstractGCEvent<GCEvent> event4 = new GCEvent(3, 10753, 6046, 10912, 0.3146707d, AbstractGCEvent.Type.FULL_GC);
        ByteArrayInputStream in = new ByteArrayInputStream("[GC 8968K->8230K(10912K), 0.0037192 secs]\r\n[GC 8968K->8230K(10[GC 8968K->8230K(10912K), 0.0037192 secs]912K), 0.0037192 secs]\r\n[Full GC 10753K->6046K(10912K), 0.3146707 secs]".getBytes());
        DataReader reader = new DataReaderSun1_3_1(in, GcLogType.SUN1_3_1);
        GCModel model = reader.read();
        assertTrue(model.size() == 4);
        Iterator<GCEvent> i = model.getGCEvents();
        AbstractGCEvent<GCEvent> event = i.next();
        assertEquals(event, event1);
        event = i.next();
        assertEquals(event, event2);
        event = i.next();
        assertEquals(event, event3);
        event = i.next();
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

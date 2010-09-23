package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.imp.DataReaderSun1_3_1;
import com.tagtraum.perf.gcviewer.imp.DataReaderSun1_4_0;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.ByteArrayInputStream;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class TestDataReaderFactory extends TestCase {

    public TestDataReaderFactory(String name) {
        super(name);
    }

    public void test1_4_0() throws Exception {
        DataReaderFactory factory = new DataReaderFactory();
        DataReader dr = factory.getDataReader(new ByteArrayInputStream("2.23492e-006: [GC 1087K->462K(16320K), 0.0154134 secs]".getBytes()));
        assertTrue(dr instanceof DataReaderSun1_4_0);
    }

    public void test1_3_1() throws Exception {
        DataReaderFactory factory = new DataReaderFactory();
        DataReader dr = factory.getDataReader(new ByteArrayInputStream("[GC 1087K->462K(16320K), 0.0154134 secs]".getBytes()));
        assertTrue(dr instanceof DataReaderSun1_3_1);
        dr = factory.getDataReader(new ByteArrayInputStream("[Full GC 1087K->462K(16320K), 0.0154134 secs]".getBytes()));
        assertTrue(dr instanceof DataReaderSun1_3_1);
    }

    public static TestSuite suite() {
        return new TestSuite(TestDataReaderFactory.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

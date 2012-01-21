package com.tagtraum.perf.gcviewer.imp;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;

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
        // although the input is java 1.4 the datareader returned should be 1.6
        // (DataReaderSun1_6_0 handles java 1.4, 1.5, 1.6, 1.7)
        DataReaderFactory factory = new DataReaderFactory();
        DataReader dr = factory.getDataReader(new ByteArrayInputStream("2.23492e-006: [GC 1087K->462K(16320K), 0.0154134 secs]".getBytes()));
        assertTrue(dr instanceof DataReaderSun1_6_0);
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

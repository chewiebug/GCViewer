package com.tagtraum.perf.gcviewer.imp;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.InputStream;


/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author Ruwin Veldwijk
 * @version $Id: $
 */
public class TestDataReaderIBMi5OS1_4_2 extends TestCase {

    public TestDataReaderIBMi5OS1_4_2(final String name) {
        super(name);
    }

    public void testParse1() throws Exception {
        final InputStream in = getClass().getResourceAsStream("SampleIBMi5OS1_4_2.txt");
        final DataReader reader = new DataReaderIBMi5OS1_4_2(in);
        reader.read();
    }

    public static TestSuite suite() {
        return new TestSuite(TestDataReaderIBMi5OS1_4_2.class);
    }

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(TestDataReaderIBMi5OS1_4_2.suite());
    }
}

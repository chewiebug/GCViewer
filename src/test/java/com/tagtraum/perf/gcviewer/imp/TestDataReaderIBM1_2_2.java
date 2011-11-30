package com.tagtraum.perf.gcviewer.imp;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.InputStream;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class TestDataReaderIBM1_2_2 extends TestCase {

    public TestDataReaderIBM1_2_2(String name) {
        super(name);
    }

    public void testParse1() throws Exception {
        InputStream in = getClass().getResourceAsStream("SampleIBM1_2_2.txt");
        DataReader reader = new DataReaderIBM1_3_0(in);
        reader.read();
    }

    public static TestSuite suite() {
        return new TestSuite(TestDataReaderIBM1_2_2.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

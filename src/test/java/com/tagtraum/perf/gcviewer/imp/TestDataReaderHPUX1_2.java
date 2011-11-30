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
public class TestDataReaderHPUX1_2 extends TestCase {

    public TestDataReaderHPUX1_2(String name) {
        super(name);
    }

    public void testParse1() throws Exception {
        InputStream in = getClass().getResourceAsStream("SampleHP-UX1_3.txt");
        DataReader reader = new DataReaderHPUX1_2(in);
        reader.read();
    }

    public static TestSuite suite() {
        return new TestSuite(TestDataReaderHPUX1_2.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.DataReader;
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
public class TestDataReaderSun1_3_1_19 extends TestCase {

    public TestDataReaderSun1_3_1_19(String name) {
        super(name);
    }

    public void testCMSPrintGCDetails() throws Exception {
        final InputStream in = getClass().getResourceAsStream("SampleSun1_3_1_19SunOS.txt");
        final DataReader reader = new DataReaderSun1_3_1(in);
        reader.read();
    }

    public static TestSuite suite() {
        return new TestSuite(TestDataReaderSun1_3_1.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
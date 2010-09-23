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
public class TestDataReaderSun1_5_0 extends TestCase {

    public TestDataReaderSun1_5_0(final String name) {
        super(name);
    }

    public void testCMSPrintGCDetails() throws Exception {
        final InputStream in = getClass().getResourceAsStream("SampleSun1_5_0CMS_PrintGCDetails.txt");
        final DataReader reader = new DataReaderSun1_5_0(in);
        reader.read();
    }

    public void testParallelOldGC() throws Exception {
        final InputStream in = getClass().getResourceAsStream("SampleSun1_5_0ParallelOldGC.txt");
        final DataReader reader = new DataReaderSun1_5_0(in);
        reader.read();
    }

    public void testCMSIncrementalPacing() throws Exception {
        final InputStream in = getClass().getResourceAsStream("SampleSun1_5_0CMS_IncrementalPacing.txt");
        final DataReader reader = new DataReaderSun1_5_0(in);
        reader.read();
    }

    public void testPromotionFailure() throws Exception {
        final InputStream in = getClass().getResourceAsStream("SampleSun1_5_0PromotionFailure.txt");
        final DataReader reader = new DataReaderSun1_5_0(in);
        reader.read();
    }

    public void testCMSConcurrentModeFailure() throws Exception {
        final InputStream in = getClass().getResourceAsStream("SampleSun1_5_0ConcurrentModeFailure.txt");
        final DataReader reader = new DataReaderSun1_5_0(in);
        reader.read();
    }

    public static TestSuite suite() {
        return new TestSuite(TestDataReaderSun1_5_0.class);
    }

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

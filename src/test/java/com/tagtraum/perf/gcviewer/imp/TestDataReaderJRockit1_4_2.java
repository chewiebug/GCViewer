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
public class TestDataReaderJRockit1_4_2 extends TestCase {

    public TestDataReaderJRockit1_4_2(String name) {
        super(name);
    }

    public void testParseGenConBig() throws Exception {
        InputStream in = getClass().getResourceAsStream("SampleJRockit1_4_2gencon-big.txt");
        DataReader reader = new DataReaderJRockit1_4_2(in);
        reader.read();
    }

    public void testParseGenCon() throws Exception {
        InputStream in = getClass().getResourceAsStream("SampleJRockit1_4_2gencon.txt");
        DataReader reader = new DataReaderJRockit1_4_2(in);
        reader.read();
    }

    public void testParseParallel() throws Exception {
        InputStream in = getClass().getResourceAsStream("SampleJRockit1_4_2parallel.txt");
        DataReader reader = new DataReaderJRockit1_4_2(in);
        reader.read();
    }

    public void testParsePrioPauseTime() throws Exception {
        InputStream in = getClass().getResourceAsStream("SampleJRockit1_4_2priopausetime.txt");
        DataReader reader = new DataReaderJRockit1_4_2(in);
        reader.read();
    }

    public void testParseTsGCReportPrioPauseTime() throws Exception {
        InputStream in = getClass().getResourceAsStream("SampleJRockit1_4_2ts-gcreport-gcpriopausetime.txt");
        DataReader reader = new DataReaderJRockit1_4_2(in);
        reader.read();
    }

    public void testParseTsGCReportPrioThroughput() throws Exception {
        InputStream in = getClass().getResourceAsStream("SampleJRockit1_4_2ts-gcreport-gcpriothroughput.txt");
        DataReader reader = new DataReaderJRockit1_4_2(in);
        reader.read();
    }

    public void testParseTsGCReportGencon() throws Exception {
        InputStream in = getClass().getResourceAsStream("SampleJRockit1_4_2ts-gcreport-gencon.txt");
        DataReader reader = new DataReaderJRockit1_4_2(in);
        reader.read();
    }

    public void testParseTsGCReportParallel() throws Exception {
        InputStream in = getClass().getResourceAsStream("SampleJRockit1_4_2ts-gcreport-parallel.txt");
        DataReader reader = new DataReaderJRockit1_4_2(in);
        reader.read();
    }

    public void testParseTsGCReportSinglecon() throws Exception {
        InputStream in = getClass().getResourceAsStream("SampleJRockit1_4_2ts-gcreport-singlecon.txt");
        DataReader reader = new DataReaderJRockit1_4_2(in);
        reader.read();
    }

    public static TestSuite suite() {
        return new TestSuite(TestDataReaderJRockit1_4_2.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

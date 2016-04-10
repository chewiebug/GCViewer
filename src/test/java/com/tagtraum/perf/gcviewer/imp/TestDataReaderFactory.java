package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;

/**
 * Tests the logic of the {@link DataReaderFactory}
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 */
public class TestDataReaderFactory {


    @Rule 
    public TestName name = new TestName();

    private InputStream getInputStreamIBM(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_IBM, fileName);
    }

    private InputStream getInputStreamJRockit(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_JROCKIT, fileName);
    }
    
    private InputStream getInputStreamOpenJdk(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_OPENJDK, fileName);
    }
    
    private void assertDataReader(
            Class<? extends DataReader> expected, 
            Class<? extends DataReader> actual) {

        assertDataReader(name.getMethodName(), expected, actual);
    }
    
    /**
     * Tests for equality of two {@link DataReader} classes.
     * 
     * @param testName name of the test to identify which test failed
     * @param expected expected type of DataReader
     * @param actual actual type of DataReader
     */
    private void assertDataReader(String testName, 
            Class<? extends DataReader> expected, 
            Class<? extends DataReader> actual) {
        
        assertEquals(testName, expected.getCanonicalName(), actual.getCanonicalName());
    }
    
    @Test
    public void testGetDataReaderJDK6GZipped() throws Exception {
        String sampleGz = "SampleSun1_6_0PrintHeapAtGC.txt.gz";
        try (InputStream in = getInputStreamOpenJdk(sampleGz)) {
        
            final DataReader reader = new DataReaderFactory().getDataReader(new GCResource(sampleGz), in);
            assertDataReader("getDataReader() reading " + sampleGz, DataReaderSun1_6_0.class, reader.getClass());

            GCModel model = reader.read();
            assertEquals("# events", 2, model.size());
        }
    }

    @Test
    public void getDataReaderG1() throws Exception {
        String sampleFile = "SampleSun1_7_0G1-ApplicationStopped.txt";
        try (InputStream in = getInputStreamOpenJdk(sampleFile)) {

            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(sampleFile), in);
            assertDataReader("getDataReader() reading " + sampleFile, DataReaderSun1_6_0G1.class, reader.getClass());

            GCModel model = reader.read();
            assertEquals("# events", 55, model.size());
        }
    }

    @Test
    public void testIBMi5OS1_4_2() throws Exception {
        String fileName = "SampleIBMi5OS1_4_2.txt";
        try (InputStream in = getInputStreamIBM(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderIBMi5OS1_4_2.class, reader.getClass());
        }
    }

    @Test
    public void testIBMJ9_5_0() throws Exception {
        String fileName = "SampleIBMJ9_5_0af-global-200811_07.txt";
        try (InputStream in = getInputStreamIBM(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderIBM_J9_5_0.class, reader.getClass());
        }
    }

    @Test
    public void testIBMJ9_R26() throws Exception {
        String fileName = "SampleIBMJ9_R26_GAFP1_full_header.txt";
        try (InputStream in = getInputStreamIBM(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderIBM_J9_R28.class, reader.getClass());
        }
    }

    @Test
    public void testIBMJ9_R27() throws Exception {
        String fileName = "SampleIBMJ9_R27_SR1_full_header.txt";
        try (InputStream in = getInputStreamIBM(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderIBM_J9_R28.class, reader.getClass());
        }
    }

    @Test
    public void testJRockit1_4GcReportGenCon() throws Exception { 
        String fileName = "SampleJRockit1_4_2ts-gcreport-gencon.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_4_2.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4GcReportParallel() throws Exception { 
        String fileName = "SampleJRockit1_4_2ts-gcreport-parallel.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_4_2.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4GcReportPrioPauseTime() throws Exception { 
        String fileName = "SampleJRockit1_4_2ts-gcreport-gcpriopausetime.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_4_2.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4GcReportPrioThroughput() throws Exception {
        String fileName = "SampleJRockit1_4_2ts-gcreport-gcpriothroughput.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_4_2.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4GcReportSingleCon() throws Exception { 
        String fileName = "SampleJRockit1_4_2ts-gcreport-singlecon.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_4_2.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4GenCon() throws Exception { 
        String fileName = "SampleJRockit1_4_2gencon.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4GenConBig() throws Exception { 
        String fileName = "SampleJRockit1_4_2gencon-big.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4Parallel() throws Exception { 
        String fileName = "SampleJRockit1_4_2parallel.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4PrioPauseTime() throws Exception { 
        String fileName = "SampleJRockit1_4_2priopausetime.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_5GenCon() throws Exception { 
        String fileName = "SampleJRockit1_5_12_gencon.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_5GenConMemStats() throws Exception { 
        String fileName = "SampleJRockit1_5_20_memstats2.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_6_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_5GenPar() throws Exception { 
        String fileName = "SampleJRockit1_5_12_genpar.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_5PrioPausetime() throws Exception { 
        String fileName = "SampleJRockit1_5_12_gcpriopausetime.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_5PrioThroughput() throws Exception { 
        String fileName = "SampleJRockit1_5_12_gcpriothroughput.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_5SimpleOpts() throws Exception { 
        String fileName = "SampleJRockit1_5_12-gcreport-simpleopts-singlecon.txt"; 
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_6GenConVerbose() throws Exception { 
        String fileName = "SampleJRockit1_6_verbose_gc_mode_gencon.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_6_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_6GenParVerbose() throws Exception {
        String fileName = "SampleJRockit1_6_33_gc_mode_genpar_verbosenursery.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_6_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_6ParCon() throws Exception { 
        String fileName = "SampleJRockit1_6_gc_mode_singleparcon.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_6_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_6ParConVerbose() throws Exception { 
        String fileName = "SampleJRockit1_6_verbose_gc_mode_singleparcon.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GCResource(fileName), in);
            assertDataReader(DataReaderJRockit1_6_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testSun1_3_1() throws Exception {
        DataReaderFactory factory = new DataReaderFactory();
        DataReader dr = factory.getDataReader(new GCResource("byteArray"), new ByteArrayInputStream("[GC 1087K->462K(16320K), 0.0154134 secs]".getBytes()));
        assertDataReader("Sun1_3_1 GC", DataReaderSun1_3_1.class, dr.getClass());

        dr = factory.getDataReader(new GCResource("byteArray2"), new ByteArrayInputStream("[Full GC 1087K->462K(16320K), 0.0154134 secs]".getBytes()));
        assertDataReader("Sun1_3_1 Full GC", DataReaderSun1_3_1.class, dr.getClass());
    }

    @Test
    public void testSun1_4_0() throws Exception {
        // although the input is java 1.4 the datareader returned should be 1.6
        // (DataReaderSun1_6_0 handles java 1.4, 1.5, 1.6, 1.7)
        DataReaderFactory factory = new DataReaderFactory();
        DataReader dr = factory.getDataReader(new GCResource("byteArray"), new ByteArrayInputStream("2.23492e-006: [GC 1087K->462K(16320K), 0.0154134 secs]".getBytes()));
        assertDataReader(DataReaderSun1_6_0.class, dr.getClass());
    }

}

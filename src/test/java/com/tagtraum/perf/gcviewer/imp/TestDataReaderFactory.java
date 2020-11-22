package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

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
        return UnittestHelper.getResourceAsStream(FOLDER.IBM, fileName);
    }

    private InputStream getInputStreamJRockit(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(FOLDER.JROCKIT, fileName);
    }
    
    private InputStream getInputStreamOpenJdk(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(FOLDER.OPENJDK, fileName);
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
        
            final DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(sampleGz), in);
            assertDataReader("getDataReader() reading " + sampleGz, DataReaderSun1_6_0.class, reader.getClass());

            GCModel model = reader.read();
            assertEquals("# events", 2, model.size());
        }
    }

    @Test
    public void getDataReaderG1() throws Exception {
        String sampleFile = "SampleSun1_7_0G1-ApplicationStopped.txt";
        try (InputStream in = getInputStreamOpenJdk(sampleFile)) {

            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(sampleFile), in);
            assertDataReader("getDataReader() reading " + sampleFile, DataReaderSun1_6_0G1.class, reader.getClass());

            GCModel model = reader.read();
            assertEquals("# events", 55, model.size());
        }
    }

    @Test
    public void testIBMi5OS1_4_2() throws Exception {
        String fileName = "SampleIBMi5OS1_4_2.txt";
        try (InputStream in = getInputStreamIBM(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderIBMi5OS1_4_2.class, reader.getClass());
        }
    }

    @Test
    public void testIBMJ9_5_0() throws Exception {
        String fileName = "SampleIBMJ9_5_0af-global-200811_07.txt";
        try (InputStream in = getInputStreamIBM(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderIBM_J9_5_0.class, reader.getClass());
        }
    }

    @Test
    public void testIBMJ9_R26() throws Exception {
        String fileName = "SampleIBMJ9_R26_GAFP1_full_header.txt";
        try (InputStream in = getInputStreamIBM(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderIBM_J9_R28.class, reader.getClass());
        }
    }

    @Test
    public void testIBMJ9_R27() throws Exception {
        String fileName = "SampleIBMJ9_R27_SR1_full_header.txt";
        try (InputStream in = getInputStreamIBM(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderIBM_J9_R28.class, reader.getClass());
        }
    }

    @Test
    public void testJRockit1_4GcReportGenCon() throws Exception { 
        String fileName = "SampleJRockit1_4_2ts-gcreport-gencon.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_4_2.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4GcReportParallel() throws Exception { 
        String fileName = "SampleJRockit1_4_2ts-gcreport-parallel.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_4_2.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4GcReportPrioPauseTime() throws Exception { 
        String fileName = "SampleJRockit1_4_2ts-gcreport-gcpriopausetime.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_4_2.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4GcReportPrioThroughput() throws Exception {
        String fileName = "SampleJRockit1_4_2ts-gcreport-gcpriothroughput.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_4_2.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4GcReportSingleCon() throws Exception { 
        String fileName = "SampleJRockit1_4_2ts-gcreport-singlecon.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_4_2.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4GenCon() throws Exception { 
        String fileName = "SampleJRockit1_4_2gencon.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4GenConBig() throws Exception { 
        String fileName = "SampleJRockit1_4_2gencon-big.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4Parallel() throws Exception { 
        String fileName = "SampleJRockit1_4_2parallel.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_4PrioPauseTime() throws Exception { 
        String fileName = "SampleJRockit1_4_2priopausetime.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_5GenCon() throws Exception { 
        String fileName = "SampleJRockit1_5_12_gencon.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_5GenConMemStats() throws Exception { 
        String fileName = "SampleJRockit1_5_20_memstats2.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_6_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_5GenPar() throws Exception { 
        String fileName = "SampleJRockit1_5_12_genpar.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_5PrioPausetime() throws Exception { 
        String fileName = "SampleJRockit1_5_12_gcpriopausetime.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_5PrioThroughput() throws Exception { 
        String fileName = "SampleJRockit1_5_12_gcpriothroughput.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_5SimpleOpts() throws Exception { 
        String fileName = "SampleJRockit1_5_12-gcreport-simpleopts-singlecon.txt"; 
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_5_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_6GenConVerbose() throws Exception { 
        String fileName = "SampleJRockit1_6_verbose_gc_mode_gencon.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_6_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_6GenParVerbose() throws Exception {
        String fileName = "SampleJRockit1_6_33_gc_mode_genpar_verbosenursery.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_6_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_6ParCon() throws Exception { 
        String fileName = "SampleJRockit1_6_gc_mode_singleparcon.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_6_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testJRockit1_6ParConVerbose() throws Exception { 
        String fileName = "SampleJRockit1_6_verbose_gc_mode_singleparcon.txt";
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(DataReaderJRockit1_6_0.class, reader.getClass());
        }
    }
    
    @Test
    public void testSun1_3_1() throws Exception {
        DataReaderFactory factory = new DataReaderFactory();
        DataReader dr = factory.getDataReader(new GcResourceFile("byteArray"), new ByteArrayInputStream("[GC 1087K->462K(16320K), 0.0154134 secs]".getBytes()));
        assertDataReader("Sun1_3_1 GC", DataReaderSun1_3_1.class, dr.getClass());

        dr = factory.getDataReader(new GcResourceFile("byteArray2"), new ByteArrayInputStream("[Full GC 1087K->462K(16320K), 0.0154134 secs]".getBytes()));
        assertDataReader("Sun1_3_1 Full GC", DataReaderSun1_3_1.class, dr.getClass());
    }

    @Test
    public void testSun1_4_0() throws Exception {
        // although the input is java 1.4 the datareader returned should be 1.6
        // (DataReaderSun1_6_0 handles java 1.4, 1.5, 1.6, 1.7)
        DataReaderFactory factory = new DataReaderFactory();
        DataReader dr = factory.getDataReader(new GcResourceFile("byteArray"), new ByteArrayInputStream("2.23492e-006: [GC 1087K->462K(16320K), 0.0154134 secs]".getBytes()));
        assertDataReader(DataReaderSun1_6_0.class, dr.getClass());
    }

    @Test
    public void testOpenJdk8DockerMemory512mCpus0_5() throws IOException {
        // Using an OpenJdk8 docker image and letting the JVM decide on the algorithm (--memory=512m --cpus="0.5")
        DataReaderFactory factory = new DataReaderFactory();
        DataReader dr = factory.getDataReader(new GcResourceFile("byteArray"), new ByteArrayInputStream(("OpenJDK 64-Bit Server VM (25.275-b01) for linux-amd64 JRE (1.8.0_275-b01), built on Nov  6 2020 14:10:46 by \"openjdk\" with gcc 4.4.7 20120313 (Red Hat 4.4.7-23)\n" +
                "Memory: 4k page, physical 524288k(521828k free), swap 1048572k(1048572k free)\n" +
                "CommandLine flags: -XX:InitialHeapSize=8388608 -XX:MaxHeapSize=134217728 -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseCompressedClassPointers -XX:+UseCompressedOops\n").getBytes()));
        assertDataReader(DataReaderSun1_6_0.class, dr.getClass());
    }

    @Test
    public void testOpenJdk8DockerMemory2gCpus2() throws IOException {
        // Using an OpenJdk8 docker image and letting the JVM decide on the algorithm (--memory=2g --cpus="2")
        DataReaderFactory factory = new DataReaderFactory();
        DataReader dr = factory.getDataReader(new GcResourceFile("byteArray"), new ByteArrayInputStream(("OpenJDK 64-Bit Server VM (25.275-b01) for linux-amd64 JRE (1.8.0_275-b01), built on Nov  6 2020 14:10:46 by \"openjdk\" with gcc 4.4.7 20120313 (Red Hat 4.4.7-23)\n" +
                "Memory: 4k page, physical 2097152k(2094956k free), swap 1048572k(1048572k free)\n" +
                "CommandLine flags: -XX:InitialHeapSize=33554432 -XX:MaxHeapSize=536870912 -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseParallelGC\n").getBytes()));
        assertDataReader(DataReaderSun1_6_0.class, dr.getClass());
    }

    @Test
    public void testOracleG1J8() throws Exception {
        DataReaderFactory factory = new DataReaderFactory();
        DataReader dr = factory.getDataReader(new GcResourceFile("byteArray"), new ByteArrayInputStream(("Java HotSpot(TM) 64-Bit Server VM (25.112-b15) for windows-amd64 JRE (1.8.0_112-b15), built on Sep 22 2016 21:31:56 by \"java_re\" with MS VC++ 10.0 (VS2010)\n" +
                "Memory: 4k page, physical 50331128k(13997304k free), swap 60569268k(13009848k free)\n" +
                "CommandLine flags: -XX:CICompilerCount=4 -XX:ConcGCThreads=3 -XX:G1HeapRegionSize=2097152 -XX:GCLogFileSize=1048576 -XX:InitialHeapSize=4294967296 -XX:+ManagementServer -XX:MarkStackSize=4194304 -XX:MaxHeapSize=8589934592 -XX:MaxNewSize=5152702464 -XX:MinHeapDeltaBytes=2097152 -XX:NumberOfGCLogFiles=5 -XX:-OmitStackTraceInFastThrow -XX:+ParallelRefProcEnabled -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution -XX:+ReduceSignalUsage -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseFastUnorderedTimeStamps -XX:+UseG1GC -XX:+UseGCLogFileRotation -XX:-UseLargePagesIndividualAllocation\n" +
                "2017-12-01T14:14:50.781-0600: 1501608.217: [GC pause (G1 Evacuation Pause) (mixed)\n").getBytes()));
        assertDataReader(DataReaderSun1_6_0G1.class, dr.getClass());
    }

    @Test
    public void testOracleG1J8ApplicationThreadsStopped() throws Exception {
        // logs with -XX:+PrintGCApplicationStoppedTime often start with a lot of "Total time for which ..." lines
        DataReaderFactory factory = new DataReaderFactory();
        DataReader dr = factory.getDataReader(new GcResourceFile("byteArray"), new ByteArrayInputStream(("Java HotSpot(TM) 64-Bit Server VM (25.112-b15) for windows-amd64 JRE (1.8.0_112-b15), built on Sep 22 2016 21:31:56 by \"java_re\" with MS VC++ 10.0 (VS2010)\n" +
                "Memory: 4k page, physical 50331128k(13997304k free), swap 60569268k(13009848k free)\n" +
                "CommandLine flags: -XX:CICompilerCount=4 -XX:ConcGCThreads=3 -XX:G1HeapRegionSize=2097152 -XX:GCLogFileSize=1048576 -XX:InitialHeapSize=4294967296 -XX:+ManagementServer -XX:MarkStackSize=4194304 -XX:MaxHeapSize=8589934592 -XX:MaxNewSize=5152702464 -XX:MinHeapDeltaBytes=2097152 -XX:NumberOfGCLogFiles=5 -XX:-OmitStackTraceInFastThrow -XX:+ParallelRefProcEnabled -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution -XX:+ReduceSignalUsage -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseFastUnorderedTimeStamps -XX:+UseG1GC -XX:+UseGCLogFileRotation -XX:-UseLargePagesIndividualAllocation\n" +
                "2019-12-15T15:53:20.985+0100: 82113.171: Total time for which application threads were stopped: 0.0019721 seconds, Stopping threads took: 0.0001040 seconds\n\n").getBytes()));
        assertDataReader(DataReaderSun1_6_0G1.class, dr.getClass());
    }

    @Test
    public void testOracleG1J8_StringDeduplication() throws Exception {
        DataReaderFactory factory = new DataReaderFactory();
        DataReader dr = factory.getDataReader(new GcResourceFile("byteArray"),
                getInputStreamOpenJdk("SampleSun1_8_0G1StringDeduplication.txt"));
        assertDataReader(DataReaderSun1_6_0G1.class, dr.getClass());
    }

    @Test
    public void testOracleShenandoahJ8Simple() throws Exception {
        DataReaderFactory factory = new DataReaderFactory();
        DataReader dr = factory.getDataReader(new GcResourceFile("byteArray"), new ByteArrayInputStream((
                "OpenJDK 64-Bit Server VM (25.161-b14) for linux-amd64 JRE (1.8.0_161-b14), built on Jan  9 2018 19:54:33 by \"mockbuild\" with gcc 4.8.5 20150623 (Red Hat 4.8.5-16)\n" +
                "Memory: 4k page, physical 8002012k(4357532k free), swap 2097148k(1656904k free)\n" +
                "CommandLine flags: -XX:HeapDumpPath=/work -XX:InitialHeapSize=2147483648 -XX:LogFile=/log/jvm.log -XX:+LogVMOutput -XX:+ManagementServer -XX:MaxHeapSize=2147483648 -XX:+PrintGC -XX:+PrintGCTimeStamps -XX:+UnlockDiagnosticVMOptions -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:-UseNUMA -XX:+UseShenandoahGC \n" +
                "13.976: [Pause Init Mark, 3.587 ms]\n").getBytes()));
        assertDataReader(DataReaderSun1_6_0.class, dr.getClass());
    }

}

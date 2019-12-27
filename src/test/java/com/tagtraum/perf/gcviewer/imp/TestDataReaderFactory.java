package com.tagtraum.perf.gcviewer.imp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the logic of the {@link DataReaderFactory}
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 */
public class TestDataReaderFactory {

    private InputStream getInputStreamIBM(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(FOLDER.IBM, fileName);
    }

    private InputStream getInputStreamJRockit(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(FOLDER.JROCKIT, fileName);
    }
    
    private InputStream getInputStreamOpenJdk(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(FOLDER.OPENJDK, fileName);
    }
    
    /**
     * Tests for equality of two {@link DataReader} classes.
     * 
     * @param testName name of the test to identify which test failed
     * @param expected expected type of DataReader
     * @param actual actual type of DataReader
     */
    private void assertDataReader(String testName, Class<? extends DataReader> expected, Class<? extends DataReader> actual) {
        assertEquals(expected.getCanonicalName(), actual.getCanonicalName(), testName);
    }
    
    @ParameterizedTest
    @MethodSource("getOpenJDKFiles")
    void testOpenJDK(final String fileName, Class<? extends DataReader> dataReader, final int events, final String testName) throws Exception {
        try (InputStream in = getInputStreamOpenJdk(fileName)) {
            final DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(testName, dataReader, reader.getClass());

            GCModel model = reader.read();
            assertEquals(events, model.size(), "# events");
        }
    }

    private static Stream<Arguments> getOpenJDKFiles() {
        return Stream.of(
                Arguments.arguments("SampleSun1_6_0PrintHeapAtGC.txt.gz", DataReaderSun1_6_0.class, 2, "getDataReader() reading SampleSun1_6_0PrintHeapAtGC.txt.gz"),
                Arguments.arguments("SampleSun1_7_0G1-ApplicationStopped.txt", DataReaderSun1_6_0G1.class, 55, "getDataReader() reading SampleSun1_7_0G1-ApplicationStopped.txt")
        );
    }

    @ParameterizedTest
    @MethodSource("getIBMFiles")
    void testIBM(final String fileName, final Class<? extends DataReader> dataReader, final String testName) throws Exception {
        try (InputStream in = getInputStreamIBM(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(testName, dataReader, reader.getClass());
        }
    }

    private static Stream<Arguments> getIBMFiles(){
        return Stream.of(
                Arguments.arguments("SampleIBMi5OS1_4_2.txt", DataReaderIBMi5OS1_4_2.class, "IBM i5 OS 1.4.2"),
                Arguments.arguments("SampleIBMJ9_5_0af-global-200811_07.txt", DataReaderIBM_J9_5_0.class, "IBM J9 5.0"),
                Arguments.arguments("SampleIBMJ9_R26_GAFP1_full_header.txt", DataReaderIBM_J9_R28.class, "IBM J9 R26"),
                Arguments.arguments("SampleIBMJ9_R27_SR1_full_header.txt", DataReaderIBM_J9_R28.class, "IBM J9 R27")
        );
    }

    @ParameterizedTest
    @MethodSource("getJRockitFiles")
    void testJRockit(final String fileName, final Class<? extends DataReader> dataReader, final String testName) throws Exception {
        try (InputStream in = getInputStreamJRockit(fileName)) {
            DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile(fileName), in);
            assertDataReader(testName, dataReader, reader.getClass());
        }
    }
    
    private static Stream<Arguments> getJRockitFiles() {
        return Stream.of(
                Arguments.arguments("SampleJRockit1_4_2ts-gcreport-gencon.txt", DataReaderJRockit1_4_2.class, "JRockit 1.4 GC report gen con"),
                Arguments.arguments("SampleJRockit1_4_2ts-gcreport-parallel.txt", DataReaderJRockit1_4_2.class, "JRockit 1.4 GC report parallel"),
                Arguments.arguments("SampleJRockit1_4_2ts-gcreport-gcpriopausetime.txt", DataReaderJRockit1_4_2.class, "JRockit 1.4 GC report prio pause time"),
                Arguments.arguments("SampleJRockit1_4_2ts-gcreport-gcpriothroughput.txt", DataReaderJRockit1_4_2.class, "JRockit 1.4 GC report prio throughput"),
                Arguments.arguments("SampleJRockit1_4_2ts-gcreport-singlecon.txt", DataReaderJRockit1_4_2.class, "JRockit 1.4 GcReportSingleCon"),
                Arguments.arguments("SampleJRockit1_4_2gencon.txt",DataReaderJRockit1_5_0.class , "JRockit 1.4 GenCon"),
                Arguments.arguments("SampleJRockit1_4_2gencon-big.txt", DataReaderJRockit1_5_0.class, "JRockit 1.4 GenConBig"),
                Arguments.arguments("SampleJRockit1_4_2parallel.txt", DataReaderJRockit1_5_0.class, "JRockit 1.4 Parallel"),
                Arguments.arguments("SampleJRockit1_4_2priopausetime.txt", DataReaderJRockit1_5_0.class, "JRockit 1.4 Prio Pause Time"),
                Arguments.arguments("SampleJRockit1_5_12_gencon.txt", DataReaderJRockit1_5_0.class, "JRockit 1.5 GenCon"),
                Arguments.arguments("SampleJRockit1_5_20_memstats2.txt", DataReaderJRockit1_6_0.class, "JRockit 1.5 GenConMemStats"),
                Arguments.arguments("SampleJRockit1_5_12_genpar.txt", DataReaderJRockit1_5_0.class, "JRockit 1.5 GenPar"),
                Arguments.arguments("SampleJRockit1_5_12_gcpriopausetime.txt", DataReaderJRockit1_5_0.class, "JRockit 1.5 Prio Pausetime"),
                Arguments.arguments("SampleJRockit1_5_12_gcpriothroughput.txt",DataReaderJRockit1_5_0.class , "JRockit 1.5 Prio Throughput"),
                Arguments.arguments("SampleJRockit1_5_12-gcreport-simpleopts-singlecon.txt", DataReaderJRockit1_5_0.class, "JRockit 1.5 SimpleOpts"),
                Arguments.arguments("SampleJRockit1_6_verbose_gc_mode_gencon.txt", DataReaderJRockit1_6_0.class, "JRockit 1.6 GenConVerbose"),
                Arguments.arguments("SampleJRockit1_6_33_gc_mode_genpar_verbosenursery.txt", DataReaderJRockit1_6_0.class, "JRockit 1.6 GenParVerbose"),
                Arguments.arguments("SampleJRockit1_6_gc_mode_singleparcon.txt", DataReaderJRockit1_6_0.class, "JRockit 1.6 ParCon"),
                Arguments.arguments("SampleJRockit1_6_verbose_gc_mode_singleparcon.txt", DataReaderJRockit1_6_0.class, "JRockit 1.6 ParConVerbose")
        );
    }

    @ParameterizedTest
    @MethodSource("getSunContent")
    void testSun(final String line, final Class<? extends DataReader> dataReader, final String testName) throws Exception {
        DataReaderFactory factory = new DataReaderFactory();
        DataReader dr = factory.getDataReader(new GcResourceFile("byteArray"), new ByteArrayInputStream(line.getBytes()));
        assertDataReader(testName, dataReader, dr.getClass());
    }

    private static Stream<Arguments> getSunContent() {
        return Stream.of(
                Arguments.arguments("[GC 1087K->462K(16320K), 0.0154134 secs]", DataReaderSun1_3_1.class, "Sun1_3_1 GC"),
                Arguments.arguments("[Full GC 1087K->462K(16320K), 0.0154134 secs]", DataReaderSun1_3_1.class, "Sun1_3_1 Full GC"),
                // although the input is java 1.4 the datareader returned should be 1.6 (DataReaderSun1_6_0 handles java 1.4, 1.5, 1.6, 1.7)
                Arguments.arguments("2.23492e-006: [GC 1087K->462K(16320K), 0.0154134 secs]", DataReaderSun1_6_0.class, "Sun 1.4.0"),
                Arguments.arguments("Java HotSpot(TM) 64-Bit Server VM (25.112-b15) for windows-amd64 JRE (1.8.0_112-b15), built on Sep 22 2016 21:31:56 by \"java_re\" with MS VC++ 10.0 (VS2010)\n" +
                        "Memory: 4k page, physical 50331128k(13997304k free), swap 60569268k(13009848k free)\n" +
                        "CommandLine flags: -XX:CICompilerCount=4 -XX:ConcGCThreads=3 -XX:G1HeapRegionSize=2097152 -XX:GCLogFileSize=1048576 -XX:InitialHeapSize=4294967296 -XX:+ManagementServer -XX:MarkStackSize=4194304 -XX:MaxHeapSize=8589934592 -XX:MaxNewSize=5152702464 -XX:MinHeapDeltaBytes=2097152 -XX:NumberOfGCLogFiles=5 -XX:-OmitStackTraceInFastThrow -XX:+ParallelRefProcEnabled -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution -XX:+ReduceSignalUsage -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseFastUnorderedTimeStamps -XX:+UseG1GC -XX:+UseGCLogFileRotation -XX:-UseLargePagesIndividualAllocation\n" +
                        "2017-12-01T14:14:50.781-0600: 1501608.217: [GC pause (G1 Evacuation Pause) (mixed)\n", DataReaderSun1_6_0G1.class, "Oracle G1 J8"),
                Arguments.arguments("OpenJDK 64-Bit Server VM (25.161-b14) for linux-amd64 JRE (1.8.0_161-b14), built on Jan  9 2018 19:54:33 by \"mockbuild\" with gcc 4.8.5 20150623 (Red Hat 4.8.5-16)\n" +
                        "Memory: 4k page, physical 8002012k(4357532k free), swap 2097148k(1656904k free)\n" +
                        "CommandLine flags: -XX:HeapDumpPath=/work -XX:InitialHeapSize=2147483648 -XX:LogFile=/log/jvm.log -XX:+LogVMOutput -XX:+ManagementServer -XX:MaxHeapSize=2147483648 -XX:+PrintGC -XX:+PrintGCTimeStamps -XX:+UnlockDiagnosticVMOptions -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:-UseNUMA -XX:+UseShenandoahGC \n" +
                        "13.976: [Pause Init Mark, 3.587 ms]\n",DataReaderSun1_6_0.class, "Oracle Shenandoah J8 Simple")
        );
    }

    @Test
    void testOracleG1J8_StringDeduplication(TestInfo testInfo) throws Exception {
        DataReaderFactory factory = new DataReaderFactory();
        DataReader dr = factory.getDataReader(new GcResourceFile("byteArray"),
                getInputStreamOpenJdk("SampleSun1_8_0G1StringDeduplication.txt"));
        assertDataReader("Orcale G1 J8 StringDeduplication", DataReaderSun1_6_0G1.class, dr.getClass());
    }
}

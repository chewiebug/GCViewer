package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.util.DateHelper;
import org.junit.jupiter.api.Test;

class TestDataReaderSun1_6_0G1 {
    
    private final DateTimeFormatter dateTimeFormatter = DateHelper.DATE_TIME_FORMATTER;

    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(FOLDER.OPENJDK, fileName);
    }
    
    /**
     * Test G1 parser with a gc verbose file (not -XX:+PrintGCDetails)
     */
    @Test
    void testG1GcVerbose() throws Exception {
    	TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleSun1_6_0G1_gc_verbose.txt");
        gcResource.getLogger().addHandler(handler);
    	
        InputStream in = getInputStream(gcResource.getResourceName());
        DataReader reader = new DataReaderSun1_6_0G1(gcResource, in, GcLogType.SUN1_6G1);
        GCModel model = reader.read();
        
        assertEquals(62.616796, model.getPause().getSum(), 0.000000001, "gc pause sum");
        assertEquals(47.7730602904, model.getThroughput(), 0.000000001, "throughput");
        assertEquals(0.1581177, model.getPause().getMax(), 0.000001, "longest pause");
        assertEquals(119.8936724, model.getRunningTime(), 0.000001, "total runtime");
        
        assertEquals(0, handler.getCount(), "number of errors");
        
        assertEquals(0.211, model.getPauseInterval().getMax(), 0.000001, "max interval");
        assertEquals(0.048291297, model.getPauseInterval().average(), 0.0000001, "avg interval");
        
        assertThat("count footprint after full gc", model.getFootprintAfterFullGC().getN(), is(5));
        assertThat("max footprint after full gc", model.getFootprintAfterFullGC().getMax(), is(33*1024));
        
        assertThat("tenured: count after concurrent sizes", model.getPostConcurrentCycleTenuredUsedSizes().getN(), is(242));
        assertThat("tenured: max size after concurrent", model.getPostConcurrentCycleTenuredUsedSizes().getMax(), is(152576));
        
        assertThat("count after concurrent sizes", model.getPostConcurrentCycleHeapUsedSizes().getN(), is(242));
        assertThat("max size after concurrent", model.getPostConcurrentCycleHeapUsedSizes().getMax(), is(152576));
    }
    
    @Test
    void testSimpleEventDateStamp() throws Exception {
        InputStream in = new ByteArrayInputStream(
                ("2012-02-29T13:41:00.721+0100: 0.163: [GC pause (young) 4096K->3936K(16M), 0.0032067 secs]")
                .getBytes());
        
        DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile("byteArray"), in, GcLogType.SUN1_6G1);
        GCModel model = reader.read();

        assertEquals(1, model.size(), "count");
        assertEquals(0.0032067, model.getGCPause().getMax(), 0.000001, "gc pause");
        assertEquals(ZonedDateTime.parse("2012-02-29T13:41:00.721+0100", dateTimeFormatter),
                model.getGCEvents().next().getDatestamp(), "datestamp");
    }
    
    @Test
    void testG1FullGcSystemGc() throws Exception {
    	InputStream in = new ByteArrayInputStream(
				("9.978: [Full GC (System.gc()) 597M->1142K(7168K), 0.1604955 secs]")
				.getBytes());
    	
		DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile("byteArray"), in, GcLogType.SUN1_6G1);
		GCModel model = reader.read();

		assertEquals(1, model.size(), "count");
		assertEquals(0.1604955, model.getFullGCPause().getMax(), 0.000001, "full gc pause");
        assertThat("is system gc", model.get(0).isSystem(), is(true));
    }
    
    @Test
    void testG1MixedLine1() throws Exception {
        InputStream in = new ByteArrayInputStream(
                ("0.388: [GC pause (young) (initial-mark) 10080K->10080K(16M)0.390: [GC concurrent-mark-start]" +
                		"\n, 0.0013065 secs]")
                .getBytes());
        
        DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile("byteArray"), in, GcLogType.SUN1_6G1);
        GCModel model = reader.read();

        assertEquals(2, model.size(), "count");
        assertEquals(0.0013065, model.getGCPause().getMax(), 0.000001, "gc pause");
    }
    
    @Test
    void testG1MixedLine2() throws Exception {
        InputStream in = new ByteArrayInputStream(
                ("61.059: [GC pause (young) (initial-mark) 135M->127M(256M)61.078: , 0.0187031 secs]" +
                        "\n[GC concurrent-mark-start]")
                .getBytes());
        
        DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile("byteArray"), in, GcLogType.SUN1_6G1);
        GCModel model = reader.read();

        assertEquals(2, model.size(), "count");
        assertEquals(Type.G1_CONCURRENT_MARK_START.toString(), model.getConcurrentGCEvents().next().getTypeAsString(), "concurrent event type");
        assertEquals(0.0187031, model.getGCPause().getMax(), 0.000001, "gc pause");
    }

    @Test
    void testDetailedYoungCollection() throws Exception {
        // parse one detailed event
        String fileName = "SampleSun1_6_0G1_Detailed-young.txt";
        InputStream in = getInputStream(fileName);
        DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile(fileName), in, GcLogType.SUN1_6G1);
        GCModel model = reader.read();
        
        assertEquals(1, model.getPause().getN(), "number of pauses");
        assertEquals(0.00594747, model.getPause().getSum(), 0.000000001, "gc pause sum");
        assertEquals(4096 - 3936, model.getFreedMemoryByGC().getMax(), "gc memory");
    }
    
    @Test
    void testDetailedCollectionMixed1() throws Exception {
        // parse one detailed event with a mixed line (concurrent event starts in the middle of an stw collection)
        // 8.775: [GC pause (young)8.775: [GC concurrent-mark-end, 0.0101638 sec]
        // , 0.00831998 secs]
        String fileName = "SampleSun1_6_0G1_Detailed-mixedLine1.txt";
        InputStream in = getInputStream(fileName);
        DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile(fileName), in, GcLogType.SUN1_6G1);
        GCModel model = reader.read();
        
        assertEquals(2, model.size(), "number of events");
        assertEquals(1, model.getPause().getN(), "number of pauses");
        assertEquals(0.00831998, model.getPause().getSum(), 0.000000001, "gc pause sum");
        assertEquals(169*1024 - 162*1024, model.getFreedMemoryByGC().getMax(), "gc memory");
    }
    
    @Test
    void testDetailedCollectionMixed2() throws Exception {
        // parse one detailed event with a mixed line (part of concurrent event starts in the middle of an stw collection)
        // 0.230: [GC pause (young)0.235:  (initial-mark), 0.00430038 secs]
        // [GC concurrent-mark-start]
        String fileName = "SampleSun1_6_0G1_Detailed-mixedLine2.txt";
        try (InputStream in = getInputStream(fileName)) {
            DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile(fileName), in, GcLogType.SUN1_6G1);
            GCModel model = reader.read();

            assertEquals(2, model.size(), "number of events");
            assertEquals(Type.G1_CONCURRENT_MARK_START.toString(), model.getConcurrentGCEvents().next().getTypeAsString(), "concurrent event type");
            assertEquals(1, model.getPause().getN(), "number of pauses");
            assertEquals(0.00430038, model.getPause().getSum(), 0.000000001, "gc pause sum");
        }
    }
    
    @Test
    void testDetailedCollectionMixed3() throws Exception {
        // parse one detailed event with a mixed line (part of concurrent event starts in the middle of an stw collection)
        // 0.331: [GC pause (young)0.333 (initial-mark), 0.00178520 secs]
        // :    [GC concurrent-mark-start]        
        String fileName = "SampleSun1_6_0G1_Detailed-mixedLine3.txt";
        try (InputStream in = getInputStream(fileName)) {
            DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile(fileName), in, GcLogType.SUN1_6G1);
            GCModel model = reader.read();

            assertEquals(2, model.size(), "number of events");
            assertEquals(Type.G1_CONCURRENT_MARK_START.toString(), model.getConcurrentGCEvents().next().getTypeAsString(), "concurrent event type");
            assertEquals(1, model.getPause().getN(), "number of pauses");
            assertEquals(0.00178520, model.getPause().getSum(), 0.000000001, "gc pause sum");
        }
    }
    
    @Test
    void testFullGcMixed() throws Exception {
        InputStream in = new ByteArrayInputStream(("107.222: [Full GC107.248: [GC concurrent-mark-end, 0.0437048 sec]" +
        		"\n 254M->59M(199M), 0.0687356 secs]" +
        		"\n [Times: user=0.06 sys=0.02, real=0.07 secs]").getBytes());
        DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile("byteArray"), in, GcLogType.SUN1_6G1);
        GCModel model = reader.read();

        assertEquals(2, model.size(), "number of events");
        assertEquals(1, model.getFullGCPause().getN(), "number of full gc pauses");
        assertEquals(0.0687356, model.getFullGCPause().getSum(), 0.0000001, "full gc pause sum");
    }
    
    @Test
    void testYoungToSpaceOverflow() throws Exception {
        // special type of GC: 0.838: "[GC pause (young) (to-space overflow)..."
        String fileName = "SampleSun1_6_0G1_young_toSpaceOverflow.txt";
        try (InputStream in = getInputStream(fileName)) {
            DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile(fileName), in, GcLogType.SUN1_6G1);
            GCModel model = reader.read();

            assertEquals(1, model.size(), "number of events");
            assertEquals(1, model.getPause().getN(), "number of pauses");
            assertEquals(0.04674512, model.getPause().getSum(), 0.000000001, "gc pause sum");
            assertEquals(228 * 1024 - 102 * 1024, model.getFreedMemoryByGC().getMax(), "gc memory");
            assertEquals(256 * 1024, model.getFootprint(), "max memory");
        }
    }
    
    @Test
    void testPartialToSpaceOverflow() throws Exception {
        // special type of GC: 0.838: "[GC pause (partial) (to-space overflow)..."
        String fileName = "SampleSun1_6_0G1_partial_toSpaceOverflow.txt";
        try (InputStream in = getInputStream(fileName)) {
            DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile(fileName), in, GcLogType.SUN1_6G1);
            GCModel model = reader.read();

            assertEquals(1, model.size(), "number of events");
            assertEquals(1, model.getPause().getN(), "number of pauses");
            assertEquals(0.00271976, model.getPause().getSum(), 0.000000001, "gc pause sum");
            assertEquals(255 * 1024 - 181 * 1024, model.getFreedMemoryByGC().getMax(), "gc memory");
            assertEquals(256 * 1024, model.getFootprint(), "max memory");
        }
    }
    
    @Test
    void testYoungToSpaceOverflowInitialMark() throws Exception {
        // special type of GC: 0.838: "[GC pause (young) (to-space overflow) (initial-mark)..."
        String fileName = "SampleSun1_6_0G1_young_initialMarkToSpaceOverflow.txt";
        try (InputStream in = getInputStream(fileName)) {
            DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile(fileName), in, GcLogType.SUN1_6G1);
            GCModel model = reader.read();

            assertEquals(2, model.size(), "number of events");
            assertEquals(Type.G1_CONCURRENT_MARK_START.toString(), model.getConcurrentGCEvents().next().getTypeAsString(), "concurrent event type");
            assertEquals(1, model.getPause().getN(), "number of pauses");
            assertEquals(0.00316185, model.getPause().getSum(), 0.000000001, "gc pause sum");
            assertEquals(254 * 1024 - 170 * 1024, model.getFreedMemoryByGC().getMax(), "gc memory");
            assertEquals(256 * 1024, model.getFootprint(), "max memory");
        }
    }

    @Test
    void testPartialToSpaceOverflowInitialMark() throws Exception {
        // special type of GC: 0.838: "[GC pause (partial) (to-space overflow) (initial-mark)..."
        String fileName = "SampleSun1_6_0G1_partial_initialMarkToSpaceOverflow.txt";
        try (InputStream in = getInputStream(fileName)) {
            DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile(fileName), in, GcLogType.SUN1_6G1);
            GCModel model = reader.read();

            assertEquals(2, model.size(), "number of events");
            assertEquals(Type.G1_CONCURRENT_MARK_START.toString(), model.getConcurrentGCEvents().next().getTypeAsString(), "concurrent event type");
            assertEquals(1, model.getPause().getN(), "number of pauses");
            assertEquals(0.00588343, model.getPause().getSum(), 0.000000001, "gc pause sum");
            assertEquals(255 * 1024 - 197 * 1024, model.getFreedMemoryByGC().getMax(), "gc memory");
            assertEquals(256 * 1024, model.getFootprint(), "max memory");
        }
    }

    @Test
    void testGcPattern() throws Exception {
        InputStream in = new ByteArrayInputStream(("0.452: [GC concurrent-count-start]").getBytes());
        
        DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile("byteArray"), in, GcLogType.SUN1_6G1);
        GCModel model = reader.read();

        assertEquals(1, model.size(), "count");
        assertEquals(0, model.getFullGCPause().getN(), "full gc pause");
        assertEquals(0, model.getGCPause().getN(), "gc pause");
    }

    @Test
    void testGcPausePattern() throws Exception {
        InputStream in = new ByteArrayInputStream(("0.360: [GC concurrent-count-end, 0.0242674]").getBytes());
        
        DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile("byteArray"), in, GcLogType.SUN1_6G1);
        GCModel model = reader.read();

        assertEquals(1, model.size(), "count");
        assertEquals(0, model.getFullGCPause().getN(), "full gc pause");
    }

    @Test
    void testGcMemoryPausePattern() throws Exception {
        InputStream in = new ByteArrayInputStream(("0.360: [GC cleanup 19M->19M(36M), 0.0007889 secs]").getBytes());
        
        DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile("byteArray"), in, GcLogType.SUN1_6G1);
        GCModel model = reader.read();

        assertEquals(1, model.size(), "count");
        assertEquals(0, model.getFullGCPause().getN(), "full gc pause");
        assertEquals(0.0007889, model.getGCPause().getMax(), 0.0000001, "gc pause");
        assertEquals(0, model.getFreedMemoryByGC().getMax(), "memory");
    }

    @Test
    void testInitialMark() throws Exception {
        InputStream in = new ByteArrayInputStream(
                ("0.319: [GC pause (young) (initial-mark), 0.00188271 secs]" +
                        "\n [Times: user=0.00 sys=0.00, real=0.00 secs] ").getBytes());
        
        DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile("byteArray"), in, GcLogType.SUN1_6G1);
        GCModel model = reader.read();

        assertEquals(1, model.size(), "count");
        assertEquals(0, model.getFullGCPause().getN(), "full gc pause");
        assertEquals(0.00188271, model.getGCPause().getMax(), 0.0000001, "gc pause");
        assertEquals(0, model.getFreedMemoryByGC().getMax(), "memory");
    }

    @Test
    void initialMarkHeapAtGcMixed() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleSun1_6_0G1InitialMarkMixed.txt");
        gcResource.getLogger().addHandler(handler);
        
        try (InputStream in = getInputStream(gcResource.getResourceName())) {
            DataReader reader = new DataReaderSun1_6_0G1(gcResource, in, GcLogType.SUN1_6G1);

            GCModel model = reader.read();

            assertThat("GC count", model.size(), is(2));
            assertThat("GC pause", model.getGCPause().getSum(), closeTo(0.03643900, 0.000000001));
            assertThat("heap size", model.getHeapAllocatedSizes().getMax(), is(10240 * 1024));
            assertThat("number of errors", handler.getCount(), is(0));

            assertThat("concurrent type name",
                    model.getConcurrentGCEvents().next().getTypeAsString(),
                    equalTo("GC concurrent-mark-start"));
        }
    }

    @Test
    void testRemark() throws Exception {
        InputStream in = new ByteArrayInputStream(
                ("0.334: [GC remark, 0.0009506 secs]" +
                        "\n [Times: user=0.00 sys=0.00, real=0.00 secs] ").getBytes());
        
        DataReader reader = new DataReaderSun1_6_0G1(new GcResourceFile("byteArray"), in, GcLogType.SUN1_6G1);
        GCModel model = reader.read();

        assertEquals(1, model.size(), "count");
        assertEquals(0, model.getFullGCPause().getN(), "full gc pause");
        assertEquals(0.0009506, model.getGCPause().getMax(), 0.0000001, "gc pause");
        assertEquals(0, model.getFreedMemoryByGC().getMax(), "memory");
    }

    @Test
    void testPrintHeapAtGC() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleSun1_6_0G1_PrintHeapAtGC.txt");
        gcResource.getLogger().addHandler(handler);
        
        try (InputStream in = getInputStream(gcResource.getResourceName())) {
            DataReader reader = new DataReaderSun1_6_0G1(gcResource, in, GcLogType.SUN1_6G1);

            GCModel model = reader.read();

            assertEquals(2, model.size(), "GC count");
            assertEquals(0.00582962 + 0.00228253, model.getGCPause().getSum(), 0.000000001, "GC pause");
            assertEquals(0, handler.getCount(), "number of errors");
        }
    }
    
    @Test
    void testMarkStackFull() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleSun1_6_0G1_MarkStackFull.txt");
        gcResource.getLogger().addHandler(handler);
        
        try (InputStream in = getInputStream(gcResource.getResourceName())) {
            DataReader reader = new DataReaderSun1_6_0G1(gcResource, in, GcLogType.SUN1_6G1);

            GCModel model = reader.read();

            assertEquals(1, model.size(), "GC count");
            assertEquals(0.08032150, model.getGCPause().getSum(), 0.000000001, "GC pause");
            assertEquals(3985 * 1024, model.getHeapAllocatedSizes().getMax(), "heap size");
            assertEquals(0, handler.getCount(), "number of errors");
        }
    }
    
    @Test
    void printHeapAtGcPrintTenuringDistributionFullGc() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleSun1_6_0G1FullGcTenuringDistribution.txt");
        gcResource.getLogger().addHandler(handler);
        
        try (InputStream in = getInputStream(gcResource.getResourceName())) {
            DataReader reader = new DataReaderSun1_6_0G1(gcResource, in, GcLogType.SUN1_6G1);

            GCModel model = reader.read();

            assertThat("GC count", model.size(), is(1));
            assertThat("GC pause", model.getFullGCPause().getSum(), closeTo(37.0629320, 0.000000001));
            assertThat("heap size", model.getHeapAllocatedSizes().getMax(), is(10240 * 1024));
            assertThat("number of errors", handler.getCount(), is(0));
        }
    }

    @Test
    void logfileRollingProducesNoWarnings() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleSun1_8_0RollingLogfile.txt");
        gcResource.getLogger().addHandler(handler);

        try (InputStream in = getInputStream(gcResource.getResourceName())) {
            DataReader reader = new DataReaderSun1_6_0G1(gcResource, in, GcLogType.SUN1_6G1);
            reader.read();

            assertThat("number of errors", handler.getCount(), is(0));
        }
    }
}

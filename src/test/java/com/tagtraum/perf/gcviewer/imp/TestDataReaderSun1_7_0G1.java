package com.tagtraum.perf.gcviewer.imp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.math.DoubleData;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

public class TestDataReaderSun1_7_0G1 {

    private static final Logger IMP_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.imp");
    private static final Logger DATA_READER_FACTORY_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.DataReaderFactory");

    @Test
    public void youngPause_u1() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);
        
        final InputStream in = getClass().getResourceAsStream("SampleSun1_7_0-01_G1_young.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in, GcLogType.SUN1_7G1);
        GCModel model = reader.read();
        
        assertEquals("gc pause", 0.00631825, model.getPause().getMax(), 0.000000001);
        assertEquals("heap", 64*1024, model.getHeapAllocatedSizes().getMax());
        assertEquals("number of errors", 0, handler.getCount());
    }
    

    /**
     * Parse memory format of java 1.7.0_u2.
     */
    @Test
    public void youngPause_u2() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);
        
        final InputStream in = getClass().getResourceAsStream("SampleSun1_7_0-02_G1_young.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in, GcLogType.SUN1_7G1);
        GCModel model = reader.read();
        
        assertEquals("gc pause", 0.14482200, model.getPause().getMax(), 0.000000001);
        GCEvent heap = (GCEvent) model.getEvents().next();
        assertEquals("heap before", 1105*1024, heap.getPreUsed());
        assertEquals("heap after", 380*1024, heap.getPostUsed());
        assertEquals("heap", 2*1024*1024, heap.getTotal());
        
        GCEvent young = model.getGCEvents().next().getYoung();
        assertNotNull("young", young);
        assertEquals("young before", 1024*1024, young.getPreUsed());
        assertEquals("young after", 128*1024, young.getPostUsed());
        assertEquals("young total", (896+128)*1024, young.getTotal());
        
        GCEvent tenured = model.getGCEvents().next().getTenured();
        assertNotNull("tenured", tenured);
        assertEquals("tenured before", (1105-1024)*1024, tenured.getPreUsed());
        assertEquals("tenured after", (380-128)*1024, tenured.getPostUsed());
        assertEquals("tenured total", 1024*1024, tenured.getTotal());
        
        assertEquals("number of errors", 0, handler.getCount());
    }
    
    @Test
    public void youngPauseDateStamp_u2() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);
        
        final InputStream in = getClass().getResourceAsStream("SampleSun1_7_0_02_G1_young_datestamp.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in, GcLogType.SUN1_7G1);
        GCModel model = reader.read();
        
        assertEquals("gc pause", 0.14482200, model.getPause().getMax(), 0.000000001);
        GCEvent heap = (GCEvent) model.getEvents().next();
        assertEquals("heap", 1105*1024, heap.getPreUsed());
        assertEquals("heap", 380*1024, heap.getPostUsed());
        assertEquals("heap", 2048*1024, heap.getTotal());
        
        assertEquals("number of errors", 0, handler.getCount());
    }
    
    @Test
    public void testDetailedCollectionDatestampMixed1() throws Exception {
        // parse one detailed event with a mixed line (concurrent event starts in the middle of an stw collection)
        // 2012-02-24T03:49:09.100-0800: 312.402: [GC pause (young)2012-02-24T03:49:09.378-0800: 312.680: [GC concurrent-mark-start]
        //  (initial-mark), 0.28645100 secs]
        final InputStream in = getClass().getResourceAsStream("SampleSun1_7_0G1_DateStamp_Detailed-mixedLine1.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in, GcLogType.SUN1_7G1);
        GCModel model = reader.read();
        
        assertEquals("nummber of events", 2, model.size());
        assertEquals("number of pauses", 1, model.getPause().getN());
        assertEquals("gc pause sum", 0.28645100, model.getPause().getSum(), 0.000000001);
        assertEquals("gc memory", 17786*1024 - 17147*1024, model.getFreedMemoryByGC().getMax());
    }
    
    @Test
    public void testDetailedCollectionDatestampMixed2() throws Exception {
        // parse one detailed event with a mixed line (concurrent event starts in the middle of an stw collection)
        // 2012-02-24T03:50:08.274-0800: 371.576: [GC pause (young)2012-02-24T03:50:08.554-0800:  (initial-mark), 0.28031200 secs]
        // 371.856:    [Parallel Time: 268.0 ms]
        // [GC concurrent-mark-start]

        final InputStream in = getClass().getResourceAsStream("SampleSun1_7_0G1_DateStamp_Detailed-mixedLine2.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in, GcLogType.SUN1_7G1);
        GCModel model = reader.read();
        
        assertEquals("nummber of events", 2, model.size());
        assertEquals("concurrent event type", Type.G1_CONCURRENT_MARK_START.toString(), model.getConcurrentGCEvents().next().getTypeAsString());
        assertEquals("number of pauses", 1, model.getPause().getN());
        assertEquals("gc pause sum", 0.28031200, model.getPause().getSum(), 0.000000001);
        assertEquals("gc memory", 20701*1024 - 20017*1024, model.getFreedMemoryByGC().getMax());
    }
    
    @Test
    public void testDetailedCollectionDatestampMixed3() throws Exception {
        // parse one detailed event with a mixed line
        // -> concurrent event occurs somewhere in the detail lines below the stw event

        final InputStream in = getClass().getResourceAsStream("SampleSun1_7_0G1_DateStamp_Detailed-mixedLine3.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in, GcLogType.SUN1_7G1);
        GCModel model = reader.read();
        
        assertEquals("nummber of events", 2, model.size());
        assertEquals("concurrent event type", Type.G1_CONCURRENT_MARK_START.toString(), model.getConcurrentGCEvents().next().getTypeAsString());
        assertEquals("number of pauses", 1, model.getPause().getN());
        assertEquals("gc pause sum", 0.08894900, model.getPause().getSum(), 0.000000001);
        assertEquals("gc memory", 29672*1024 - 28733*1024, model.getFreedMemoryByGC().getMax());
    }
    
    @Test
    public void applicationStoppedMixedLine() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);
        
        final InputStream in = new ByteArrayInputStream(
                ("2012-07-26T14:58:54.045+0200: Total time for which application threads were stopped: 0.0078335 seconds" +
                        "\n3.634: [GC concurrent-root-region-scan-start]")
                .getBytes());
        
        final DataReader reader = new DataReaderSun1_6_0G1(in, GcLogType.SUN1_7G1);
        GCModel model = reader.read();

        assertEquals("count", 1, model.size());
        assertEquals("gc type", "GC concurrent-root-region-scan-start", model.get(0).getTypeAsString());
        assertEquals("number of errors", 0, handler.getCount());
    }

    @Test
    public void applicationTimeMixed() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);
        
        final InputStream in = new ByteArrayInputStream(
                ("2012-07-26T15:24:21.845+0200: 3.100: [GC concurrent-root-region-scan-end, 0.0000680]" +
                 "\n2012-07-26T14:58:58.320+0200Application time: 0.0000221 seconds" +
                        "\n: 7.907: [GC concurrent-mark-start]")
                .getBytes());
        
        final DataReader reader = new DataReaderSun1_6_0G1(in, GcLogType.SUN1_7G1);
        GCModel model = reader.read();

        assertEquals("count", 2, model.size());
        assertEquals("gc type", "GC concurrent-mark-start", model.get(1).getTypeAsString());
        assertEquals("number of errors", 0, handler.getCount());
    }
    
    @Test
    public void eventNoMemory() throws Exception {
        // there are (rarely) events, where the memory information could not be parsed,
        // because the line with the memory information was mixed with another event
        // looks like this:    [251.448:  213M->174M(256M)[GC concurrent-mark-start]
        // (produced using -XX:+PrintGcDetails -XX:+PrintHeapAtGC)

        GCEvent event = new GCEvent();
        event.setType(Type.G1_YOUNG_INITIAL_MARK);
        event.setTimestamp(0.5);
        event.setPause(0.2);
        // but no memory information -> all values zero there
        
        GCModel model = new GCModel(false);
        model.add(event);
        
        DoubleData initiatingOccupancyFraction = model.getCmsInitiatingOccupancyFraction();
        assertEquals("fraction", 0, initiatingOccupancyFraction.getSum(), 0.1);
    }
    
    @Test
    public void gcRemark() throws Exception {
        final InputStream in = new ByteArrayInputStream(
                ("0.197: [GC remark 0.197: [GC ref-proc, 0.0000070 secs], 0.0005297 secs]" +
                		"\n [Times: user=0.00 sys=0.00, real=0.00 secs]")
                .getBytes());
        
        final DataReader reader = new DataReaderSun1_6_0G1(in, GcLogType.SUN1_7G1);
        GCModel model = reader.read();

        assertEquals("count", 1, model.size());
        assertEquals("gc pause", 0.0005297, model.getGCPause().getMax(), 0.000001);
    }
    
    @Test
    public void printApplicationTimePrintTenuringDistribution() throws Exception {
        // test parsing when the following options are set:
        // -XX:+PrintTenuringDistribution (output ignored)
        // -XX:+PrintGCApplicationStoppedTime (output ignored)
        // -XX:+PrintGCApplicationConcurrentTime (output ignored)
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);
        
        final InputStream in = getClass().getResourceAsStream("SampleSun1_7_0_02PrintApplicationTimeTenuringDistribution.txt");
        final DataReader reader = new DataReaderSun1_6_0G1(in, GcLogType.SUN1_7G1);
        GCModel model = reader.read();
        
        assertEquals("number of events", 5, model.size());
        assertEquals("number of concurrent events", 2, model.getConcurrentEventPauses().size());
        
        GCEvent youngEvent = (GCEvent) model.get(0);
        assertEquals("gc pause (young)", 0.00784501, youngEvent.getPause(), 0.000000001);
        assertEquals("heap (young)", 20 * 1024, youngEvent.getTotal());

        GCEvent partialEvent = (GCEvent) model.get(4);
        assertEquals("gc pause (partial)", 0.02648319, partialEvent.getPause(), 0.000000001);
        assertEquals("heap (partial)", 128 * 1024, partialEvent.getTotal());

        assertEquals("number of errors", 0, handler.getCount());
    }
}

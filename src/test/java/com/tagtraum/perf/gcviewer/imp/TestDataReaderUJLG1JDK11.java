package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Test;

/**
 * Test unified java logging G1 algorithm in OpenJDK 11
 */
public class TestDataReaderUJLG1JDK11 {
    private GCModel getGCModelFromLogFile(String fileName) throws IOException {
        return UnittestHelper.getGCModelFromLogFile(fileName, FOLDER.OPENJDK_UJL, DataReaderUnifiedJvmLogging.class);
    }

    @Test
    public void testDefaultsPauseYoungNormal() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[1.113s][info][gc] GC(4) Pause Young (Normal) (G1 Evacuation Pause) 70M->70M(128M) 12.615ms")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("pause", model.get(0).getPause(), closeTo(0.012615, 0.00000001));
    }

    @Test
    public void testNewUptimestamp() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[113ms][info][gc] GC(4) Pause Young (Normal) (G1 Evacuation Pause) 70M->70M(128M) 12.615ms")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat(model.getEvents().next().getTimestamp(), is(0.113));
    }

    @Test
    public void testDefaultsPauseYoungConcurrentStart() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[1.155s][info][gc] GC(5) Pause Young (Concurrent Start) (G1 Evacuation Pause) 84M->79M(128M) 5.960ms")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("heap before", model.get(0).getPreUsed(), is(84 * 1024));
    }

    @Test
    public void testDefaultsPauseYoungPrepareMixed() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2.649s][info][gc] GC(218) Pause Young (Prepare Mixed) (G1 Evacuation Pause) 81M->79M(128M) 1.322ms")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("heap after", model.get(0).getPostUsed(), is(79 * 1024));
    }

    @Test
    public void testDefaultsPauseYoungMixed() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2.651s][info][gc] GC(219) Pause Young (Mixed) (G1 Evacuation Pause) 84M->83M(128M) 1.599ms")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("total heap", model.get(0).getTotal(), is(128 * 1024));
    }

    @Test
    public void testFullGcWithPhases() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[1.204s][info][gc,task      ] GC(15) Using 3 workers of 4 for full compaction\n" +
                    "[1.204s][info][gc,start     ] GC(15) Pause Full (G1 Evacuation Pause)\n" +
                    "[1.204s][info][gc,phases,start] GC(15) Phase 1: Mark live objects\n" +
                    "[1.207s][info][gc,stringtable ] GC(15) Cleaned string and symbol table, strings: 7381 processed, 70 removed, symbols: 49128 processed, 10 removed\n" +
                    "[1.207s][info][gc,phases      ] GC(15) Phase 1: Mark live objects 3.036ms\n" +
                    "[1.207s][info][gc,phases,start] GC(15) Phase 2: Prepare for compaction\n" +
                    "[1.208s][info][gc,phases      ] GC(15) Phase 2: Prepare for compaction 0.808ms\n" +
                    "[1.208s][info][gc,phases,start] GC(15) Phase 3: Adjust pointers\n" +
                    "[1.210s][info][gc,phases      ] GC(15) Phase 3: Adjust pointers 1.916ms\n" +
                    "[1.210s][info][gc,phases,start] GC(15) Phase 4: Compact heap\n" +
                    "[1.223s][info][gc,phases      ] GC(15) Phase 4: Compact heap 13.268ms\n" +
                    "[1.224s][info][gc,heap        ] GC(15) Eden regions: 0->0(17)\n" +
                    "[1.224s][info][gc,heap        ] GC(15) Survivor regions: 0->0(1)\n" +
                    "[1.224s][info][gc,heap        ] GC(15) Old regions: 128->62\n" +
                    "[1.224s][info][gc,heap        ] GC(15) Humongous regions: 0->0\n" +
                    "[1.224s][info][gc,metaspace   ] GC(15) Metaspace: 15025K->15025K(1062912K)\n" +
                    "[1.224s][info][gc             ] GC(15) Pause Full (G1 Evacuation Pause) 127M->59M(128M) 20.596ms\n" +
                    "[1.225s][info][gc,cpu         ] GC(15) User=0.05s Sys=0.00s Real=0.02s\n" +
                    "[1.225s][info][safepoint      ] Leaving safepoint region\n" +
                    "[1.225s][info][safepoint      ] Total time for which application threads were stopped: 0.0222150 seconds, Stopping threads took: 0.0000452 seconds\n")
                .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("total heap", model.get(0).getTotal(), is(128 * 1024));
    }

    @Test
    public void testPauseYoungConcurrentStartMetadataGcThreshold() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[1.459s][info][gc,start      ] GC(1) Pause Young (Concurrent Start) (Metadata GC Threshold)\n" +
                "[1.459s][info][gc,task       ] GC(1) Using 8 workers of 8 for evacuation\n" +
                "[1.464s][info][gc,phases     ] GC(1)   Pre Evacuate Collection Set: 0.0ms\n" +
                "[1.464s][info][gc,phases     ] GC(1)   Evacuate Collection Set: 4.1ms\n" +
                "[1.464s][info][gc,phases     ] GC(1)   Post Evacuate Collection Set: 1.1ms\n" +
                "[1.464s][info][gc,phases     ] GC(1)   Other: 0.4ms\n" +
                "[1.464s][info][gc,heap       ] GC(1) Eden regions: 8->0(38)\n" +
                "[1.465s][info][gc,heap       ] GC(1) Survivor regions: 3->1(3)\n" +
                "[1.465s][info][gc,heap       ] GC(1) Old regions: 4->7\n" +
                "[1.465s][info][gc,heap       ] GC(1) Humongous regions: 5->5\n" +
                "[1.465s][info][gc,metaspace  ] GC(1) Metaspace: 20599K->20599K(1069056K)\n" +
                "[1.465s][info][gc            ] GC(1) Pause Young (Concurrent Start) (Metadata GC Threshold) 19M->12M(256M) 5.774ms\n" +
                "[1.465s][info][gc,cpu        ] GC(1) User=0.03s Sys=0.00s Real=0.00s\n")
                    .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_PAUSE_YOUNG));
        assertThat("total heap", model.get(0).getTotal(), is(256 * 1024));
    }

    @Test
    public void calculatePromotion() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[0.011s][info][gc,heap] Heap region size: 1M\n" +
                        "[0.023s][info][gc     ] Using G1\n" +
                        "[0.023s][info][gc,heap,coops] Heap address: 0x0000000700000000, size: 4096 MB, Compressed Oops mode: Zero based, Oop shift amount: 3\n" +
                        "[18.700s][info][gc,start      ] GC(12) Pause Young (Normal) (G1 Evacuation Pause)\n" +
                        "[18.700s][info][gc,task       ] GC(12) Using 8 workers of 8 for evacuation\n" +
                        "[18.709s][info][gc,phases     ] GC(12)   Pre Evacuate Collection Set: 0.0ms\n" +
                        "[18.709s][info][gc,phases     ] GC(12)   Evacuate Collection Set: 7.8ms\n" +
                        "[18.709s][info][gc,phases     ] GC(12)   Post Evacuate Collection Set: 0.9ms\n" +
                        "[18.709s][info][gc,phases     ] GC(12)   Other: 0.2ms\n" +
                        "[18.709s][info][gc,heap       ] GC(12) Eden regions: 137->0(140)\n" +
                        "[18.709s][info][gc,heap       ] GC(12) Survivor regions: 16->13(20)\n" +
                        "[18.709s][info][gc,heap       ] GC(12) Old regions: 8->13\n" +
                        "[18.709s][info][gc,heap       ] GC(12) Humongous regions: 18->8\n" +
                        "[18.709s][info][gc,metaspace  ] GC(12) Metaspace: 48235K->48235K(1093632K)\n" +
                        "[18.709s][info][gc            ] GC(12) Pause Young (Normal) (G1 Evacuation Pause) 177M->32M(256M) 9.073ms\n" +
                        "[18.709s][info][gc,cpu        ] GC(12) User=0.00s Sys=0.00s Real=0.01s")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_PAUSE_YOUNG));
        assertThat("total heap", model.get(0).getTotal(), is(256 * 1024));
        assertThat("promotion", model.getPromotion().getSum(), is(new Long(5 * 1024)));
    }
}

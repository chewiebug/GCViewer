package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Test;

/**
 * Test unified java logging G1 algorithm in OpenJDK 17
 */
public class TestDataReaderUJLG1JDK17 {
    private GCModel getGCModelFromLogFile(String fileName) throws IOException {
        return UnittestHelper.getGCModelFromLogFile(fileName, FOLDER.OPENJDK_UJL, DataReaderUnifiedJvmLogging.class);
    }

    @Test
    public void testG1MetaspacePreamble() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2022-01-29T16:34:51.445+0000][36ms][info][gc,metaspace] CDS archive(s) mapped at: [0x0000000800000000-0x0000000800bcf000-0x0000000800bcf000), size 12382208, SharedBaseAddress: 0x0000000800000000, ArchiveRelocationMode: 0.\n" +
                        "[2022-01-29T16:34:51.445+0000][36ms][info][gc,metaspace] Compressed class space mapped at: 0x0000000800c00000-0x0000000840c00000, reserved size: 1073741824\n" +
                        "[2022-01-29T16:34:51.445+0000][36ms][info][gc,metaspace] Narrow klass base: 0x0000000800000000, Narrow klass shift: 3, Narrow klass range: 0x100000000\n")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(0));
    }

    @Test
    public void testG1PauseYoungNormal() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2022-04-19T19:34:40.899+0000][23ms][info][gc,init] Heap Region Size: 2M\n" +
                        "[2022-01-29T16:34:51.601+0000][192ms][info][gc,start    ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)\n" +
                        "[2022-01-29T16:34:51.601+0000][192ms][info][gc,task     ] GC(0) Using 1 workers of 1 for evacuation\n" +
                        "[2022-01-29T16:34:51.608+0000][199ms][info][gc,phases   ] GC(0)   Pre Evacuate Collection Set: 0.2ms\n" +
                        "[2022-01-29T16:34:51.608+0000][199ms][info][gc,phases   ] GC(0)   Merge Heap Roots: 0.2ms\n" +
                        "[2022-01-29T16:34:51.609+0000][200ms][info][gc,phases   ] GC(0)   Evacuate Collection Set: 4.8ms\n" +
                        "[2022-01-29T16:34:51.609+0000][200ms][info][gc,phases   ] GC(0)   Post Evacuate Collection Set: 1.0ms\n" +
                        "[2022-01-29T16:34:51.609+0000][200ms][info][gc,phases   ] GC(0)   Other: 0.9ms\n" +
                        "[2022-01-29T16:34:51.609+0000][201ms][info][gc,heap     ] GC(0) Eden regions: 2->0(1)\n" +
                        "[2022-01-29T16:34:51.610+0000][201ms][info][gc,heap     ] GC(0) Survivor regions: 0->1(1)\n" +
                        "[2022-01-29T16:34:51.610+0000][201ms][info][gc,heap     ] GC(0) Old regions: 0->1\n" +
                        "[2022-01-29T16:34:51.610+0000][201ms][info][gc,heap     ] GC(0) Archive regions: 2->2\n" +
                        "[2022-01-29T16:34:51.610+0000][202ms][info][gc,heap     ] GC(0) Humongous regions: 0->0\n" +
                        "[2022-01-29T16:34:51.611+0000][202ms][info][gc,metaspace] GC(0) Metaspace: 257K(448K)->257K(448K) NonClass: 242K(320K)->242K(320K) Class: 15K(128K)->15K(128K)\n" +
                        "[2022-01-29T16:34:51.611+0000][203ms][info][gc          ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 2M->2M(18M) 10.669ms\n" +
                        "[2022-01-29T16:34:51.612+0000][203ms][info][gc,cpu      ] GC(0) User=0.00s Sys=0.00s Real=0.01s\n")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        Iterator<GCEvent> gcEventIterator = (Iterator<GCEvent>)model.get(0).details();
        GCEvent tenuredEvent = ((GCEvent)model.get(0)).getTenured();
        assertThat("tenured event", tenuredEvent, notNullValue());
        assertThat("tenured event pre", tenuredEvent.getPreUsed(), is(2 * 2048));
        assertThat("tenured event post", tenuredEvent.getPostUsed(), is(3 * 2048));
    }

    @Test
    public void testG1FullConcurrentMarkCycle() throws Exception {
        GCModel model = getGCModelFromLogFile("sample-ujl-jdk17-full-concurrent-cycle-g1-gc-all-safepont,os-cpu.txt");
        assertThat("size", model.size(), is(11));
        assertThat("concurrent event count", model.getConcurrentEventPauses().size(), is(1));

        Iterator<ConcurrentGCEvent> concurrentGCEventIterator = model.getConcurrentGCEvents();
        ConcurrentGCEvent concurrentGCEvent = concurrentGCEventIterator.next();
        assertThat("concurrent event, start", concurrentGCEvent.isConcurrentCollectionStart(), is(true));
        assertThat("iterator hasNext", concurrentGCEventIterator.hasNext(), is(true));

        ConcurrentGCEvent concurrentGCEventEnd = concurrentGCEventIterator.next();
        assertThat("concurrent event, end", concurrentGCEventEnd.isConcurrentCollectionEnd(), is(true));
        assertThat("concurrent event, duration", concurrentGCEventEnd.getPause(), closeTo(0.077704, 0.000001));

        assertThat("iterator is finished", concurrentGCEventIterator.hasNext(), is(false));
    }

    @Test
    public void testG1ConcurrentUndoCycle() throws IOException {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2022-04-19T19:34:58.116+0000][17241ms][info][gc             ] GC(822) Concurrent Undo Cycle\n" +
                        "[2022-04-19T19:34:58.117+0000][17241ms][info][gc,marking     ] GC(822) Concurrent Cleanup for Next Mark\n" +
                        "[2022-04-19T19:34:58.118+0000][17243ms][info][gc,marking     ] GC(822) Concurrent Cleanup for Next Mark 1.819ms\n" +
                        "[2022-04-19T19:34:58.119+0000][17243ms][info][gc             ] GC(822) Concurrent Undo Cycle 2.662ms\n")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(2));

        assertThat("Concurrent undo Cycle", model.get(0).getPause(), closeTo(0.0, 0.01));
        assertThat("Concurrent undo Cycle with Pause", model.get(1).getPause(), closeTo(0.002662, 0.000001));

    }
}
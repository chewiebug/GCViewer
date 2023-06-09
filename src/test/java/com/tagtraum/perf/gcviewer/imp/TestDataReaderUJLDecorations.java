package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.util.DateHelper;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test unified java logging with different decorations
 */
public class TestDataReaderUJLDecorations {
    @Test
    public void testUJLWithTimeLevelTagsDecorations() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2022-04-19T19:34:40.899+0000][info][gc,init] Heap Region Size: 2M\n" +
                        "[2022-01-29T16:34:51.601+0000][info][gc,start    ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)\n" +
                        "[2022-01-29T16:34:51.601+0000][info][gc,task     ] GC(0) Using 1 workers of 1 for evacuation\n" +
                        "[2022-01-29T16:34:51.608+0000][info][gc,phases   ] GC(0)   Pre Evacuate Collection Set: 0.2ms\n" +
                        "[2022-01-29T16:34:51.608+0000][info][gc,phases   ] GC(0)   Merge Heap Roots: 0.2ms\n" +
                        "[2022-01-29T16:34:51.609+0000][info][gc,phases   ] GC(0)   Evacuate Collection Set: 4.8ms\n" +
                        "[2022-01-29T16:34:51.609+0000][info][gc,phases   ] GC(0)   Post Evacuate Collection Set: 1.0ms\n" +
                        "[2022-01-29T16:34:51.609+0000][info][gc,phases   ] GC(0)   Other: 0.9ms\n" +
                        "[2022-01-29T16:34:51.609+0000][info][gc,heap     ] GC(0) Eden regions: 2->0(1)\n" +
                        "[2022-01-29T16:34:51.610+0000][info][gc,heap     ] GC(0) Survivor regions: 0->1(1)\n" +
                        "[2022-01-29T16:34:51.610+0000][info][gc,heap     ] GC(0) Old regions: 0->1\n" +
                        "[2022-01-29T16:34:51.610+0000][info][gc,heap     ] GC(0) Archive regions: 2->2\n" +
                        "[2022-01-29T16:34:51.610+0000][info][gc,heap     ] GC(0) Humongous regions: 0->0\n" +
                        "[2022-01-29T16:34:51.611+0000][info][gc,metaspace] GC(0) Metaspace: 257K(448K)->257K(448K) NonClass: 242K(320K)->242K(320K) Class: 15K(128K)->15K(128K)\n" +
                        "[2022-01-29T16:34:51.611+0000][info][gc          ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 2M->2M(18M) 10.669ms\n" +
                        "[2022-01-29T16:34:51.612+0000][info][gc,cpu      ] GC(0) User=0.00s Sys=0.00s Real=0.01s\n")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();
        assertEquals("number of warnings", 0, handler.getCount());
        assertEquals("number of events", 1, model.size());

        GCEvent tenuredEvent = ((GCEvent)model.get(0)).getTenured();
        assertNotNull("tenured event", tenuredEvent);
        assertEquals("tenured event time", "2022-01-29T16:34:51.610+0000", DateHelper.formatDate(tenuredEvent.getDatestamp()));
        assertEquals("tenured event pre", 2 * 2048, tenuredEvent.getPreUsed());
        assertEquals("tenured event post", 3 * 2048, tenuredEvent.getPostUsed());
    }

    @Test
    public void testUJLWithUptimeLevelTagsDecorations() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[0.023s][info][gc,init] Heap Region Size: 2M\n" +
                        "[0.192s][info][gc,start    ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)\n" +
                        "[0.192s][info][gc,task     ] GC(0) Using 1 workers of 1 for evacuation\n" +
                        "[0.199s][info][gc,phases   ] GC(0)   Pre Evacuate Collection Set: 0.2ms\n" +
                        "[0.199s][info][gc,phases   ] GC(0)   Merge Heap Roots: 0.2ms\n" +
                        "[0.200s][info][gc,phases   ] GC(0)   Evacuate Collection Set: 4.8ms\n" +
                        "[0.200s][info][gc,phases   ] GC(0)   Post Evacuate Collection Set: 1.0ms\n" +
                        "[0.200s][info][gc,phases   ] GC(0)   Other: 0.9ms\n" +
                        "[0.201s][info][gc,heap     ] GC(0) Eden regions: 2->0(1)\n" +
                        "[0.201s][info][gc,heap     ] GC(0) Survivor regions: 0->1(1)\n" +
                        "[0.201s][info][gc,heap     ] GC(0) Old regions: 0->1\n" +
                        "[0.201s][info][gc,heap     ] GC(0) Archive regions: 2->2\n" +
                        "[0.202s][info][gc,heap     ] GC(0) Humongous regions: 0->0\n" +
                        "[0.202s][info][gc,metaspace] GC(0) Metaspace: 257K(448K)->257K(448K) NonClass: 242K(320K)->242K(320K) Class: 15K(128K)->15K(128K)\n" +
                        "[0.203s][info][gc          ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 2M->2M(18M) 10.669ms\n" +
                        "[0.203s][info][gc,cpu      ] GC(0) User=0.00s Sys=0.00s Real=0.01s\n")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();
        assertEquals("number of warnings", 0, handler.getCount());
        assertEquals("number of events", 1, model.size());

        GCEvent tenuredEvent = ((GCEvent)model.get(0)).getTenured();
        assertNotNull("tenured event", tenuredEvent);
        assertEquals("tenured event timestamp", 0.202, tenuredEvent.getTimestamp(), 0.001);
        assertEquals("tenured event pre", 2 * 2048, tenuredEvent.getPreUsed());
        assertEquals("tenured event post", 3 * 2048, tenuredEvent.getPostUsed());
    }

    @Test
    public void testUJLWithTimePidTidLevelTagsDecorations() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2022-04-19T19:34:40.899+0000][11][44][info][gc,init] Heap Region Size: 2M\n" +
                        "[2022-01-29T16:34:51.601+0000][11][50][info][gc,start    ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)\n" +
                        "[2022-01-29T16:34:51.601+0000][11][50][info][gc,task     ] GC(0) Using 1 workers of 1 for evacuation\n" +
                        "[2022-01-29T16:34:51.608+0000][11][50][info][gc,phases   ] GC(0)   Pre Evacuate Collection Set: 0.2ms\n" +
                        "[2022-01-29T16:34:51.608+0000][11][50][info][gc,phases   ] GC(0)   Merge Heap Roots: 0.2ms\n" +
                        "[2022-01-29T16:34:51.609+0000][11][50][info][gc,phases   ] GC(0)   Evacuate Collection Set: 4.8ms\n" +
                        "[2022-01-29T16:34:51.609+0000][11][50][info][gc,phases   ] GC(0)   Post Evacuate Collection Set: 1.0ms\n" +
                        "[2022-01-29T16:34:51.609+0000][11][50][info][gc,phases   ] GC(0)   Other: 0.9ms\n" +
                        "[2022-01-29T16:34:51.609+0000][11][50][info][gc,heap     ] GC(0) Eden regions: 2->0(1)\n" +
                        "[2022-01-29T16:34:51.610+0000][11][50][info][gc,heap     ] GC(0) Survivor regions: 0->1(1)\n" +
                        "[2022-01-29T16:34:51.610+0000][11][50][info][gc,heap     ] GC(0) Old regions: 0->1\n" +
                        "[2022-01-29T16:34:51.610+0000][11][50][info][gc,heap     ] GC(0) Archive regions: 2->2\n" +
                        "[2022-01-29T16:34:51.610+0000][11][50][info][gc,heap     ] GC(0) Humongous regions: 0->0\n" +
                        "[2022-01-29T16:34:51.611+0000][11][50][info][gc,metaspace] GC(0) Metaspace: 257K(448K)->257K(448K) NonClass: 242K(320K)->242K(320K) Class: 15K(128K)->15K(128K)\n" +
                        "[2022-01-29T16:34:51.611+0000][11][50][info][gc          ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 2M->2M(18M) 10.669ms\n" +
                        "[2022-01-29T16:34:51.612+0000][11][50][info][gc,cpu      ] GC(0) User=0.00s Sys=0.00s Real=0.01s\n")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();
        assertEquals("number of warnings", 0, handler.getCount());
        assertEquals("number of events", 1, model.size());

        GCEvent tenuredEvent = ((GCEvent)model.get(0)).getTenured();
        assertNotNull("tenured event", tenuredEvent);
        assertEquals("tenured event time", "2022-01-29T16:34:51.610+0000", DateHelper.formatDate(tenuredEvent.getDatestamp()));
        assertEquals("tenured event pre", 2 * 2048, tenuredEvent.getPreUsed());
        assertEquals("tenured event post", 3 * 2048, tenuredEvent.getPostUsed());
    }

    @Test
    public void testUJLWithUptimePidTidLevelTagsDecorations() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[0.023s][11][50][info][gc,init] Heap Region Size: 2M\n" +
                        "[0.192s][11][50][info][gc,start    ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)\n" +
                        "[0.192s][11][50][info][gc,task     ] GC(0) Using 1 workers of 1 for evacuation\n" +
                        "[0.199s][11][50][info][gc,phases   ] GC(0)   Pre Evacuate Collection Set: 0.2ms\n" +
                        "[0.199s][11][50][info][gc,phases   ] GC(0)   Merge Heap Roots: 0.2ms\n" +
                        "[0.200s][11][50][info][gc,phases   ] GC(0)   Evacuate Collection Set: 4.8ms\n" +
                        "[0.200s][11][50][info][gc,phases   ] GC(0)   Post Evacuate Collection Set: 1.0ms\n" +
                        "[0.200s][11][50][info][gc,phases   ] GC(0)   Other: 0.9ms\n" +
                        "[0.201s][11][50][info][gc,heap     ] GC(0) Eden regions: 2->0(1)\n" +
                        "[0.201s][11][50][info][gc,heap     ] GC(0) Survivor regions: 0->1(1)\n" +
                        "[0.201s][11][50][info][gc,heap     ] GC(0) Old regions: 0->1\n" +
                        "[0.201s][11][50][info][gc,heap     ] GC(0) Archive regions: 2->2\n" +
                        "[0.202s][11][50][info][gc,heap     ] GC(0) Humongous regions: 0->0\n" +
                        "[0.202s][11][50][info][gc,metaspace] GC(0) Metaspace: 257K(448K)->257K(448K) NonClass: 242K(320K)->242K(320K) Class: 15K(128K)->15K(128K)\n" +
                        "[0.203s][11][50][info][gc          ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 2M->2M(18M) 10.669ms\n" +
                        "[0.203s][11][50][info][gc,cpu      ] GC(0) User=0.00s Sys=0.00s Real=0.01s\n")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();
        assertEquals("number of warnings", 0, handler.getCount());
        assertEquals("number of events", 1, model.size());

        GCEvent tenuredEvent = ((GCEvent)model.get(0)).getTenured();
        assertNotNull("tenured event", tenuredEvent);
        assertEquals("tenured event timestamp", 0.202, tenuredEvent.getTimestamp(), 0.001);
        assertEquals("tenured event pre", 2 * 2048, tenuredEvent.getPreUsed());
        assertEquals("tenured event post", 3 * 2048, tenuredEvent.getPostUsed());
    }

    @Test
    public void testUJLWithMillsTimeLevelTagsDecorations() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[1682424038948ms][info ][gc,init   ] Heap Region Size: 2M\n" +
                        "[1682424051450ms][info ][gc,start                ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)\n" +
                        "[1682424051451ms][info ][gc,task                 ] GC(0) Using 1 workers of 1 for evacuation\n" +
                        "[1682424051479ms][info ][gc,phases               ] GC(0)   Pre Evacuate Collection Set: 0.2ms\n" +
                        "[1682424051479ms][info ][gc,phases               ] GC(0)   Merge Heap Roots: 0.2ms\n" +
                        "[1682424051479ms][info ][gc,phases               ] GC(0)   Evacuate Collection Set: 4.8ms\n" +
                        "[1682424051479ms][info ][gc,phases               ] GC(0)   Post Evacuate Collection Set: 1.0ms\n" +
                        "[1682424051479ms][info ][gc,phases               ] GC(0)   Other: 0.9ms\n" +
                        "[1682424051479ms][info ][gc,heap                 ] GC(0) Eden regions: 2->0(1)\n" +
                        "[1682424051479ms][info ][gc,heap                 ] GC(0) Survivor regions: 0->1(1)\n" +
                        "[1682424051479ms][info ][gc,heap                 ] GC(0) Old regions: 0->1\n" +
                        "[1682424051479ms][info ][gc,heap                 ] GC(0) Archive regions: 2->2\n" +
                        "[1682424051479ms][info ][gc,heap                 ] GC(0) Humongous regions: 0->0\n" +
                        "[1682424051479ms][info ][gc,metaspace            ] GC(0) Metaspace: 257K(448K)->257K(448K) NonClass: 242K(320K)->242K(320K) Class: 15K(128K)->15K(128K)\n" +
                        "[1682424051480ms][info ][gc                      ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 2M->2M(18M) 10.669ms\n" +
                        "[1682424051480ms][info ][gc,cpu                  ] GC(0) User=0.00s Sys=0.00s Real=0.01s\n")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();
        assertEquals("number of warnings", 0, handler.getCount());
        assertEquals("number of events", 1, model.size());

        GCEvent tenuredEvent = ((GCEvent)model.get(0)).getTenured();
        assertNotNull("tenured event", tenuredEvent);
        assertEquals("tenured event time", 1682424051479L, tenuredEvent.getDatestamp().toInstant().toEpochMilli());
        assertEquals("tenured event pre", 2 * 2048, tenuredEvent.getPreUsed());
        assertEquals("tenured event post", 3 * 2048, tenuredEvent.getPostUsed());
    }

    @Test
    public void testUJLWithMillsUptimeLevelTagsDecorations() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[36ms][info ][gc,init   ] Heap Region Size: 2M\n" +
                        "[12538ms][info ][gc,start                ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)\n" +
                        "[12539ms][info ][gc,task                 ] GC(0) Using 1 workers of 1 for evacuation\n" +
                        "[12567ms][info ][gc,phases               ] GC(0)   Pre Evacuate Collection Set: 0.2ms\n" +
                        "[12567ms][info ][gc,phases               ] GC(0)   Merge Heap Roots: 0.2ms\n" +
                        "[12567ms][info ][gc,phases               ] GC(0)   Evacuate Collection Set: 4.8ms\n" +
                        "[12567ms][info ][gc,phases               ] GC(0)   Post Evacuate Collection Set: 1.0ms\n" +
                        "[12567ms][info ][gc,phases               ] GC(0)   Other: 0.9ms\n" +
                        "[12567ms][info ][gc,heap                 ] GC(0) Eden regions: 2->0(1)\n" +
                        "[12567ms][info ][gc,heap                 ] GC(0) Survivor regions: 0->1(1)\n" +
                        "[12567ms][info ][gc,heap                 ] GC(0) Old regions: 0->1\n" +
                        "[12567ms][info ][gc,heap                 ] GC(0) Archive regions: 2->2\n" +
                        "[12567ms][info ][gc,heap                 ] GC(0) Humongous regions: 0->0\n" +
                        "[12567ms][info ][gc,metaspace            ] GC(0) Metaspace: 257K(448K)->257K(448K) NonClass: 242K(320K)->242K(320K) Class: 15K(128K)->15K(128K)\n" +
                        "[12567ms][info ][gc                      ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 2M->2M(18M) 10.669ms\n" +
                        "[12568ms][info ][gc,cpu                  ] GC(0) User=0.00s Sys=0.00s Real=0.01s\n")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();
        assertEquals("number of warnings", 0, handler.getCount());
        assertEquals("number of events", 1, model.size());

        GCEvent tenuredEvent = ((GCEvent)model.get(0)).getTenured();
        assertNotNull("tenured event", tenuredEvent);
        assertEquals("tenured event timestamp", 12.567, tenuredEvent.getTimestamp(), 0.001);
        assertEquals("tenured event pre", 2 * 2048, tenuredEvent.getPreUsed());
        assertEquals("tenured event post", 3 * 2048, tenuredEvent.getPostUsed());
    }

    @Test
    public void testUJLWithMillsTimeMillsUptimeLevelTagsDecorations() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[1682424038948ms][36ms][info ][gc,init   ] Heap Region Size: 2M\n" +
                        "[1682424051450ms][12538ms][info ][gc,start                ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)\n" +
                        "[1682424051451ms][12539ms][info ][gc,task                 ] GC(0) Using 1 workers of 1 for evacuation\n" +
                        "[1682424051479ms][12567ms][info ][gc,phases               ] GC(0)   Pre Evacuate Collection Set: 0.2ms\n" +
                        "[1682424051479ms][12567ms][info ][gc,phases               ] GC(0)   Merge Heap Roots: 0.2ms\n" +
                        "[1682424051479ms][12567ms][info ][gc,phases               ] GC(0)   Evacuate Collection Set: 4.8ms\n" +
                        "[1682424051479ms][12567ms][info ][gc,phases               ] GC(0)   Post Evacuate Collection Set: 1.0ms\n" +
                        "[1682424051479ms][12567ms][info ][gc,phases               ] GC(0)   Other: 0.9ms\n" +
                        "[1682424051479ms][12567ms][info ][gc,heap                 ] GC(0) Eden regions: 2->0(1)\n" +
                        "[1682424051479ms][12567ms][info ][gc,heap                 ] GC(0) Survivor regions: 0->1(1)\n" +
                        "[1682424051479ms][12567ms][info ][gc,heap                 ] GC(0) Old regions: 0->1\n" +
                        "[1682424051479ms][12567ms][info ][gc,heap                 ] GC(0) Archive regions: 2->2\n" +
                        "[1682424051479ms][12567ms][info ][gc,heap                 ] GC(0) Humongous regions: 0->0\n" +
                        "[1682424051479ms][12567ms][info ][gc,metaspace            ] GC(0) Metaspace: 257K(448K)->257K(448K) NonClass: 242K(320K)->242K(320K) Class: 15K(128K)->15K(128K)\n" +
                        "[1682424051480ms][12567ms][info ][gc                      ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 2M->2M(18M) 10.669ms\n" +
                        "[1682424051480ms][12568ms][info ][gc,cpu                  ] GC(0) User=0.00s Sys=0.00s Real=0.01s\n")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();
        assertEquals("number of warnings", 0, handler.getCount());
        assertEquals("number of events", 1, model.size());

        GCEvent tenuredEvent = ((GCEvent)model.get(0)).getTenured();
        assertNotNull("tenured event", tenuredEvent);
        assertEquals("tenured event time", 1682424051479L, tenuredEvent.getDatestamp().toInstant().toEpochMilli());
        assertEquals("tenured event timestamp", 12.567, tenuredEvent.getTimestamp(), 0.001);
        assertEquals("tenured event pre", 2 * 2048, tenuredEvent.getPreUsed());
        assertEquals("tenured event post", 3 * 2048, tenuredEvent.getPostUsed());
    }

    @Test
    public void testUJLWithNanoTimeNanoUptimeLevelTagsDecorations() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[3713267866602725ns][36215026ns][info ][gc,init   ] Heap Region Size: 2M\n" +
                        "[3713280368972030ns][12538584330ns][info ][gc,start                ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)\n" +
                        "[3713280369719424ns][12539331741ns][info ][gc,task                 ] GC(0) Using 1 workers of 1 for evacuation\n" +
                        "[3713280397999190ns][12567611501ns][info ][gc,phases               ] GC(0)   Pre Evacuate Collection Set: 0.2ms\n" +
                        "[3713280398055938ns][12567668275ns][info ][gc,phases               ] GC(0)   Merge Heap Roots: 0.2ms\n" +
                        "[3713280398065376ns][12567677692ns][info ][gc,phases               ] GC(0)   Evacuate Collection Set: 4.8ms\n" +
                        "[3713280398073261ns][12567685567ns][info ][gc,phases               ] GC(0)   Post Evacuate Collection Set: 1.0ms\n" +
                        "[3713280398085351ns][12567697659ns][info ][gc,phases               ] GC(0)   Other: 0.9ms\n" +
                        "[3713280398183418ns][12567795761ns][info ][gc,heap                 ] GC(0) Eden regions: 2->0(1)\n" +
                        "[3713280398210215ns][12567822522ns][info ][gc,heap                 ] GC(0) Survivor regions: 0->1(1)\n" +
                        "[3713280398223704ns][12567836010ns][info ][gc,heap                 ] GC(0) Old regions: 0->1\n" +
                        "[3713280398236252ns][12567848558ns][info ][gc,heap                 ] GC(0) Archive regions: 2->2\n" +
                        "[3713280398260137ns][12567872443ns][info ][gc,heap                 ] GC(0) Humongous regions: 0->0\n" +
                        "[3713280398275986ns][12567888293ns][info ][gc,metaspace            ] GC(0) Metaspace: 257K(448K)->257K(448K) NonClass: 242K(320K)->242K(320K) Class: 15K(128K)->15K(128K)\n" +
                        "[3713280398371361ns][12567983672ns][info ][gc                      ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 2M->2M(18M) 10.669ms\n" +
                        "[3713280398396786ns][12568009089ns][info ][gc,cpu                  ] GC(0) User=0.00s Sys=0.00s Real=0.01s\n")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();
        assertEquals("number of warnings", 0, handler.getCount());
        assertEquals("number of events", 1, model.size());

        GCEvent tenuredEvent = ((GCEvent)model.get(0)).getTenured();
        assertNotNull("tenured event", tenuredEvent);
        assertEquals("tenured event timestamp", 12.567872443, tenuredEvent.getTimestamp(), 0.000000001);
        assertEquals("tenured event pre", 2 * 2048, tenuredEvent.getPreUsed());
        assertEquals("tenured event post", 3 * 2048, tenuredEvent.getPostUsed());
    }

    @Test
    public void testUJLWithOnlyOneNanoTimeLevelTagsDecorations() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[36215026ns][info ][gc,init   ] Heap Region Size: 2M\n" +
                        "[12538584330ns][info ][gc,start                ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)\n" +
                        "[12539331741ns][info ][gc,task                 ] GC(0) Using 1 workers of 1 for evacuation\n" +
                        "[12567611501ns][info ][gc,phases               ] GC(0)   Pre Evacuate Collection Set: 0.2ms\n" +
                        "[12567668275ns][info ][gc,phases               ] GC(0)   Merge Heap Roots: 0.2ms\n" +
                        "[12567677692ns][info ][gc,phases               ] GC(0)   Evacuate Collection Set: 4.8ms\n" +
                        "[12567685567ns][info ][gc,phases               ] GC(0)   Post Evacuate Collection Set: 1.0ms\n" +
                        "[12567697659ns][info ][gc,phases               ] GC(0)   Other: 0.9ms\n" +
                        "[12567795761ns][info ][gc,heap                 ] GC(0) Eden regions: 2->0(1)\n" +
                        "[12567822522ns][info ][gc,heap                 ] GC(0) Survivor regions: 0->1(1)\n" +
                        "[12567836010ns][info ][gc,heap                 ] GC(0) Old regions: 0->1\n" +
                        "[12567848558ns][info ][gc,heap                 ] GC(0) Archive regions: 2->2\n" +
                        "[12567872443ns][info ][gc,heap                 ] GC(0) Humongous regions: 0->0\n" +
                        "[12567888293ns][info ][gc,metaspace            ] GC(0) Metaspace: 257K(448K)->257K(448K) NonClass: 242K(320K)->242K(320K) Class: 15K(128K)->15K(128K)\n" +
                        "[12567983672ns][info ][gc                      ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 2M->2M(18M) 10.669ms\n" +
                        "[12568009089ns][info ][gc,cpu                  ] GC(0) User=0.00s Sys=0.00s Real=0.01s\n")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();
        assertNotEquals("number of warnings", 0, handler.getCount());
        assertEquals("number of events", 0, model.size());
    }

    @Test
    public void testUJLWithAllDecorations() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2023-04-25T20:00:38.948+0800][0.036s][1682424038948ms][36ms][3713267866602725ns][36215026ns][15][49][info ][gc,init   ] Heap Region Size: 2M\n" +
                        "[2023-04-25T20:00:51.450+0800][12.539s][1682424051450ms][12538ms][3713280368972030ns][12538584330ns][15][55 ][info ][gc,start                ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)\n" +
                        "[2023-04-25T20:00:51.451+0800][12.539s][1682424051451ms][12539ms][3713280369719424ns][12539331741ns][15][55 ][info ][gc,task                 ] GC(0) Using 1 workers of 1 for evacuation\n" +
                        "[2023-04-25T20:00:51.479+0800][12.568s][1682424051479ms][12567ms][3713280397999190ns][12567611501ns][15][55 ][info ][gc,phases               ] GC(0)   Pre Evacuate Collection Set: 0.2ms\n" +
                        "[2023-04-25T20:00:51.479+0800][12.568s][1682424051479ms][12567ms][3713280398055938ns][12567668275ns][15][55 ][info ][gc,phases               ] GC(0)   Merge Heap Roots: 0.2ms\n" +
                        "[2023-04-25T20:00:51.479+0800][12.568s][1682424051479ms][12567ms][3713280398065376ns][12567677692ns][15][55 ][info ][gc,phases               ] GC(0)   Evacuate Collection Set: 4.8ms\n" +
                        "[2023-04-25T20:00:51.479+0800][12.568s][1682424051479ms][12567ms][3713280398073261ns][12567685567ns][15][55 ][info ][gc,phases               ] GC(0)   Post Evacuate Collection Set: 1.0ms\n" +
                        "[2023-04-25T20:00:51.479+0800][12.568s][1682424051479ms][12567ms][3713280398085351ns][12567697659ns][15][55 ][info ][gc,phases               ] GC(0)   Other: 0.9ms\n" +
                        "[2023-04-25T20:00:51.479+0800][12.568s][1682424051479ms][12567ms][3713280398183418ns][12567795761ns][15][55 ][info ][gc,heap                 ] GC(0) Eden regions: 2->0(1)\n" +
                        "[2023-04-25T20:00:51.479+0800][12.568s][1682424051479ms][12567ms][3713280398210215ns][12567822522ns][15][55 ][info ][gc,heap                 ] GC(0) Survivor regions: 0->1(1)\n" +
                        "[2023-04-25T20:00:51.479+0800][12.568s][1682424051479ms][12567ms][3713280398223704ns][12567836010ns][15][55 ][info ][gc,heap                 ] GC(0) Old regions: 0->1\n" +
                        "[2023-04-25T20:00:51.479+0800][12.568s][1682424051479ms][12567ms][3713280398236252ns][12567848558ns][15][55 ][info ][gc,heap                 ] GC(0) Archive regions: 2->2\n" +
                        "[2023-04-25T20:00:51.479+0800][12.568s][1682424051479ms][12567ms][3713280398260137ns][12567872443ns][15][55 ][info ][gc,heap                 ] GC(0) Humongous regions: 0->0\n" +
                        "[2023-04-25T20:00:51.479+0800][12.568s][1682424051479ms][12567ms][3713280398275986ns][12567888293ns][15][55 ][info ][gc,metaspace            ] GC(0) Metaspace: 257K(448K)->257K(448K) NonClass: 242K(320K)->242K(320K) Class: 15K(128K)->15K(128K)\n" +
                        "[2023-04-25T20:00:51.480+0800][12.568s][1682424051480ms][12567ms][3713280398371361ns][12567983672ns][15][55 ][info ][gc                      ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 2M->2M(18M) 10.669ms\n" +
                        "[2023-04-25T20:00:51.480+0800][12.568s][1682424051480ms][12568ms][3713280398396786ns][12568009089ns][15][55 ][info ][gc,cpu                  ] GC(0) User=0.00s Sys=0.00s Real=0.01s\n")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();
        assertEquals("number of warnings", 0, handler.getCount());
        assertEquals("number of events", 1, model.size());

        GCEvent tenuredEvent = ((GCEvent)model.get(0)).getTenured();
        assertNotNull("tenured event", tenuredEvent);
        assertEquals("tenured event time", "2023-04-25T20:00:51.479+0800", DateHelper.formatDate(tenuredEvent.getDatestamp()));
        assertEquals("tenured event timestamp", 12.567872443, tenuredEvent.getTimestamp(), 0.000000001);
        assertEquals("tenured event pre", 2 * 2048, tenuredEvent.getPreUsed());
        assertEquals("tenured event post", 3 * 2048, tenuredEvent.getPostUsed());
    }
}

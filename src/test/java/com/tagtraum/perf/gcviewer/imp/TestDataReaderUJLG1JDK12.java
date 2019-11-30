package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Test;

/**
 * Test unified java logging G1 algorithm in OpenJDK 12
 */
public class TestDataReaderUJLG1JDK12 {
    @Test
    public void testG1ArchiveRegions() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2019-09-16T20:06:59.836+0000][0.153s][info][gc,start     ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)\n" +
                        "[2019-09-16T20:06:59.836+0000][0.153s][info][gc,task      ] GC(0) Using 2 workers of 2 for evacuation\n" +
                        "[2019-09-16T20:06:59.840+0000][0.157s][info][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms\n" +
                        "[2019-09-16T20:06:59.840+0000][0.157s][info][gc,phases    ] GC(0)   Evacuate Collection Set: 3.1ms\n" +
                        "[2019-09-16T20:06:59.840+0000][0.157s][info][gc,phases    ] GC(0)   Post Evacuate Collection Set: 0.7ms\n" +
                        "[2019-09-16T20:06:59.840+0000][0.157s][info][gc,phases    ] GC(0)   Other: 0.4ms\n" +
                        "[2019-09-16T20:06:59.840+0000][0.157s][info][gc,heap      ] GC(0) Eden regions: 7->0(4)\n" +
                        "[2019-09-16T20:06:59.840+0000][0.157s][info][gc,heap      ] GC(0) Survivor regions: 0->1(1)\n" +
                        "[2019-09-16T20:06:59.840+0000][0.157s][info][gc,heap      ] GC(0) Old regions: 0->6\n" +
                        "[2019-09-16T20:06:59.840+0000][0.157s][info][gc,heap      ] GC(0) Archive regions: 2->2\n" +
                        "[2019-09-16T20:06:59.840+0000][0.157s][info][gc,heap      ] GC(0) Humongous regions: 0->0\n" +
                        "[2019-09-16T20:06:59.840+0000][0.157s][info][gc,metaspace ] GC(0) Metaspace: 189K->189K(1056768K)\n" +
                        "[2019-09-16T20:06:59.840+0000][0.157s][info][gc           ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 7M->7M(34M) 4.379ms\n" +
                        "[2019-09-16T20:06:59.840+0000][0.157s][info][gc,cpu       ] GC(0) User=0.00s Sys=0.01s Real=0.01s\n")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("pause", model.get(0).getPause(), closeTo(0.004379, 0.0000001));
        assertThat("name", model.get(0).getTypeAsString(), containsString("Archive regions"));
    }

}

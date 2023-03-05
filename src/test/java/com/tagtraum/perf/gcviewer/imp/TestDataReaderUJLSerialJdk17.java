package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Test;

/**
 * Tests unified jvm logging parser for serial gc events.
 */
public class TestDataReaderUJLSerialJdk17 {

    @Test
    public void testStandardEvent() throws Exception  {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[2023-03-05T15:03:14.411+0000][0.276s][info][gc,start    ] GC(0) Pause Young (Allocation Failure)\n" +
                        "[2023-03-05T15:03:14.415+0000][0.280s][info][gc,heap     ] GC(0) DefNew: 8703K(9792K)->1088K(9792K) Eden: 8703K(8704K)->0K(8704K) From: 0K(1088K)->1088K(1088K)\n" +
                        "[2023-03-05T15:03:14.415+0000][0.281s][info][gc,heap     ] GC(0) Tenured: 0K(21888K)->6627K(21888K)\n" +
                        "[2023-03-05T15:03:14.416+0000][0.281s][info][gc,metaspace] GC(0) Metaspace: 309K(512K)->309K(512K) NonClass: 293K(384K)->293K(384K) Class: 16K(128K)->16K(128K)\n" +
                        "[2023-03-05T15:03:14.416+0000][0.282s][info][gc          ] GC(0) Pause Young (Allocation Failure) 8M->7M(30M) 5.480ms\n" +
                        "[2023-03-05T15:03:14.416+0000][0.282s][info][gc,cpu      ] GC(0) User=0.00s Sys=0.00s Real=0.01s\n" +
                        "[2023-03-05T15:03:14.417+0000][0.282s][info][safepoint   ] Safepoint \"GenCollectForAllocation\", Time since last: 209726300 ns, Reaching safepoint: 51400 ns, At safepoint: 6279300 ns, Total: 6330700 ns\n"
                ).getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(0));
        assertThat("number of events", model.size(), is(1));
        assertThat("event type", model.get(0).getExtendedType().getType(), is(Type.UJL_PAUSE_YOUNG));
        assertThat("event pause", model.get(0).getPause(), closeTo(0.00548, 0.0000001));

        assertThat("phases", model.getGcEventPhases().size(), is(0));
    }

}

package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Test;

/**
 * Tests unified jvm logging parser for serial gc events.
 */
public class TestDataReaderUJLSerial {
    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_OPENJDK_UJL, fileName);
    }

    @Test
    public void parseGcDefaults() throws Exception {
        GCModel model = getGCModelFromLogFile("sample-ujl-serial-gc-defaults.txt");
        assertThat("size", model.size(), is(11));
        assertThat("amount gc events", model.getGcEventPauses().size(), is(1));
        assertThat("amount full gc events", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));

        AbstractGCEvent<?> event1 = model.get(0);
        assertThat("event1 type", event1.getTypeAsString(), startsWith(Type.UJL_PAUSE_YOUNG.getName()));
        assertThat("event1 pause", event1.getPause(), closeTo(0.011421, 0.00001));
        assertThat("event1 heap before", event1.getPreUsed(), is(1024 * 25));
        assertThat("event1 heap after", event1.getPostUsed(), is(1024 * 23));
        assertThat("event1 total heap", event1.getTotal(), is(1024 * 92));

        AbstractGCEvent<?> event2 = model.get(2);
        assertThat("event2 type", event2.getTypeAsString(), startsWith(Type.UJL_PAUSE_FULL.getName()));
        assertThat("event2 pause", event2.getPause(), closeTo(0.007097, 0.00001));
        assertThat("event2 heap before", event2.getPreUsed(), is(1024 * 74));
        assertThat("event2 heap after", event2.getPostUsed(), is(1024 * 10));
        assertThat("event2 total heap", event2.getTotal(), is(1024 * 92));

        // gc log says GC(10) for this event
        AbstractGCEvent<?> event3 = model.get(9);
        assertThat("event3 type", event3.getTypeAsString(), startsWith(Type.UJL_PAUSE_FULL.getName()));

        // gc log says GC(9) for this event, but comes after GC(10)
        AbstractGCEvent<?> event4 = model.get(10);
        assertThat("event4 type", event4.getTypeAsString(), startsWith(Type.UJL_PAUSE_YOUNG.getName()));
    }

    @Test
    public void parseGcAllSafepointOsCpu() throws Exception {
        GCModel model = getGCModelFromLogFile("sample-ujl-serial-gc-all,safepoint,os+cpu.txt");
        assertThat("size", model.size(), is(4));
        assertThat("amount of STW GC pause types", model.getGcEventPauses().size(), is(1));
        assertThat("amount of STW Full GC pause types", model.getFullGcEventPauses().size(), is(1));
        assertThat("amount of concurrent pause types", model.getConcurrentEventPauses().size(), is(0));

        AbstractGCEvent<?> event1 = model.get(0);
        assertThat("event1 type", event1.getTypeAsString(), startsWith(Type.UJL_PAUSE_YOUNG.getName()));
        assertThat("event1 pause", event1.getPause(), closeTo(0.009814, 0.00001));
        assertThat("event1 heap before", event1.getPreUsed(), is(1024 * 25));
        assertThat("event1 heap after", event1.getPostUsed(), is(1024 * 23));
        assertThat("event1 total heap", event1.getTotal(), is(1024 * 92));

        // TODO fix timestamps or renderers (seeing the ujl logs, I realise, that the timestamps usually are the end of the event, not the beginning, as I kept thinking)
        // GC(3) Pause Full (Allocation Failure)
        AbstractGCEvent<?> event2 = model.get(2);
        assertThat("event2 type", event2.getTypeAsString(), startsWith(Type.UJL_PAUSE_FULL.getName()));
        assertThat("event2 pause", event2.getPause(), closeTo(0.006987, 0.00001));
        assertThat("event2 time", event2.getTimestamp(), closeTo(0.290, 0.0001));

        // GC(2) Pause Young (Allocation Failure)
        AbstractGCEvent<?> event3 = model.get(3);
        assertThat("event3 type", event3.getTypeAsString(), startsWith(Type.UJL_PAUSE_YOUNG.getName()));
        assertThat("event3 pause", event3.getPause(), closeTo(0.007118, 0.00001));
        assertThat("event3 time", event3.getTimestamp(), closeTo(0.290, 0.0001));
    }

    @Test
    public void testParseUnknownLineFormat() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[0.317s][info][gc            ] GC(11) Pause Young (G1 Evacuation Pause) 122M->113M(128M) unexpected 0.774ms")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(1));
        assertThat("warning message", handler.getLogRecords().get(0).getMessage(), startsWith("Failed to parse line number"));
    }

    @Test
    public void testParseUnknownGcType() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("byteArray");
        gcResource.getLogger().addHandler(handler);
        InputStream in = new ByteArrayInputStream(
                ("[0.317s][info][gc            ] GC(11) Pause Young unknown event")
                        .getBytes());

        DataReader reader = new DataReaderUnifiedJvmLogging(gcResource, in);
        GCModel model = reader.read();

        assertThat("number of warnings", handler.getCount(), is(1));
        assertThat("warning message", handler.getLogRecords().get(0).getMessage(), startsWith("Failed to parse gc event ("));
    }

    private GCModel getGCModelFromLogFile(String fileName) throws IOException {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile(fileName);
        gcResource.getLogger().addHandler(handler);

        try (InputStream in = getInputStream(gcResource.getResourceName())) {
            DataReader reader = new DataReaderFactory().getDataReader(gcResource, in);
            assertThat("reader from factory", reader.getClass().getName(), is(DataReaderUnifiedJvmLogging.class.getName()));

            GCModel model = reader.read();
            assertThat("model format", model.getFormat(), is(GCModel.Format.UNIFIED_JVM_LOGGING));
            assertThat("number of errors", handler.getCount(), is(0));
            return model;
        }
    }
}

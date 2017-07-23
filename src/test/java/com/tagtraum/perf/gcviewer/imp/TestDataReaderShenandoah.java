package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.*;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Mart on 10/05/2017.
 */
public class TestDataReaderShenandoah {
    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_OPENJDK, fileName);
    }

    private DataReader getDataReader(GCResource gcResource) throws IOException {
        return new DataReaderShenandoah(gcResource, getInputStream(gcResource.getResourceName()));
    }

    @Test
    public void parseBasicEvent() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleShenandoahBasic.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("number of errors", handler.getCount(), is(0));
        assertThat("size", model.size(), is(5));
        assertThat("model format", model.getFormat(), is(GCModel.Format.RED_HAT_SHENANDOAH_GC));

        assertThat("heap size after concurrent cycle", model.getPostConcurrentCycleHeapUsedSizes().getMax(), is(122000));
        assertThat("amount of concurrent pauses", model.getConcurrentEventPauses().size(), is(3));
        assertThat("amount of STW pauses", model.getFullGcEventPauses().size(), is(2));
        assertThat("max memory freed during STW pauses", model.getFreedMemoryByFullGC().getMax(), is(34000));
    }

    /**
     * In java 8, suddenly the full gc events in G1 got detailed information about the generation
     * sizes again. Test, that they are parsed correctly.
     */
    @Test
    public void parseAllocationFailure() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleShenandoahAllocationFailure.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();
        GCEvent event = (GCEvent) model.get(0);

        assertThat("number of errors", handler.getCount(), is(0));
        assertThat("size", model.size(), is(1));
        assertThat("model format", model.getFormat(), is(GCModel.Format.RED_HAT_SHENANDOAH_GC));

        assertThat("amount of STW pauses", model.getFullGcEventPauses().size(), is(1));
        assertThat("preUsed matches", event.getPreUsed(), is(7943000));
        assertThat("postUsed matches", event.getPostUsed(), is(6013000));
        assertThat("total heap size", event.getTotal(), is(8192000));
        assertThat("generation", event.getGeneration(), is(AbstractGCEvent.Generation.TENURED));
        assertThat("timestamp", event.getTimestamp(), is(43.948));
        assertThat("type", event.getTypeAsString(), is(AbstractGCEvent.Type.SHEN_STW_ALLOC_FAILURE.toString()));
        assertThat("total pause", model.getPause().getSum(), is(14.289335));
    }
}

package com.tagtraum.perf.gcviewer.imp;

import static com.tagtraum.perf.gcviewer.UnittestHelper.toKiloBytes;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Test;

/**
 * Tests some J9_R26 sample files against the IBM J9 parser.
 */
public class TestDataReaderIBM_J9_R26 {
    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(FOLDER.IBM, fileName);
    }

    private DataReader getDataReader(GCResource gcResource) throws IOException {
        return new DataReaderIBM_J9_R28(gcResource, getInputStream(gcResource.getResourceName()));
    }

    @Test
    public void testFullHeaderWithAfGcs() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleIBMJ9_R26_GAFP1_full_header.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("model size", model.size(), is(1));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("pause", event.getPause(), closeTo(0.00529, 0.0000001));

        assertThat("number of errors", handler.getCount(), is(1));
    }

    @Test
    public void testSystemGc() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleIBMJ9_R26_GAFP1_global.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("model size", model.size(), is(1));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("pause", event.getPause(), closeTo(0.036392, 0.0000001));

        assertThat("total before", event.getTotal(), is(toKiloBytes(514064384)));
        assertThat("free before", event.getPreUsed(), is(toKiloBytes(514064384 - 428417552)));
        assertThat("free after", event.getPostUsed(), is(toKiloBytes(514064384 - 479900360)));

        assertThat("total young before", event.getYoung().getTotal(), is(toKiloBytes(111411200)));
        assertThat("young before", event.getYoung().getPreUsed(), is(toKiloBytes(111411200 - 29431656)));
        assertThat("young after", event.getYoung().getPostUsed(), is(toKiloBytes(111411200 - 80831520)));

        assertThat("total tenured before", event.getTenured().getTotal(), is(toKiloBytes(402653184)));
        assertThat("tenured before", event.getTenured().getPreUsed(), is(toKiloBytes(402653184 - 398985896)));
        assertThat("tenured after", event.getTenured().getPostUsed(), is(toKiloBytes(402653184 - 399068840)));

        assertThat("number of errors", handler.getCount(), is(0));
    }

}

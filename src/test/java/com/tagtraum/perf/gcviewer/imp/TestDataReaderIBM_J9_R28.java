package com.tagtraum.perf.gcviewer.imp;

import static com.tagtraum.perf.gcviewer.UnittestHelper.toKiloBytes;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import org.junit.Test;

/**
 * @author <a href="gcviewer@gmx.ch">Joerg Wuethrich</a>
 *         <p>created on 08.10.2014</p>
 */
public class TestDataReaderIBM_J9_R28 {

    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_IBM, fileName);
    }

    private DataReader getDataReader(GCResource gcResource) throws IOException {
        return new DataReaderIBM_J9_R28(gcResource, getInputStream(gcResource.getResourceName()));
    }

    @Test
    public void testFullHeaderWithAfGcs() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("SampleIBMJ9_R28_full_header.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();
        
        assertThat("model size", model.size(), is(2));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("pause", event.getPause(), closeTo(0.025388, 0.0000001));

        assertThat("total before", event.getTotal(), is(toKiloBytes(536870912)));
        assertThat("free before", event.getPreUsed(), is(toKiloBytes(536870912 - 401882552)));
        assertThat("free after", event.getPostUsed(), is(toKiloBytes(536870912 - 457545744)));

        assertThat("total young before", event.getYoung().getTotal(), is(toKiloBytes(134217728)));
        assertThat("young before", event.getYoung().getPreUsed(), is(toKiloBytes(134217728)));
        assertThat("young after", event.getYoung().getPostUsed(), is(toKiloBytes(134217728 - 55663192)));

        assertThat("total tenured before", event.getTenured().getTotal(), is(toKiloBytes(402653184)));
        assertThat("tenured before", event.getTenured().getPreUsed(), is(toKiloBytes(402653184 - 401882552)));
        assertThat("tenured after", event.getTenured().getPostUsed(), is(toKiloBytes(402653184 - 401882552)));

        assertThat("timestamp 1", event.getTimestamp(), closeTo(0.0, 0.0001));
        assertThat("timestamp 2", model.get(1).getTimestamp(), closeTo(1.272, 0.0001));

        assertThat("number of errors", handler.getCount(), is(0));
    }

    @Test
    public void testSystemGc() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("SampleIBMJ9_R28_global.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("model size", model.size(), is(1));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("pause", event.getPause(), closeTo(0.097756, 0.0000001));

        assertThat("number of errors", handler.getCount(), is(0));
    }

    @Test
    public void testConcurrentMinimal() throws Exception {
        // there are minimal concurrent blocks, that don't contain any information, that the parser can use (at least, at the moment)
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("SampleIBMJ9_R28_concurrentMinimal.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("model size", model.size(), is(0));
        assertThat("number of errors", handler.getCount(), is(0));
    }

}

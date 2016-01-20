package com.tagtraum.perf.gcviewer.imp;

import static com.tagtraum.perf.gcviewer.imp.UnittestHelper.toKiloBytes;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import org.junit.Test;

/**
 * @author <a href="gcviewer@gmx.ch">Joerg Wuethrich</a>
 *         <p>created on 08.10.2014</p>
 */
public class TestDataReaderIBM_J9_R27 {
    private static final Logger IMP_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.imp");
    private static final Logger DATA_READER_FACTORY_LOGGER = Logger.getLogger("com.tagtraum.perf.gcviewer.DataReaderFactory");

    @Test
    public void testFullHeaderWithAfGcs() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        InputStream in = UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_IBM, "SampleIBMJ9_R27_SR1_full_header.txt");
        DataReader reader = new DataReaderIBM_J9_R28(in);
        GCModel model = reader.read();
        
        assertThat("model size", model.size(), is(3));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("pause", event.getPause(), closeTo(0.042303, 0.0000001));

        assertThat("total before", event.getTotal(), is(toKiloBytes(1073741824)));
        assertThat("free before", event.getPreUsed(), is(toKiloBytes(1073741824 - 804158480)));
        assertThat("free after", event.getPostUsed(), is(toKiloBytes(1073741824 - 912835672)));

        assertThat("total young before", event.getYoung().getTotal(), is(toKiloBytes(268435456)));
        assertThat("young before", event.getYoung().getPreUsed(), is(toKiloBytes(268435456)));
        assertThat("young after", event.getYoung().getPostUsed(), is(toKiloBytes(268435456 - 108677192)));

        assertThat("total tenured before", event.getTenured().getTotal(), is(toKiloBytes(805306368)));
        assertThat("tenured before", event.getTenured().getPreUsed(), is(toKiloBytes(805306368 - 804158480)));
        assertThat("tenured after", event.getTenured().getPostUsed(), is(toKiloBytes(805306368 - 804158480)));

        assertThat("timestamp 1", event.getTimestamp(), closeTo(0.0, 0.0001));
        assertThat("timestamp 2", model.get(1).getTimestamp(), closeTo(1.927, 0.0001));
        assertThat("timestamp 3", model.get(2).getTimestamp(), closeTo(3.982, 0.0001));

        assertThat("number of errors", handler.getCount(), is(1));
    }

    @Test
    public void testSystemGc() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        IMP_LOGGER.addHandler(handler);
        DATA_READER_FACTORY_LOGGER.addHandler(handler);

        InputStream in = UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_IBM, "SampleIBMJ9_R27_SR1_global.txt");
        DataReader reader = new DataReaderIBM_J9_R28(in);
        GCModel model = reader.read();

        assertThat("model size", model.size(), is(1));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("pause", event.getPause(), closeTo(0.075863, 0.0000001));

        assertThat("number of errors", handler.getCount(), is(0));
    }

}

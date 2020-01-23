package com.tagtraum.perf.gcviewer.imp;

import static com.tagtraum.perf.gcviewer.UnittestHelper.toKiloBytes;
import static com.tagtraum.perf.gcviewer.imp.TestDataReaderIBM_J9_R28.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
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
 * @author <a href="gcviewer@gmx.ch">Joerg Wuethrich</a>
 *         <p>created on 08.10.2014</p>
 */
public class TestDataReaderIBM_J9_R27 {

    public static final String EXPECTED_ERROR_MESSAGE = "line 233: javax.xml.stream.XMLStreamException: ParseError at [row,col]:[234,1]";

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
        GCResource gcResource = new GcResourceFile("SampleIBMJ9_R27_SR1_full_header.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
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

        verifyTimestamp("timestamp 1", event.getTimestamp(), "2014-09-24T15:57:32.116");
        verifyTimestamp("timestamp 2", model.get(1).getTimestamp(), "2014-09-24T15:57:34.043");
        verifyTimestamp("timestamp 3", model.get(2).getTimestamp(), "2014-09-24T15:57:36.098");

        assertThat("number of errors", handler.getCount(), is(1));
        String message = handler.getLogRecords().get(0).getMessage();
        assertThat("missing close tag </verbosegc>", message, startsWith(EXPECTED_ERROR_MESSAGE));
    }

    @Test
    public void testSystemGc() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GcResourceFile("SampleIBMJ9_R27_SR1_global.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("model size", model.size(), is(1));

        GCEvent event = (GCEvent) model.get(0);
        assertThat("pause", event.getPause(), closeTo(0.075863, 0.0000001));

        assertThat("number of errors", handler.getCount(), is(0));
    }

}

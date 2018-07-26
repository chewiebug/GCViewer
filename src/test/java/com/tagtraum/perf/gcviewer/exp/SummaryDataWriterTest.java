package com.tagtraum.perf.gcviewer.exp;

import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.tagtraum.perf.gcviewer.exp.impl.SummaryDataWriter;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test implementation of {@link SummaryDataWriter}
 *
 * <p>hint: don't use memory numbers > 999, because they are not formatted the same on all platforms -&gt; unstable tests</p>
 */
public class SummaryDataWriterTest {

    private GCModel createGcModel() throws MalformedURLException {
        GCModel model = new GCModel();
        model.setURL(new URL("file", "localhost", "test-file"));

        model.add(new GCEvent(0.1, 996, 768, 999, 0.3, Type.GC));
        model.add(new GCEvent(0.2, 996, 424, 997, 0.4, Type.GC));
        model.add(new GCEvent(0.3, 612, 512, 998, 0.3, Type.GC));
        model.add(new GCEvent(0.4, 816, 768, 999, 0.3, Type.GC));

        return model;
    }

    @Test
    public void testWriteForEmptyModel() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        SummaryDataWriter objectUnderTest = new SummaryDataWriter(output);
        GCModel model = new GCModel();
        model.setURL(new URL("file", "localhost", "test-file"));

        objectUnderTest.write(model);

        String csv = output.toString();

        assertThat("totalTenuredAllocMax", csv, Matchers.containsString("totalTenuredAllocMax; n/a; M"));
    }

    @Test
    public void testWrite() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        SummaryDataWriter objectUnderTest = new SummaryDataWriter(output);

        objectUnderTest.write(createGcModel());

        String csv = output.toString();

        assertThat("totalHeapAllocMax", csv, Matchers.containsString("totalHeapAllocMax; 999; K"));
    }

    @Test
    public void testWriteWithFullGc() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        SummaryDataWriter objectUnderTest = new SummaryDataWriter(output);

        GCModel model = createGcModel();
        model.add(new GCEvent(0.5, 999, 724, 999, 0.8, Type.FULL_GC));

        objectUnderTest.write(model);

        String csv = output.toString();

        assertThat("totalHeapAllocMax", csv, Matchers.containsString("avgfootprintAfterFullGC; 724; K"));
    }
}

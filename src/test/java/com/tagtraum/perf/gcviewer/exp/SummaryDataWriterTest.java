package com.tagtraum.perf.gcviewer.exp;

import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;

import com.tagtraum.perf.gcviewer.exp.impl.SummaryDataWriter;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.MemoryFormat;

import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test implementation of {@link SummaryDataWriter}
 *
 * <p>hint: don't use memory numbers > 999, because they are not formatted the same on all platforms -&gt; unstable tests</p>
 */
public class SummaryDataWriterTest {

    private static NumberFormat percentFormatter;
    private static MemoryFormat memoryFormatter;

    @BeforeClass
	public static void setupClass() {
        percentFormatter = NumberFormat.getInstance();
        percentFormatter.setMaximumFractionDigits(1);
        percentFormatter.setMinimumFractionDigits(1);
        memoryFormatter = new MemoryFormat();
    }

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

    @Test
    public void testWriteWithPerm() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        SummaryDataWriter objectUnderTest = new SummaryDataWriter(output);

        // 83.403: [Full GC 83.403: [Tenured: 38156K->54636K(349568K), 0.6013150 secs] 141564K->54636K(506944K), [Perm : 73727K->73727K(73728K)], 0.6014256 secs] [Times: user=0.58 sys=0.00, real=0.59 secs]
        GCEvent fullGcEvent = new GCEvent(83.403, 141564, 54636, 506944, 0.6014256, Type.FULL_GC);
        GCEvent tenured = new GCEvent(83.403, 38156, 54636, 349568, 0.6013150, Type.TENURED);
        GCEvent perm = new GCEvent(83.403, 73727, 73727, 73728, 0.6014256, Type.PERM);
        fullGcEvent.add(tenured);
        fullGcEvent.add(perm);

        GCModel model = createGcModel();
        model.add(fullGcEvent);

        objectUnderTest.write(model);

        String csv = output.toString();

        assertThat("totalPermAllocMax", csv, Matchers.containsString("totalPermAllocMax; 72; M"));
        assertThat("totalPermUsedMax", csv, Matchers.containsString("totalPermUsedMax; " + memoryFormatter.formatToFormatted(73727).getValue() + "; M"));
        assertThat("totalPermUsedMaxpc", csv, Matchers.containsString("totalPermUsedMaxpc; " + percentFormatter.format(100.0) + "; %"));
    }
}

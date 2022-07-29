package com.tagtraum.perf.gcviewer.exp;

import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;

import com.tagtraum.perf.gcviewer.exp.impl.SummaryDataWriter;
import com.tagtraum.perf.gcviewer.imp.DataReader;
import com.tagtraum.perf.gcviewer.imp.DataReaderFactory;
import com.tagtraum.perf.gcviewer.imp.DataReaderSun1_6_0;
import com.tagtraum.perf.gcviewer.imp.GcLogType;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
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

    @Test
    public void testWriteWithPromotion() throws IOException {
    	ByteArrayInputStream in = new ByteArrayInputStream(
                ("2011-01-25T17:10:16.889+0100: 12076.859: [GC 12076.859: [ParNew2011-01-25T17:10:16.896+0100: 12076.866: [CMS-concurrent-abortable-preclean: 0.929/4.899 secs] [Times: user=2.13 sys=0.04, real=4.90 secs]" +
                		"\n" +
                		"\nDesired survivor size 720896 bytes, new threshold 1 (max 4)" +
                		"\n- age   1:    1058016 bytes,    1058016 total" +
                		"\n: 13056K->1408K(13056K), 0.0128277 secs] 131480K->122757K(141328K), 0.0131346 secs] [Times: user=0.15 sys=0.00, real=0.01 secs]")
                       .getBytes());
        DataReader reader = new DataReaderSun1_6_0(new GcResourceFile("byteArray"), in, GcLogType.SUN1_6);
        GCModel model = reader.read();
        model.setURL(new URL("file", "localhost", "test-file"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        SummaryDataWriter objectUnderTest = new SummaryDataWriter(output);

        objectUnderTest.write(model);

        String csv = output.toString();

        assertThat("avgPromotion", csv, Matchers.containsString("avgPromotion; " + memoryFormatter.formatToFormatted(2925).getValue() + "; K"));
    }

    @Test
    public void testWriteWithZGCPhases() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("[2022-08-18T17:18:46.761+0800][0.053s][info][gc     ] Using The Z Garbage Collector\n" +
                        "[2022-08-18T17:18:47.977+0800][1.269s][info][gc,start ] GC(0) Garbage Collection (Warmup)\n" +
                        "[2022-08-18T17:18:47.978+0800][1.270s][info][gc,phases] GC(0) Pause Mark Start 0.385ms\n" +
                        "[2022-08-18T17:18:47.985+0800][1.277s][info][gc,phases] GC(0) Concurrent Mark 7.383ms\n" +
                        "[2022-08-18T17:18:47.985+0800][1.277s][info][gc,phases] GC(0) Pause Mark End 0.050ms\n" +
                        "[2022-08-18T17:18:47.986+0800][1.277s][info][gc,phases] GC(0) Concurrent Process Non-Strong References 0.239ms\n" +
                        "[2022-08-18T17:18:47.986+0800][1.277s][info][gc,phases] GC(0) Concurrent Reset Relocation Set 0.000ms\n" +
                        "[2022-08-18T17:18:47.986+0800][1.278s][info][gc,phases] GC(0) Concurrent Destroy Detached Pages 0.174ms\n" +
                        "[2022-08-18T17:18:47.989+0800][1.281s][info][gc,phases] GC(0) Concurrent Select Relocation Set 3.505ms\n" +
                        "[2022-08-18T17:18:47.990+0800][1.282s][info][gc,phases] GC(0) Concurrent Prepare Relocation Set 0.760ms\n" +
                        "[2022-08-18T17:18:47.991+0800][1.282s][info][gc,phases] GC(0) Pause Relocate Start 0.499ms\n" +
                        "[2022-08-18T17:18:47.994+0800][1.286s][info][gc,phases] GC(0) Concurrent Relocate 3.545ms\n" +
                        "[2022-08-18T17:18:47.994+0800][1.286s][info][gc          ] GC(0) Garbage Collection (Warmup) 434M(11%)->32M(1%)")
                       .getBytes());
        DataReader reader = new DataReaderFactory().getDataReader(new GcResourceFile("byteArray"), in);
        GCModel model = reader.read();
        model.setURL(new URL("file", "localhost", "test-file"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        SummaryDataWriter objectUnderTest = new SummaryDataWriter(output);

        objectUnderTest.write(model);

        String csv = output.toString();

        assertThat("pausePercentile75th", csv, Matchers.containsString("pausePercentile75th; 0,000934; s"));
        assertThat("gcPausePercentile95th", csv, Matchers.containsString("gcPausePercentile95th; 0,000934; s"));
        assertThat("pauseCount", csv, Matchers.containsString("pauseCount; 1; -"));
        assertThat("gcPhaseCount", csv, Matchers.containsString("gcPhaseCount; 3; -"));
        assertThat("gcPhaseAverage", csv, Matchers.containsString("gcPhaseAverage; 0,000311; s"));
        assertThat("gcPhasePercentile99th", csv, Matchers.containsString("gcPhasePercentile99th; 0,000499; s"));
    }
}

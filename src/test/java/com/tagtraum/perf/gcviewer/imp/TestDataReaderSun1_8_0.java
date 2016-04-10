package com.tagtraum.perf.gcviewer.imp;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;

/**
 * Test logs generated specifically by java 1.8.
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 25.09.2013</p>
 */
public class TestDataReaderSun1_8_0 {

    private InputStream getInputStream(String fileName) throws IOException {
        return UnittestHelper.getResourceAsStream(UnittestHelper.FOLDER_OPENJDK, fileName);
    }

    private DataReader getDataReader(GCResource gcResource) throws IOException {
        return new DataReaderSun1_6_0(gcResource, getInputStream(gcResource.getResourceName()), GcLogType.SUN1_8);
    }

    @Test
    public void parallelPrintHeapAtGC() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("SampleSun1_8_0ParallelPrintHeapAtGC.txt");
        gcResource.getLogger().addHandler(handler);
        
        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc pause sum", model.getPause().getSum(), closeTo(0.0103603, 0.000000001));

        assertThat("number of errors", handler.getCount(), is(0));
    }

    @Test
    public void scavengeBeforeRemarkPrintHeapAtGC_YGOccupancy() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("SampleSun1_8_0CMS_ScavengeBeforeRemark_HeapAtGc.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(2));
        assertThat("scavenge before remark event", model.get(0).getPause(), closeTo(0.0000778, 0.000000001));
        assertThat("remark event", model.get(1).getPause(), closeTo(0.0019970 - 0.0000778, 0.000000001));

        assertEquals("number of errors", 0, handler.getCount());
    }

    @Test
    public void scavengeBeforeRemark_HeapAtGC_PrintTenuringDistribution_PrintFLSStats() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("SampleSun1_8_0CMS_ScavengeBR_HeapAtGC_TenuringDist_PrintFLS.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(2));
        assertThat("scavenge before remark event", model.get(0).getPause(), closeTo(0.1306264, 0.000000001));
        assertThat("remark event", model.get(1).getPause(), closeTo(0.1787717 - 0.1306264, 0.000000001));

        assertEquals("number of errors", 0, handler.getCount());
    }

    @Test
    public void parallelPrintTenuringGcCause() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("SampleSun1_8_0Parallel_Tenuring_PrintGCCause.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(5));
        assertThat("gc name", model.get(0).getTypeAsString(), equalTo("GC (Allocation Failure); PSYoungGen"));
        assertThat("pause", model.get(0).getPause(), closeTo(0.0199218, 0.000000001));

        assertEquals("number of errors", 0, handler.getCount());
    }

    @Test
    public void parallelApple() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("SampleSun1_8_0Parallel_Apple.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(6));

        assertEquals("number of errors", 0, handler.getCount());
    }

    @Test
    public void cmsPrintHeapBeforeFullGc() throws Exception {
        TestLogHandler handler = new TestLogHandler();
        handler.setLevel(Level.WARNING);
        GCResource gcResource = new GCResource("SampleSun1_8_0CMS_HeadDumpBeforeFullGc.txt");
        gcResource.getLogger().addHandler(handler);

        DataReader reader = getDataReader(gcResource);
        GCModel model = reader.read();

        assertThat("gc count", model.size(), is(2));
        assertThat("gc name concurrent", model.get(0).getTypeAsString(), equalTo("CMS-concurrent-mark"));

        assertThat("gc name full gc", model.get(1).getTypeAsString(), equalTo("Full GC (GCLocker Initiated GC); CMS (concurrent mode failure); Metaspace"));
        assertThat("pause", model.get(1).getPause(), closeTo(218.6928810, 0.000000001));

        assertEquals("number of errors", 0, handler.getCount());
    }
}

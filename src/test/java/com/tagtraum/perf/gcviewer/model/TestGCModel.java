package com.tagtraum.perf.gcviewer.model;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.math.DoubleData;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Specific tests for {@link GCModel} implementation.
 */
public class TestGCModel {
    @Test
    public void fullGcInterval() throws Exception {
        GCModel gcModel = new GCModel();
        gcModel.add(new GCEvent(1.0, 10, 5, 100, 0.1, Type.GC));
        gcModel.add(new GCEvent(2.0, 10, 5, 100, 0.1, Type.GC));
        gcModel.add(new GCEvent(3.0, 10, 5, 100, 0.1, Type.FULL_GC));
        gcModel.add(new GCEvent(4.0, 10, 5, 100, 0.1, Type.GC));
        gcModel.add(new GCEvent(5.0, 10, 5, 100, 0.1, Type.GC));
        gcModel.add(new GCEvent(6.0, 10, 5, 100, 0.1, Type.FULL_GC));
        gcModel.add(new GCEvent(7.0, 10, 5, 100, 0.1, Type.FULL_GC));

        DoubleData fullGcInterval = gcModel.getFullGCPauseInterval();

        assertThat("max interval", fullGcInterval.getMax(), closeTo(3, 0.001));
        assertThat("min interval", fullGcInterval.getMin(), closeTo(1, 0.001));
    }

    @Test
    public void testEquals_ForIdenticalModels() throws IOException, ExecutionException, InterruptedException, DataReaderException {
        // load model twice, should be identical
        GCResource gcResource = new GcResourceFile(UnittestHelper.getResourceAsString(UnittestHelper.FOLDER_OPENJDK, "SampleSun1_6_0CMS.txt"));
        DataReaderFacade dataReader = new DataReaderFacade();

        GCModel actual = dataReader.loadModel(gcResource);
        GCModel expected = dataReader.loadModel(gcResource);
        assertThat(actual, is(expected));
    }

    @Test
    public void testEquals_ForEmptyModels() {
        assertThat(new GCModel(), is(new GCModel()));
    }
}

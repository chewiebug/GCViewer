package com.tagtraum.perf.gcviewer.model;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import com.tagtraum.perf.gcviewer.math.DoubleData;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import org.junit.Test;

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
}

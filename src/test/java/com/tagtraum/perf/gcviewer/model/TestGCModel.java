package com.tagtraum.perf.gcviewer.model;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
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

    @Test
    public void testEquals_ForIdenticalModels() throws IOException, ExecutionException, InterruptedException, DataReaderException {
        // load model twice, should be identical
        GCResource gcResource = new GcResourceFile(UnittestHelper.getResourceAsString(FOLDER.OPENJDK, "SampleSun1_6_0CMS.txt"));
        DataReaderFacade dataReader = new DataReaderFacade();

        GCModel actual = dataReader.loadModel(gcResource);
        GCModel expected = dataReader.loadModel(gcResource);
        assertThat(actual, is(expected));
    }

    @Test
    public void testEquals_ForEmptyModels() {
        assertThat(new GCModel(), is(new GCModel()));
    }

    @Test
    public void testGetStartDate_WhenNeitherDateNorTimeStampsAreAvailable() throws Exception {
        GCModel model = new GCModel();
        model.setURL(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_6_0CMS.txt"));

        assertThat(model.getStartDate(), is(ZonedDateTime.ofInstant(Instant.ofEpochMilli(model.getLastModified()), ZoneId.systemDefault())));
    }

    @Test
    public void testGetStartDate_WhenDateStampsAreAvailable() throws Exception {
        DataReaderFacade dataReader = new DataReaderFacade();
        GCModel model = dataReader.loadModel(new GcResourceFile(UnittestHelper.getResourceAsString(FOLDER.OPENJDK, "SampleSun1_6_0CMSAdaptiveSizePolicy.txt")));

        ZonedDateTime expectedTime = ZonedDateTime.of(2012, 4, 18, 14, 23, 59, 890_000_000, ZoneId.ofOffset("", ZoneOffset.ofHours(2)));
        assertThat(model.getStartDate(), is(expectedTime));
    }

    @Test
    public void testGetStartDate_WhenNoDateButTimestampsAreAvailable() throws Exception {
        DataReaderFacade dataReader = new DataReaderFacade();
        GcResourceFile gcResource = new GcResourceFile(UnittestHelper.getResourceAsString(FOLDER.OPENJDK, "SampleSun1_6_0CMS.txt"));
        GCModel model = dataReader.loadModel(gcResource);

        ZonedDateTime expectedTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(model.getLastModified()), ZoneId.systemDefault());
        expectedTime = expectedTime.minus(1381, ChronoUnit.MILLIS);// 1,381s (diff between last and first timestamp
        assertThat(model.getStartDate(), is(expectedTime));
    }
}

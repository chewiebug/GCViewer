package com.tagtraum.perf.gcviewer.ctrl.impl;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.ctrl.GCModelLoader;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;
import org.junit.jupiter.api.Test;

public class GCModelSeriesLoaderImplTest {

    @Test
    void getGcResource() throws Exception {
        ArrayList<GCResource> gcResourceList = new ArrayList<>();
        gcResourceList.add(new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part1.txt").getPath()));
        gcResourceList.add(new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part2.txt").getPath()));
        gcResourceList.add(new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part3.txt").getPath()));

        GCModelLoader loader = new GCModelSeriesLoaderImpl(new GcResourceSeries(gcResourceList));
        assertThat(loader.getGcResource(), notNullValue());
    }

    @Test
    void GCModelSeriesLoaderImpl_ForEmptySeries() {
        assertThrows(IllegalArgumentException.class, () -> new GCModelSeriesLoaderImpl(new GcResourceSeries(new ArrayList<>())));
    }

    @Test
    void loadGcModel() throws Exception {
        ArrayList<GCResource> gcResourceList = new ArrayList<>();
        gcResourceList.add(new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part1.txt").getPath()));
        gcResourceList.add(new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part2.txt").getPath()));
        gcResourceList.add(new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part3.txt").getPath()));
        GCModelSeriesLoaderImpl loader = new GCModelSeriesLoaderImpl(new GcResourceSeries(gcResourceList));

        assertThat(loader.loadGcModel(), notNullValue());
    }
}

package com.tagtraum.perf.gcviewer.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GcResourceSeriesTest {

    private GcResourceSeries resourceSeries;
    private List<GCResource> resourceList;

    @BeforeEach
    void setUp() throws Exception {
        resourceList = new ArrayList<>();
        resourceList.add(new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part1.txt").getPath()));
        resourceList.add(new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part2.txt").getPath()));
        resourceList.add(new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part3.txt").getPath()));

        resourceSeries = new GcResourceSeries(resourceList);
    }

    @Test
    void hasUnderlyingResourceChanged() throws Exception {
        for (GCResource resource : resourceList) {
            GCModel model = new GCModel();
            model.setURL(((GcResourceFile) resource).getResourceNameAsUrl());
            resource.setModel(model);
        }

        assertThat(resourceSeries.hasUnderlyingResourceChanged(), is(false));

        // Changing first resource doesn't register
        resourceList.get(0).setModel(new GCModel());
        assertThat(resourceSeries.hasUnderlyingResourceChanged(), is(false));

        // Changing last resource does register
        resourceList.get(resourceList.size() - 1).setModel(new GCModel());
        assertThat(resourceSeries.hasUnderlyingResourceChanged(), is(true));
    }

    @Test
    void buildName() throws Exception {
        assertThat(GcResourceSeries.buildName(resourceList), is(
                UnittestHelper.getResource(FOLDER.OPENJDK.getFolderName()).getPath() + "/" + "SampleSun1_8_0Series-Part1-3"));
    }

    @Test
    void buildName_ForRotatedFile() {
        assertThat(GcResourceSeries.buildName("garbageCollection.log.0", "garbageCollection.log.6.current"), is("garbageCollection.log.0-6"));
    }

    @Test
    void buildName_WhenOnlyOneEntryExists() throws Exception {
        resourceList = new ArrayList<>();
        GcResourceFile gcResourceFile =
                new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part1.txt").getPath());
        resourceList.add(gcResourceFile);
        assertThat(GcResourceSeries.buildName(resourceList), is(gcResourceFile.getResourceName()));
    }

    @Test
    void buildName_WhenNoCommonPrefixExists() {
        List<GCResource> resources = new ArrayList<>();
        GCResource resource1 = mock(GCResource.class);
        when(resource1.getResourceName()).thenReturn("abc.log");
        resources.add(resource1);

        GCResource resource2 = mock(GCResource.class);
        when(resource2.getResourceName()).thenReturn("xyz.log");
        resources.add(resource2);
        assertThat(GcResourceSeries.buildName(resources), is("abc.log-xyz.log"));
    }

    @Test
    void buildName_WhenListIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> GcResourceSeries.buildName(new ArrayList<>()));
    }
}

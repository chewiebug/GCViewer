package com.tagtraum.perf.gcviewer.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GcResourceSeriesTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private GcResourceSeries resourceSeries;
    private List<GCResource> resourceList;

    @Before
    public void setUp() throws Exception {
        resourceList = new ArrayList<>();
        resourceList.add(new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part1.txt").getPath()));
        resourceList.add(new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part2.txt").getPath()));
        resourceList.add(new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part3.txt").getPath()));

        resourceSeries = new GcResourceSeries(resourceList);
    }

    @Test
    public void hasUnderlyingResourceChanged() throws Exception {
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
    public void buildName() throws Exception {
        assertThat(GcResourceSeries.buildName(resourceList), is(
                UnittestHelper.getResource(FOLDER.OPENJDK.getFolderName()).getPath() + "/" + "SampleSun1_8_0Series-Part1-3"));
    }

    @Test
    public void buildName_ForRotatedFile() throws Exception {
        assertThat(GcResourceSeries.buildName("garbageCollection.log.0", "garbageCollection.log.6.current"), is("garbageCollection.log.0-6"));
    }

    @Test
    public void buildName_WhenOnlyOneEntryExists() throws Exception {
        resourceList = new ArrayList<>();
        GcResourceFile gcResourceFile =
                new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part1.txt").getPath());
        resourceList.add(gcResourceFile);
        assertThat(GcResourceSeries.buildName(resourceList), is(gcResourceFile.getResourceName()));
    }

    @Test
    public void buildName_WhenNoCommonPrefixExists() throws Exception {
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
    public void buildName_WhenListIsEmpty() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        GcResourceSeries.buildName(new ArrayList<>());
    }
}

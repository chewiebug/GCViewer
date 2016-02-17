package com.tagtraum.perf.gcviewer.view.model;

import static org.hamcrest.Matchers.is;

import java.util.Arrays;

import com.tagtraum.perf.gcviewer.model.GCResource;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class {@link RecentGCResourcesModel}.
 */
public class TestRecentGCResourcesModel {
    @Test
    public void addString() {
        RecentGCResourcesModel model = new RecentGCResourcesModel();
        model.add("d:/temp/test.log");
        Assert.assertThat("add first entry", model.getResourceNameGroups().size(), is(1));

        model.add("d:/temp/test.log");
        Assert.assertThat("add identical entry", model.getResourceNameGroups().size(), is(1));

        model.add("file:/d:/temp/test.log");
        Assert.assertThat("add url entry of same file", model.getResourceNameGroups().size(), is(1));
    }

    @Test
    public void addList() {
        RecentGCResourcesModel model = new RecentGCResourcesModel();
        model.add(Arrays.asList(new GCResource("d:/temp/test.log")));
        Assert.assertThat("add first entry", model.getResourceNameGroups().size(), is(1));

        model.add(Arrays.asList(new GCResource("d:/temp/test.log")));
        Assert.assertThat("add identical entry", model.getResourceNameGroups().size(), is(1));

        model.add(Arrays.asList(new GCResource("file:/d:/temp/test.log")));
        Assert.assertThat("add url entry of same file", model.getResourceNameGroups().size(), is(1));
    }

}

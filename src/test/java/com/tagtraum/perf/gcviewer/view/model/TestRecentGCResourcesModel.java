package com.tagtraum.perf.gcviewer.view.model;

import static org.hamcrest.Matchers.is;

import java.net.MalformedURLException;
import java.util.Arrays;

import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class {@link RecentGCResourcesModel}.
 */
public class TestRecentGCResourcesModel {
    /**
     * Return the missing part of a relative path compared to its absolute path.
     */
    private String getPathExpansion() {
        String resourceNameAsUrlString = new GcResourceFile("temp").getResourceNameAsUrlString();
        return resourceNameAsUrlString.substring("file:/".length(), resourceNameAsUrlString.indexOf("temp"));
    }

    @Test
    public void addString() throws MalformedURLException {
        RecentGCResourcesModel model = new RecentGCResourcesModel();
        model.add("temp/test.log");
        Assert.assertThat("add first entry", model.getResourceNameGroups().size(), is(1));

        model.add("temp/test.log");
        Assert.assertThat("add identical entry", model.getResourceNameGroups().size(), is(1));

        model.add("file:/" + getPathExpansion() + "temp/test.log");
        Assert.assertThat("add url entry of same file", model.getResourceNameGroups().size(), is(1));
    }

    @Test
    public void addList() {
        RecentGCResourcesModel model = new RecentGCResourcesModel();
        model.add(Arrays.asList(new GcResourceFile("temp/test.log")));
        Assert.assertThat("add first entry", model.getResourceNameGroups().size(), is(1));

        model.add(Arrays.asList(new GcResourceFile("temp/test.log")));
        Assert.assertThat("add identical entry", model.getResourceNameGroups().size(), is(1));

        model.add(Arrays.asList(new GcResourceFile("file:/" + getPathExpansion() + "temp/test.log")));
        Assert.assertThat("add url entry of same file", model.getResourceNameGroups().size(), is(1));
    }

}

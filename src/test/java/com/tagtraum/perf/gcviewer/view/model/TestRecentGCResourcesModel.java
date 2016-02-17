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
    /**
     * "Encoding" a fully qualified path as an url is handled differently on different platforms -> compensate for differences.
     */
    private String getPathPrefix() {
        String resourceNameAsUrlString = new GCResource("/temp").getResourceNameAsUrlString();
        return resourceNameAsUrlString.substring("file:/".length(), resourceNameAsUrlString.indexOf("/temp"));
    }

    @Test
    public void addString() {
        String rootPath = getPathPrefix();
        RecentGCResourcesModel model = new RecentGCResourcesModel();
        model.add(rootPath + "/temp/test.log");
        Assert.assertThat("add first entry", model.getResourceNameGroups().size(), is(1));

        model.add(rootPath + "/temp/test.log");
        Assert.assertThat("add identical entry", model.getResourceNameGroups().size(), is(1));

        model.add("file:/" + rootPath + "/temp/test.log");
        System.out.println(model.toString());
        Assert.assertThat("add url entry of same file", model.getResourceNameGroups().size(), is(1));
    }

    @Test
    public void addList() {
        String rootPath = getPathPrefix();
        RecentGCResourcesModel model = new RecentGCResourcesModel();
        model.add(Arrays.asList(new GCResource(rootPath + "/temp/test.log")));
        Assert.assertThat("add first entry", model.getResourceNameGroups().size(), is(1));

        model.add(Arrays.asList(new GCResource(rootPath + "/temp/test.log")));
        Assert.assertThat("add identical entry", model.getResourceNameGroups().size(), is(1));

        model.add(Arrays.asList(new GCResource("file:/" + rootPath + "/temp/test.log")));
        Assert.assertThat("add url entry of same file", model.getResourceNameGroups().size(), is(1));
    }

}

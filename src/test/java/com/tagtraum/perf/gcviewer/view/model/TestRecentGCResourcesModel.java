package com.tagtraum.perf.gcviewer.view.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;

import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import org.junit.jupiter.api.Test;

/**
 * Test class {@link RecentGCResourcesModel}.
 */
class TestRecentGCResourcesModel {
    /**
     * Return the missing part of a relative path compared to its absolute path.
     */
    private String getPathExpansion() {
        String resourceNameAsUrlString = new GcResourceFile("temp").getResourceNameAsUrlString();
        return resourceNameAsUrlString.substring("file:/".length(), resourceNameAsUrlString.indexOf("temp"));
    }

    @Test
    void addString() {
        RecentGCResourcesModel model = new RecentGCResourcesModel();
        model.add("temp/test.log");
        assertThat("add first entry", model.getResourceNameGroups().size(), is(1));

        model.add("temp/test.log");
        assertThat("add identical entry", model.getResourceNameGroups().size(), is(1));

        model.add("file:/" + getPathExpansion() + "temp/test.log");
        assertThat("add url entry of same file", model.getResourceNameGroups().size(), is(1));
    }

    @Test
    void addList() {
        RecentGCResourcesModel model = new RecentGCResourcesModel();
        model.add(Arrays.asList(new GcResourceFile("temp/test.log")));
        assertThat("add first entry", model.getResourceNameGroups().size(), is(1));

        model.add(Arrays.asList(new GcResourceFile("temp/test.log")));
        assertThat("add identical entry", model.getResourceNameGroups().size(), is(1));

        model.add(Arrays.asList(new GcResourceFile("file:/" + getPathExpansion() + "temp/test.log")));
        assertThat("add url entry of same file", model.getResourceNameGroups().size(), is(1));
    }

}

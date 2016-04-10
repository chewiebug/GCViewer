package com.tagtraum.perf.gcviewer.view.model;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.util.ArrayList;
import java.util.List;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link GCResourceGroup} class.
 */
public class TestGCResourceGroup {
    private List<GCResource> gcResourceList;

    @Before
    public void setup() {
        GCResource gcResource1 = new GCResource("gcResource1");
        gcResource1.getModel().add(new GCEvent(1.1, 100, 50, 200, 0.003, Type.GC));

        GCResource gcResource2 = new GCResource("gcResource2");
        gcResource2.getModel().add(new GCEvent(1.5, 1000, 400, 2000, 0.044, Type.FULL_GC));

        gcResourceList = new ArrayList<>();
        gcResourceList.add(gcResource1);
        gcResourceList.add(gcResource2);
    }

    @Test
    public void constructorList() {
        Assert.assertThat("has event before", gcResourceList.get(0).getModel().size(), is(1));
        GCResourceGroup group = new GCResourceGroup(gcResourceList);
        Assert.assertThat("has still the event from before", gcResourceList.get(0).getModel().size(), is(1));
        Assert.assertThat("doesn't have event inside group", group.getGCResourceList().get(0).getModel().size(), is(0));
    }

    @Test
    public void getUrlGroupString() {
        GCResourceGroup gcResourceGroup = new GCResourceGroup(gcResourceList);
        Assert.assertThat("starts with", gcResourceGroup.getUrlGroupString(), startsWith("file"));
        Assert.assertThat("contains resource 1", gcResourceGroup.getUrlGroupString(), containsString("gcResource1"));
        Assert.assertThat("contains resource 2", gcResourceGroup.getUrlGroupString(), containsString("gcResource2"));
    }

    @Test
    public void getGroupStringShort2Elements() {
        GCResourceGroup gcResourceGroup = new GCResourceGroup(gcResourceList);
        Assert.assertThat(gcResourceGroup.getGroupStringShort(), equalTo("gcResource1;gcResource2;"));
    }

    @Test
    public void getGroupStringShort1Element() {
        GCResourceGroup gcResourceGroup = new GCResourceGroup("c:/temp/test/gc-log-file.log");
        Assert.assertThat("should start with", gcResourceGroup.getGroupStringShort(), startsWith("file:/"));
        Assert.assertThat("should end with", gcResourceGroup.getGroupStringShort(), endsWith("/temp/test/gc-log-file.log"));
    }
}

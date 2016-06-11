package com.tagtraum.perf.gcviewer.view.model;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link GCResourceGroup} class.
 */
public class TestGCResourceGroup {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private List<GCResource> gcResourceList;

    @Before
    public void setup() {
        GCResource gcResource1 = new GcResourceFile("gcResource1");
        gcResource1.getModel().add(new GCEvent(1.1, 100, 50, 200, 0.003, Type.GC));

        GCResource gcResource2 = new GcResourceFile("gcResource2");
        gcResource2.getModel().add(new GCEvent(1.5, 1000, 400, 2000, 0.044, Type.FULL_GC));

        gcResourceList = new ArrayList<>();
        gcResourceList.add(gcResource1);
        gcResourceList.add(gcResource2);
    }

    @Test
    public void testGCResourceGroup_List() {
        assertThat("has event before", gcResourceList.get(0).getModel().size(), is(1));
        GCResourceGroup group = new GCResourceGroup(gcResourceList);
        assertThat("has still the event from before", gcResourceList.get(0).getModel().size(), is(1));
        assertThat("doesn't have event inside group", group.getGCResourceList().get(0).getModel().size(), is(0));
    }

    @Test
    public void testGetGCResourceList_SingleFile() throws Exception {
        File file = temporaryFolder.newFile();
        GCResourceGroup group = new GCResourceGroup(file.toURI().toURL().toString());
        assertThat(group.getGCResourceList(), contains(new GcResourceFile(file)));
    }

    @Test
    public void testGetGCResourceList_SeveralFiles() throws Exception {
        File file1 = temporaryFolder.newFile();
        File file2 = temporaryFolder.newFile();
        File file3 = temporaryFolder.newFile();
        GCResourceGroup group =
                new GCResourceGroup(file1.toURI().toURL().toString() + ";" + file2.toURI().toURL().toString() + ";" + file3.toURI().toURL().toString());
        assertThat(group.getGCResourceList(), contains(new GcResourceFile(file1), new GcResourceFile(file2), new GcResourceFile(file3)));
    }

    @Test
    public void testGetGCResourceList_SingleSeries() throws Exception {
        File file1 = temporaryFolder.newFile();
        File file2 = temporaryFolder.newFile();
        File file3 = temporaryFolder.newFile();
        List<GCResource> resources = Arrays.asList(new GcResourceFile(file1), new GcResourceFile(file2), new GcResourceFile(file3));
        GCResourceGroup group =
                new GCResourceGroup(file1.toURI().toURL().toString() + ">" + file2.toURI().toURL().toString() + ">" + file3.toURI().toURL().toString());
        assertThat(group.getGCResourceList(), contains(new GcResourceSeries(resources)));
    }

    @Test
    public void testGetGCResourceList_SeveralSeries() throws Exception {
        File file1 = temporaryFolder.newFile();
        File file2 = temporaryFolder.newFile();
        File file3 = temporaryFolder.newFile();
        List<GCResource> resourcesForSeries1 = Arrays.asList(new GcResourceFile(file1), new GcResourceFile(file2), new GcResourceFile(file3));

        File file4 = temporaryFolder.newFile();
        File file5 = temporaryFolder.newFile();
        File file6 = temporaryFolder.newFile();
        String resourceNameGroup1 = file1.toURI().toURL().toString() + ">" + file2.toURI().toURL().toString() + ">" + file3.toURI().toURL().toString();
        String resourceNameGroup2 = file4.toURI().toURL().toString() + ">" + file5.toURI().toURL().toString() + ">" + file6.toURI().toURL().toString();
        GCResourceGroup group = new GCResourceGroup(resourceNameGroup1 + ";" + resourceNameGroup2);
        List<GCResource> resourcesForSeries2 = Arrays.asList(new GcResourceFile(file4), new GcResourceFile(file5), new GcResourceFile(file6));
        assertThat(group.getGCResourceList(), contains(new GcResourceSeries(resourcesForSeries1), new GcResourceSeries(resourcesForSeries2)));
    }

    @Test
    public void getUrlGroupString() {
        GCResourceGroup gcResourceGroup = new GCResourceGroup(gcResourceList);
        assertThat("starts with", gcResourceGroup.getUrlGroupString(), startsWith("file"));
        assertThat("contains resource 1", gcResourceGroup.getUrlGroupString(), containsString("gcResource1"));
        assertThat("contains resource 2", gcResourceGroup.getUrlGroupString(), containsString("gcResource2"));
    }

    @Test
    public void getGroupStringShort2Elements() {
        GCResourceGroup gcResourceGroup = new GCResourceGroup(gcResourceList);
        assertThat(gcResourceGroup.getGroupStringShort(), equalTo("gcResource1;gcResource2;"));
    }

    @Test
    public void getGroupStringShort1Element() {
        GCResourceGroup gcResourceGroup = new GCResourceGroup("c:/temp/test/gc-log-file.log");
        assertThat("should start with", gcResourceGroup.getGroupStringShort(), startsWith("file:/"));
        assertThat("should end with", gcResourceGroup.getGroupStringShort(), endsWith("/temp/test/gc-log-file.log"));
    }

    @Test
    public void getGroupStringShort_Series() {
        GCResourceGroup gcResourceGroup = new GCResourceGroup("file:/log1>file:/log2>file:/log3>file:/log4");
        assertThat(gcResourceGroup.getGroupStringShort(), is("file:/log1 (series, 3 more files)"));
    }

    @Test
    public void getGroupStringShort_SeveralSeries() {
        GCResourceGroup gcResourceGroup = new GCResourceGroup("file:/log1>file:/log2>file:/log3>file:/log4;file:/log5>file:/log6>file:/log7");
        assertThat(gcResourceGroup.getGroupStringShort(), is("log1 (series, 3 more files);log5 (series, 2 more files);"));
    }
}

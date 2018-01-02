package com.tagtraum.perf.gcviewer.ctrl.impl;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.UnittestHelper.FOLDER;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.model.GCPreferences;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unittest for main controller class of GCViewerGui ({@link GCModelLoaderControllerImpl}).
 * This is rather an integration test than a unittest, so if one of these tests fail, first
 * check other failures before checking here.
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 *         <p>created on: 05.01.2014</p>
 */
public class TestGCModelLoaderController {
    private GCModelLoaderControllerImpl controller;
    private GCViewerGui gcViewerGui;

    @Before
    public void setUp() throws Exception {
        gcViewerGui = new GCViewerGui();
        controller = new GCModelLoaderControllerImpl(gcViewerGui);
        new GCViewerGuiBuilder().initGCViewerGui(gcViewerGui, controller);
        new GCViewerGuiController().applyPreferences(gcViewerGui, new GCPreferences());
    }

    @Test
    public void openStringFail() throws Exception {
        controller.open(new GcResourceFile("does_not_exist"));
        assertThat("number of gcdocuments", controller.getGCViewerGui().getDesktopPane().getAllFrames().length, is(1));
    }

    @Test
    public void openString() throws Exception {
        assertThat("number of gcdocuments before", controller.getGCViewerGui().getDesktopPane().getAllFrames().length, is(0));
        controller.open(new GcResourceFile(UnittestHelper.getResourceAsString(FOLDER.OPENJDK, "SampleSun1_6_0CMS.txt")));
        assertThat("number of gcdocuments after", controller.getGCViewerGui().getDesktopPane().getAllFrames().length, is(1));

    }

    @Test
    public void openAsSeries() throws Exception {
        assertThat("number of gcdocuments before", controller.getGCViewerGui().getDesktopPane().getAllFrames().length, is(0));
        ArrayList<GCResource> gcResourceList = getGcResourcesForSeries();

        controller.openAsSeries(gcResourceList);
        assertThat("number of gcdocuments after", controller.getGCViewerGui().getDesktopPane().getAllFrames().length, is(1)); // Input files are merged -> only one file is open
        assertThat(getOpenResources(), contains(new GcResourceSeries(gcResourceList)));
    }

    private ArrayList<GCResource> getGcResourcesForSeries() throws IOException {
        ArrayList<GCResource> gcResourceList = new ArrayList<>();
        gcResourceList.add(new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part1.txt").getPath()));
        gcResourceList.add(new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part2.txt").getPath()));
        gcResourceList.add(new GcResourceFile(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_8_0Series-Part3.txt").getPath()));
        return gcResourceList;
    }

    /**
     * Test drag and drop action on GCViewerGui.
     */
    @Test
    public void dropOnDesktopPane() throws Exception {
        // TODO SWINGWORKER: test drag and drop on GCDocument
        assertThat("number of gcdocuments before", controller.getGCViewerGui().getDesktopPane().getAllFrames().length, is(0));

        List<File> fileList = new ArrayList<File>();
        fileList.add(new File(UnittestHelper.getResource(FOLDER.OPENJDK, "SampleSun1_6_0G1_MarkStackFull.txt").getPath()));

        Transferable tr = Mockito.mock(Transferable.class);
        Mockito.when(tr.getTransferData(DataFlavor.javaFileListFlavor)).thenReturn(fileList);

        DropTargetDropEvent dte = Mockito.mock(DropTargetDropEvent.class);
        Mockito.when(dte.isDataFlavorSupported(DataFlavor.javaFileListFlavor)).thenReturn(true);
        Mockito.when(dte.getTransferable()).thenReturn(tr);

        DropTarget target = controller.getGCViewerGui().getDesktopPane().getDropTarget();
        target.drop(dte);

        assertThat("number of gcdocuments after drop", controller.getGCViewerGui().getDesktopPane().getAllFrames().length, is(1));
    }

    @Test
    public void open_File() throws Exception {
        File[] files = new File[1];
        File file = new File(UnittestHelper.getResourceAsString(FOLDER.OPENJDK, "SampleSun1_6_0CMS.txt"));
        files[0] = file;
        controller.open(files);
        assertThat(getOpenResources(), contains(new GcResourceFile(file)));
    }

    @Test
    public void open_GcResourceFile() throws Exception {
        GCResource resource = new GcResourceFile(UnittestHelper.getResourceAsString(FOLDER.OPENJDK, "SampleSun1_6_0CMS.txt"));
        controller.open(resource);
        assertThat(getOpenResources(), contains(resource));
    }

    @Test
    public void open_GcResourceSeries() throws Exception {
        List<GCResource> resources = getGcResourcesForSeries();
        GCResource series = new GcResourceSeries(resources);
        controller.open(series);
        assertThat(getOpenResources(), contains(series));
    }

    private List<GCResource> getOpenResources() {
        return gcViewerGui.getAllGCDocuments().stream().flatMap(x -> x.getGCResources().stream()).collect(Collectors.toList());
    }
}

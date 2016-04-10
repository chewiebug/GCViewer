package com.tagtraum.perf.gcviewer.ctrl.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.model.GCPreferences;

/**
 * Unittest for main controller class of GCViewerGui ({@link GCModelLoaderControllerImpl}).
 * This is rather an integration test than a unittest, so if one of these tests fail, first
 * check other failures before checking here.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 05.01.2014</p>
 */
public class TestGCModelLoaderController {
    private GCModelLoaderControllerImpl controller;
    
    @Before
    public void setUp() throws Exception {
        GCViewerGui gcViewerGui = new GCViewerGui();
        controller = new GCModelLoaderControllerImpl(gcViewerGui);
        new GCViewerGuiBuilder().initGCViewerGui(gcViewerGui, controller);
        new GCViewerGuiController().applyPreferences(gcViewerGui, new GCPreferences());
    }
    
    @Test
    public void openStringFail() throws Exception {
        controller.open(new GCResource("does_not_exist"));
        assertThat("number of gcdocuments", controller.getGCViewerGui().getDesktopPane().getAllFrames().length, is(1));
    }
    
    @Test
    public void openString() throws Exception {
        assertThat("number of gcdocuments before", controller.getGCViewerGui().getDesktopPane().getAllFrames().length, is(0));
        controller.open(new GCResource(UnittestHelper.getResourceAsString(UnittestHelper.FOLDER_OPENJDK, "SampleSun1_6_0CMS.txt")));
        assertThat("number of gcdocuments after", controller.getGCViewerGui().getDesktopPane().getAllFrames().length, is(1));
        
    }
    
    /**
     * Test drag and drop action on GCViewerGui.
     */
    @Test
    public void dropOnDesktopPane() throws Exception {
        // TODO SWINGWORKER: test drag and drop on GCDocument
        assertThat("number of gcdocuments before", controller.getGCViewerGui().getDesktopPane().getAllFrames().length, is(0));
        
        List<File> fileList = new ArrayList<File>();
        fileList.add(new File(UnittestHelper.getResource(UnittestHelper.FOLDER_OPENJDK, "SampleSun1_6_0G1_MarkStackFull.txt").getPath()));
        
        Transferable tr = Mockito.mock(Transferable.class);
        Mockito.when(tr.getTransferData(DataFlavor.javaFileListFlavor)).thenReturn(fileList);
        
        DropTargetDropEvent dte = Mockito.mock(DropTargetDropEvent.class);
        Mockito.when(dte.isDataFlavorSupported(DataFlavor.javaFileListFlavor)).thenReturn(true);
        Mockito.when(dte.getTransferable()).thenReturn(tr);
        
        DropTarget target = controller.getGCViewerGui().getDesktopPane().getDropTarget();
        target.drop(dte);
        
        assertThat("number of gcdocuments after drop", controller.getGCViewerGui().getDesktopPane().getAllFrames().length, is(1));
    }
}

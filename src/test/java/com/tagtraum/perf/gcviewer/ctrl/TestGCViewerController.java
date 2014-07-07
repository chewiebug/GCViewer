package com.tagtraum.perf.gcviewer.ctrl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.tagtraum.perf.gcviewer.UnittestHelper;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;

/**
 * Unittest for main controller class of GCViewerGui ({@link GCViewerController}).
 * This is rather an integration test than a unittest, so if one of these tests fail, first
 * check other failures before checking here.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 05.01.2014</p>
 */
public class TestGCViewerController {
    private GCViewerController controller;
    
    @Before
    public void setUp() throws Exception {
        controller = new GCViewerController();
        controller.setGCViewerGui(new GCViewerGui(controller));
    }
    
    @Test
    public void openStringFail() throws Exception {
        controller.open("does_not_exist");
        assertThat("number of gcdocuments", controller.getGCViewerGui().getDesktopPane().getAllFrames().length, is(1));
    }
    
    @Test
    public void openString() throws Exception {
        controller.open(UnittestHelper.getResourceAsString(UnittestHelper.FOLDER_OPENJDK, "SampleSun1_6_0CMS.txt"));
        assertThat("number of gcdocuments", controller.getGCViewerGui().getDesktopPane().getAllFrames().length, is(1));
    }
}

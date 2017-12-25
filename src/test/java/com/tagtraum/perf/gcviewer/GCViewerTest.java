package com.tagtraum.perf.gcviewer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import com.tagtraum.perf.gcviewer.ctrl.impl.GCViewerGuiController;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;
import org.junit.Test;

/**
 * @author martin.geldmacher
 */
public class GCViewerTest {

    @Test
    public void singleArgumentOpensGui() throws Exception {
        GCViewerGuiController controller = mock(GCViewerGuiController.class);
        GCViewer gcViewer = new GCViewer(controller, new GCViewerArgsParser());

        String[] args = {"some_gc.log"};
        int exitValue = gcViewer.doMain(args);
        verify(controller).startGui(new GcResourceFile("some_gc.log"));
        assertThat("exitValue of doMain", exitValue, is(0));
    }

    @Test
    public void singleArgumentWithSeriesOpensGui() throws Exception {
        GCViewerGuiController controller = mock(GCViewerGuiController.class);
        GCViewer gcViewer = new GCViewer(controller, new GCViewerArgsParser());

        String[] args = {"some_gc.log.0;some_gc.log.1;some_gc.log.2"};
        int exitValue = gcViewer.doMain(args);
        verify(controller).startGui(new GcResourceSeries(Arrays.asList(new GcResourceFile("some_gc.log.0"), new GcResourceFile("some_gc.log.1"), new GcResourceFile("some_gc.log.2"))));
        assertThat("result of doMain", exitValue, is(0));
    }

    @Test
    public void moreThan3ArgumentsPrintsUsage() throws Exception {
        GCViewerGuiController controller = mock(GCViewerGuiController.class);
        GCViewer gcViewer = new GCViewer(controller, new GCViewerArgsParser());

        String[] args = {"argument1", "argument2", "argument3", "argument4"};
        int exitValue = gcViewer.doMain(args);
        verify(controller, never()).startGui(any(GCResource.class));
        assertThat("result of doMain", exitValue, is(-3));
    }

    @Test
    public void export() throws Exception {
        GCViewerGuiController controller = mock(GCViewerGuiController.class);
        GCViewer gcViewer = new GCViewer(controller, new GCViewerArgsParser());

        String[] args = {"target/test-classes/openjdk/SampleSun1_7_0-01_G1_young.txt", "target/export.csv", "target/export.png", "-t", "PLAIN"};
        int exitValue = gcViewer.doMain(args);
        verify(controller, never()).startGui(any(GCResource.class));
        assertThat("result of doMain", exitValue, is(0));
    }

    @Test
    public void exportFileNotFound() throws Exception {
        GCViewerGuiController controller = mock(GCViewerGuiController.class);
        GCViewer gcViewer = new GCViewer(controller, new GCViewerArgsParser());

        String[] args = {"doesNotExist.log", "export.csv", "-t", "PLAIN"};
        int exitValue = gcViewer.doMain(args);
        verify(controller, never()).startGui(any(GCResource.class));
        assertThat("result of doMain", exitValue, is(-1));
    }

    @Test
    public void illegalExportFormat() throws Exception {
        GCViewer gcViewer = new GCViewer();

        String[] args = {"-t", "INVALID"};
        int exitValue = gcViewer.doMain(args);
        assertThat("result of doMain", exitValue, is(-2));
    }
}

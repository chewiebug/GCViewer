package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.ctrl.impl.GCViewerGuiController;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author martin.geldmacher
 */
public class GCViewerTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private PrintStream oldOut;
    private PrintStream oldErr;

    @Before
    public void replaceStreams() {
        oldOut = System.out;
        oldErr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void revertToOriginalStreams() {
        System.setOut(oldOut);
        System.setErr(oldErr);
    }

    @Test
    public void singleArgumentOpensGui() throws Exception {
        GCViewerGuiController controller = mock(GCViewerGuiController.class);
        GCViewer gcViewer = new GCViewer(controller, new GCViewerArgsParser());

        String[] args = {"some_gc.log"};
        gcViewer.doMain(args);
        verify(controller).startGui(new GcResourceFile("some_gc.log"));
        assertThat(outContent.toString(), isEmptyString());
        assertThat(errContent.toString(), isEmptyString());
    }

    @Test
    public void singleArgumentWithSeriesOpensGui() throws Exception {
        GCViewerGuiController controller = mock(GCViewerGuiController.class);
        GCViewer gcViewer = new GCViewer(controller, new GCViewerArgsParser());

        String[] args = {"some_gc.log.0;some_gc.log.1;some_gc.log.2"};
        gcViewer.doMain(args);
        verify(controller).startGui(new GcResourceSeries(Arrays.asList(new GcResourceFile("some_gc.log.0"), new GcResourceFile("some_gc.log.1"), new GcResourceFile("some_gc.log.2"))));
        assertThat(outContent.toString(), isEmptyString());
        assertThat(errContent.toString(), isEmptyString());
    }

    @Test
    public void moreThan3ArgumentsPrintsUsage() throws Exception {
        GCViewerGuiController controller = mock(GCViewerGuiController.class);
        GCViewer gcViewer = new GCViewer(controller, new GCViewerArgsParser());

        String[] args = {"argument1", "argument2", "argument3", "argument4"};
        gcViewer.doMain(args);
        verify(controller, never()).startGui(any(GCResource.class));
        assertThat(outContent.toString(), containsString("Welcome to GCViewer with cmdline"));
        assertThat(errContent.toString(), isEmptyString());
    }
}

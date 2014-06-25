package com.tagtraum.perf.gcviewer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests the class {@link com.tagtraum.perf.gcviewer.GCViewer} - makes sure that argument inputs parse correctly
 *
 * @author <a href="mailto:smendenh@redhat.com">Samuel Mendenhall</a>
 * <p>created on: 06.09.2014</p>
 */
public class TestGCViewer {

    @Test
    public void noArguments() throws IOException {
        String[] args = {};
        GCViewer gcViewer = new GCViewer();
        gcViewer.parseArguments(args);

        assertEquals(gcViewer.getArguments().size(), 0);
        assertEquals(gcViewer.getType(), "SUMMARY");

//        assertNotNull("version", version);
//        assertFalse("must not be n/a", version.equals("n/a"));
    }

    @Test
    public void onlyGCLog() throws IOException {
        String[] args = {"some_gc.log"};
        GCViewer gcViewer = new GCViewer();
        gcViewer.parseArguments(args);

        assertEquals(gcViewer.getArguments().size(), 1);
        assertEquals(gcViewer.getArguments().get(0), "some_gc.log");
        assertEquals(gcViewer.getType(), "SUMMARY");
    }

    @Test
    public void gcAndExportFile() throws IOException {
        String[] args = {"some_gc.log", "export_to.csv"};
        GCViewer gcViewer = new GCViewer();
        gcViewer.parseArguments(args);

        assertEquals(gcViewer.getArguments().size(), 2);
        assertEquals(gcViewer.getArguments().get(0), "some_gc.log");
        assertEquals(gcViewer.getArguments().get(1), "export_to.csv");
        assertEquals(gcViewer.getType(), "SUMMARY");
    }

    @Test
    public void onlyType() throws IOException {
        String[] args = {"-t", "CSV_TS"};
        GCViewer gcViewer = new GCViewer();
        gcViewer.parseArguments(args);

        assertEquals(gcViewer.getArguments().size(), 0);
        assertEquals(gcViewer.getType(), "CSV_TS");
    }

    @Test
    public void allInitialArgsWithType() throws IOException {
        String[] args = {"some_gc.log", "export_to.csv", "the_chart.png", "-t", "CSV"};
        GCViewer gcViewer = new GCViewer();
        gcViewer.parseArguments(args);

        assertEquals(gcViewer.getArguments().size(), 3);
        assertEquals(gcViewer.getArguments().get(0), "some_gc.log");
        assertEquals(gcViewer.getArguments().get(1), "export_to.csv");
        assertEquals(gcViewer.getArguments().get(2), "the_chart.png");
        assertEquals(gcViewer.getType(), "CSV");
    }
}

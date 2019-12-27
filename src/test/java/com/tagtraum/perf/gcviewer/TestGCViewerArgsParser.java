package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.exp.DataWriterType;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests the class {@link com.tagtraum.perf.gcviewer.GCViewerArgsParser} 
 * - makes sure that argument inputs parse correctly
 *
 * @author <a href="mailto:smendenh@redhat.com">Samuel Mendenhall</a>
 * <p>created on: 06.09.2014</p>
 */
class TestGCViewerArgsParser {

    @Test
    void noArguments() throws Exception {
        String[] args = {};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        gcViewerArgsParser.parseArguments(args);

        assertEquals(0, gcViewerArgsParser.getArgumentCount());
        assertEquals(DataWriterType.SUMMARY, gcViewerArgsParser.getType());
    }

    @Test
    void onlyGCLog() throws Exception {
        String[] args = {"some_gc.log"};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        gcViewerArgsParser.parseArguments(args);

        assertEquals(1, gcViewerArgsParser.getArgumentCount());
        assertEquals(new GcResourceFile("some_gc.log"), gcViewerArgsParser.getGcResource());
        assertEquals(DataWriterType.SUMMARY, gcViewerArgsParser.getType());
    }

    @Test
    void onlyGcLogSeries() throws Exception {
        String[] args = {"some_gc.log.0;some_gc.log.1;some_gc.log.2"};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        gcViewerArgsParser.parseArguments(args);

        assertEquals(1, gcViewerArgsParser.getArgumentCount());
        List<GCResource> resources = Arrays.asList(new GcResourceFile("some_gc.log.0"), new GcResourceFile("some_gc.log.1"), new GcResourceFile("some_gc.log.2"));
        assertEquals(new GcResourceSeries(resources), gcViewerArgsParser.getGcResource());
        assertEquals(DataWriterType.SUMMARY, gcViewerArgsParser.getType());
    }

    @Test
    void gcAndExportFile() throws Exception {
        String[] args = {"some_gc.log", "export_to.csv"};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        gcViewerArgsParser.parseArguments(args);

        assertEquals(2, gcViewerArgsParser.getArgumentCount());
        assertEquals(new GcResourceFile("some_gc.log"), gcViewerArgsParser.getGcResource());
        assertEquals("export_to.csv", gcViewerArgsParser.getSummaryFilePath());
        assertEquals(DataWriterType.SUMMARY, gcViewerArgsParser.getType());
    }

    @Test
    void gcSeriesAndExportFile() throws Exception {
        String[] args = {"some_gc.log.0;some_gc.log.1;some_gc.log.2", "export_to.csv"};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        gcViewerArgsParser.parseArguments(args);

        assertEquals(2, gcViewerArgsParser.getArgumentCount());
        List<GCResource> resources = Arrays.asList(new GcResourceFile("some_gc.log.0"), new GcResourceFile("some_gc.log.1"), new GcResourceFile("some_gc.log.2"));
        assertEquals(new GcResourceSeries(resources), gcViewerArgsParser.getGcResource());
        assertEquals("export_to.csv", gcViewerArgsParser.getSummaryFilePath());
        assertEquals(DataWriterType.SUMMARY, gcViewerArgsParser.getType());
    }

    @Test
    void onlyType() throws Exception {
        String[] args = {"-t", "CSV_TS"};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        gcViewerArgsParser.parseArguments(args);

        assertEquals(0, gcViewerArgsParser.getArgumentCount());
        assertEquals(DataWriterType.CSV_TS, gcViewerArgsParser.getType());
    }

    @Test
    void allInitialArgsWithType() throws Exception {
        String[] args = {"some_gc.log", "export_to.csv", "the_chart.png", "-t", "CSV"};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        gcViewerArgsParser.parseArguments(args);

        assertEquals(3, gcViewerArgsParser.getArgumentCount());
        assertEquals(new GcResourceFile("some_gc.log"), gcViewerArgsParser.getGcResource());
        assertEquals("export_to.csv", gcViewerArgsParser.getSummaryFilePath());
        assertEquals("the_chart.png", gcViewerArgsParser.getChartFilePath());
        assertEquals(DataWriterType.CSV, gcViewerArgsParser.getType());
    }

    @Test
    void illegalType() {
        String[] args = {"some_gc.log", "export_to.csv", "the_chart.png", "-t", "ILLEGAL"};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        GCViewerArgsParserException e = assertThrows(GCViewerArgsParserException.class, () -> gcViewerArgsParser.parseArguments(args));
        assertThat("exception message", e.getMessage(), startsWith("Illegal type 'ILLEGAL'"));
    }
}

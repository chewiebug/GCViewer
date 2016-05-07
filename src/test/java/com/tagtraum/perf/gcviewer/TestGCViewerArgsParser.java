package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.exp.DataWriterType;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.model.GcResourceFile;
import com.tagtraum.perf.gcviewer.model.GcResourceSeries;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.*;

/**
 * Tests the class {@link com.tagtraum.perf.gcviewer.GCViewerArgsParser} 
 * - makes sure that argument inputs parse correctly
 *
 * @author <a href="mailto:smendenh@redhat.com">Samuel Mendenhall</a>
 * <p>created on: 06.09.2014</p>
 */
public class TestGCViewerArgsParser {

    @Test
    public void noArguments() throws Exception {
        String[] args = {};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        gcViewerArgsParser.parseArguments(args);

        assertEquals(gcViewerArgsParser.getArgumentCount(), 0);
        assertEquals(gcViewerArgsParser.getType(), DataWriterType.SUMMARY);
    }

    @Test
    public void onlyGCLog() throws Exception {
        String[] args = {"some_gc.log"};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        gcViewerArgsParser.parseArguments(args);

        assertEquals(gcViewerArgsParser.getArgumentCount(), 1);
        assertEquals(gcViewerArgsParser.getGcResource(), new GcResourceFile("some_gc.log"));
        assertEquals(gcViewerArgsParser.getType(), DataWriterType.SUMMARY);
    }

    @Test
    public void onlyGcLogSeries() throws Exception {
        String[] args = {"some_gc.log.0;some_gc.log.1;some_gc.log.2"};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        gcViewerArgsParser.parseArguments(args);

        assertEquals(gcViewerArgsParser.getArgumentCount(), 1);
        List<GCResource> resources = Arrays.asList(new GcResourceFile("some_gc.log.0"), new GcResourceFile("some_gc.log.1"), new GcResourceFile("some_gc.log.2"));
        assertEquals(gcViewerArgsParser.getGcResource(), new GcResourceSeries(resources));
        assertEquals(gcViewerArgsParser.getType(), DataWriterType.SUMMARY);
    }

    @Test
    public void gcAndExportFile() throws Exception {
        String[] args = {"some_gc.log", "export_to.csv"};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        gcViewerArgsParser.parseArguments(args);

        assertEquals(gcViewerArgsParser.getArgumentCount(), 2);
        assertEquals(gcViewerArgsParser.getGcResource(), new GcResourceFile("some_gc.log"));
        assertEquals(gcViewerArgsParser.getSummaryFilePath(), "export_to.csv");
        assertEquals(gcViewerArgsParser.getType(), DataWriterType.SUMMARY);
    }

    @Test
    public void gcSeriesAndExportFile() throws Exception {
        String[] args = {"some_gc.log.0;some_gc.log.1;some_gc.log.2", "export_to.csv"};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        gcViewerArgsParser.parseArguments(args);

        assertEquals(gcViewerArgsParser.getArgumentCount(), 2);
        List<GCResource> resources = Arrays.asList(new GcResourceFile("some_gc.log.0"), new GcResourceFile("some_gc.log.1"), new GcResourceFile("some_gc.log.2"));
        assertEquals(gcViewerArgsParser.getGcResource(), new GcResourceSeries(resources));
        assertEquals(gcViewerArgsParser.getSummaryFilePath(), "export_to.csv");
        assertEquals(gcViewerArgsParser.getType(), DataWriterType.SUMMARY);
    }

    @Test
    public void onlyType() throws Exception {
        String[] args = {"-t", "CSV_TS"};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        gcViewerArgsParser.parseArguments(args);

        assertEquals(gcViewerArgsParser.getArgumentCount(), 0);
        assertEquals(gcViewerArgsParser.getType(), DataWriterType.CSV_TS);
    }

    @Test
    public void allInitialArgsWithType() throws Exception {
        String[] args = {"some_gc.log", "export_to.csv", "the_chart.png", "-t", "CSV"};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        gcViewerArgsParser.parseArguments(args);

        assertEquals(gcViewerArgsParser.getArgumentCount(), 3);
        assertEquals(gcViewerArgsParser.getGcResource(), new GcResourceFile("some_gc.log"));
        assertEquals(gcViewerArgsParser.getSummaryFilePath(), "export_to.csv");
        assertEquals(gcViewerArgsParser.getChartFilePath(), "the_chart.png");
        assertEquals(gcViewerArgsParser.getType(), DataWriterType.CSV);
    }
    
    @Test
    public void illegalType() {
        String[] args = {"some_gc.log", "export_to.csv", "the_chart.png", "-t", "ILLEGAL"};
        try {
            GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
            gcViewerArgsParser.parseArguments(args);
            fail("GCVIewerArgsParserException expected");
        }
        catch (GCViewerArgsParserException e) {
            assertThat("exception message", e.getMessage(), startsWith("Illegal type 'ILLEGAL'"));
        }
    }
}

package com.tagtraum.perf.gcviewer;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.tagtraum.perf.gcviewer.exp.DataWriterType;

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
        assertEquals(gcViewerArgsParser.getGcfile(), "some_gc.log");
        assertEquals(gcViewerArgsParser.getType(), DataWriterType.SUMMARY);
    }

    @Test
    public void gcAndExportFile() throws Exception {
        String[] args = {"some_gc.log", "export_to.csv"};
        GCViewerArgsParser gcViewerArgsParser = new GCViewerArgsParser();
        gcViewerArgsParser.parseArguments(args);

        assertEquals(gcViewerArgsParser.getArgumentCount(), 2);
        assertEquals(gcViewerArgsParser.getGcfile(), "some_gc.log");
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
        assertEquals(gcViewerArgsParser.getGcfile(), "some_gc.log");
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

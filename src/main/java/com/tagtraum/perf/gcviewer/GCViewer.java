package com.tagtraum.perf.gcviewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tagtraum.perf.gcviewer.ctrl.impl.GCViewerGuiController;
import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.exp.DataWriterType;
import com.tagtraum.perf.gcviewer.exp.impl.DataWriterFactory;
import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.view.SimpleChartRenderer;

/**
 * Main class of GCViewer. Parses command line parameters if there are any and either remains
 * in command line mode or starts the gui (depending on parameters). 
 */
public class GCViewer {
    private static final Logger LOGGER = Logger.getLogger(GCViewer.class.getName());
    private static final int EXIT_OK = 0;
    private static final int EXIT_EXPORT_FAILED = -1;
    private static final int EXIT_ARGS_PARSE_FAILED = -2;

	public static void main(final String[] args) throws InvocationTargetException, InterruptedException {
        new GCViewer().doMain(args);
    }

    public void doMain(String[] args) throws InvocationTargetException, InterruptedException {
        GCViewerArgsParser argsParser = new GCViewerArgsParser();
        try {
            argsParser.parseArguments(args);
        }
        catch (GCViewerArgsParserException e) {
            usage();
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            System.exit(EXIT_ARGS_PARSE_FAILED);
        }

        if (argsParser.getArgumentCount() > 3) {
            usage();
        }
        else if (argsParser.getArgumentCount() >= 2) {
            LOGGER.info("GCViewer command line mode");
            String gcfile = argsParser.getGcfile();
            String summaryFilePath = argsParser.getSummaryFilePath();
            String chartFilePath = argsParser.getChartFilePath();
            DataWriterType type = argsParser.getType();

            //export summary:
            try {
                export(gcfile, summaryFilePath, chartFilePath, type);
                LOGGER.info("export completed successfully");
                System.exit(EXIT_OK);
            }
            catch(Exception e) {
                LOGGER.log(Level.SEVERE, "Error during report generation", e);
                System.exit(EXIT_EXPORT_FAILED);
            }
        }
        else {
            new GCViewerGuiController().startGui(argsParser.getArgumentCount() == 1 ? argsParser.getGcfile() : null);
        }
    }

    private void export(String gcFilename, String summaryFilePath, String chartFilePath, DataWriterType type)
            throws IOException, DataReaderException {
        
        DataReaderFacade dataReaderFacade = new DataReaderFacade();
        GCModel model = dataReaderFacade.loadModel(new GCResource(gcFilename));

        exportType(model, summaryFilePath, type);
        if (chartFilePath != null)
            renderChart(model, chartFilePath);
    }

	private void exportType(GCModel model, String summaryFilePath, DataWriterType type) throws IOException {
        try (DataWriter summaryWriter = DataWriterFactory.getDataWriter(new File(summaryFilePath), type)) {
            summaryWriter.write(model);
        }
    }

    private void renderChart(GCModel model, String chartFilePath) throws IOException {
        SimpleChartRenderer renderer = new SimpleChartRenderer();
        renderer.render(model, new FileOutputStream(new File(chartFilePath)));
    }

	private static void usage() {
		System.out.println("Welcome to GCViewer with cmdline");
        System.out.println("java -jar gcviewer.jar [<gc-log-file|url>] -> opens gui and loads given file");
        System.out.println("java -jar gcviewer.jar [<gc-log-file>] [<export.csv>] -> cmdline: writes report to <export.csv>");
        System.out.println("java -jar gcviewer.jar [<gc-log-file>] [<export.csv>] [<chart.png>] " +
                "-> cmdline: writes report to <export.csv> and renders gc chart to <chart.png>");
        System.out.println("java -jar gcviewer.jar [<gc-log-file>] [<export.csv>] [<chart.png>] [-t <SUMMARY, CSV, CSV_TS, PLAIN, SIMPLE>]");
    }

}

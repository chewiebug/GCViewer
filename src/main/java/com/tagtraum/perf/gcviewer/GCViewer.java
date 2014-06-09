package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.exp.DataWriterType;
import com.tagtraum.perf.gcviewer.exp.impl.DataWriterFactory;
import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.model.GCModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GCViewer {
    private static final Logger LOGGER = Logger.getLogger(GCViewer.class.getName());

    // Default to the summary type
    private String type = "SUMMARY";
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    // receives other command line parameters than options
    private List<String> arguments = new ArrayList<String>();
    public List<String> getArguments() { return arguments; }
    public void setArguments(List<String> arguments) { this.arguments = arguments; }

	public static void main(final String[] args) throws IOException {
        new GCViewer().doMain(args);
    }

    public void parseArguments(String[] args) {
        List<String> argsList = new ArrayList<String>(Arrays.asList(args));
        int typeIdx = argsList.indexOf("-t");

        // If there is a -t and there is a string after, set the type
        if(typeIdx != -1 && argsList.size() > (typeIdx + 1)) {
            type = argsList.get(typeIdx + 1);
            // Chomp these two from the array to prevent any order issues
            argsList.remove(typeIdx);
            argsList.remove(typeIdx);
        } else if (typeIdx != -1) {
            // No specific type set, just keep the default
            argsList.remove(typeIdx);
        }

        // Set the arguments to the remaining arguments
        arguments = argsList;
    }

    public void doMain(String[] args) throws IOException {
        parseArguments(args);

        if (arguments.size() > 3) {
            usage();
        }
        else if (arguments.size() >= 2) {
            final String gcfile = arguments.get(0);
            final String summaryFilePath = arguments.get(1);
            final String chartFilePath = arguments.size() == 3 ? arguments.get(2) : null;

            //export summary:
            try {
                export(gcfile, summaryFilePath, chartFilePath);
                System.exit(0);
            } catch(IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, "Type must be one of SUMMARY, CSV, CSV_TS, PLAIN, SIMPLE", e);
                System.exit(-1);
            } catch(Exception e) {
                LOGGER.log(Level.SEVERE, "Error during report generation", e);
                System.exit(-1);
            }
        }
        else {
            GCViewerGui.start(arguments.size() == 1 ? arguments.get(0) : null);
        }
    }

    private void export(String gcFilename, String summaryFilePath, String chartFilePath)
            throws IOException, DataReaderException, IllegalArgumentException {
        DataReaderFacade dataReaderFacade = new DataReaderFacade();
        GCModel model = dataReaderFacade.loadModel(gcFilename, false, null);
        DataWriterType dataWriterType = DataWriterType.valueOf(type.trim().toUpperCase());

        exportType(model, summaryFilePath, dataWriterType);
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
        renderer.render(model, chartFilePath);
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

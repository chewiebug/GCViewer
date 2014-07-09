package com.tagtraum.perf.gcviewer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tagtraum.perf.gcviewer.ctrl.GCViewerGuiController;
import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.exp.DataWriterType;
import com.tagtraum.perf.gcviewer.exp.impl.DataWriterFactory;
import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.view.SimpleChartRenderer;

public class GCViewer {
    private static final Logger LOGGER = Logger.getLogger(GCViewer.class.getName());

	public static void main(final String[] args) throws InvocationTargetException, InterruptedException {
        if (args.length > 3) {
            usage();
        }
        else if (args.length >= 2) {
        	final String gcfile = args[0];
        	final String summaryFilePath = args[1];
            final String chartFilePath = args.length == 3 ? args[2] : null;

            //export summary:
            try {
                export(gcfile, summaryFilePath, chartFilePath);
                System.exit(0);
            }
            catch(Exception e) {
                LOGGER.log(Level.SEVERE, "Error during report generation", e);
                System.exit(-1);
            }
        }
        else {
            new GCViewerGuiController().startGui(args.length == 1 ? args[0] : null);
        }
    }

    private static void export(String gcFilename, String summaryFilePath, String chartFilePath)
            throws IOException, DataReaderException {
        
        final DataReaderFacade dataReaderFacade = new DataReaderFacade();
        GCModel model = dataReaderFacade.loadModel(new GCResource(gcFilename));

        exportSummary(model, summaryFilePath);
        if (chartFilePath != null)
            renderChart(model, chartFilePath);
    }

	private static void exportSummary(GCModel model, String summaryFilePath) throws IOException {
        try (DataWriter summaryWriter = DataWriterFactory.getDataWriter(new File(summaryFilePath), DataWriterType.SUMMARY)) {
            summaryWriter.write(model);
        }
    }

    private static void renderChart(GCModel model, String chartFilePath) throws IOException {
        SimpleChartRenderer renderer = new SimpleChartRenderer();
        renderer.render(model, chartFilePath);
    }

	private static void usage() {
		System.out.println("Welcome to GCViewer with cmdline");
        System.out.println("java -jar gcviewer.jar [<gc-log-file|url>] -> opens gui and loads given file");
        System.out.println("java -jar gcviewer.jar [<gc-log-file>] [<export.csv>] -> cmdline: writes report to <export.csv>");
        System.out.println("java -jar gcviewer.jar [<gc-log-file>] [<export.csv>] [<chart.png>] " +
                "-> cmdline: writes report to <export.csv> and renders gc chart to <chart.png>");
    }
	
}

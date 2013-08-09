package com.tagtraum.perf.gcviewer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.tagtraum.perf.gcviewer.exp.DataWriter;
import com.tagtraum.perf.gcviewer.exp.DataWriterType;
import com.tagtraum.perf.gcviewer.exp.impl.DataWriterFactory;
import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.model.GCModel;

import javax.imageio.ImageIO;

public class GCViewer {
    private static final Logger log = Logger.getAnonymousLogger();

	public static void main(final String[] args) {
	    // TODO: unify parameter handling from command line
        if (args.length == 2) {
        	final String gcfile = args[0];
        	final String summaryFilePath = args[1];

            //export summary:
            try {
                exportSummary(summaryFilePath, gcfile);
                System.exit(0);
            }
            catch(Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }

        }
        else if (args.length > 1) {
            usage();
        }
        else {
        	GCViewerGui.start(args.length == 1 ? args[0] : null);
        }
    }

	private static void exportSummary(String summaryFilePath, String gcFilename) throws DataReaderException, IOException {
	    try (DataWriter summaryWriter = DataWriterFactory.getDataWriter(new File(summaryFilePath), DataWriterType.SUMMARY)) {
	        DataReaderFacade dataReaderFacade = new DataReaderFacade();
	        GCModel model = dataReaderFacade.loadModel(new File(gcFilename).toURI().toURL(), false, null);
	        summaryWriter.write(model);

            GCPreferences gcPreferences = new GCPreferences();
            gcPreferences.load();

            final ModelChartImpl pane = new ModelChartImpl();

            Dimension s = new Dimension(1024, 768);

            pane.setModel(model, gcPreferences);
            pane.setFootprint(model.getFootprint());
            pane.setMaxPause(model.getPause().getMax());
            pane.setRunningTime(model.getRunningTime());

            pane.setSize(s);
            pane.addNotify();
            pane.validate();

            double zoomFactor = s.getWidth() / model.getRunningTime() * 0.9;
            pane.setScaleFactor(zoomFactor);
            log.info(String.valueOf(zoomFactor));

            final BufferedImage image = new BufferedImage(s.width, s.height, BufferedImage.TYPE_INT_RGB);
            final Graphics2D graphics = image.createGraphics();
            graphics.setBackground(Color.WHITE);
            graphics.clearRect(0, 0, image.getWidth(), image.getHeight());

            pane.paint(graphics);
            try
            {
                ImageIO.write(image, "png", new File("snapshot.png"));
            }
            catch (IOException e)
            {
            }
        }
    }



	private static void usage() {
		System.out.println("Welcome to GCViewer with cmdline");
        System.out.println("java -jar gcviewer.jar [<gc-log-file>] [<export.csv>]");
    }

}

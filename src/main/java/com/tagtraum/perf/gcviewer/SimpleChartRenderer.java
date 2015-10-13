package com.tagtraum.perf.gcviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.tagtraum.perf.gcviewer.model.GCModel;

public class SimpleChartRenderer {
    private static final Logger LOGGER = Logger.getLogger(SimpleChartRenderer.class.getName());

    public void render(GCModel model, FileOutputStream outputStream) throws IOException {
        GCPreferences gcPreferences = new GCPreferences();
        gcPreferences.load();
        Dimension d = new Dimension(gcPreferences.getWindowWidth(), gcPreferences.getWindowHeight());

        BufferedImage image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, image.getWidth(), image.getHeight());

        ChartDrawingParameters params
                = new ChartDrawingParameters(model, gcPreferences, d, graphics, image, outputStream);

        if (EventQueue.isDispatchThread()) {
            drawAndSaveToStream(params);
        }
        else {
            new SwingChartToStreamHelper().execute(params);
        }
    }

    private void drawAndSaveToStream(ChartDrawingParameters params) throws IOException {
        ModelChartImpl pane = new ModelChartImpl();
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        pane.setModel(params.model, params.gcPreferences);
        pane.setFootprint(params.model.getFootprint());
        pane.setMaxPause(params.model.getPause().getMax());
        pane.setRunningTime(params.model.getRunningTime());

        pane.setSize(params.dimension);
        pane.addNotify();
        pane.validate();

        pane.autoSetScaleFactor();
        pane.paint(params.graphics);

        ImageIO.write(params.image, "png", params.outputStream);
        params.outputStream.close();
    }

    /**
     * Helperclass that saves the chart to a file making sure, it happens on the EventDispathThread.
     */
    private class SwingChartToStreamHelper {

        private IOException ioException;

        public void execute(ChartDrawingParameters params) throws IOException {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        drawAndSaveToStream(params);
                    }
                    catch (IOException e) {
                        this.ioException = e;
                    }
                });
            }
            catch (InterruptedException | InvocationTargetException e) {
                // may look a bit strange, but allows to defer exception handling to a place, where it makes sense.
                ioException = new IOException(e);
            }

            if (ioException != null) {
                throw ioException;
            }
        }
    }

    /**
     * Holder class wrapping all parameters needed to draw the chart and save it to an OutputStream.
     */
    private static class ChartDrawingParameters {

        GCModel model;
        GCPreferences gcPreferences;
        Dimension dimension;
        Graphics2D graphics;
        BufferedImage image;
        OutputStream outputStream;

        public ChartDrawingParameters(GCModel model,
                                      GCPreferences gcPreferences,
                                      Dimension dimension,
                                      Graphics2D graphics,
                                      BufferedImage image,
                                      OutputStream outputStream) {

            this.model = model;
            this.gcPreferences = gcPreferences;
            this.dimension = dimension;
            this.graphics = graphics;
            this.image = image;
            this.outputStream = outputStream;
        }
    }
}

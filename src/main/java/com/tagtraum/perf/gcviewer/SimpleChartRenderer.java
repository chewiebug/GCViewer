package com.tagtraum.perf.gcviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JScrollPane;

import com.tagtraum.perf.gcviewer.model.GCModel;

public class SimpleChartRenderer {
    private static final Logger LOGGER = Logger.getLogger(SimpleChartRenderer.class.getName());

    public void render(GCModel model, FileOutputStream outputStream) throws IOException {
        GCPreferences gcPreferences = new GCPreferences();
        gcPreferences.load();

        final ModelChartImpl pane = new ModelChartImpl();
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        pane.setModel(model, gcPreferences);
        pane.setFootprint(model.getFootprint());
        pane.setMaxPause(model.getPause().getMax());
        pane.setRunningTime(model.getRunningTime());

        Dimension d = new Dimension(gcPreferences.getWindowWidth(), gcPreferences.getWindowHeight());
        pane.setSize(d);
        pane.addNotify();
        pane.validate();

        pane.autoSetScaleFactor();

        final BufferedImage image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics = image.createGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, image.getWidth(), image.getHeight());

        pane.paint(graphics);

        ImageIO.write(image, "png", outputStream);
        outputStream.close();
    }
}

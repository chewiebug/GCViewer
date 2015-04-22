package com.tagtraum.perf.gcviewer;

import com.tagtraum.perf.gcviewer.model.GCModel;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleChartRenderer {
    private static final Logger LOGGER = Logger.getLogger(SimpleChartRenderer.class.getName());

    public void render(GCModel model, String chartFilePath) throws IOException {
        GCPreferences gcPreferences = new GCPreferences();
        gcPreferences.load();
        Dimension d = new Dimension(gcPreferences.getWindowWidth(), gcPreferences.getWindowHeight());


        BufferedImage image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
        try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
			        ModelChartImpl pane = new ModelChartImpl();
			        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			        pane.setModel(model, gcPreferences);
			        pane.setFootprint(model.getFootprint());
			        pane.setMaxPause(model.getPause().getMax());
			        pane.setRunningTime(model.getRunningTime());

			        pane.setSize(d);
			        pane.addNotify();
			        pane.validate();

			        pane.autoSetScaleFactor();
			        pane.paint(graphics);
				}
			});
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to paint graphic", e);
		}


        ImageIO.write(image, "png", new File(chartFilePath));
    }
}

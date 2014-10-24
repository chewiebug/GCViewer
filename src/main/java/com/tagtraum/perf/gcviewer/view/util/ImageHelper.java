package com.tagtraum.perf.gcviewer.view.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * Helper class to deal with images.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 04.01.2014</p>
 */
public class ImageHelper {
    
    private static final String IMAGE_PATH = "images/";
    
    /**
     * Creates an empty image.
     *
     * @param width width of image
     * @param height height of image
     * @return empty image
     */
    public static ImageIcon createEmptyImageIcon(final int width, final int height) {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
        final Graphics2D g = image.createGraphics();
        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Creates a rectangular mono colored image.
     *
     * @param paint color of the image
     * @param width width of the image
     * @param height height of the image
     * @return mono colored rectangular image
     */
    public static ImageIcon createMonoColoredImageIcon(final Paint paint, final int width, final int height) {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
        final Graphics2D g = image.createGraphics();
        g.setPaint(paint);
        final int lineHeight = 4;
        g.fill3DRect(0, height / 2 - lineHeight / 2, width, lineHeight, false);
        g.dispose();
        return new ImageIcon(image);
    }

    /**
     * Loads an image from the given path.
     * 
     * @param imageName path to image
     * @return loaded image
     * @throws IllegalArgumentException when image can't be found in classpath
     */
    public static Image loadImage(String imageName) {
        URL imageUrl = Thread.currentThread().getContextClassLoader().getResource(IMAGE_PATH + imageName);
        if (imageUrl == null) {
            throw new IllegalArgumentException("'" + imageName + "' could not be found in classpath");
        }
        
        return Toolkit.getDefaultToolkit().getImage(imageUrl);
    }
    
    /**
     * Loads an image from a given path and returns it as an ImageIcon.
     * 
     * @param imageName path to image
     * @return loaded image
     * @throws IllegalArgumentException when image can't be found in classpath
     */
    public static ImageIcon loadImageIcon(String imageName) {
        return new ImageIcon(loadImage(imageName));
    }
}

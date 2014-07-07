package com.tagtraum.perf.gcviewer.view.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * Helper class to load images from classpath.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 04.01.2014</p>
 */
public class ImageLoader {
    
    private static final String IMAGE_PATH = "images/";
    
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

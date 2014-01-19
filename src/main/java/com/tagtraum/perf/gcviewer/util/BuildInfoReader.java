package com.tagtraum.perf.gcviewer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Small helper class to provide current version of GCViewer.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 14.07.2012</p>
 *
 */
public class BuildInfoReader {

    private final static String FILE_NAME = "META-INF/MANIFEST.MF";
    private final static String BUILD_VERSION = "Implementation-Version";
    private final static String BUILD_TIMESTAMP = "Implementation-Date";
    
    /**
     * Reads the value of a property from FILE_NAME (must be in classpath).
     * 
     * @param propertyName name of the property to be read
     * @return "n/a" if it couldn't be found or the value
     */
    private static String readPropertyValue(String propertyName) {
        String propertyValue = "n/a";
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(FILE_NAME)) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                propertyValue = props.getProperty(propertyName);
                if (propertyValue == null || propertyValue.length() == 0) {
                    propertyValue = "n/a";
                }
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }

        return propertyValue;
    }
    
    /**
     * Read version from properties file in classpath if it can be found.
     * 
     * @return version or "n/a" if not found.
     */
    public static String getVersion() {
        return readPropertyValue(BUILD_VERSION);
    }

    /**
     * Read build date from properties file in classpath if it can be found.
     * 
     * @return date or "n/a" if not found.
     */
    public static String getBuildDate() {
        return readPropertyValue(BUILD_TIMESTAMP);
    }
}

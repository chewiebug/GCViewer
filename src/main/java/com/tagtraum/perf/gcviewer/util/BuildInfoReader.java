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

    private final static String FILE_NAME = "build.info.properties";
    
    /**
     * Reads the value of a property from FILE_NAME (must be in classpath).
     * 
     * @param propertyName name of the property to be read
     * @return "n/a" if it couldn't be found or the value
     */
    private static String readPropertyValue(String propertyName) {
        String propertyValue = "n/a";
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(FILE_NAME);
        if (in != null) {
            Properties props = new Properties();
            try {
                try {
                    props.load(in);
                    propertyValue = props.getProperty(propertyName);
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return propertyValue;
    }
    
    /**
     * Read version from properties file in classpath if it can be found.
     * 
     * @return version or "n/a" if not found.
     */
    public static String getVersion() {
        return readPropertyValue("build.version");
    }

    /**
     * Read build date from properties file in classpath if it can be found.
     * 
     * @return date or "n/a" if not found.
     */
    public static String getBuildDate() {
        return readPropertyValue("build.date");
    }
}

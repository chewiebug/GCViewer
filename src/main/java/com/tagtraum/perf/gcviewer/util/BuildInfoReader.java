package com.tagtraum.perf.gcviewer.util;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Small helper class to provide current version of GCViewer.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 14.07.2012</p>
 *
 */
public class BuildInfoReader {

    private final static String FILE_NAME = "/META-INF/MANIFEST.MF";
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
        try {
            Attributes attributes = getAttributes();
            propertyValue = attributes.getValue(propertyName);
            if (propertyValue == null || propertyValue.length() == 0) {
                propertyValue = "n/a";
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return propertyValue;
    }

    /**
     * Returns Manifest-Attributes for MANIFEST.MF, if running for a .jar file
     *
     * @return Manifest Attributes (may be empty but never null)
     * @throws IOException If something went wrong finding the MANIFEST file
     * @see <a href="http://stackoverflow.com/a/1273432">stackoverflow article</a>
     */
    private static Attributes getAttributes() throws IOException {
        Class clazz = BuildInfoReader.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (!classPath.startsWith("jar")) {
            // Class not from JAR
            return new Attributes(0);
        }
        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + FILE_NAME;
        Manifest manifest = new Manifest(new URL(manifestPath).openStream());
        return manifest.getMainAttributes();
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

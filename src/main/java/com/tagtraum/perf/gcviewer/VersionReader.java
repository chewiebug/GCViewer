package com.tagtraum.perf.gcviewer;

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
public class VersionReader {

    /**
     * Read version from properties file in classpath if it can be found.
     * 
     * @return version or "n/a" if not found.
     */
    public static String getVersion() {
        String version = "n/a";
        InputStream in = VersionReader.class.getResourceAsStream("version.properties");
        if (in != null) {
            Properties props = new Properties();
            try {
                try {
                    props.load(in);
                    version = props.getProperty("build.version");
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return version;
    }
}

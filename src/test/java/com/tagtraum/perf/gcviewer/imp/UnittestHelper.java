package com.tagtraum.perf.gcviewer.imp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Helper class to support the unittests.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 22.05.2013</p>
 */
public class UnittestHelper {
    
    public static final String FOLDER_HP = "hp";
    public static final String FOLDER_IBM = "ibm";
    public static final String FOLDER_JROCKIT = "jrockit";
    public static final String FOLDER_OPENJDK = "openjdk";
    public static final String FOLDER_HTTP = "http";
    
    /**
     * Load resource as stream if it is present somewhere in the classpath.
     * @param name Name of the resource
     * @return instance of an input stream or <code>null</code> if the resource couldn't be found
     * @throws IOException if resource can't be found
     */
    public static InputStream getResourceAsStream(String name) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        if (in == null) {
            throw new IOException("could not find " + name + " in classpath");
        }
        
        return in; 
    }
    
    /**
     * Load a resource as stream from a given <code>folder</code>.
     * 
     * @see #getResourceAsStream(String)
     */
    public static InputStream getResourceAsStream(String folder, String name) throws IOException {
        return getResourceAsStream(folder + File.separator + name);
    }

    /**
     * Load resource as stream if it is present somewhere in the classpath.
     * @param name Name of the resource
     * @return instance of an input stream or <code>null</code> if the resource couldn't be found
     * @throws IOException if resource can't be found
     */
    public static URL getResource(String name) throws IOException {
    	URL url = Thread.currentThread().getContextClassLoader().getResource(name);
        if (url == null) {
            throw new IOException("could not find " + name + " in classpath");
        }
        
        return url; 
    }
    
    /**
     * Get URL of a resource from a given <code>folder</code>.
     * 
     * @see #getResource(String)
     */
    public static URL getResource(String folder, String name) throws IOException {
        return getResource(folder + File.separator + name);
    }

    /**
     * Converter from bytes to kilobytes.
     *
     * @param bytes value in bytes
     * @return value in kilobytes
     */
    public static int toKiloBytes(long bytes) {
        return (int)Math.rint(bytes / (double)1024);
    }
}

package com.tagtraum.perf.gcviewer.exp;

/**
 * Enumeration for the different types of data writers that exist. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 07.10.2012</p>
 */
public enum DataWriterType {
    PLAIN,
    CSV,
    SUMMARY;
    
    /**
     * Converts a file extension to a <code>DataWriterType</code>.
     * 
     * @param extension file extension
     * @return corresponding DataWriterType if available
     * @throws IllegalArgumentException if extension is unknown
     */
    public static DataWriterType asType(String extension) {
        if (".csv".equals(extension)) {
            return CSV;
        }
        else if (".txt".equals(extension)) {
            return PLAIN;
        }
        else {
            throw new IllegalArgumentException("'" + extension + "' is not a valid parameter");
        }
    }
}

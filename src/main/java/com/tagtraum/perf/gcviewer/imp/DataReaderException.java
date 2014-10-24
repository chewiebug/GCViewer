package com.tagtraum.perf.gcviewer.imp;

/**
 * Specific exception type to indicate that something went wrong while trying to read data
 * from a gc log file / url. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 24.11.2012</p>
 */
public class DataReaderException extends Exception {

    public DataReaderException() {
        super();
    }

    public DataReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataReaderException(String message) {
        super(message);
    }

    public DataReaderException(Throwable cause) {
        super(cause);
    }

}

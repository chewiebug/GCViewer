package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;

/**
 * Baseclass for every {@link DataReader} implementation.
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 11.01.2014</p>
 *
 */
public abstract class AbstractDataReader implements DataReader {

    /** the resource being read */
    protected GCResource gcResource;
    /** the reader accessing the log file */
    protected LineNumberReader in;

    protected AbstractDataReader(GCResource gcResource, InputStream in) throws UnsupportedEncodingException {
        super();

        this.in = new LineNumberReader(new InputStreamReader(in, "ASCII"), 64 * 1024);
        this.gcResource = gcResource;
    }

    /**
     * Returns a logger instance that logs in the context of the current GCResource being loaded.
     * This logger should always be used, because otherwise the "Logger" tab won't show any
     * log entries written during loading process.
     *
     * @return thread specific logger
     */
    protected Logger getLogger() {
        return gcResource.getLogger();
    }

    private DataReaderTools dataReaderTools;

    protected DataReaderTools getDataReaderTools() {
        if (dataReaderTools == null) {
            dataReaderTools = new DataReaderTools(getLogger());
        }

        return dataReaderTools;
    }

    @Override
    public abstract GCModel read() throws IOException;

    /**
     * Returns <code>true</code> as long as read was not cancelled.
     * @return <code>true</code> as long as read was not cancelled
     */
    protected boolean shouldContinue() {
        if (getLogger().isLoggable(Level.FINE)) {
            getLogger().fine(gcResource.getResourceName() + " read cancelled");
        }
        return !gcResource.isReadCancelled();
    }
}

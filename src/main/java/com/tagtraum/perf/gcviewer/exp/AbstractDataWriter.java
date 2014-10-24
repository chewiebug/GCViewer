package com.tagtraum.perf.gcviewer.exp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Abstract base class for all classes implementing {@link DataWriter}.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 07.10.2012</p>
 *
 */
public abstract class AbstractDataWriter implements DataWriter {
    /** PrintWriter where the output should be written to. */
    protected PrintWriter out;
    
    /** holder of additional configuration objects needed individually by a DataWriter */
    private Map<String, Object> configuration;
    
    public AbstractDataWriter(OutputStream outputStream) {
        this(outputStream, null);
    }
    
    public AbstractDataWriter(OutputStream outputStream, Map<String, Object> configuration) {
        super();
        
        out = new PrintWriter(outputStream);
        this.configuration = configuration;
        if (this.configuration == null) {
            this.configuration = new TreeMap<String, Object>();
        }
    }
    
    /**
     * Access to the additional configuration objects (may be empty but never <code>null</code>).
     * @return additional configuration objects
     */
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    @Override
    public abstract void write(GCModel model) throws IOException;

    @Override
    public void close() throws IOException {
        out.close();
    }
}

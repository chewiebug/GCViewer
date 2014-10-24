package com.tagtraum.perf.gcviewer.exp;

import java.io.Closeable;
import java.io.IOException;

import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Writes a GCModel into a given Stream.
 *
 * Date: Feb 1, 2002
 * Time: 9:56:19 AM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public interface DataWriter extends Closeable {
    public void write(GCModel model) throws IOException;

    /**
     * @see java.io.Closeable#close()
     */
    public void close() throws IOException;
}

package com.tagtraum.perf.gcviewer.exp;

import java.io.IOException;

import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Writes a GCModel into a given Stream.
 *
 * Date: Feb 1, 2002
 * Time: 9:56:19 AM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public interface DataWriter {
    public void write(GCModel model) throws IOException;

    public void close();
}

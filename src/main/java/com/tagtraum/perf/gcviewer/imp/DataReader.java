package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;

import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:01:01 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public interface DataReader {
    /**
     * A call to this method should read a {@link GCModel}.
     *
     * @return GCModel representing a gc log file
     * @throws IOException thrown, when an exception reading the file occurred
     */
    GCModel read() throws IOException;
}

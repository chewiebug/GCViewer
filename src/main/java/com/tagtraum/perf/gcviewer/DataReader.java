package com.tagtraum.perf.gcviewer;

import java.io.IOException;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:01:01 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public interface DataReader {
    public GCModel read() throws IOException;
}

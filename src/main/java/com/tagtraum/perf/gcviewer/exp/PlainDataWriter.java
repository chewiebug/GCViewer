package com.tagtraum.perf.gcviewer.exp;

import com.tagtraum.perf.gcviewer.DataWriter;
import com.tagtraum.perf.gcviewer.GCEvent;
import com.tagtraum.perf.gcviewer.GCModel;
import com.tagtraum.perf.gcviewer.AbstractGCEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;

/**
 * Writes the model using the toString()-methode of {@link GCEvent}.
 *
 * Date: Feb 1, 2002
 * Time: 9:58:11 AM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class PlainDataWriter implements DataWriter {

    private PrintWriter out;

    public PlainDataWriter(OutputStream out) {
        this.out = new PrintWriter(new OutputStreamWriter(out));
    }

    /**
     * Writes the model and flushes the internal PrintWriter.
     */
    public void write(GCModel model) throws IOException {
        for (Iterator i = model.getEvents(); i.hasNext();) {
            out.println(i.next().toString());
        }
        out.flush();
    }

    public void close() {
        if (out != null) out.close();
    }
}

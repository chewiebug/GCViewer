package com.tagtraum.perf.gcviewer.exp.impl;

import com.tagtraum.perf.gcviewer.exp.AbstractDataWriter;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 *
 * Date: Feb 1, 2002
 * Time: 10:07:52 AM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class CSVTSDataWriter extends AbstractDataWriter {

    public CSVTSDataWriter(OutputStream out) {
        super(out);
    }

    private void writeHeader() {
        out.println("Timestamp(unix/#),Used(K),Total(K),Pause(sec),GC-Type");
    }

    /**
     * Writes the model and flushes the internal PrintWriter.
     */
    public void write(GCModel model) throws IOException {
        writeHeader();
        
        Iterator<GCEvent> i = model.getGCEvents();
        while (i.hasNext()) {
            GCEvent event = i.next();
            // Since this data writer is only concerned with one line per gc entry, don't write two like the others.

            // If the true timestamp is present, output the unix timestamp
            if (model.hasCorrectTimestamp()) {
                out.print(event.getDatestamp().getTime());
            } else {
                out.print(event.getTimestamp());
            }
            out.print(',');
            out.print(event.getPreUsed()); // pre
            out.print(',');
            out.print(event.getTotal());
            out.print(',');
            out.print(event.getPause());
            out.print(',');
            out.println(event.getExtendedType());
        }
        out.flush();
    }

}

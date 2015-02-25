package com.tagtraum.perf.gcviewer.exp.impl;

import com.tagtraum.perf.gcviewer.exp.AbstractDataWriter;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * Export GC history with comma separated values.
 * <p>
 * It uses the {@literal "Timestamp(unix/#),Used(K),Total(K),Pause(sec),GC-Type"} format.
 *
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
        
        Iterator<AbstractGCEvent<?>> i = model.getStopTheWorldEvents();
        while (i.hasNext()) {
            AbstractGCEvent<?> abstractGCEvent = i.next();
            // filter "application stopped" events
            if (abstractGCEvent instanceof GCEvent) {
                GCEvent event = (GCEvent) abstractGCEvent;
                // Since this data writer is only concerned with one line per gc entry, don't write two like the others.

                // If the true timestamp is present, output the unix timestamp
                if (model.hasDateStamp()) {
                    out.print(event.getDatestamp().toInstant().getEpochSecond());
                } else if (model.hasCorrectTimestamp()) {
                    // we have the timestamps therefore we can correct it with the pause time
                    out.print((event.getTimestamp() - event.getPause()));
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
        }
        out.flush();
    }

}

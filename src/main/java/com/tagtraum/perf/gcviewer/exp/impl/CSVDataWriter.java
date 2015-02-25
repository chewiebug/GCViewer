package com.tagtraum.perf.gcviewer.exp.impl;

import com.tagtraum.perf.gcviewer.exp.AbstractDataWriter;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * Write GC history with comma separated values.
 * <p>
 * It uses the {@literal "Timestamp(sec/#),Used(K),Total(K),Pause(sec),GC-Type"} format.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class CSVDataWriter extends AbstractDataWriter {

    public CSVDataWriter(OutputStream out) {
        super(out);
    }

    private void writeHeader() {
        out.println("Timestamp(sec/#),Used(K),Total(K),Pause(sec),GC-Type");
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
                // write always two lines so that there is a nice used memory curve
                if (model.hasCorrectTimestamp()) {
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

                out.print(event.getTimestamp());
                out.print(',');
                out.print(event.getPostUsed()); // post
                out.print(',');
                out.print(event.getTotal());
                out.print(',');
                out.print(0);
                out.print(',');
                out.println("NONE");
            }
        }

        out.flush();
    }

}

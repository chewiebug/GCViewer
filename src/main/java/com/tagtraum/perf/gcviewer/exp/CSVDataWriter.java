package com.tagtraum.perf.gcviewer.exp;

import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;

/**
 *
 * Date: Feb 1, 2002
 * Time: 10:07:52 AM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class CSVDataWriter implements DataWriter {

    private PrintWriter out;

    public CSVDataWriter(OutputStream out) {
        this.out = new PrintWriter(new OutputStreamWriter(out));
    }

    private void writeHeader() {
        out.println("Timestamp(sec/#),Used(K),Total(K),Pause(sec),GC-Type");
    }

    /**
     * Writes the model and flushes the internal PrintWriter.
     */
    public void write(GCModel model) throws IOException {
        writeHeader();
        for (Iterator i = model.getGCEvents(); i.hasNext();) {
            GCEvent event = (GCEvent) i.next();
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
            out.println(event.getType());

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
        out.flush();
    }

    public void close() {
        if (out != null) out.close();
    }
}

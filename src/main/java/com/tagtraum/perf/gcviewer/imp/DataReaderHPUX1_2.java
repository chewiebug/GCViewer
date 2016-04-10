package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Type;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.NumberParser;

/**
 * DataReader for HP-UX 1.2/1.3/1.4.0
 *
 * @see <a href="http://www.hp.com/products1/unix/java/infolibrary/prog_guide/xverbosegc.html">http://www.hp.com/products1/unix/java/infolibrary/prog_guide/xverbosegc.html</a>
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class DataReaderHPUX1_2 extends AbstractDataReader {

    public DataReaderHPUX1_2(GCResource gcResource, InputStream in) throws UnsupportedEncodingException {
        super(gcResource, in);
    }

    public GCModel read() throws IOException {
        if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reading HP-UX 1.2-1.4.0 format...");
        try {
            GCModel model = new GCModel();
            model.setFormat(GCModel.Format.SUN_X_LOG_GC);
            String line = null;
            GCEvent event = null;
            while ((line = in.readLine()) != null && shouldContinue()) {
                StringTokenizer st = new StringTokenizer(line, " ");
                if (st.countTokens() != 20) {
                    if (getLogger().isLoggable(Level.WARNING)) getLogger().warning("Malformed line (" + in.getLineNumber() + "). Wrong number of tokens ("+st.countTokens()+"): " + line);
                    continue;
                }
                if (!"<GC:".equals(st.nextToken())) {
                    if (getLogger().isLoggable(Level.WARNING)) getLogger().warning("Malformed line (" + in.getLineNumber() + "). Expected \"<GC:\" in " + line);
                    continue;
                }
                event = new GCEvent();
                /*
                %1:  Indicates the cause of the garbage collection.
                     -1:  indicates a Scavenge (GC of New Generation only)
                    0-6:  indicates a Full GC (collection of all spaces in the
                          Java Heap)
                     Reason:
                        0:  Call to System.gc
                        1:  Old Generation full
                        2:  Permanent Generation full
                        3:  Train Generation full
                        4:  Old generation expanded on last scavenge
                        5:  Old generation too full to scavenge
                        6:  FullGCAlot
                */
                final int reason = Integer.parseInt(st.nextToken());
                event.setType(findType(reason));
                // %2:  Program time at the beginning of the collection, in seconds
                event.setTimestamp(NumberParser.parseDouble(st.nextToken()));
                // %3:  Garbage collection invocation.  Counts of Scavenge and
                // Full GCs are maintained separately
                st.nextToken();
                // %4:  Size of the object allocation request that forced the GC, in bytes
                st.nextToken();
                // %5:  Tenuring threshold - determines how long the new born object
                // remains in the New Generation
                st.nextToken();
                // Eden Sub-space (within the New Generation)
                // %6:  Before
                // %7:  After
                // %8:  Capacity
                final long edenBefore = Long.parseLong(st.nextToken());
                final long edenAfter = Long.parseLong(st.nextToken());
                final long edenCapacity = Long.parseLong(st.nextToken());
                /*
                GCEvent edenEvent = new GCEvent();
                edenEvent.setType(AbstractGCEvent.Type.DEF_NEW);
                edenEvent.setPreUsed((int)(edenBefore / 1024));
                edenEvent.setPostUsed((int)(edenAfter / 1024));
                edenEvent.setTotal((int)(edenCapacity / 1024));
                */

                // Survivor Sub-space (within the New Generation)
                // %9:   Before
                // %10:  After
                // %11:  Capacity
                final long survivorBefore = Long.parseLong(st.nextToken());
                final long survivorAfter = Long.parseLong(st.nextToken());
                final long survivorCapacity = Long.parseLong(st.nextToken());
                /*
                GCEvent survivorEvent = new GCEvent();
                survivorEvent.setType(AbstractGCEvent.Type.DEF_NEW);
                survivorEvent.setPreUsed((int)(survivorBefore / 1024));
                survivorEvent.setPostUsed((int)(survivorAfter / 1024));
                survivorEvent.setTotal((int)(survivorCapacity / 1024));
                */

                // Since we don't distinguish between survivor spaces and eden, we add things up.
                GCEvent newEvent = new GCEvent();
                newEvent.setType(AbstractGCEvent.Type.DEF_NEW);
                newEvent.setPreUsed((int)((survivorBefore + edenBefore) / 1024));
                newEvent.setPostUsed((int)((survivorAfter + edenAfter) / 1024));
                newEvent.setTotal((int)((survivorCapacity + edenCapacity) / 1024));

                // Old Generation
                // %12:  Before
                // %13:  After
                // %14:  Capacity
                final long oldBefore = Long.parseLong(st.nextToken());
                final long oldAfter = Long.parseLong(st.nextToken());
                final long oldCapacity = Long.parseLong(st.nextToken());
                GCEvent oldEvent = new GCEvent();
                oldEvent.setType(AbstractGCEvent.Type.TENURED);
                oldEvent.setPreUsed((int)(oldBefore / 1024));
                oldEvent.setPostUsed((int)(oldAfter / 1024));
                oldEvent.setTotal((int)(oldCapacity / 1024));

                // Permanent Generation (Storage of Reflective Objects)
                // %15:  Before
                // %16:  After
                // %17:  Capacity
                final long permBefore = Long.parseLong(st.nextToken());
                final long permAfter = Long.parseLong(st.nextToken());
                final long permCapacity = Long.parseLong(st.nextToken());
                GCEvent permEvent = new GCEvent();
                permEvent.setType(AbstractGCEvent.Type.PERM);
                permEvent.setPreUsed((int)(permBefore / 1024));
                permEvent.setPostUsed((int)(permAfter / 1024));
                permEvent.setTotal((int)(permCapacity / 1024));

                // %18:  Time taken in seconds to finish the gc
                final double pause = NumberParser.parseDouble(st.nextToken());
                event.setPause(pause);
                event.setPreUsed(newEvent.getPreUsed() + oldEvent.getPreUsed());
                event.setPostUsed(newEvent.getPostUsed() + oldEvent.getPostUsed());
                event.setTotal(newEvent.getTotal() + oldEvent.getTotal());
                event.add(newEvent);
                if (event.isFull()) {
                    event.add(oldEvent);
                }
                event.add(permEvent);
                model.add(event);
            }
            return model;
        }
        finally {
            if (in != null)
                try {
                    in.close();
                }
                catch (IOException ioe) {
                }
            if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reading done.");
        }
    }

    private Type findType(final int reason) {
        return reason == -1 ? Type.GC : Type.FULL_GC;
    }

}

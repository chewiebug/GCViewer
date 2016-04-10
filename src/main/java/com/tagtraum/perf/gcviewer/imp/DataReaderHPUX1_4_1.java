/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
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
 * DataReader for HP-UX 1.4.1/1.4.2
 *
 * @see <a href="http://www.hp.com/products1/unix/java/infolibrary/prog_guide/xverbosegc_1-4-1.html?jumpid=reg_R1002_USEN">http://www.hp.com/products1/unix/java/infolibrary/prog_guide/xverbosegc_1-4-1.html?jumpid=reg_R1002_USEN</a>
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class DataReaderHPUX1_4_1 extends AbstractDataReader {

    public DataReaderHPUX1_4_1(GCResource gcResource, InputStream in) throws UnsupportedEncodingException {
        super(gcResource, in);
    }

    public GCModel read() throws IOException {
        if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reading HP-UX 1.4.1-1.4.2 format...");
        try {
            final GCModel model = new GCModel();
            model.setFormat(GCModel.Format.SUN_X_LOG_GC);
            String line = null;
            GCEvent event = null;
            while ((line = in.readLine()) != null && shouldContinue()) {
                final StringTokenizer st = new StringTokenizer(line, " ");
                if (st.countTokens() != 22) {
                    if (getLogger().isLoggable(Level.WARNING)) {
                        getLogger().warning("Malformed line (" + in.getLineNumber() + "). Wrong number of tokens ("+st.countTokens()+"): " + line);
                        continue;
                    }
                }
                if (!"<GC:".equals(st.nextToken())) {
                    if (getLogger().isLoggable(Level.WARNING)) getLogger().warning("Malformed line (" + in.getLineNumber() + "). Expected \"<GC:\" in " + line);
                    continue;
                }
                event = new GCEvent();
                /*
                %1:  Indicates the type of the garbage collection.
                        1: represents a Scavenge (GC of New Generation only)
                           %2: indicates if this is a parallel scavenge.
                                0: non-parallel scavenge
                                n(>0): parallel scavenge, n represents the number of
                                       parallel GC threads

                        2: represents an Old Generation GC or a Full GC
                           %2: indicates the GC reason:
                                1: Allocation failure
                                2: Call to System.gc
                                3: Tenured Generation full
                                4: Permanent Generation full
                                5: Train Generation full
                                6: Concurrent-Mark-Sweep (CMS) Generation full
                                7: Old generation expanded on last scavenge
                                8: Old generation too full to scavenge
                                9: FullGCAlot
                               10: Allocation profiler triggered
                               11: Last ditch collection
                                   If the heap area holding the reflection objects (representing classes and methods) is
                                   full, VM first invokes permanent generation collection. If that fails, then it tries to
                                   expand permanent generation.
                           If that also fails, it invokes last ditch collection, to reclaim as much space as possible.

                               12: Heap dump triggered
                               13: gcLocker triggered
                               14: No cause specified

                               (Number 11: "Last ditch collection" is added since 1.4.2.03. Number 12, 13 and 14 are added since 1.4.2.10).

                        3: represents a complete background CMS GC
                           %2:  indicates the GC reason:
                                1: Occupancy > initiatingOccupancy
                                2: Expanded recently
                                3: Incremental collection will fail
                                4: Linear allocation will fail
                                5: Anticipated promotion

                        4: represents an incomplete background CMS GC
                              (exited after yielding to foreground GC)
                           %2:  n.m
                                n indicates the GC reason:
                                  1: Occupancy > initiatingOccupancy
                                  2: Expanded recently
                                  3: Incremental collection will fail
                                  4: Linear allocation will fail
                                  5: Anticipated promotion
                                m indicates the background CMS state when yielding:
                                  0: Resetting
                                  1: Idling
                                  2: InitialMarking
                                  3: Marking
                                  4: FinalMarking
                                  5: Precleaning
                                  6: Sweeping
                */
                final int typeOfGC = Integer.parseInt(st.nextToken());
                // %2:  see above
                final float gcDetails = Float.parseFloat(st.nextToken());
                event.setType(findType(typeOfGC, gcDetails));
                // %3:  Program time at the beginning of the collection, in seconds
                event.setTimestamp(NumberParser.parseDouble(st.nextToken()));
                // %4:  Garbage collection invocation. Counts of background CMS GCs
                // and other GCs are maintained separately
                st.nextToken();
                // %5:  Size of the object allocation request that forced the GC, in bytes
                st.nextToken();
                // %6:  Tenuring threshold - determines how long the new born object
                // remains in the New Generation
                st.nextToken();
                // Eden Sub-space (within the New Generation)
                // %7:  Before
                // %8:  After
                // %9:  Capacity
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
                // %10:   Before
                // %11:  After
                // %12:  Capacity
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
                final GCEvent newEvent = new GCEvent();
                newEvent.setType(AbstractGCEvent.Type.DEF_NEW);
                newEvent.setPreUsed((int)((survivorBefore + edenBefore) / 1024));
                newEvent.setPostUsed((int)((survivorAfter + edenAfter) / 1024));
                newEvent.setTotal((int)((survivorCapacity + edenCapacity) / 1024));

                // Old Generation
                // %13:  Before
                // %14:  After
                // %15:  Capacity
                final long oldBefore = Long.parseLong(st.nextToken());
                final long oldAfter = Long.parseLong(st.nextToken());
                final long oldCapacity = Long.parseLong(st.nextToken());
                final GCEvent oldEvent = new GCEvent();
                oldEvent.setType(AbstractGCEvent.Type.TENURED);
                oldEvent.setPreUsed((int)(oldBefore / 1024));
                oldEvent.setPostUsed((int)(oldAfter / 1024));
                oldEvent.setTotal((int)(oldCapacity / 1024));

                // Permanent Generation (Storage of Reflective Objects)
                // %16:  Before
                // %17:  After
                // %18:  Capacity
                final long permBefore = Long.parseLong(st.nextToken());
                final long permAfter = Long.parseLong(st.nextToken());
                final long permCapacity = Long.parseLong(st.nextToken());
                final GCEvent permEvent = new GCEvent();
                permEvent.setType(AbstractGCEvent.Type.PERM);
                permEvent.setPreUsed((int)(permBefore / 1024));
                permEvent.setPostUsed((int)(permAfter / 1024));
                permEvent.setTotal((int)(permCapacity / 1024));

                // %19:  The total stop-the-world duration, in seconds.
                final double pause = NumberParser.parseDouble(st.nextToken());
                event.setPause(pause);
                // %20:  The total time used in collection, in seconds.
                // ignore for now
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

    private Type findType(final int typeOfGC, final float details) {
        final Type type;
        switch (typeOfGC) {
            case 1:
                if (details == 0) {
                    type = Type.GC;
                    break;
                }
                type = Type.PAR_NEW;
                break;
            case 2:
                type = Type.FULL_GC;
                break;
            case 3:
                type = Type.CMS;
                break;
            case 4:
                type = Type.CMS;
                break;
            default:
                type = Type.FULL_GC;
        }
        return type;
    }

}

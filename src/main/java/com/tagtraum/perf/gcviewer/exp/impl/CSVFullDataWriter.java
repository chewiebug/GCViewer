package com.tagtraum.perf.gcviewer.exp.impl;

import com.tagtraum.perf.gcviewer.exp.AbstractDataWriter;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class CSVFullDataWriter extends AbstractDataWriter {

    public CSVFullDataWriter(OutputStream out) {
        super(out);
    }

    private void writeHeader() {
        out.println("Time," +                   //0
                "Pause(sec)," +                 //1
                "GC-Type," +                    //2
                "Simple-GC-Type," +             //3
                "generation, " +                //4
                "YoungSizeBefore(B)," +         //5
                "YoungSizeAfter(B)," +          //6
                "YoungSizeMax(K)," +            //7
                "YoungCollected(B)," +          //8
                "YoungAllocated(B)," +          //9
                "AllocationRate(B/sec)," +      //10
                "TotalSizeBefore(B)," +         //11
                "TotalSizeAfter(B)," +          //12
                "TotalSizeMax(B)," +            //13
                "TotalCollected(B)," +          //14
                "Promoted(B)," +                //15
                "PromotionRate(B/sec)," +       //16
                "TenuredSizeBefore(B)," +       //17
                "TenuredSizeAfter(B)," +        //18
                "TenuredSizeTotal(B)" +         //19
                "MetaSizeBefore(B)," +          //20
                "MetaSizeAfter(B)," +           //21
                "MetaSizeTotal(B)"              //22
        );

    }

    /**
     * Writes the model and flushes the internal PrintWriter.
     */
    public void write(GCModel model) throws IOException {
        writeHeader();

        long lastYoungUsed = 0;
        double lastTime = 0;
        for (Iterator<AbstractGCEvent<?>> i = model.getStopTheWorldEvents(); i.hasNext(); ) {
            AbstractGCEvent<?> abstractGCEvent = i.next();
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
                out.print(event.getPause());//1
                out.print(',');
                out.print(event.getExtendedType());//2
                out.print(',');
                out.print(SimpleGcWriter.getSimpleType(event));//3
                out.print(',');
                out.print(event.getGeneration());//4

                long youngCollected = -1;
                GCEvent youngEvent = event.getYoung();

                if (youngEvent == null) {
                    out.print(",-1,-1,-1,-1.-1,-1");
                } else {
                    if (youngEvent.getTotal() > 0) {
                        out.print(',');
                        writeAsBytes(youngEvent.getPreUsed());

                        out.print(',');
                        writeAsBytes(youngEvent.getPostUsed());

                        out.print(',');
                        writeAsBytes(youngEvent.getTotal());

                        out.print(',');
                        youngCollected = youngEvent.getPreUsed() - youngEvent.getPostUsed();
                        writeAsBytes(youngCollected);
                    } else {
                        out.print(",-1,-1,-1,-1");
                    }

                    out.print(',');
                    long youngAllocated = youngEvent.getPreUsed() - lastYoungUsed;
                    //https://plumbr.eu/blog/garbage-collection/what-is-allocation-rate
                    writeAsBytes(youngAllocated);
                    out.print(',');

                    double duration = event.getTimestamp() - lastTime;
                    double allocationRate = youngAllocated/duration;

                    out.print(1000 * allocationRate);
                }

                out.print(',');
                writeAsBytes(event.getPreUsed());
                out.print(',');
                writeAsBytes(event.getPostUsed());
                out.print(',');
                writeAsBytes(event.getTotal());
                out.print(',');
                long totalCollected = event.getPreUsed() - event.getPostUsed();
                writeAsBytes(totalCollected);

                if (youngEvent.getPostUsed() == 0) {
                    out.print(",-1,-1");
                } else {
                    out.print(',');

                    // https://plumbr.eu/blog/garbage-collection/what-is-promotion-rate
                    long promotedSize = youngCollected - totalCollected;
                    writeAsBytes(promotedSize);//15

                    double duration = event.getTimestamp() - lastTime;

                    out.print(',');          //16
                    double promotionRate = promotedSize / duration;
                    out.print(1000 * promotionRate);
                }

                // todo: promotion rate can be calculated based on the previous entry time
                GCEvent tenured = event.getTenured();
                if (tenured == null || tenured.getTotal() == 0) {
                    out.print(",-1,-1,-1");
                } else {
                    out.print(',');
                    writeAsBytes(tenured.getPreUsed());
                    out.print(',');
                    writeAsBytes(tenured.getPostUsed());
                    out.print(',');
                    writeAsBytes(tenured.getTotal());
                }

                GCEvent perm = event.getPerm();
                if (perm == null) {
                    out.print(",-1,-1,-1");
                } else {
                    out.print(',');
                    writeAsBytes(perm.getPreUsed());
                    out.print(',');
                    writeAsBytes(perm.getPostUsed());
                    out.print(',');
                    writeAsBytes(perm.getTotal());
                }
                out.println();
                lastYoungUsed = youngEvent.getPostUsed();
                lastTime = event.getTimestamp();
            }
        }
        out.flush();
    }

    private void writeAsBytes(long kb) {
        out.print(1000L * kb);
    }

}

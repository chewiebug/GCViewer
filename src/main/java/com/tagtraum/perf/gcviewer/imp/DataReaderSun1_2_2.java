package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;

/**
 * Parses -verbose:gc output from Sun JDK 1.2.2.
 *
 * Date: Jan 30, 2002
 * Time: 5:15:44 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class DataReaderSun1_2_2 extends AbstractDataReader {

    public DataReaderSun1_2_2(GCResource gcResource, InputStream in) throws UnsupportedEncodingException {
        super(gcResource, in);
    }

    public GCModel read() throws IOException {
        if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reading Sun 1.2.2 format...");
        try {
            GCModel model = new GCModel();
            model.setFormat(GCModel.Format.SUN_1_2_2VERBOSE_GC);
            String line = null;
            boolean timeline = false;
            AbstractGCEvent<GCEvent> lastEvent = new GCEvent();
            GCEvent event = null;
            while ((line = in.readLine()) != null && shouldContinue()) {
                if (!timeline) {
                    if (line.endsWith("milliseconds since last GC>")) {
                        timeline = true;
                        double time = Integer.parseInt(line.substring(5, line.indexOf(' ', 5)));
                        event = new GCEvent();
                        event.setTimestamp(lastEvent.getTimestamp() + (time/1000.0d));
                    }
                }
                else {
                    timeline = false;
                    // we have a time, so now we expect a either expansion or freed objects
                    if (line.indexOf("expanded object space by") != -1) {
                        // expansion
                        int endIndex = line.indexOf(' ', "<GC: expanded object space by ".length());
                        //int incBy = Integer.parseInt(line.substring("<GC: expanded object space by ".length(), endIndex));
                        int beginIndex = endIndex + " to ".length();
                        int incTo = Integer.parseInt(line.substring(beginIndex, line.indexOf(' ', beginIndex)));
                        int percentUsed = Integer.parseInt(line.substring(line.length() - "XX% free>".length(), line.length() - "% free>".length()));
                        event.setPostUsed((int)((incTo * percentUsed / 1024L / 100l)));
                        event.setPreUsed(event.getPostUsed());
                        event.setTotal((int)(incTo / 1024L));
                        event.setType(AbstractGCEvent.Type.GC);
                        event.setPause(0);
                        model.add(event);
                        lastEvent = event;
                    }
                    else if (line.indexOf(" freed ") != -1 && line.indexOf(" objects, ") != -1) {
                        // freed objects
                        int startIndex = line.indexOf(',') + 2;
                        int endIndex = line.indexOf(' ', startIndex);
                        int freed = Integer.parseInt(line.substring(startIndex, endIndex));
                        startIndex = line.indexOf("in ") + 3;
                        endIndex = line.indexOf(' ', startIndex);
                        int pause = Integer.parseInt(line.substring(startIndex, endIndex));
                        startIndex = line.indexOf('(') + 1;
                        endIndex = line.indexOf('/', startIndex);
                        int postFree = Integer.parseInt(line.substring(startIndex, endIndex));
                        startIndex = line.indexOf('/') + 1;
                        endIndex = line.indexOf(')', startIndex);
                        int total = Integer.parseInt(line.substring(startIndex, endIndex));

                        event.setPostUsed((total - postFree) / 1024);
                        event.setPreUsed((total - postFree + freed) / 1024);
                        //event.setPostUsed(event.getPreUsed());
                        event.setTotal(total / 1024);
                        event.setType(AbstractGCEvent.Type.GC);
                        event.setPause((pause) / 1000.0d);
                        model.add(event);
                        lastEvent = event;
                        /*
                        event = new GCEvent();
                        event.setTimestamp(lastEvent.getTimestamp() + lastEvent.getPause());
                        event.setPostUsed((total - postFree) / 1024L);
                        event.setPreUsed(lastEvent.getPostUsed());
                        event.setTotal(total / 1024L);
                        event.setType(GCEvent.Type.GC);
                        event.setPause(0);
                        model.add(event);
                        lastEvent = event;
                        */
                    }
                    else {
                        // hm. what now...?
                    }
                }
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
            if (getLogger().isLoggable(Level.INFO)) getLogger().info("Done reading.");
        }
    }


}

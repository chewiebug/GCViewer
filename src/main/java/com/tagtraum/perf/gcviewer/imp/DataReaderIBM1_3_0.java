package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Parses -verbose:gc output from IBM JDK 1.3.0.
 *
 * Date: Jan 30, 2002
 * Time: 5:15:44 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class DataReaderIBM1_3_0 implements DataReader {

    private static Logger LOG = Logger.getLogger(DataReaderIBM1_3_0.class.getName());

    private LineNumberReader in;

    public DataReaderIBM1_3_0(InputStream in) {
        this.in = new LineNumberReader(new InputStreamReader(in));
    }

    public GCModel read() throws IOException {
        if (LOG.isLoggable(Level.INFO)) LOG.info("Reading IBM 1.3.0 format...");
        try {
            GCModel model = new GCModel(true);
            model.setFormat(GCModel.Format.IBM_VERBOSE_GC);
            int state = 0;
            String line = null;
            AbstractGCEvent<GCEvent> lastEvent = new GCEvent();
            GCEvent event = null;
            while ((line = in.readLine()) != null) {
                String trimmedLine = line.trim();
                if ((!trimmedLine.equals("")) && (!trimmedLine.startsWith("<GC: ")) && (!(trimmedLine.startsWith("<") && trimmedLine.endsWith(">")))) {
                    if (LOG.isLoggable(Level.WARNING)) LOG.warning("Malformed line (" + in.getLineNumber() + "): " + line);
                    state = 0;
                }
                switch (state) {
                    case 0:
                        if (line.indexOf("Allocation Failure.") != -1) {
                            event = new GCEvent();
                            event.setType(AbstractGCEvent.Type.FULL_GC);
                            event.setTimestamp(lastEvent.getTimestamp() + parseTimeSinceLastAF(line));
                            state++;
                            break;
                        }
                    case 1:
                        if (line.indexOf("managing allocation failure, action=1") != -1) {
                            event.setPreUsed(parsePreUsed(line));
                            state++;
                            break;
                        }
                    case 2:
                        if (line.indexOf("freed") != -1 && line.indexOf("unloaded") == -1) {
                            event.setPostUsed(parsePostUsed(line));
                            event.setTotal(parseTotalAfterGC(line));
                            state++;
                            break;
                        }
                    case 3:
                        if (line.indexOf("expanded heap by ") != -1) {
                            event.setTotal(parseTotalAfterHeapExpansion(line));
                            state++;
                            break;
                        }
                    case 4:
                        if (line.indexOf("completed in ") != -1) {
                            event.setPause(parsePause(line));
                            model.add(event);
                            lastEvent = event;
                            event = null;
                            state = 0;
                        }
                    default:
                }
            }
            return model;
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            if (LOG.isLoggable(Level.INFO)) LOG.info("Reading done.");
        }
    }

    private double parseTimeSinceLastAF(String line) {
        int start = line.indexOf(',') + 2;
        int end = line.indexOf(' ', start);
        return Double.parseDouble(line.substring(start, end)) / 1000.0d;
    }

    private int parsePreUsed(String line) {
        int start = line.indexOf('(') + 1;
        int end = line.indexOf(')', start);
        int mid = line.indexOf('/', start);
        long a = (Long.parseLong(line.substring(mid + 1, end)) - Long.parseLong(line.substring(start, mid)));
        start = line.indexOf('(', start) + 1;
        end = line.indexOf(')', start);
        mid = line.indexOf('/', start);
        long b = (Long.parseLong(line.substring(mid + 1, end)) - Long.parseLong(line.substring(start, mid)));
        return (int)((a+b) / 1024l);
    }

    private int parsePostUsed(String line) {
        int start = line.indexOf('(', line.indexOf("freed")) + 1;
        int end = line.indexOf(')', start);
        int mid = line.indexOf('/', start);
        return (int)((Long.parseLong(line.substring(mid + 1, end)) - Long.parseLong(line.substring(start, mid))) / 1024L);
    }

    private int parseTotalAfterGC(String line) {
        int start = line.indexOf('/') + 1;
        int end = line.indexOf(')', start);
        return (int)(Long.parseLong(line.substring(start, end)) / 1024L);
    }

    private int parseTotalAfterHeapExpansion(String line) {
        int start = line.indexOf("to ") + 3;
        int end = line.indexOf(' ', start);
        return (int)(Long.parseLong(line.substring(start, end)) / 1024L);
    }

    private double parsePause(String line) {
        int start = line.indexOf("in ") + 3;
        int end = line.indexOf(' ', start);
        return Double.parseDouble(line.substring(start, end)) / 1000.0d;
    }
}

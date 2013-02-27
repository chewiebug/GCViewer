package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.ParsePosition;

/**
 * Parses -verbose:gc output from Sun JDK 1.3.1.
 *
 * Date: Jan 30, 2002
 * Time: 5:15:44 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class DataReaderSun1_3_1 extends AbstractDataReaderSun implements DataReader {

    private static Logger LOG = Logger.getLogger(DataReaderSun1_3_1.class.getName());
    
    private int count;

    public DataReaderSun1_3_1(InputStream in, GcLogType gcLogType) throws UnsupportedEncodingException {
        super(in, gcLogType);
    }

    public GCModel read() throws IOException {
        if (LOG.isLoggable(Level.INFO)) LOG.info("Reading Sun 1.3.1 format...");
        try {
            count = 0;
            GCModel model = new GCModel(true);
            model.setFormat(GCModel.Format.SUN_VERBOSE_GC);
            List<StringBuilder> lineStack = new ArrayList<StringBuilder>();
            int i;
            StringBuilder line = null;
            while ((i = in.read()) != -1) {
                char c = (char) i;
                if (c == '[') {
                    if (line != null) lineStack.add(line); // push
                    line = new StringBuilder(64);
                } else if (c == ']') {
                    try {
                        model.add(parseLine(line.toString(), null));
                    } catch (ParseException e) {
                        if (LOG.isLoggable(Level.WARNING)) LOG.log(Level.WARNING, e.getMessage(), e);
                        System.out.println(e.getMessage());
                    }
                    if (!lineStack.isEmpty()) {
                        line = lineStack.remove(lineStack.size() - 1); // pop
                    }
                } else {
                    if (line != null) line.append(c);
                }
            }
            return model;
        } finally {
            if (in != null) try {
                in.close();
            } catch (IOException ioe) {
            }
            if (LOG.isLoggable(Level.INFO)) LOG.info("Done reading.");
        }
    }

    protected AbstractGCEvent<GCEvent> parseLine(String line, ParsePosition pos) throws ParseException {
        AbstractGCEvent<GCEvent> event = new GCEvent();
        try {
            event.setTimestamp((long) count);
            count++;
            StringTokenizer st = new StringTokenizer(line, " ,->()K\r\n");
            String token = st.nextToken();
            if (token.equals("Full") && st.nextToken().equals("GC")) {
                event.setType(AbstractGCEvent.Type.FULL_GC);
            } else if (token.equals("Inc") && st.nextToken().equals("GC")) {
                event.setType(AbstractGCEvent.Type.INC_GC);
            } else if (token.equals("GC")) {
                event.setType(AbstractGCEvent.Type.GC);
            } else {
                throw new ParseException("Error parsing entry: " + line);
            }
            setMemoryAndPauses((GCEvent)event, line);
            // debug
            //System.out.println("Parsed: " + event);
            //System.out.println("Real  : [" + line + "]");
            return event;
        } catch (RuntimeException rte) {
            final ParseException parseException = new ParseException("Error parsing entry: " + line + ", " + rte.toString());
            parseException.initCause(rte);
            throw parseException;
        }
    }

}

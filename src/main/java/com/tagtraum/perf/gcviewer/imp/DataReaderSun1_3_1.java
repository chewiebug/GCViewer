package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.ParseInformation;

/**
 * Parses -verbose:gc output from Sun JDK 1.3.1.
 *
 * Date: Jan 30, 2002
 * Time: 5:15:44 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class DataReaderSun1_3_1 extends AbstractDataReaderSun {

    private int count;

    public DataReaderSun1_3_1(GCResource gcResource, InputStream in, GcLogType gcLogType) throws UnsupportedEncodingException {
        super(gcResource, in, gcLogType);
    }

    public GCModel read() throws IOException {
        if (getLogger().isLoggable(Level.INFO)) getLogger().info("Reading Sun 1.3.1 format...");
        try {
            count = 0;
            GCModel model = new GCModel();
            model.setFormat(GCModel.Format.SUN_VERBOSE_GC);
            List<StringBuilder> lineStack = new ArrayList<StringBuilder>();
            int i;
            StringBuilder line = null;
            while ((i = in.read()) != -1 && shouldContinue()) {
                char c = (char) i;
                if (c == '[') {
                    if (line != null) lineStack.add(line); // push
                    line = new StringBuilder(64);
                }
                else if (c == ']') {
                    try {
                        model.add(parseLine(line.toString(), null));
                    }
                    catch (ParseException e) {
                        if (getLogger().isLoggable(Level.WARNING)) getLogger().log(Level.WARNING, e.getMessage(), e);
                        e.printStackTrace();
                    }
                    if (!lineStack.isEmpty()) {
                        line = lineStack.remove(lineStack.size() - 1); // pop
                    }
                }
                else {
                    if (line != null) line.append(c);
                }
            }
            return model;
        }
        finally {
            if (in != null) try {
                in.close();
            }
            catch (IOException ioe) {
            }
            if (getLogger().isLoggable(Level.INFO)) getLogger().info("Done reading.");
        }
    }

    protected AbstractGCEvent<GCEvent> parseLine(String line, ParseInformation pos) throws ParseException {
        AbstractGCEvent<GCEvent> event = new GCEvent();
        try {
            event.setTimestamp(count);
            count++;
            StringTokenizer st = new StringTokenizer(line, " ,->()K\r\n");
            String token = st.nextToken();
            if (token.equals("Full") && st.nextToken().equals("GC")) {
                event.setType(AbstractGCEvent.Type.FULL_GC);
            }
            else if (token.equals("Inc") && st.nextToken().equals("GC")) {
                event.setType(AbstractGCEvent.Type.INC_GC);
            }
            else if (token.equals("GC")) {
                event.setType(AbstractGCEvent.Type.GC);
            }
            else {
                throw new ParseException("Error parsing entry: " + line);
            }
            setMemoryAndPauses((GCEvent)event, line);
            // debug
            //System.out.println("Parsed: " + event);
            //System.out.println("Real  : [" + line + "]");
            return event;
        }
        catch (RuntimeException rte) {
            final ParseException parseException = new ParseException("Error parsing entry: " + line + ", " + rte.toString());
            parseException.initCause(rte);
            throw parseException;
        }
    }

}

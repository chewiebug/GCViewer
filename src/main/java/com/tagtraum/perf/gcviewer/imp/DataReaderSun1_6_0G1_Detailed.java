package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tagtraum.perf.gcviewer.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.AbstractGCEvent.GcPattern;
import com.tagtraum.perf.gcviewer.G1GcEvent;
import com.tagtraum.perf.gcviewer.GCEvent;
import com.tagtraum.perf.gcviewer.GCModel;
import com.tagtraum.perf.gcviewer.util.ParsePosition;

public class DataReaderSun1_6_0G1_Detailed extends DataReaderSun1_6_0G1 {

    private static Logger LOG = Logger.getLogger(DataReaderSun1_6_0G1.class .getName());

    // "0.295: [GC pause (young), 0.00594747 secs]"
    private static final Pattern PATTERN_GC_PAUSE = Pattern.compile("^([0-9.]+)[: \\[]{3}([A-Za-z- ()]+)[, ]+([0-9.]+)[ sec\\]]+$");
    // "   [ 4096K->3936K(16M)]"
    private static final Pattern PATTERN_MEMORY = Pattern.compile("^[ \\[]{5}[0-9]+[KMG].*");

    private static final int GC_TIMESTAMP = 1;
    private static final int GC_TYPE = 2;
    private static final int GC_PAUSE = 3;

    public DataReaderSun1_6_0G1_Detailed(InputStream in)
            throws UnsupportedEncodingException {
        super(in);
    }

    @Override
    public GCModel read() throws IOException {
        if (LOG.isLoggable(Level.INFO))
            LOG.info("Reading Sun 1.6.x G1 (PrintGcDetails) format...");

        try {
            final GCModel model = new GCModel(true);
            // TODO what is this for?
            model.setFormat(GCModel.Format.SUN_X_LOG_GC);
            String line;
            final ParsePosition parsePosition = new ParsePosition(0);
            boolean isInDetailedEvent = false;
            Matcher gcPauseMatcher = PATTERN_GC_PAUSE.matcher("");
            Matcher memoryMatcher = PATTERN_MEMORY.matcher("");
            GCEvent gcEvent = null;
            int lineNumber = 0;
            while ((line = in.readLine()) != null) {
                ++lineNumber;
                parsePosition.setLineNumber(lineNumber);
                // the following case is special for -XX:+PrintGCDetails and must be treated
                // different from the other cases occuring in G1 standard mode
                // 0.356: [GC pause (young), 0.00219944 secs] -> GC_PAUSE pattern but GC_MEMORY_PAUSE 
                //   event (has extensive details)
                // all other GC types are the same as in standard G1 mode.
                try {
                    if (!isInDetailedEvent) {
                        gcPauseMatcher.reset(line);
                        if (gcPauseMatcher.matches()) {
                            AbstractGCEvent.Type type = AbstractGCEvent.Type.parse(gcPauseMatcher.group(GC_TYPE));
                            if (type.getPattern().compareTo(GcPattern.GC_MEMORY_PAUSE) == 0) {
                                // detailed G1 events start with GC_MEMORY pattern, but are of type GC_MEMORY_PAUSE
                                isInDetailedEvent = true;
    
                                gcEvent = new G1GcEvent();
                                gcEvent.setTimestamp(Double.parseDouble(gcPauseMatcher.group(GC_TIMESTAMP)));
                                gcEvent.setType(type);
                                gcEvent.setPause(Double.parseDouble(gcPauseMatcher.group(GC_PAUSE)));
                            }
                            else {
                                model.add(parseLine(line, parsePosition));
                            }
                        }
                        else if (line.indexOf("Times") < 0) {
                            model.add(parseLine(line, parsePosition));
                        }
                    }
                    else {
                        // now we parse details of a pause
                        // currently everything except memory is skipped
                        memoryMatcher.reset(line);
                        if (memoryMatcher.matches()) {
                            setMemory(gcEvent, line);
                        }
                        
                    }
                    if (line.indexOf("Times") >= 0) {
                        // detailed gc description ends with " [Times: user=...]" -> finalize gcEvent
                        // otherwise just ignore 
                        if (gcEvent != null) {
                            model.add(gcEvent);
                            gcEvent = null;
                            isInDetailedEvent = false;
                        }
                    }
                    parsePosition.setIndex(0);
                } catch (Exception pe) {
                    if (LOG.isLoggable(Level.WARNING)) LOG.log(Level.WARNING, pe.getMessage(), pe);
                    if (LOG.isLoggable(Level.FINE)) LOG.log(Level.FINE, pe.getMessage(), pe);
                }
            }
            return model;
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            if (LOG.isLoggable(Level.INFO))
                LOG.info("Done reading.");
        }
    }

}

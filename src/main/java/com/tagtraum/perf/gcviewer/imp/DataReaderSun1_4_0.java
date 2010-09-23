package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.DataReader;
import com.tagtraum.perf.gcviewer.GCEvent;
import com.tagtraum.perf.gcviewer.GCModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Parses -verbose:gc output from Sun JDK 1.4.0.
 *
 * Date: Jan 30, 2002
 * Time: 5:15:44 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class DataReaderSun1_4_0 extends AbstractDataReaderSun implements DataReader {

    private static Logger LOG = Logger.getLogger(DataReaderSun1_4_0.class.getName());
    private static final String UNLOADING_CLASS = "[Unloading class ";
    private static final String DESIRED_SURVIVOR = "Desired survivor";
    private static final String APPLICATION_TIME = "Application time:";
    private static final String TOTAL_TIME_THREADS_STOPPED = "Total time for which application threads were stopped:";
    private static final String SURVIVOR_AGE = "- age";
    private static final HashSet EXCLUDE_STRINGS = new HashSet();

    static {
        EXCLUDE_STRINGS.add(UNLOADING_CLASS);
        EXCLUDE_STRINGS.add(DESIRED_SURVIVOR);
        EXCLUDE_STRINGS.add(APPLICATION_TIME);
        EXCLUDE_STRINGS.add(TOTAL_TIME_THREADS_STOPPED);
        EXCLUDE_STRINGS.add(SURVIVOR_AGE);
    }
    private Pattern unloadingClassPattern = Pattern.compile(".*\\[Unloading class [^\\]]+\\]$");

    public DataReaderSun1_4_0(final InputStream in) throws UnsupportedEncodingException {
        super(in);
    }

    public GCModel read() throws IOException {
        if (LOG.isLoggable(Level.INFO)) LOG.info("Reading Sun 1.4.x/1.5.x format...");
        try {
            final GCModel model = new GCModel(true);
            model.setFormat(GCModel.Format.SUN_X_LOG_GC);
            String line;
            String beginningOfLine = null;
            final ParsePosition parsePosition = new ParsePosition(0);
            OUTERLOOP:
            while ((line = in.readLine()) != null) {
                try {
                    // filter out [Unloading class com.xyz] statements
                    for (Iterator i=EXCLUDE_STRINGS.iterator(); i.hasNext(); ) {
                        final String s = (String)i.next();
                        if (line.indexOf(s) == 0) continue OUTERLOOP;
                    }
                    final int unloadingClassIndex = line.indexOf(UNLOADING_CLASS);
                    if (unloadingClassPattern.matcher(line).matches()) {
                        beginningOfLine = line.substring(0, unloadingClassIndex);
                        continue;
                    } else if (line.endsWith("[DefNew") || line.endsWith("[ParNew")) {
                        beginningOfLine = line;
                        continue;
                    }
                    else if (beginningOfLine != null) {
                        line = beginningOfLine + line;
                        beginningOfLine = null;
                    }
                    model.add(parseLine(line, parsePosition));
                } catch (ParseException pe) {
                    if (LOG.isLoggable(Level.WARNING)) LOG.warning(pe.getMessage());
                    if (LOG.isLoggable(Level.FINE)) LOG.log(Level.FINE, pe.getMessage(), pe);
                }
                // reset ParsePosition
                parsePosition.setIndex(0);
            }
            return model;
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            if (LOG.isLoggable(Level.INFO)) LOG.info("Done reading.");
        }
    }


    protected AbstractGCEvent parseLine(final String line, final ParsePosition pos) throws ParseException {
        try {
            // parse timestamp          "double:"
            // parse collection type    "[TYPE"
            // either GC data or another collection type starting with timestamp
            // pre-used->post-used, total, time
            final GCEvent event = new GCEvent();
            final double timestamp = parseTimestamp(line, pos);
            event.setTimestamp(timestamp);
            event.setType(parseType(line, pos));
            // now add detail gcevents, should they exist
            while (hasNextDetail(line, pos)) {
                final GCEvent detailEvent = new GCEvent();
                if (nextCharIsBracket(line, pos)) {
                    detailEvent.setTimestamp(timestamp);
                }
                else {
                    detailEvent.setTimestamp(parseTimestamp(line, pos));
                }
                detailEvent.setType(parseType(line, pos));
                setMemoryAndPauses(detailEvent, line, pos);
                event.add(detailEvent);
            }
            setMemoryAndPauses(event, line, pos);
            if (event.getPause() == 0) {
                if (hasNextDetail(line, pos)) {
                    final GCEvent detailEvent = new GCEvent();
                    if (nextCharIsBracket(line, pos)) {
                        detailEvent.setTimestamp(timestamp);
                    }
                    else {
                        detailEvent.setTimestamp(parseTimestamp(line, pos));
                    }
                    detailEvent.setType(parseType(line, pos));
                    setMemoryAndPauses(detailEvent, line, pos);
                    event.add(detailEvent);
                }
                    setPause(event, line, pos);
            }
            return event;
        } catch (RuntimeException rte) {
            if (LOG.isLoggable(Level.FINE)) LOG.log(Level.FINE, rte.toString(), rte);
            throw new ParseException("Error parsing entry, " + rte.toString(), line);
        }
    }

}

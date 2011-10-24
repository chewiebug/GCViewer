package com.tagtraum.perf.gcviewer.imp;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tagtraum.perf.gcviewer.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.AbstractGCEvent.Concurrency;
import com.tagtraum.perf.gcviewer.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.GCEvent;

/**
 * Parses -Xloggc: output from Sun JDK 1.5.0.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class DataReaderSun1_5_0 extends DataReaderSun1_4_0 {

    private static Logger LOG = Logger.getLogger(DataReaderSun1_5_0.class.getName());

    public DataReaderSun1_5_0(final InputStream in) throws UnsupportedEncodingException {
        super(in);
    }

    protected AbstractGCEvent parseLine(final String line, final ParsePosition pos) throws ParseException {
        AbstractGCEvent ae = null;
        try {
            // parse timestamp          "double:"
            // parse collection type    "[TYPE"
            // either GC data or another collection type starting with timestamp
            // pre-used->post-used, total, time
            final double timestamp = parseTimestamp(line, pos);
            final GCEvent.Type type = parseType(line, pos);
            // special provision for CMS events
            if (type.getConcurrency() == Concurrency.CONCURRENT) {
                if (type.toString().endsWith("-start")) {
                    final ConcurrentGCEvent event = new ConcurrentGCEvent();
                    event.setTimestamp(timestamp);
                    event.setType(type);
                    ae = event;
                    // nothing more to parse...
                } else {
                    final ConcurrentGCEvent event = new ConcurrentGCEvent();
                    event.setTimestamp(timestamp);
                    event.setType(type);

                    int start = pos.getIndex();
                    int end = line.indexOf('/', pos.getIndex());
                    event.setPause(Double.parseDouble(line.substring(start, end)));
                    start = end + 1;
                    end = line.indexOf(' ', start);
                    ((ConcurrentGCEvent) event).setDuration(Double.parseDouble(line.substring(start, end)));
                    // nothing more to parse
                    ae = event;
                }
            } else {
                final GCEvent event = new GCEvent();
                event.setTimestamp(timestamp);
                event.setType(type);
                final ParsePosition recoverPos = new ParsePosition(pos.getIndex());
                try {
                    // now add detail gcevents, should they exist
                    while (hasNextDetail(line, pos)) {
                        final GCEvent detailEvent = new GCEvent();
                        if (nextCharIsBracket(line, pos)) {
                            detailEvent.setTimestamp(timestamp);
                        } else {
                            detailEvent.setTimestamp(parseTimestamp(line, pos));
                        }
                        detailEvent.setType(parseType(line, pos));
                        setMemoryAndPauses(detailEvent, line, pos);
                        event.add(detailEvent);
                    }
                } catch (ParseException e) {
                    pos.setIndex(recoverPos.getIndex());
                    skipDetails(line, pos);
                    // hack to add tenured detail event CMS_REMARK
                    if (line.indexOf(AbstractGCEvent.Type.CMS_REMARK.getType()) != -1) {
                        final GCEvent detailEvent = new GCEvent();
                        detailEvent.setTimestamp(timestamp);
                        detailEvent.setType(AbstractGCEvent.Type.CMS_REMARK);
                        event.add(detailEvent);
                    }
                    else {
                        if (LOG.isLoggable(Level.INFO)) LOG.info("Skipping details in line \"" + line + "\" because of " + e);
                    }

                }
                setMemoryAndPauses(event, line, pos);
                if (event.getPause() == 0) {
                    if (hasNextDetail(line, pos)) {
                        final GCEvent detailEvent = new GCEvent();
                        if (nextCharIsBracket(line, pos)) {
                            detailEvent.setTimestamp(timestamp);
                        } else {
                            detailEvent.setTimestamp(parseTimestamp(line, pos));
                        }
                        detailEvent.setType(parseType(line, pos));
                        setMemoryAndPauses(detailEvent, line, pos);
                        event.add(detailEvent);
                    }
                    parsePause(event, line, pos);
                }
                ae = event;
            }
            return ae;
        } catch (RuntimeException rte) {
            if (LOG.isLoggable(Level.WARNING)) LOG.log(Level.WARNING, rte.toString() + " while parsing line: " + line, rte);
            throw new ParseException("Error parsing entry: " + line + ", " + rte.toString());
        }
    }

}

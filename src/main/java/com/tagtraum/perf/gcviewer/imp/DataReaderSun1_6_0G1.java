package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tagtraum.perf.gcviewer.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.AbstractGCEvent.Concurrency;
import com.tagtraum.perf.gcviewer.AbstractGCEvent.GcPattern;
import com.tagtraum.perf.gcviewer.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.DataReader;
import com.tagtraum.perf.gcviewer.GCEvent;
import com.tagtraum.perf.gcviewer.GCModel;

public class DataReaderSun1_6_0G1 extends AbstractDataReaderSun implements DataReader {

    private static Logger LOG = Logger.getLogger(DataReaderSun1_6_0G1.class.getName());

    // G1 log output in 1.6.0_u25 sometimes starts a new line somewhere in line being written
    // the pattern is always "...)<timestamp>"
    private static Pattern linesMixedPattern = Pattern.compile("(.*\\))([0-9.]+.*)"); 

    public DataReaderSun1_6_0G1(InputStream in) throws UnsupportedEncodingException {
		super(in);
	}

	@Override
	public GCModel read() throws IOException {
        if (LOG.isLoggable(Level.INFO)) LOG.info("Reading Sun 1.6.x G1 format...");
        
        try {
            final GCModel model = new GCModel(true);
            model.setFormat(GCModel.Format.SUN_X_LOG_GC);
            String line;
            String beginningOfLine = null;
            final ParsePosition parsePosition = new ParsePosition(0);
            while ((line = in.readLine()) != null) {
                try {
                	// if a new timestamp occurs in the middle of a line, that should be treated as a new line
                	// -> the rest of the old line appears on the next line
                	Matcher mixedLine = linesMixedPattern.matcher(line);
                    if (mixedLine.matches()) {
                    	beginningOfLine = mixedLine.group(1);
                    	model.add(parseLine(mixedLine.group(2), parsePosition));
                    	parsePosition.setIndex(0);
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

	@Override
    protected AbstractGCEvent parseLine(final String line, final ParsePosition pos) throws ParseException {
        AbstractGCEvent ae = null;
        try {
        	// parse timestamp          "double:"
            // parse collection type    "[TYPE"
            // pre-used->post-used, total, time
            final double timestamp = parseTimestamp(line, pos);
            final GCEvent.Type type = parseType(line, pos);
            // special provision for concurrent events
            if (type.getConcurrency() == Concurrency.CONCURRENT) {
                final ConcurrentGCEvent event = new ConcurrentGCEvent();
                if (type.getPattern() == GcPattern.GC) {
                    event.setTimestamp(timestamp);
                    event.setType(type);
                    // nothing more to parse...
                } 
                else {
                    event.setTimestamp(timestamp);
                    event.setType(type);

                    event.setPause(parsePause(line, pos));
                    event.setDuration(event.getPause());
                    // nothing more to parse
                }
                ae = event;
            } else {
            	final GCEvent event = new GCEvent();
                event.setTimestamp(timestamp);
                event.setType(type);
                if (event.getType().getPattern() == GcPattern.GC_MEMORY_PAUSE) {
                	setMemoryAndPauses((GCEvent)event, line, pos);
                }
                else {
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

package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Concurrency;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.GcPattern;
import com.tagtraum.perf.gcviewer.model.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.ParsePosition;

public class DataReaderSun1_6_0 extends AbstractDataReaderSun {

    private static final String DATE_STAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S";
	private static final int LENGTH_OF_DATESTAMP = 29;

    private static Logger LOG = Logger.getLogger(DataReaderSun1_6_0.class.getName());
    private static SimpleDateFormat dateParser = new SimpleDateFormat(DATE_STAMP_FORMAT);

    private static final String UNLOADING_CLASS = "[Unloading class ";
    private static final String DESIRED_SURVIVOR = "Desired survivor";
    private static final String APPLICATION_TIME = "Application time:";
    private static final String TOTAL_TIME_THREADS_STOPPED = "Total time for which application threads were stopped:";
    private static final String SURVIVOR_AGE = "- age";
    private static final String TIMES_ALONE = " [Times";
    private static final List<String> EXCLUDE_STRINGS = new LinkedList<String>();

    static {
        EXCLUDE_STRINGS.add(UNLOADING_CLASS);
        EXCLUDE_STRINGS.add(DESIRED_SURVIVOR);
        EXCLUDE_STRINGS.add(APPLICATION_TIME);
        EXCLUDE_STRINGS.add(TOTAL_TIME_THREADS_STOPPED);
        EXCLUDE_STRINGS.add(SURVIVOR_AGE);
        EXCLUDE_STRINGS.add(TIMES_ALONE);
    }
    
    private static final String CMS_ABORT_PRECLEAN = " CMS: abort preclean due to time ";

    private static final String HEAP_SIZING_START = "Heap";
    
    private static final List<String> HEAP_STRINGS = new LinkedList<String>();
    static {
        HEAP_STRINGS.add("def new generation"); // serial young collection -XX:+UseSerialGC
        HEAP_STRINGS.add("PSYoungGen"); // parallel young collection -XX:+UseParallelGC
        HEAP_STRINGS.add("par new generation"); // parallel young (CMS)
        HEAP_STRINGS.add("eden space");
        HEAP_STRINGS.add("from space");
        HEAP_STRINGS.add("to   space");
        
        HEAP_STRINGS.add("ParOldGen"); // parallel old collection -XX:+UseParallelOldGC
        HEAP_STRINGS.add("PSOldGen"); // serial old collection -XX:+UseParallelGC without -XX:+UseParallelOldGC
        HEAP_STRINGS.add("object space");
        HEAP_STRINGS.add("PSPermGen"); // serial (?) perm collection
        HEAP_STRINGS.add("tenured generation"); // serial old collection -XX:UseSerialGC
        HEAP_STRINGS.add("the space");
        HEAP_STRINGS.add("compacting perm gen"); // serial perm collection -XX:UseSerialGC
        HEAP_STRINGS.add("concurrent mark-sweep generation total"); // CMS old collection
        HEAP_STRINGS.add("concurrent-mark-sweep perm gen"); // CMS perm collection
        
        HEAP_STRINGS.add("No shared spaces configured.");
        
        HEAP_STRINGS.add("}");
    }

    private static Pattern unloadingClassPattern = Pattern.compile(".*\\[Unloading class [^\\]]+\\]$");

    // 1_6_0_u24 mixes lines, when outputing a "promotion failed" which leads to a "concurrent mode failure"
    // pattern looks always like "...[CMS<datestamp>..." or "...[CMS<timestamp>..."
    // the next line starts with " (concurrent mode failure)" which in earlier releases followed "CMS" immediately
    // the same can happen with "...ParNew<timestamp|datestamp>..."
    private static Pattern linesMixedPattern = Pattern.compile("(.*CMS|.*ParNew)([0-9]+[-.].*)"); 

	public DataReaderSun1_6_0(InputStream in) throws UnsupportedEncodingException {
		super(in);
	}

    public GCModel read() throws IOException {
        if (LOG.isLoggable(Level.INFO)) LOG.info("Reading Sun 1.6.x format...");
        
        try {
            final GCModel model = new GCModel(false);
            model.setFormat(GCModel.Format.SUN_X_LOG_GC);
            String line;
            String beginningOfLine = null;
            int lineNumber = 0;
            final ParsePosition parsePosition = new ParsePosition(0);
            OUTERLOOP:
            while ((line = in.readLine()) != null) {
                ++lineNumber;
                parsePosition.setLineNumber(lineNumber);
                if ("".equals(line)) {
                    continue;
                }
                try {
                    // filter out [Unloading class com.xyz] statements
                    for (String i : EXCLUDE_STRINGS) {
                        if (line.indexOf(i) == 0) continue OUTERLOOP;
                    }
                    if (line.indexOf(CMS_ABORT_PRECLEAN) >= 0) {
                        // line contains like " CMS: abort preclean due to time "
                        // -> remove the text
                        int indexOfStart = line.indexOf(CMS_ABORT_PRECLEAN);
                        StringBuilder sb = new StringBuilder(line);
                        sb.replace(indexOfStart, indexOfStart + CMS_ABORT_PRECLEAN.length(), "");
                        line = sb.toString();
                    }
                	Matcher mixedLineMatcher = linesMixedPattern.matcher(line);
                    final int unloadingClassIndex = line.indexOf(UNLOADING_CLASS);
                    if (unloadingClassPattern.matcher(line).matches()) {
                        beginningOfLine = line.substring(0, unloadingClassIndex);
                        continue;
                    }
                    else if (line.endsWith("[DefNew") || line.endsWith("[ParNew") || line.endsWith("[ParNew (promotion failed)")) {
                        // this is the case, when -XX:+PrintTenuringDistribution is used
                        // here we want to skip "Desired survivor..." and "- age..." lines
                        beginningOfLine = line;
                        continue;
                    }
                    else if (mixedLineMatcher.matches()) {
                    	beginningOfLine = mixedLineMatcher.group(1);
                    	model.add(parseLine(mixedLineMatcher.group(2), parsePosition));
                    	parsePosition.setIndex(0);
                    	continue;
                    }
                    else if (beginningOfLine != null) {
                        line = beginningOfLine + line;
                        beginningOfLine = null;
                    }
                    else if (line.indexOf(HEAP_SIZING_START) >= 0) {
                        // the next few lines will be the sizing of the heap
                        lineNumber = skipHeapSizes(in, lineNumber, HEAP_STRINGS);
                        continue;
                    }
                    model.add(parseLine(line, parsePosition));
                } catch (ParseException pe) {
                    if (LOG.isLoggable(Level.WARNING)) LOG.warning(pe.toString());
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
        AbstractGCEvent ae = null;
        try {
            // parse datestamp          "yyyy-MM-dd'T'hh:mm:ssZ:"
        	// parse timestamp          "double:"
            // parse collection type    "[TYPE"
            // either GC data or another collection type starting with timestamp
            // pre-used->post-used, total, time
        	final Date datestamp = parseDatestamp(line, pos);
            final double timestamp = parseTimestamp(line, pos);
            final GCEvent.Type type = parseType(line, pos);
            // special provision for CMS events
            if (type.getConcurrency() == Concurrency.CONCURRENT) {
                if (type.getPattern() == GcPattern.GC) {
                    final ConcurrentGCEvent event = new ConcurrentGCEvent();
                    event.setDateStamp(datestamp);
                    event.setTimestamp(timestamp);
                    event.setType(type);
                    ae = event;
                    // nothing more to parse...
                } else {
                    final ConcurrentGCEvent event = new ConcurrentGCEvent();
                    event.setDateStamp(datestamp);
                    event.setTimestamp(timestamp);
                    event.setType(type);

                    int start = pos.getIndex();
                    int end = line.indexOf('/', pos.getIndex());
                    event.setPause(Double.parseDouble(line.substring(start, end)));
                    start = end + 1;
                    end = line.indexOf(' ', start);
                    event.setDuration(Double.parseDouble(line.substring(start, end)));
                    // nothing more to parse
                    ae = event;
                }
            } else {
                final GCEvent event = new GCEvent();
                event.setDateStamp(datestamp);
                event.setTimestamp(timestamp);
                event.setType(type);
                // now add detail gcevents, should they exist
                int currentIndex = pos.getIndex();
                boolean currentIndexHasChanged = true;
                while (hasNextDetail(line, pos) && currentIndexHasChanged) {
                    final GCEvent detailEvent = new GCEvent();
                    try {
                        if (nextCharIsBracket(line, pos)) {
                            detailEvent.setTimestamp(timestamp);
                        } else {
                            detailEvent.setTimestamp(parseTimestamp(line, pos));
                        }
                        detailEvent.setType(parseType(line, pos));
                        setMemoryAndPauses(detailEvent, line, pos);
                        event.add(detailEvent);
                    } catch (UnknownGcTypeException e) {
                        skipUntilEndOfDetail(line, pos, e);
                    } catch (NumberFormatException e) {
                        skipUntilEndOfDetail(line, pos, e);
                    }
                    
                    // in a line with complete garbage the parser must not get stuck; use emergency exit...
                    currentIndexHasChanged = currentIndex != pos.getIndex();
                    currentIndex = pos.getIndex();
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
            throw new ParseException("Error parsing entry (" + rte.toString() + ")", line, pos);
        }
    }

    private void skipUntilEndOfDetail(final String line, final ParsePosition pos, Exception e) {
        // moving position to the end of this detail event -> skip it
        int indexOfNextBracket = line.indexOf("]", pos.getIndex())+1;
        if (indexOfNextBracket > pos.getIndex()) { 
            pos.setIndex(indexOfNextBracket);
            while (line.charAt(pos.getIndex()) == ' ') {
                pos.setIndex(pos.getIndex()+1);
            }
        }
        if (LOG.isLoggable(Level.FINE)) LOG.fine("Skipping detail event because of " + e);
    }

    public Date parseDatestamp(String line, ParsePosition pos) throws ParseException {
    	Date date = null;
    	if (nextIsDatestamp(line, pos)) {
    		try {
				date = dateParser.parse(line.substring(pos.getIndex(), LENGTH_OF_DATESTAMP-1));
				pos.setIndex(pos.getIndex() + LENGTH_OF_DATESTAMP);
			}
    		catch (java.text.ParseException e) {
    			throw new ParseException(e.toString(), line);
			}
    	}
    	
    	return date;
    }
    
    private boolean nextIsDatestamp(String line, ParsePosition pos) {
        if (line.length() < 10) {
            return false;
        }

        return line.indexOf("-", pos.getIndex()) == 4 && line.indexOf("-", pos.getIndex() + 5) == 7;
    }
	
}

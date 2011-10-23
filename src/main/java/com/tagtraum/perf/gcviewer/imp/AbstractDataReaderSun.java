package com.tagtraum.perf.gcviewer.imp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tagtraum.perf.gcviewer.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.GCEvent;
import com.tagtraum.perf.gcviewer.util.NumberParser;

/**
 *
 * Date: Feb 12, 2002
 * Time: 4:30:27 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public abstract class AbstractDataReaderSun {

    private static Logger LOG = Logger.getLogger(AbstractDataReaderSun.class.getName());
    protected BufferedReader in;

    public AbstractDataReaderSun(InputStream in) throws UnsupportedEncodingException {
        this.in = new BufferedReader(new InputStreamReader(in, "ASCII"), 64 * 1024);
    }

    public void setMemoryAndPauses(GCEvent event, String line) throws ParseException {
        setMemoryAndPauses(event, line, new ParsePosition(0));
    }

    // Suns G1 log output uses K, M and G for memory sizes; I only want K, so I have to change M and G values
    private int getMemoryUnitMultiplier(String line, char memUnit) {
    	if ('K' == memUnit) {
    		return 1;
    	}
    	else if ('M' == memUnit) {
    		return 1024;
    	}
    	else if ('G' == memUnit) {
    		return 1024*1024;
    	}
    	else {
    		if (LOG.isLoggable(Level.WARNING)) {
    			LOG.warning("unknown memoryunit: " + memUnit + " in line " + line);
    		}
    		return 1;
    	}
    }
    
    public void setMemoryAndPauses(GCEvent event, String line, ParsePosition pos) throws ParseException {
        int start = pos.getIndex();
        int end = line.indexOf("->", pos.getIndex()) - 1;
        if (end != -2) for (start = end-1; start >= 0 && Character.isDigit(line.charAt(start)); start--) {}
        int parenthesis = line.indexOf('(', start);
        boolean foundPreUsed = end != -2 && parenthesis > end;
        if (foundPreUsed) {
            start = line.lastIndexOf(' ', end) + 1;
            event.setPreUsed(NumberParser.parseInt(line, start, end-start) * getMemoryUnitMultiplier(line, line.charAt(end)));
            start = end + 3;
        }
        for (end = start; Character.isDigit(line.charAt(end)); ++end);
        event.setPostUsed(NumberParser.parseInt(line, start, end-start) * getMemoryUnitMultiplier(line, line.charAt(end)));
        if (!foundPreUsed) {
            event.setPreUsed(event.getPostUsed());
        }

        start = end + 2;
        end = line.indexOf(')', start) - 1;
        event.setTotal(NumberParser.parseInt(line, start, end-start) * getMemoryUnitMultiplier(line, line.charAt(end)));
        if (line.charAt(end + 1) == ')') pos.setIndex(end+2);
        else pos.setIndex(end+1);

        while (isGeneration(line, pos)) {
            final GCEvent detailEvent = new GCEvent();
            final AbstractGCEvent.Type type = parseType(line, pos);
            detailEvent.setType(type);
            start = pos.getIndex();
            end = line.indexOf("K", pos.getIndex());
            detailEvent.setPreUsed(NumberParser.parseInt(line, start, end-start));
            start = end + 3;
            end = line.indexOf("K", start);
            detailEvent.setPostUsed(NumberParser.parseInt(line, start, end-start));
            if (line.charAt(end+1) == '(') {
                start = end+2;
                end = line.indexOf('K', start);
                detailEvent.setTotal(NumberParser.parseInt(line, start, end-start));
                // skip ')'
                end++;
            }
            detailEvent.setTimestamp(event.getTimestamp());
            if (detailEvent.getPreUsed() > detailEvent.getPostUsed()) event.add(detailEvent);
            pos.setIndex(end+1);
        }

        parsePause(event, line, pos);
    }

    private boolean isGeneration(final String line, final ParsePosition pos) {
        final String trimmedLine = line.substring(pos.getIndex()).trim();
        return trimmedLine.startsWith("eden")
                || trimmedLine.startsWith("survivor")
                || trimmedLine.startsWith("tenured")
                || trimmedLine.startsWith("new")
                || trimmedLine.startsWith("permanent");
    }

    protected void parsePause(GCEvent event, String line, ParsePosition pos) {
    	// TODO refactor

    	// in case there is no time, this ends with a ']'
        int closingBracket = line.indexOf(']', pos.getIndex());
        if (closingBracket == pos.getIndex()) {
            pos.setIndex(closingBracket + 1);
            if (line.charAt(closingBracket + 1) == ',')
                pos.setIndex(closingBracket + 3);
            else
                pos.setIndex(closingBracket + 1);
        } 
        else if (Character.isDigit(line.charAt(pos.getIndex()))) {
        	// if current position is a digit, this is where the pause starts
            int end = line.indexOf(' ', pos.getIndex());
            if (end < 0) {
            	end = line.indexOf(']', pos.getIndex());
            }
    		event.setPause(Double.parseDouble(line.substring(pos.getIndex(), end).replace(',', '.')));

    		// skip "secs]"
            pos.setIndex(line.indexOf(']', end) + 1);
    	}
    	else {
	        final int openingBracket = line.indexOf('[', pos.getIndex());
	        if (openingBracket == pos.getIndex()+3 || openingBracket == pos.getIndex()+2 || openingBracket == pos.getIndex()+1) {
	            pos.setIndex(openingBracket);
	        }
	        else if (line.indexOf(',', pos.getIndex()) == pos.getIndex()) {
	            pos.setIndex(pos.getIndex() + 2);
	            parsePause(event, line, pos);
	        }
	        else if (line.indexOf("icms_dc=", pos.getIndex()) != -1) {
	            // skip CMS incremental pacing output
	            // - patch provided by Anders Wallgren to handle -XX:+CMSIncrementalPacing flag
	            final int comma = line.indexOf(',', pos.getIndex());
	            pos.setIndex(comma+2);
	            parsePause(event, line, pos);
	        }
	        else {
	            // in case there is no time, this ends with a ']'
	            closingBracket = line.indexOf(']', pos.getIndex());
	            if (closingBracket == pos.getIndex()) {
	                pos.setIndex(closingBracket + 1);
	                if (line.charAt(closingBracket + 1) == ',')
	                    pos.setIndex(closingBracket + 3);
	                else
	                    pos.setIndex(closingBracket + 1);
	            } else {
	                LOG.severe("Hm... something went wrong here... (line='" + line + "'");
	            }
	        }
    	}
    }
    
    public double parsePause(String line, ParsePosition pos) {
    	// usual pattern expected: "..., 0.002032 secs]"
    	// but may be as well (G1): "..., 0.003032]"
        int end = line.indexOf(' ', pos.getIndex());
        if (end < 0) {
        	end = line.indexOf(']', pos.getIndex());
        }
        final double pause = Double.parseDouble(line.substring(pos.getIndex(), end).replace(',', '.'));
        
        // skip "secs]"
        pos.setIndex(line.indexOf(']', end) + 1);
        
        return pause;
    }
    
    protected boolean hasNextDetail(String line, ParsePosition pos) {
        return isTimestamp(line, pos) || nextCharIsBracket(line, pos);
    }

    protected boolean nextCharIsBracket(String line, ParsePosition pos) {
        // skip spaces
        while (line.charAt(pos.getIndex()) == ' ' && pos.getIndex()+1<line.length()) pos.setIndex(pos.getIndex()+1);
        return line.charAt(pos.getIndex()) == '[';
    }

    public GCEvent.Type parseType(String line, ParsePosition pos) throws ParseException {
        int i = pos.getIndex();
        try {
            // consume all leading spaces and [
            final int lineLength = line.length();
            final char[] lineChars = line.toCharArray();
            char c = lineChars[i];
            for (; i<lineLength; c = lineChars[++i]) {
                if (c != ' ' && c != '[') break;
            }
            if (i>=lineLength) throw new ParseException("Unexpected end of line.", line);
            StringBuffer sb = new StringBuffer(20);
            // check whether the type name starts with a number
            // e.g. 0.406: [GC [1 CMS-initial-mark: 7664K(12288K)] 7666K(16320K), 0.0006855 secs]
            //final int startNumbers = i;
            // -> skip number
            for (; Character.isDigit(c) && i<lineLength; c = lineChars[++i]);
            //if (startNumbers != i) sb.append(lineChars, startNumbers, i);
            // append all chars, but no numbers, colons, [ or ]
            final int startType = i;
            for (; i<lineLength; c = lineChars[++i]) {
                if (c == ':' || c == '[' || c == ']' || c== ',' || Character.isDigit(c)) break;
            }
            sb.append(lineChars, startType, i-startType);
            for (; i<lineLength; c = lineChars[++i]) {
                if (c == '[' || c == ']' || Character.isDigit(c)) break;
            }
            final String s = sb.toString();
            GCEvent.Type gcType = GCEvent.Type.parse(s);
            // special case: 5.0: [GC Desired survivor size 3342336 bytes, new threshold 1 (max 32) - age   1:  6684672 bytes,  6684672 total 52471K->22991K(75776K), 1.0754938 secs]
            if (gcType == null) {
                String type = s.trim();
                if (type.startsWith(GCEvent.Type.GC.getType()) && type.indexOf("Desired survivor") != -1) {
                    i = line.indexOf("total", i);
                    i += 5;
                    gcType = GCEvent.Type.GC;
                }
            }
            if (gcType == null) {
                //System.out.println("Error parsing entry: " + line + " Unknown GC type: " + s);
                throw new UnknownGcTypeException(s, line);
            }
            return gcType;
        }
        finally {
            pos.setIndex(i);
        }
    }

    /**
     * Determines whether there is a timestamp coming up earlier than a
     * footprint value.
     *
     * @param line
     * @param pos
     * @return true if we will encounter a timestamp earlier than a footprint
     */
    public boolean isTimestamp(String line, ParsePosition pos) {
        // timestamps end with colons
        // footprint values end with K's
        int colonIndex = line.indexOf(':', pos.getIndex());
        int kIndex = line.indexOf('K', pos.getIndex());
        if (colonIndex < 0) return false;
        if (kIndex < 0) return true;
        return colonIndex < kIndex;
    }

    /**
     * Parses a timestamp that has the format <code>double:</code>.
     * The parseposition will be adjusted by this method to after the timestamp.
     *
     * @param line line to parse
     * @param pos current position
     * @return the parsed timestamp
     * @throws ParseException
     */
    public double parseTimestamp(String line, ParsePosition pos) throws ParseException {
        // look for end of timestamp, which is a colon ':'
        int endOfTimestamp = line.indexOf(':', pos.getIndex());
        if (endOfTimestamp == -1) throw new ParseException("Error parsing entry.", line);
        // we have to replace , with . for Europe
        final String timestampString = line.substring(pos.getIndex(), endOfTimestamp).replace(',', '.');
        final double timestamp = Double.parseDouble(timestampString);
        pos.setIndex(endOfTimestamp+1);
        return timestamp;
    }

    protected abstract AbstractGCEvent parseLine(String line, ParsePosition pos) throws ParseException;

    public void skipDetails(String line, ParsePosition pos) throws ParseException {
        int index = line.lastIndexOf(']', line.length()-2) + 1;
        if (index == 0) throw new ParseException("Failed to skip details.", line);
        if (line.charAt(index) == ' ') index++;
        pos.setIndex(index);
    }
}

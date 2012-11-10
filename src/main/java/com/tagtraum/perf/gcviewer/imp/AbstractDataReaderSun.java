package com.tagtraum.perf.gcviewer.imp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.util.NumberParser;
import com.tagtraum.perf.gcviewer.util.ParsePosition;

/**
 *
 * Date: Feb 12, 2002
 * Time: 4:30:27 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public abstract class AbstractDataReaderSun implements DataReader {

    /**
     * Datestamps are parsed without timezone information. I assume that if two people
     * discuss a gc log, it is easier for them to use the same timestamps without 
     * timezone adjustments.
     */
    public static final String DATE_STAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S";
    private static final int LENGTH_OF_DATESTAMP = 29;
    
    private static final String CMS_PRINT_PROMOTION_FAILURE = "promotion failure size";
    
    private static Logger LOG = Logger.getLogger(AbstractDataReaderSun.class.getName());
    private static SimpleDateFormat dateParser = new SimpleDateFormat(DATE_STAMP_FORMAT);
    
    protected BufferedReader in;
    protected GcLogType gcLogType;

    /**
     * Create an instance of this class passing an inputStream an the type of the logfile.
     * @param in inputstream to the log file
     * @param gcLogType type of the logfile
     * @throws UnsupportedEncodingException if ASCII is not supported
     */
    public AbstractDataReaderSun(InputStream in, GcLogType gcLogType) throws UnsupportedEncodingException {
        super();
        this.in = new BufferedReader(new InputStreamReader(in, "ASCII"), 64 * 1024);
        this.gcLogType = gcLogType;
    }
    
    protected void setMemoryAndPauses(GCEvent event, String line) throws ParseException {
        setMemoryAndPauses(event, line, new ParsePosition(0));
    }

    /**
     * Returns the amount of memory in kilobyte. Depending on <code>memUnit</code>, input is
     * converted to kilobyte.
     * @param memoryValue amount of memory
     * @param memUnit memory unit
     * @param line line that is parsed
     * @return amount of memory in kilobyte
     */
    private int getMemoryInKiloByte(int memoryValue, char memUnit, String line) {
        if ('B' == memUnit) {
            return memoryValue / 1024;
        }
        else if ('K' == memUnit) {
            return memoryValue;
        }
        else if ('M' == memUnit) {
            return memoryValue * 1024;
        }
        else if ('G' == memUnit) {
            return memoryValue * 1024*1024;
        }
        else {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.warning("unknown memoryunit '" + memUnit + "' in line " + line);
            }
            return 1;
        }
    }
    
    protected void setMemoryAndPauses(GCEvent event, String line, ParsePosition pos) throws ParseException {
        setMemory(event, line, pos);
        parsePause(event, line, pos);
    }

    /**
     * Parses a memory information with the following form: 8192K[(16M)]->7895K[(16M)] ("[...]" means
     * optional.
     * 
     * @param event event, where parsed information should be stored
     * @param line line to be parsed
     * @param pos current parse position; all characters between current position and next digits
     * will be skipped
     * @throws ParseException parsing was not possible
     */
    protected void setMemoryExtended(GCEvent event, String line, ParsePosition pos) throws ParseException {
        int startPos = pos.getIndex();
        boolean lineHasMoreChars = true;
        int currentPos = startPos;
        // skip "not digits" until first digit is found
        while (!Character.isDigit(line.charAt(currentPos)) && lineHasMoreChars) {
            ++currentPos;
            lineHasMoreChars = currentPos < line.length();
        }
        if (!lineHasMoreChars) {
            pos.setIndex(currentPos);
            throw new ParseException("unexpected memory format found", line, pos);
        }
        
        int endOfNextNumber = line.indexOf("(", currentPos);
        int separatorPos = line.indexOf("->", currentPos);
        if (endOfNextNumber > separatorPos) {
            // if format is "before"->"after"("total"), the next parentesis is the one of the "total"
            endOfNextNumber = separatorPos;
        }
        event.setPreUsed(getMemoryInKiloByte(NumberParser.parseInt(line, currentPos, endOfNextNumber-currentPos-1),
                line.charAt(endOfNextNumber-1),
                line));
        
        // skip until after "->"
        currentPos = line.indexOf("->", endOfNextNumber) + 2;
        
        
        boolean hasTotalHeap = true;
        endOfNextNumber = line.indexOf("(", currentPos);
        if (endOfNextNumber == -1 || endOfNextNumber-currentPos > 10) {
            // there is no total heap information, only after (like in "Survivors" of G1)
            hasTotalHeap = false;
            endOfNextNumber = currentPos;
            // goto end of next number
            
            while (Character.isDigit(line.charAt(endOfNextNumber))) {
                ++endOfNextNumber;
            }
            
            ++endOfNextNumber;
        }
        event.setPostUsed(getMemoryInKiloByte(NumberParser.parseInt(line, currentPos, endOfNextNumber-currentPos-1),
                line.charAt(endOfNextNumber-1),
                line));
        currentPos = endOfNextNumber;
        
        if (hasTotalHeap) {
            // skip "(" and read heap size
            ++currentPos;
            endOfNextNumber = line.indexOf(")", currentPos);
            event.setTotal(getMemoryInKiloByte(NumberParser.parseInt(line, currentPos, endOfNextNumber-currentPos-1),
                    line.charAt(endOfNextNumber-1),
                    line));
            currentPos = endOfNextNumber;
        }
        
        pos.setIndex(currentPos);
    }
    
    protected void setMemory(GCEvent event, String line, ParsePosition pos) throws ParseException {
        int start = pos.getIndex();
        int end = line.indexOf("->", pos.getIndex()) - 1;
        if (end != -2) for (start = end-1; start >= 0 && Character.isDigit(line.charAt(start)); start--) {}
        int parenthesis = line.indexOf('(', start);
        boolean foundPreUsed = end != -2 && parenthesis > end;
        if (foundPreUsed) {
            start = line.lastIndexOf(' ', end) + 1;
            event.setPreUsed(getMemoryInKiloByte(NumberParser.parseInt(line, start, end-start),
                    line.charAt(end),
                    line));
            start = end + 3;
        }
        for (end = start; Character.isDigit(line.charAt(end)); ++end);
        event.setPostUsed(getMemoryInKiloByte(NumberParser.parseInt(line, start, end-start),
                line.charAt(end),
                line));
        if (!foundPreUsed) {
            event.setPreUsed(event.getPostUsed());
        }

        start = end + 2;
        end = line.indexOf(')', start) - 1;
        event.setTotal(getMemoryInKiloByte(NumberParser.parseInt(line, start, end-start),
                line.charAt(end),
                line));
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
	                LOG.severe("Hm... something went wrong here... (line " + pos.getLineNumber() + "='" + line + "'");
	            }
	        }
    	}
    }
    
    protected double parsePause(String line, ParsePosition pos) {
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
        return nextIsTimestamp(line, pos) || nextCharIsBracket(line, pos);
    }

    protected boolean nextCharIsBracket(String line, ParsePosition pos) {
        // skip spaces
        while (line.charAt(pos.getIndex()) == ' ' && pos.getIndex()+1<line.length()) pos.setIndex(pos.getIndex()+1);
        return line.charAt(pos.getIndex()) == '[';
    }

    protected GCEvent.Type parseType(String line, ParsePosition pos) throws ParseException {
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
            boolean isInParantesis = false;
            for (; i<lineLength; c = lineChars[++i]) {
                if (c == '(' || isInParantesis || c == ')') {
                    // option "-XX:+PrintPromotionFailure" inserts text in parentheses between "ParNew" and "(promotion failed)"
                    // [ParNew (0: promotion failure size = 4098)  (1: promotion failure size = 4098)  (2: promotion failure size = 4098) (promotion failed):
                    // parsing must not stop until end of (promotion failed)...
                    isInParantesis = (c != ')');
                    continue;
                }
                if (c == ':' || c == '[' || c == ']' || c== ',' || Character.isDigit(c)) break;
            }
            sb.append(lineChars, startType, i-startType);
            if (sb.indexOf(CMS_PRINT_PROMOTION_FAILURE) > 0) {
                // ... now remove the "promotion failure size" parts inside parentheses
                while (sb.indexOf(CMS_PRINT_PROMOTION_FAILURE) > 0) {
                    int firstParenthesis = sb.indexOf("(");
                    sb.delete(firstParenthesis, sb.indexOf(")", firstParenthesis) + 3);
                }
            }
            for (; i<lineLength; c = lineChars[++i]) {
                if (c == '[' || c == ']' || Character.isDigit(c)) break;
            }
            final String s = sb.toString();
            GCEvent.Type gcType = AbstractGCEvent.Type.parse(s);
            if (gcType == null) {
                // special case: 5.0: [GC Desired survivor size 3342336 bytes, new threshold 1 (max 32) - age   1:  6684672 bytes,  6684672 total 52471K->22991K(75776K), 1.0754938 secs]
                String type = s.trim();
                if (type.startsWith(AbstractGCEvent.Type.GC.getType()) && type.indexOf("Desired survivor") != -1) {
                    i = line.indexOf("total", i);
                    i += 5;
                    gcType = AbstractGCEvent.Type.GC;
                }
            }
            if (gcType == null) {
                throw new UnknownGcTypeException(s, line, pos);
            }
            return gcType;
        }
        finally {
            pos.setIndex(i);
        }
    }

    /**
     * Returns <code>true</code>, if next "token" is a timestamp.
     * 
     * @param line line to be parsed
     * @param pos current position in line
     * @return <code>true</code> if next is timestamp, <code>false</code> otherwise
     */
    protected boolean nextIsTimestamp(String line, ParsePosition pos) {
        // format of a timestamp is the following: "0.013:"
        // make sure that after the next blanks a timestamp follows
        
        if (line.indexOf(':', pos.getIndex()) < 0) {
            return false;
        }
        
        int index = pos.getIndex();
        // skip blanks
        while (Character.isSpaceChar(line.charAt(index))) {
            ++index;
        }
        
        boolean hasDigitsBeforeDot = false;
        boolean hasDot = false;
        boolean hasDigitsAfterDot = false;
        boolean hasColon = false;
        
        // digits before "."
        int startIndex = index;
        while (Character.isDigit(line.charAt(index))) {
            ++index;
            hasDigitsBeforeDot = true;
        }
        
        // "." / ","
        if (line.charAt(index) == '.' || line.charAt(index) == ',') {
            ++index;
            hasDot = true;
        }
        
        // digits after "."
        while (Character.isDigit(line.charAt(index))) {
            ++index;
            hasDigitsAfterDot = true;
        }
        
        hasColon = line.charAt(index) == ':';
        
        return index > startIndex && hasDigitsBeforeDot && hasDot && hasDigitsAfterDot && hasColon;
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
    protected double parseTimestamp(String line, ParsePosition pos) throws ParseException {
        // look for end of timestamp, which is a colon ':'
        int endOfTimestamp = line.indexOf(':', pos.getIndex());
        if (endOfTimestamp == -1) throw new ParseException("Error parsing entry.", line, pos);
        // we have to replace , with . for Europe
        final String timestampString = line.substring(pos.getIndex(), endOfTimestamp).replace(',', '.');
        final double timestamp = Double.parseDouble(timestampString);
        pos.setIndex(endOfTimestamp+1);
        return timestamp;
    }

    protected abstract AbstractGCEvent<?> parseLine(String line, ParsePosition pos) throws ParseException;

    protected void skipDetails(String line, ParsePosition pos) throws ParseException {
        int index = line.lastIndexOf(']', line.length()-2) + 1;
        if (index == 0) throw new ParseException("Failed to skip details.", line);
        if (line.charAt(index) == ' ') index++;
        pos.setIndex(index);
    }

    /**
     * Skips a block of lines containing information like they are generated by
     * -XX:+PrintHeapAtGC or -XX:+PrintAdaptiveSizePolicy.
     * 
     * @param in inputStream of the current log to be read
     * @param lineNumber current line number
     * @param lineStartStrings lines starting with these strings should be ignored
     * @return line number including lines read in this method
     * @throws IOException problem with reading from the file
     */
    protected int skipLines(BufferedReader in, ParsePosition pos, int lineNumber, List<String> lineStartStrings) throws IOException {
        String line = "";
        
        if (!in.markSupported()) {
            LOG.warning("input stream does not support marking!");
        } 
        else {
            in.mark(200);
        }
        
        boolean startsWithString = true;
        while (startsWithString && (line = in.readLine()) != null) {
            ++lineNumber;
            pos.setLineNumber(lineNumber);
            // for now just skip those lines
            startsWithString = startsWith(line, lineStartStrings);
            if (startsWithString) {
                // don't mark any more if line didn't match -> it is the first line that
                // is of interest after the skipped block
                if (in.markSupported()) {
                    in.mark(200);
                }
            }
        }
        
        // push last read line back into stream - it is the next event to be parsed
        if (in.markSupported()) {
            try {
                in.reset();
            }
            catch (IOException e) {
                throw new ParseException("problem resetting stream (" + e.toString() + ")", line, pos);
            }
        }
        
        return --lineNumber;
    }

    private boolean startsWith(String line, List<String> lineStartStrings) {
        boolean containsString = false;
        for (String lineStartString : lineStartStrings) {
            containsString |= line.trim().startsWith(lineStartString);
        }
        
        return containsString;
    }

    /**
     * Parses a datestamp in <code>line</code> at <code>pos</code>.
     * 
     * @param line current line
     * @param pos current parse position
     * @return returns parsed datestamp if found one, <code>null</code> otherwise
     * @throws ParseException datestamp could not be parsed
     */
    protected Date parseDatestamp(String line, ParsePosition pos) throws ParseException {
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

    /**
     * Returns <code>true</code> if text at parsePosition is a datestamp.
     * 
     * @param line current line
     * @param pos current parse position
     * @return <code>true</code> if in current line at current parse position we have a datestamp
     */
    private boolean nextIsDatestamp(String line, ParsePosition pos) {
        if (line == null || line.length() < 10) {
            return false;
        }
    
        return line.indexOf("-", pos.getIndex()) == 4 && line.indexOf("-", pos.getIndex() + 5) == 7;
    }
}

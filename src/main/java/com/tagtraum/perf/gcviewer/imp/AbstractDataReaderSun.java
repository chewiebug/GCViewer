package com.tagtraum.perf.gcviewer.imp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.ExtendedType;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.GcPattern;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.NumberParser;
import com.tagtraum.perf.gcviewer.util.ParsePosition;

/**
 * <p>The AbstractDataReaderSun is the base class of most Sun / Oracle parser implementations.
 * It contains a lot of helper methods to do the actual parsing of the details of a gc event.
 * New parsers for Sun / Oracle gc algorithms should derive from this class.</p>
 *
 * <p>Date: Feb 12, 2002</p>
 * <p>Time: 4:30:27 PM</p>
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 */
public abstract class AbstractDataReaderSun extends AbstractDataReader {

    /**
     * Datestamps are parsed without timezone information. I assume that if two people
     * discuss a gc log, it is easier for them to use the same timestamps without 
     * timezone adjustments.
     */
    public static final String DATE_STAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.S";
    private static final int LENGTH_OF_DATESTAMP = 29;
    
    private static final String CMS_PRINT_PROMOTION_FAILURE = "promotion failure size";
    
    private final SimpleDateFormat dateParser = new SimpleDateFormat(DATE_STAMP_FORMAT);
    
    private static Pattern parenthesesPattern = Pattern.compile("\\([^()]*\\) ?");

    /** the log type allowing for small differences between different versions of the gc logs */
    protected GcLogType gcLogType;

    /**
     * Create an instance of this class passing an inputStream an the type of the logfile.
     * @param in inputstream to the log file
     * @param gcLogType type of the logfile
     * @throws UnsupportedEncodingException if ASCII is not supported
     */
    protected AbstractDataReaderSun(GCResource gcResource, InputStream in, GcLogType gcLogType) throws UnsupportedEncodingException {
        super(gcResource, in);
        this.gcLogType = gcLogType;
    }
    
    /**
     * Returns the amount of memory in kilobyte. Depending on <code>memUnit</code>, input is
     * converted to kilobyte.
     * @param memoryValue amount of memory
     * @param memUnit memory unit
     * @param line line that is parsed
     * @return amount of memory in kilobyte
     */
    private int getMemoryInKiloByte(double memoryValue, char memUnit, String line) {
        if ('B' == memUnit) {
            return (int) (memoryValue / 1024);
        }
        else if ('K' == memUnit) {
            return (int) memoryValue;
        }
        else if ('M' == memUnit) {
            return (int) (memoryValue * 1024);
        }
        else if ('G' == memUnit) {
            return (int) (memoryValue * 1024*1024);
        }
        else {
            if (getLogger().isLoggable(Level.WARNING)) {
                getLogger().warning("unknown memoryunit '" + memUnit + "' in line " + line);
            }
            return 1;
        }
    }
    
    /**
     * Convenience method to parse memory information followed by a pause time.
     * 
     * @param event event where the result should be written to
     * @param line line to be parsed (from the beginning)
     * @throws ParseException is thrown to report any problems the parser runs into
     * @see #setMemoryAndPauses(GCEvent, String, ParsePosition)
     */
    protected void setMemoryAndPauses(GCEvent event, String line) throws ParseException {
        setMemoryAndPauses(event, line, new ParsePosition(0));
    }

    /**
     * Parses memory information in the format &lt;number&gt;KB-&gt;&lt;number&gt;KB(&lt;number&gt;KB), &lt;number&gt;ms
     * 
     * @param event event where result of parsing is to be stored
     * @param line line to be parsed
     * @param pos position where parsing should start
     * @throws ParseException is thrown to report any problems the parser runs into
     */
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
        event.setPreUsed(getMemoryInKiloByte(NumberParser.parseDouble(line, currentPos, endOfNextNumber-currentPos-1),
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
            
            while (Character.isDigit(line.charAt(endOfNextNumber))|| line.charAt(endOfNextNumber) == '.') {
                ++endOfNextNumber;
            }
            
            ++endOfNextNumber;
        }
        event.setPostUsed(getMemoryInKiloByte(NumberParser.parseDouble(line, currentPos, endOfNextNumber-currentPos-1),
                line.charAt(endOfNextNumber-1),
                line));
        currentPos = endOfNextNumber;
        
        if (hasTotalHeap) {
            // skip "(" and read heap size
            ++currentPos;
            endOfNextNumber = line.indexOf(")", currentPos);
            event.setTotal(getMemoryInKiloByte(NumberParser.parseDouble(line, currentPos, endOfNextNumber-currentPos-1),
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
	                getLogger().severe("Hm... something went wrong here... (line " + pos.getLineNumber() + "='" + line + "'");
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
        return nextIsTimestamp(line, pos) 
                || nextIsDatestamp(line, pos) 
                || nextCharIsBracket(line, pos);
    }

    protected boolean nextCharIsBracket(String line, ParsePosition pos) {
        // skip spaces
        while (line.charAt(pos.getIndex()) == ' ' && pos.getIndex()+1<line.length()) pos.setIndex(pos.getIndex()+1);
        return line.charAt(pos.getIndex()) == '[';
    }


    protected String parseTypeString(String line, ParsePosition pos) throws ParseException {
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
            StringBuilder sb = new StringBuilder(20);
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
            return sb.toString().trim();
        }
        finally {
            pos.setIndex(i);
        }
    }

    protected ExtendedType parseType(String line, ParsePosition pos) throws ParseException {
        String typeString = parseTypeString(line, pos);
        ExtendedType gcType = extractTypeFromParsedString(typeString);
        if (gcType == null) {
            throw new UnknownGcTypeException(typeString, line, pos);
        }
        
        return gcType;
    }


    protected ExtendedType extractTypeFromParsedString(String typeName) throws UnknownGcTypeException {
        ExtendedType extendedType = null;
        String lookupTypeName = typeName.endsWith("--") 
                ? typeName.substring(0, typeName.length()-2) 
                        : typeName;
        AbstractGCEvent.Type gcType = AbstractGCEvent.Type.lookup(lookupTypeName);
        // the gcType may be null because there was a PrintGCCause flag enabled - if so, reparse it with the first paren set stripped
        if (gcType == null) {
            // try to parse it again with the parens removed
            Matcher parenMatcher = parenthesesPattern.matcher(typeName);
            if (parenMatcher.find()) {
                gcType = AbstractGCEvent.Type.lookup(parenMatcher.replaceFirst(""));
            }
        }
        
        if (gcType != null) {
            extendedType = new ExtendedType(gcType, typeName);
        }
        
        return extendedType;
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
            getLogger().warning("input stream does not support marking!");
        } 
        else {
            in.mark(200);
        }
        
        boolean startsWithString = true;
        while (startsWithString && (line = in.readLine()) != null) {
            ++lineNumber;
            pos.setLineNumber(lineNumber);
            // for now just skip those lines
            startsWithString = startsWith(line, lineStartStrings, true);
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

    /**
     * Tests if <code>line</code> starts with one of the strings in <code>lineStartStrings</code>.
     * If <code>trimLine</code> is <code>true</code>, then <code>line</code> is trimmed first. 
     * 
     * @param line line to be checked against
     * @param lineStartStrings list of strings to check
     * @param trimLine if <code>true</code> then trim <code>line</code>
     * @return <code>true</code>, if <code>line</code> starts with one of the strings in 
     * <code>lineStartStrings</code>
     */
    protected boolean startsWith(String line, List<String> lineStartStrings, boolean trimLine) {
        String lineToTest = trimLine ? line.trim() : line;
        for (String lineStartString : lineStartStrings) {
            if (lineToTest.startsWith(lineStartString)) {
                return true;
            }
        }
        
        return false;
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
                date = dateParser.parse(line.substring(pos.getIndex(), pos.getIndex()+LENGTH_OF_DATESTAMP-1));
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
    protected boolean nextIsDatestamp(String line, ParsePosition pos) {
        if (line == null || line.length() < 10) {
            return false;
        }
    
        return line.indexOf("-", pos.getIndex()) == pos.getIndex()+4 && line.indexOf("-", pos.getIndex() + 5) == pos.getIndex()+7;
    }

    /**
     * Parses detail events if any exist at current <code>pos</code> in <code>line</code>.
     * 
     * @param line current line
     * @param pos current parse position
     * @param event enclosing event
     * @throws ParseException some problem when parsing the detail event
     */
    protected void parseDetailEventsIfExist(final String line, final ParsePosition pos,
            final GCEvent event) throws ParseException {
                
                int currentIndex = pos.getIndex();
                boolean currentIndexHasChanged = true;
                while (hasNextDetail(line, pos) && currentIndexHasChanged) {
                    final GCEvent detailEvent = new GCEvent();
                    try {
                        if (nextCharIsBracket(line, pos)) {
                            detailEvent.setDateStamp(event.getDatestamp());
                            detailEvent.setTimestamp(event.getTimestamp());
                        } 
                        else {
                            detailEvent.setDateStamp(parseDatestamp(line, pos));
                            detailEvent.setTimestamp(parseTimestamp(line, pos));
                        }
                        detailEvent.setExtendedType(parseType(line, pos));
                        if (nextIsTimestamp(line, pos) || nextIsDatestamp(line, pos)) {
                            parseDetailEventsIfExist(line, pos, detailEvent);
                        }
                        if (event.getExtendedType().getPattern() == GcPattern.GC_MEMORY_PAUSE) {
                            setMemoryAndPauses(detailEvent, line, pos);
                        }
                        else {
                            parsePause(detailEvent, line, pos);
                        }
                        event.add(detailEvent);
                    } 
                    catch (UnknownGcTypeException e) {
                        skipUntilEndOfDetail(line, pos, e);
                    } 
                    catch (NumberFormatException e) {
                        skipUntilEndOfDetail(line, pos, e);
                    }
                    
                    // promotion failed indicators "--" are sometimes separated from their primary
                    // event name -> stick them together here (they are part of the "parent" event)
                    if (nextIsPromotionFailed(line, pos)) {
                        pos.setIndex(pos.getIndex() + 2);
                        event.setExtendedType(extractTypeFromParsedString(event.getExtendedType() + "--"));
                    }
            
                    // in a line with complete garbage the parser must not get stuck; just stop parsing.
                    currentIndexHasChanged = currentIndex != pos.getIndex();
                    currentIndex = pos.getIndex();
                }
                
            }

    private boolean nextIsPromotionFailed(String line, ParsePosition pos) {
        StringBuffer nextString = new StringBuffer();
        int index = pos.getIndex();
        while (line.charAt(index) == ' ') {
            ++index;
        }
        
        if (index < line.length()-3) {
            nextString.append(line.charAt(index)).append(line.charAt(index + 1));
        }
        
        return nextString.toString().equals("--");
    }

    /**
     * Skips until the end of the current detail event.
     * 
     * @param line current line
     * @param pos current parse position
     * @param e exception that made skipping necessary
     */
    private void skipUntilEndOfDetail(final String line, final ParsePosition pos, Exception e) {
        skipUntilEndOfDetail(line, pos, 1);
        
        if (getLogger().isLoggable(Level.FINE)) getLogger().fine("Skipping detail event because of " + e);
    }

    /**
     * Skips until end of current detail event. If the detail event contains detail events
     * itself, those are skipped as well.
     * 
     * @param line current line
     * @param pos current parse position
     * @param levelOfDetailEvent level of nesting within detail event
     */
    private void skipUntilEndOfDetail(final String line, final ParsePosition pos, int levelOfDetailEvent) {
        // moving position to the end of this detail event -> skip it
        // if it contains other detail events, skip those as well (recursion)
        int indexOfNextOpeningBracket = line.indexOf("[", pos.getIndex());
        int indexOfNextClosingBracket = line.indexOf("]", pos.getIndex());
        if (indexOfNextOpeningBracket > 0 && indexOfNextOpeningBracket < indexOfNextClosingBracket) {
            ++levelOfDetailEvent;
            pos.setIndex(indexOfNextOpeningBracket + 1);
        }
        else if (indexOfNextClosingBracket > 0) {
            --levelOfDetailEvent;
            pos.setIndex(indexOfNextClosingBracket + 1);
        }
        else {
            // unexpected: no opening and no closing bracket -> skip out
            --levelOfDetailEvent;
        }
        
        if (levelOfDetailEvent > 0) {
            skipUntilEndOfDetail(line, pos, levelOfDetailEvent);
        }
    }
    
}

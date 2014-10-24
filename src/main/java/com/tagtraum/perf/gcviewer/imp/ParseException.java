package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;

import com.tagtraum.perf.gcviewer.util.ParseInformation;

/**
 * Is thrown whenever a ParseError occurs.
 *
 * Date: Jan 30, 2002
 * Time: 6:19:45 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class ParseException extends IOException {
    private String line;
    private ParseInformation parsePosition;

    public ParseException(String s) {
        this(s, null);
    }
    
    public ParseException(String s, String line) {
        super(s);
        this.line = line;
    }

    public ParseException(String s, String line, ParseInformation pos) {
        super(s);
        this.line = line;
        this.parsePosition = pos;
    }

    @Override
    public String getMessage() {
        if (line == null) {
            return super.getMessage();
        }
        return super.getMessage() + (parsePosition != null 
                ? (" Line " + parsePosition.getLineNumber() + ": " + line) 
                        : (" Line: " + line));
    }
    
}

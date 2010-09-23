package com.tagtraum.perf.gcviewer.imp;

import java.io.IOException;

/**
 * Is thrown whenever a ParseError occurs.
 *
 * Date: Jan 30, 2002
 * Time: 6:19:45 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class ParseException extends IOException {
    private String line;

    public ParseException(String s) {
        this(s, null);
    }
    public ParseException(String s, String line) {
        super(s);
        this.line = line;
    }

    public String toString() {
        if (line == null) return super.toString();
        return super.toString() + " Line: " + line;
    }
}

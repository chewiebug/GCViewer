package com.tagtraum.perf.gcviewer.util;

import java.text.ParsePosition;
import java.time.ZonedDateTime;

/**
 * This class holds information about the current parsing process. 
 * 
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 */
public class ParseInformation extends ParsePosition {

    private ZonedDateTime firstDateStamp;
    private int lineNumber;

    public ParseInformation(int index) {
        super(index);
    }
    
    public ZonedDateTime getFirstDateStamp() {
        return firstDateStamp;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }

    public void setFirstDateStamp(ZonedDateTime firstDateStamp) {
        this.firstDateStamp = firstDateStamp;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

}

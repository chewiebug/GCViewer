package com.tagtraum.perf.gcviewer.util;

import java.text.ParsePosition;
import java.util.Date;

/**
 * This class holds information about the current parsing process. 
 * 
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 */
public class ParseInformation extends ParsePosition {

    private Date firstDateStamp;
    private int lineNumber;

    public ParseInformation(int index) {
        super(index);
    }
    
    public Date getFirstDateStamp() {
        return firstDateStamp;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }

    public void setFirstDateStamp(Date firstDateStamp) {
        this.firstDateStamp = firstDateStamp;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

}

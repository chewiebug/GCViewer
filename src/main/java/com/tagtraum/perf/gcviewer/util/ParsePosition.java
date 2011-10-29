package com.tagtraum.perf.gcviewer.util;

public class ParsePosition extends java.text.ParsePosition {

    private int lineNumber;

    public ParsePosition(int index) {
        super(index);
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }

}

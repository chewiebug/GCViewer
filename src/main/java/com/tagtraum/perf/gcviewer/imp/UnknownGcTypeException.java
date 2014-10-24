package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.util.ParseInformation;

/**
 * GC Types are the text introducing specific information for (part of) a GC (e.g. "Full GC") 
 * 
 * @author <a href="mailto:jwu@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 23.10.2011</p>
 */
public class UnknownGcTypeException extends ParseException {

    public UnknownGcTypeException(String gcType, String line, ParseInformation pos) {
        super("Unknown gc type: '" + gcType + "'", line, pos);
    }

    public UnknownGcTypeException(String gcType) {
		this(gcType, null);
	}

    public UnknownGcTypeException(String gcType, String line) {
		this(gcType, line, null);
	}


}

package com.tagtraum.perf.gcviewer.imp;

/**
 * GC Types are the text introducing specific information for (part of) a GC (e.g. "Full GC") 
 * 
 * @author <a href="mailto:jwu@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 23.10.2011</p>
 */
public class UnknownGcTypeException extends ParseException {

    public UnknownGcTypeException(String gcType) {
		this(gcType, null);
	}

    public UnknownGcTypeException(String gcType, String line) {
		super("Unknown gc type: '" + gcType + "'", line);
	}


}

package com.tagtraum.perf.gcviewer.util;

/**
 * 
 * Class to keep the formatted amount, separate from the units.
 * This makes for better export format of data.
 * 
 * @author sean 
 */
public class Formatted {
	StringBuffer bufferValue;
	String units;
	
	public Formatted(StringBuffer toAppendTo, char units) {
		this.bufferValue = toAppendTo;
		this.units = "" + units;
	}

	public Formatted(StringBuffer toAppendTo, String units) {
		this.bufferValue = toAppendTo;
		this.units = units;
	}	
	
	public StringBuffer getBuffer() {
		return bufferValue;
	}
	
	public String getUnits() {
		return units;
	}

	public String getValue() {
		return bufferValue.toString();
	}
}

package com.tagtraum.perf.gcviewer.util;

/**
 * 
 * Class to keep the formatted amount, separate from the units.
 * This makes for better export format of data.
 * 
 * @author sean 
 */
public class FormattedValue {
	StringBuffer bufferValue;
	String units;
	
	public FormattedValue(StringBuffer toAppendTo, char units) {
		this.bufferValue = toAppendTo;
		this.units = "" + units;
	}

	public FormattedValue(StringBuffer toAppendTo, String units) {
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

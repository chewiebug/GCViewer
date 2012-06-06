package com.tagtraum.perf.gcviewer.util;

import java.text.NumberFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;

/**
 * @author sean
 *
 * Allows formatting
 */
public class NumberFormatNoCommas extends NumberFormat {

    private NumberFormat format = NumberFormat.getInstance();
    
    private int iMaxFracDigits;
    private int iMinFracDigits;
    
    public NumberFormatNoCommas() {
    	iMaxFracDigits = 3;
    	iMinFracDigits = format.getMinimumFractionDigits();
    }
        
    public void setMaximumFractionDigits(int newValue) {
    	iMaxFracDigits = newValue;    	
    }

    public void setMinimumFractionDigits(int newValue) {
    	iMinFracDigits = newValue;    	
    }
    
    public String formatDouble(double dVal) {
    	return format(dVal, new StringBuffer(), new FieldPosition(0)).toString();
    }
    
    public StringBuffer format(double dVal, StringBuffer toAppendTo, FieldPosition pos) {
    	int iOrigMaxFracDigits = format.getMaximumFractionDigits();
    	int iOrigMinFracDigits = format.getMinimumFractionDigits();
    	format.setMaximumFractionDigits(iMaxFracDigits);
    	format.setMinimumFractionDigits(iMinFracDigits);
    	String withCommas = format.format(dVal); //call this, to perform rounding
    	format.setMaximumFractionDigits(iOrigMaxFracDigits);
    	format.setMinimumFractionDigits(iOrigMinFracDigits);
    	
    	return removeCommas(toAppendTo, withCommas);
    }    
    
    public StringBuffer format(long lVal, StringBuffer toAppendTo, FieldPosition pos) {
    	String withCommas = format.format(lVal); //call this, to perform rounding
    	return removeCommas(toAppendTo, withCommas);
    }

    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
    	String withCommas = format.format(obj); //call this, to perform rounding
    	return removeCommas(toAppendTo, withCommas);
    }  
    
	/**
	 * @param toAppendTo
	 * @param withCommas
	 * @return
	 */
	private StringBuffer removeCommas(StringBuffer toAppendTo, String withCommas) {
		String noCommas = withCommas.replaceAll(",", "");
    	
    	toAppendTo.append(noCommas);
        return toAppendTo;
	}

    public Number parse(String source, ParsePosition parsePosition) {
        throw new RuntimeException("Not implemented.");
    }
}

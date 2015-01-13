package com.tagtraum.perf.gcviewer.util;

import java.text.NumberFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;

/**
 * MemoryFormat.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class MemoryFormat extends NumberFormat {

	private static final long serialVersionUID = 7269835290617687327L;
	protected static final long ONE_KB = 1024;
	protected static final long TEN_KB = ONE_KB * 10l;
	protected static final long ONE_MB = 1024l * ONE_KB;
	protected static final long TEN_MB = ONE_MB * 10;
    protected NumberFormat format = NumberFormat.getInstance();

    public MemoryFormat() {
        format.setMaximumFractionDigits(3);
    }

    public void setMaximumFractionDigits(int newValue) {
        format.setMaximumFractionDigits(newValue);
    }

    public StringBuffer format(double memInK, StringBuffer toAppendTo, FieldPosition pos) {
        final double bytes = memInK * ONE_KB;
        if (bytes >= TEN_MB) {
            format.format(bytes / ONE_MB, toAppendTo, pos);
            toAppendTo.append('M');
        }
        else if (bytes >= TEN_KB) {
            format.format(bytes / ONE_KB, toAppendTo, pos);
            toAppendTo.append('K');
        }
        else {
            int maxFrac = format.getMaximumFractionDigits();
            format.setMaximumFractionDigits(0);
            format.format(bytes, toAppendTo, pos);
            format.setMaximumFractionDigits(maxFrac);
            toAppendTo.append('B');
        }
        return toAppendTo;
    }

    public StringBuffer format(long memInK, StringBuffer toAppendTo, FieldPosition pos) {
        final double bytes = memInK * ONE_KB;
        if (bytes >= TEN_MB) {
            format.format(bytes / ONE_MB, toAppendTo, pos);
            toAppendTo.append('M');
        }
        else if (bytes >= TEN_KB) {
            format.format(bytes / ONE_KB, toAppendTo, pos);
            toAppendTo.append('K');
        }
        else {
            int maxFrac = format.getMaximumFractionDigits();
            format.setMaximumFractionDigits(0);
            format.format(bytes, toAppendTo, pos);
            format.setMaximumFractionDigits(maxFrac);
            toAppendTo.append('B');
        }
        return toAppendTo;
    }

    public Number parse(String source, ParsePosition parsePosition) {
        throw new RuntimeException("Not implemented.");
    }


    public FormattedValue formatToFormatted(double memInK) {
    	StringBuffer toAppendTo = new StringBuffer();
    	FieldPosition pos = new FieldPosition(0);

    	FormattedValue formed = formatToFormatted(memInK, toAppendTo, pos, false);

    	return formed;
	}

    protected FormattedValue formatToFormatted(double memInK, StringBuffer toAppendTo, FieldPosition pos, boolean bAppendUnits) {

    	char units = ' ';

    	int iOrigMaxFracDigits = format.getMaximumFractionDigits();
        int iOrigMinFracDigits = format.getMinimumFractionDigits();
        format.setMaximumFractionDigits(3);
        format.setMinimumFractionDigits(format.getMinimumFractionDigits());

        final double bytes = memInK * ONE_KB;
        if (bytes >= TEN_MB) {
        	format.format(bytes / ONE_MB, toAppendTo, pos);
            units = 'M';
            if(bAppendUnits)
            	toAppendTo.append(units);
        }
        else if (bytes >= TEN_KB) {
        	format.format(bytes / ONE_KB, toAppendTo, pos);
            units = 'K';
            if(bAppendUnits)
            	toAppendTo.append(units);
        }
        else {
            int maxFrac = format.getMaximumFractionDigits();
            format.setMaximumFractionDigits(0);
            format.format(bytes, toAppendTo, pos);
            format.setMaximumFractionDigits(maxFrac);
            units = 'B';
            if(bAppendUnits)
            	toAppendTo.append(units);
        }

        format.setMaximumFractionDigits(iOrigMaxFracDigits);
        format.setMinimumFractionDigits(iOrigMinFracDigits);

        return new FormattedValue(toAppendTo, units);
    }

}

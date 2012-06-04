package com.tagtraum.perf.gcviewer.util;

import java.text.FieldPosition;

/**
 * MemoryFormat.
 * <p/>
 * Date: Sep 17, 2005
 * Time: 5:13:32 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class MemoryFormatNoCommas extends MemoryFormat {

    public MemoryFormatNoCommas() {
    	format = new NumberFormatNoCommas();
        format.setMaximumFractionDigits(3);
    }

    public Formatted formatToFormatted(double memInK) {
    	StringBuffer toAppendTo = new StringBuffer();
    	FieldPosition pos = new FieldPosition(0);
    	
    	Formatted formed = formatToFormatted(memInK, toAppendTo, pos, false);
    	
    	return formed;
	}
    
    protected Formatted formatToFormatted(double memInK, StringBuffer toAppendTo, FieldPosition pos, boolean bAppendUnits) {

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
        
        return new Formatted(toAppendTo, units);
    }


}

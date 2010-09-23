package com.tagtraum.perf.gcviewer.util;

import java.text.NumberFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;

/**
 * MemoryFormat.
 * <p/>
 * Date: Sep 17, 2005
 * Time: 5:13:32 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class MemoryFormat extends NumberFormat {

    private static final long ONE_KB = 1024;
    private static final long TEN_KB = ONE_KB * 10l;
    private static final long ONE_MB = 1024l * ONE_KB;
    private static final long TEN_MB = ONE_MB * 10;
    private NumberFormat format = NumberFormat.getInstance();

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
}

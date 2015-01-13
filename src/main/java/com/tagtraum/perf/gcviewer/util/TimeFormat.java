package com.tagtraum.perf.gcviewer.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * TimeStampFormatter.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TimeFormat extends DateFormat {

    private static final long ONE_SECOND = 1000l;
    private static final long ONE_MINUTE = ONE_SECOND * 60l;
    private static final long ONE_HOUR = ONE_MINUTE * 60l;
    private static final long ONE_DAY = ONE_HOUR * 24l;

    private SimpleDateFormat millisFormat = new SimpleDateFormat("S");
    private SimpleDateFormat secondsFormat = new SimpleDateFormat("s's'");
    private SimpleDateFormat minuteFormat = new SimpleDateFormat("m'm'");
    private SimpleDateFormat hourFormat = new SimpleDateFormat("H'h'");
    private DateFormat fullFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

    public TimeFormat() {
        final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
        millisFormat.setTimeZone(utcTimeZone);
        minuteFormat.setTimeZone(utcTimeZone);
        hourFormat.setTimeZone(utcTimeZone);
    }

    //@author sean
    public FormattedValue formatToFormatted(Date date) {
        StringBuffer appendTo = new StringBuffer();
        appendTo.append(date.getTime() / ONE_SECOND);
        FormattedValue formed = new FormattedValue(appendTo, "s");
        return formed;
    }

    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        long time = date.getTime();
        if (time >= ONE_DAY * 365) {
            return fullFormat.format(date, toAppendTo, fieldPosition);
        }
        if (time >= ONE_DAY * 3) {
            toAppendTo.append(time / ONE_DAY);
            toAppendTo.append('d');
            if (time % ONE_DAY != 0) {
                hourFormat.format(date, toAppendTo, fieldPosition);
            }
        }
        else if (time >= ONE_HOUR) {
            toAppendTo.append(time / ONE_HOUR);
            toAppendTo.append('h');
        }

        if (time >= ONE_MINUTE && time % ONE_HOUR !=0) {
            minuteFormat.format(date, toAppendTo, fieldPosition);
        }
        if (time >= ONE_SECOND && time % ONE_MINUTE !=0) {
            secondsFormat.format(date, toAppendTo, fieldPosition);
        }
        return toAppendTo;
    }

    public Date parse(String source, ParsePosition pos) {
        throw new RuntimeException("Not implemented.");
    }

}

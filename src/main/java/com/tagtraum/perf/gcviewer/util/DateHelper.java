package com.tagtraum.perf.gcviewer.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 02.01.2018</p>
 */
public class DateHelper {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static ZonedDateTime parseDate(String dateStampAsString) {
        return ZonedDateTime.parse(dateStampAsString, DateHelper.DATE_TIME_FORMATTER);
    }

    public static String formatDate(ZonedDateTime dateTime) {
        return DATE_TIME_FORMATTER.format(dateTime);
    }

}

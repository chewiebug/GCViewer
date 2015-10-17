package com.tagtraum.perf.gcviewer.log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Formatter for a {@link LogRecord} to be displayed in a text area.
 * 
 * <ul><li>Date: Oct 3, 2005</li>
 * <li>Time: 4:50:41 PM</li></ul>
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TextAreaFormatter extends Formatter {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getLevel().getLocalizedName());
        sb.append(" [");
        final String logger = record.getSourceClassName();
        sb.append(logger.substring(logger.lastIndexOf('.') + 1));
        sb.append("]: ");
        sb.append(record.getMessage());
        sb.append(LINE_SEPARATOR);
        if (record.getThrown() != null) {
            try (StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw)) {

                record.getThrown().printStackTrace(pw);
                sb.append(sw.toString());
            }
            catch (IOException e) {
                // ignore
            }
        }
        return sb.toString();
    }
}

package com.tagtraum.perf.gcviewer.log;

import javax.swing.*;
import java.util.logging.*;

/**
 * TextAreaLogHandler.
 * <p/>
 * Date: Sep 29, 2005
 * Time: 9:20:36 PM
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TextAreaLogHandler extends Handler {

    private JTextArea textArea;
    private boolean errors;
    private int errorCount;

    public TextAreaLogHandler() {
        this.textArea = new JTextArea();
        setFormatter(new TextAreaFormatter());
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public boolean hasErrors() {
        return errors;
    }
    
    public int getErrorCount() {
    	return errorCount;
    }

    public void publish(LogRecord record) {
        try {
            if (isLoggable(record)) {
            	++errorCount;
                if (!errors) {
                    final int level = record.getLevel().intValue();
                    errors = level >= Level.WARNING.intValue() && level < Level.OFF.intValue();
                }
                String formattedRecord = null;
                try {
                    formattedRecord = getFormatter().format(record);
                } catch (Exception e) {
                    reportError(e.toString(), e, ErrorManager.FORMAT_FAILURE);
                }
                try {
                    textArea.append(formattedRecord);
                } catch (Exception e) {
                    reportError(e.toString(), e, ErrorManager.WRITE_FAILURE);
                }
            }
        } catch (Exception e) {
            reportError(e.toString(), e, ErrorManager.GENERIC_FAILURE);
        }
    }

    public void flush() {
    }

    public void close() throws SecurityException {
    }
}

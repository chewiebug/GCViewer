package com.tagtraum.perf.gcviewer.log;

import javax.swing.*;
import java.util.logging.*;

/**
 * TextAreaLogHandler.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TextAreaLogHandler extends Handler {

    private JTextArea textArea;
    private boolean hasErrors;
    private int errorCount;

    public TextAreaLogHandler() {
        this.textArea = new JTextArea();
        setFormatter(new TextAreaFormatter());
    }

    /**
     * @see java.util.logging.Handler#close()
     */
    public void close() throws SecurityException {
    }

    /**
     * @see java.util.logging.Handler#flush()
     */
    public void flush() {
    }

    public int getErrorCount() {
    	return errorCount;
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    /**
     * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
     */
    public void publish(LogRecord record) {
        try {
            if (isLoggable(record)) {
                final int level = record.getLevel().intValue();
                if (level >=Level.WARNING.intValue() && level < Level.OFF.intValue()) {
                    ++errorCount;
                }
                if (!hasErrors) {
                    hasErrors = level >= Level.WARNING.intValue() && level < Level.OFF.intValue();
                }
                try {
                    String formattedRecord = getFormatter().format(record);
                    textArea.append(formattedRecord);
                } 
                catch (RuntimeException e) {
                    reportError(e.toString(), e, ErrorManager.WRITE_FAILURE);
                }
            }
        }
        catch (Exception e) {
            reportError(e.toString(), e, ErrorManager.GENERIC_FAILURE);
        }
    }
    
    /**
     * Resets all internal state to an initial state and is ready to receive log events.
     */
    public void reset() {
        textArea.setText("");
        errorCount = 0;
        hasErrors = false;
    }
}

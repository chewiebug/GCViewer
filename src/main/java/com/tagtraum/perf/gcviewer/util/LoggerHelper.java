package com.tagtraum.perf.gcviewer.util;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class contains convenience methods to support logging.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p> created on: 26.02.2012</p>
 */
public class LoggerHelper {

    /**
     * Logs a given exception using the given <code>logger</code>.
     * 
     * @param logger logger of the class where the exception occurred
     * @param level level to be used
     * @param msg message to be logged
     * @param t exception thrown
     */
    public static void logException(Logger logger, Level level, String msg, Throwable t) {
        StackTraceElement callerStackTraceElement = getCallerStackTraceElement();
        if (callerStackTraceElement != null) {
            // using this stackTraceElements allows to log the message using the caller's class / method name
            logger.logp(level, 
                    callerStackTraceElement.getClassName(), 
                    callerStackTraceElement.getMethodName(), 
                    msg, 
                    t);
        }
        else {
            // we haven't found the caller, so just log what we have
            LogRecord logRecord = new LogRecord(level, msg);
            logRecord.setThrown(t);
            logger.log(logRecord);
        }
    }

    /**
     * Finds the first StackTraceElement outside this loggerHelper.
     */
    private static StackTraceElement getCallerStackTraceElement() {
        Throwable throwable = new Throwable();

        String logClassName = LoggerHelper.class.getName();
        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            String cname = stackTraceElement.getClassName();
            if (!cname.equals(logClassName)) {
                return stackTraceElement;
            }
        }

        // should not happen, but means that we haven't found a stackTraceElement outside this class 
        return null;
    }
}

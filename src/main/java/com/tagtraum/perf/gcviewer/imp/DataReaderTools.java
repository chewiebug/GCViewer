package com.tagtraum.perf.gcviewer.imp;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tools useful for (most) DataReader implementations.
 */
public class DataReaderTools {

    private Logger logger;

    public DataReaderTools(Logger logger) {
        this.logger = logger;
    }

    /**
     * Returns the amount of memory in kilobyte. Depending on <code>memUnit</code>, input is
     * converted to kilobyte.
     * @param memoryValue amount of memory
     * @param memUnit memory unit
     * @param line line that is parsed
     * @return amount of memory in kilobyte
     */
    public int getMemoryInKiloByte(double memoryValue, char memUnit, String line) {
        if ('B' == memUnit) {
            return (int) Math.rint(memoryValue / 1024);
        }
        else if ('K' == memUnit) {
            return (int) Math.rint(memoryValue);
        }
        else if ('M' == memUnit) {
            return (int) Math.rint(memoryValue * 1024);
        }
        else if ('G' == memUnit) {
            return (int) Math.rint(memoryValue * 1024*1024);
        }
        else {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("unknown memoryunit '" + memUnit + "' in line " + line);
            }
            return 1;
        }
    }

}

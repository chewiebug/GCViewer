package com.tagtraum.perf.gcviewer.imp;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Simple implementation of a handler for java.util.logging to support checks of logging
 * in unittests.
 *  
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 08.03.2013</p>
 */
public class TestLogHandler extends Handler {

    private List<LogRecord> recordList = new LinkedList<LogRecord>();
	
	public int getCount() {
		return recordList.size();
	}
	
	public List<LogRecord> getLogRecords() {
	    return recordList;
	}
	
	@Override
	public void publish(LogRecord record) {
	    if (isLoggable(record)) {
	        recordList.add(record);
	    }
	}

	@Override
	public void flush() {
		// nothing to do
	}

	@Override
	public void close() throws SecurityException {
		// nothing to do
	}

}

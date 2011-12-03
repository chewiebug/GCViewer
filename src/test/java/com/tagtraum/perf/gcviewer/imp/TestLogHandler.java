package com.tagtraum.perf.gcviewer.imp;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class TestLogHandler extends Handler {

    private List<LogRecord> recordList = new LinkedList<LogRecord>();
	
	public int getCount() {
		return recordList.size();
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

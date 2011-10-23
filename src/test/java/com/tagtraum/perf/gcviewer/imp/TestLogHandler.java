package com.tagtraum.perf.gcviewer.imp;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class TestLogHandler extends Handler {

	private int count;
	
	public int getCount() {
		return count;
	}
	
	@Override
	public void publish(LogRecord record) {
			++count;
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

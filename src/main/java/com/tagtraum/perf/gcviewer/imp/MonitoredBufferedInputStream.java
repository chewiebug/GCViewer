package com.tagtraum.perf.gcviewer.imp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Like BufferedInputStream, but keeps counter of total bytes received.
 * 
 * @see #getBytesRead
 */
public class MonitoredBufferedInputStream extends BufferedInputStream {

	public interface ProgressCallback {
		void publishP(Integer... chunks);
		void updateProgress(int progress);
		String getLoggerName();
	}
	
	private final ProgressCallback callback;
	private final AtomicInteger percentRead = new AtomicInteger(0);
	private final AtomicLong firstPercentBound;
	private final long contentLength;
	private final AtomicLong bytesRead = new AtomicLong(0L);
	
	public MonitoredBufferedInputStream(InputStream in, long contentLength, ProgressCallback callback) {
		this(in, 8192, contentLength, callback);
	}

	public MonitoredBufferedInputStream(InputStream in, int size, long contentLength, ProgressCallback callback) {
		super(in, size);
		this.callback = callback;
		this.contentLength = contentLength;
		this.firstPercentBound = new AtomicLong(contentLength/100L);
	}
	
	private void updateCounters(final long increment) {
		final long newValue = bytesRead.addAndGet(increment);
		final long nextBound = firstPercentBound.longValue();
		if ((newValue > nextBound) && (nextBound != 0L)) {
			final int percentage = Math.min(100, (int)Math.floor(100 * newValue/contentLength));
			percentRead.set(percentage);
			// to next percentage value
			firstPercentBound.set((1 + percentRead.get()) * contentLength/100);
			
			if (callback != null) {
				callback.updateProgress(percentage);
				callback.publishP(percentage);
			}
		}
	}

	@Override
	public int read() throws IOException {
		final int result = super.read();
		updateCounters(result);
		return result;
	}

	@Override
	public int read(byte[] b) throws IOException {
		final int result = super.read(b); 
		updateCounters(result);
		return result;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		final int result = super.read(b, off, len); 
		updateCounters(result);
		return result;
	}

	@Override
	public long skip(long n) throws IOException {
		final long result = super.skip(n); 
		updateCounters(result);
		return result;
	}

	/**
	 * Get the number of bytes read since creation or since last resetBytesRead() call.
	 * 
	 * @return The number of bytes read
	 * @see MonitoredBufferedInputStream#resetBytesRead
	 */	
	public long getBytesRead() {
		return bytesRead.longValue();
	}

	/**
	 * Resets the number of bytes read.
	 */
	public void resetBytesRead() {
		bytesRead.set(0L);
	}

	/**
	 * Calculate percentage read.
	 * 
	 * @return percentage read or -1 if not known.
	 */
	public int getPercentageRead() {
		if (firstPercentBound.get() > 0) {
			return percentRead.get();
		}
		final long result = contentLength == 0L ? -1L : 100 * (getBytesRead()/contentLength);
		return result >= 100L ? -1 : (int)result;
	}

	public long getContentLength() {
		return contentLength;
	}
}

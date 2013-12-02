package com.tagtraum.perf.gcviewer.model;

import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Identifies a GC resource: a file or URL resource containing GC info.
 *
 * @author Hans Bausewein
 * <p>Date: November 8, 2013</p>
 */
public class GCResource {
	
	private static final AtomicInteger COUNT = new AtomicInteger(0); 
	
	private final Logger logger;
	private final URL url;
	private long lastModified;
	private long length;
	private long bytesProcessed;	
	
	public GCResource(URL url) {
		super();
		
		if (url == null) {
			throw new IllegalArgumentException("URL cannot be null");
		}
		
		this.url = url;
		
		final String loggerName = "GCResource".concat(Integer.toString(COUNT.incrementAndGet()));
		this.logger = Logger.getLogger(loggerName);
	}
	
	public long getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
	
	public long getLength() {
		return length;
	}
	
	public void setLength(long length) {
		this.length = length;
	}
	
	public long getBytesProcessed() {
		return bytesProcessed;
	}
	
	public void setBytesProcessed(long bytesProcessed) {
		this.bytesProcessed = bytesProcessed;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public URL getUrl() {
		return url;
	}
	
	public String getName() {
		return url.toString();
	}

	@Override
	public int hashCode() {		
		return url.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean equal = obj instanceof GCResource;
			
		if (equal) {
			GCResource other = (GCResource)obj;
			equal = url.equals(other.getUrl());
		}
		return equal;
	}
    
}

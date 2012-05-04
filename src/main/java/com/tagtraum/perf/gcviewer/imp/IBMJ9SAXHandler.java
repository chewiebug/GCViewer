/*
 * =================================================
 * Copyright 2007 Justin Kilimnik (IBM UK) 
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.perf.gcviewer.imp;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Simple (only for the -Xgcpolicy:optthruput output) IBMJ9 verbose GC reader. 
 * Implemented as a SAX parser since XML based.
 * @author <a href="mailto:justink@au1.ibm.com">Justin Kilimnik (IBM)</a>
 * @version $Id$
 */
public class IBMJ9SAXHandler extends DefaultHandler {
	private GCModel model;
	private DateFormat cycleStartGCFormat5 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US);
	private DateFormat cycleStartGCFormat6 = new SimpleDateFormat("MMM dd HH:mm:ss yyyy", Locale.US);
	private DateFormat current = cycleStartGCFormat5;
	protected AF currentAF;
	int currentTenured = 0; // 0 = none, 1=pre, 2=mid, 3=end
	private static Logger LOG = Logger.getLogger(IBMJ9SAXHandler.class.getName());
	Date begin = null;
	
	public IBMJ9SAXHandler(GCModel model) {
		this.model = model;
		
	}

	protected Date parseTime(String ts) throws ParseException{
		try{
			return current.parse(ts);
		}catch(ParseException e){
			if(current != cycleStartGCFormat6){
				current = cycleStartGCFormat6;
				return parseTime(ts);
			}
			throw e;
		}
	}
	
	public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {
		try {
		//System.out.println("START: [" + qName + "]");
		if(currentAF == null){
			
		
		if("af".equals(qName)){
			currentAF = new AF();
			String type = attrs.getValue("type");
			String id = attrs.getValue("id");
			String ts = attrs.getValue("timestamp");
			currentAF.id = id;
			currentAF.type = type;
			final Date date = parseTime(ts);
			currentAF.timestamp = date;
			if(begin == null) {
				begin = date;
				currentAF.elapsedTime = 0L;
			} else {
				currentAF.elapsedTime = (currentAF.timestamp.getTime() - begin.getTime())/1000;
				System.out.println("ElapsedTime: " + currentAF.elapsedTime);
			}
			//System.out.println("START: [af, " + type + "]");
			
		}
		} else if(currentAF != null){
			if("time".equals(qName)){
//				String pauseStr = attrs.getValue("exclusiveaccessms");
//				double pause = -1D;
//				if(pauseStr != null){
//					pause = Double.parseDouble(pauseStr);
//					currentAF.totalTime = pause/1000;
//				}
				
				String totalStr = attrs.getValue("totalms");
				double total = -1D;
				if(totalStr != null){
					total = Double.parseDouble(totalStr);
					currentAF.totalTime = total/1000;
				}
				
			} else if("gc".equals(qName)){
				String type = attrs.getValue("type");
				currentAF.gcType = type;
			} else if("timesms".equals(qName)){
				String markStr = attrs.getValue("mark");
				double mark = -1D;
				if(markStr != null){
					mark = Double.parseDouble(markStr);
					currentAF.gcTimeMark = mark;
				}				
				String sweepStr = attrs.getValue("sweep");
				double sweep = -1D;
				if(sweepStr != null){
					mark = Double.parseDouble(sweepStr);
					currentAF.gcTimeSweep = sweep;
				}					
			} else if("tenured".equals(qName)){
				currentTenured++;
				String freeStr = attrs.getValue("freebytes");
				int free = -1;
				if(freeStr != null){
					free = Integer.parseInt(freeStr);
				}
				String totalStr = attrs.getValue("totalbytes");
				int total = -1;
				if(totalStr != null){
					total = Integer.parseInt(totalStr);
				}
				
				// For now only care about Total - don't break into SOA and LOA
				if(currentTenured == 1){
					currentAF.initialFreeBytes = free/1000;
					currentAF.initialTotalBytes = total/1000;
				} else if(currentTenured == 2){
					// ignore
				} else if(currentTenured == 3){
					currentAF.afterFreeBytes = free/1000;
					currentAF.afterTotalBytes = total/1000;
				} else {
					LOG.warning("currentTenured is > 3!");
				}
			} else if("soa".equals(qName)){
				String freeStr = attrs.getValue("freebytes");
				int free = -1;
				if(freeStr != null){
					free = Integer.parseInt(freeStr);
				}
				String totalStr = attrs.getValue("totalbytes");
				int total = -1;
				if(totalStr != null){
					total = Integer.parseInt(totalStr);
				}
				
				if(currentTenured == 1){
					currentAF.initialSOAFreeBytes = free/1000;
					currentAF.initialSOATotalBytes = total/1000;
				} else if(currentTenured == 2){
					// ignore
				} else if(currentTenured == 3){
					currentAF.afterSOAFreeBytes = free/1000;
					currentAF.afterSOATotalBytes = total/1000;
				} else {
					LOG.warning("currentTenured is > 3!");
				}
			} else if("loa".equals(qName)){
				String freeStr = attrs.getValue("freebytes");
				int free = -1;
				if(freeStr != null){
					free = Integer.parseInt(freeStr);
				}
				String totalStr = attrs.getValue("totalbytes");
				int total = -1;
				if(totalStr != null){
					total = Integer.parseInt(totalStr);
				}
				
				if(currentTenured == 1){
					currentAF.initialLOAFreeBytes = free/1000;
					currentAF.initialLOATotalBytes = total/1000;
				} else if(currentTenured == 2){
					// ignore
				} else if(currentTenured == 3){
					currentAF.afterLOAFreeBytes = free/1000;
					currentAF.afterLOATotalBytes = total/1000;
				} else {
					LOG.warning("currentTenured is > 3!");
				}
			}
		}
		
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void endElement(String namespaceURI, String sName, // simple name
			String qName // qualified name
	) throws SAXException {
		if("af".equals(qName)){
			System.out.println("In AF endElement!");
			if(currentAF != null){
				GCEvent event = new GCEvent();
				if(!"tenured".equals(currentAF.type))
				{
					LOG.warning("Unhandled AF type: " + currentAF.type);
				}
				if(!"global".equals(currentAF.gcType))
				{
					LOG.warning("Different GC type: " + currentAF.gcType);
				} else
				{
					event.setType(AbstractGCEvent.Type.FULL_GC);
				}
				if(currentAF.initialTotalBytes != -1 && currentAF.initialFreeBytes != -1){
					int preUsed = currentAF.initialTotalBytes - currentAF.initialFreeBytes;
					event.setPreUsed(preUsed);
				}

				if(currentAF.afterTotalBytes != -1 && currentAF.afterFreeBytes != -1){
					int postUsed = currentAF.afterTotalBytes - currentAF.afterFreeBytes;
					event.setPostUsed(postUsed);
				}
				
				if(currentAF.afterTotalBytes != -1) {
					event.setTotal(currentAF.afterTotalBytes);
				}
				
                //event.setTimestamp(currentAF.timestamp.getTime());
				event.setTimestamp(currentAF.elapsedTime);
                
                if(currentAF.totalTime != -1){
                	event.setPause(currentAF.totalTime);
                }
                
                if(currentAF.afterSOATotalBytes != -1 && currentAF.afterSOAFreeBytes != -1 && currentAF.initialSOAFreeBytes  != -1 && currentAF.initialSOATotalBytes != -1){
                	int preUsed = currentAF.initialSOATotalBytes - currentAF.initialSOAFreeBytes;
                	int postUsed = currentAF.afterSOATotalBytes - currentAF.afterSOAFreeBytes;
                	final GCEvent detailEvent = new GCEvent();
                	detailEvent.setTimestamp(currentAF.elapsedTime);
                	detailEvent.setType(AbstractGCEvent.Type.PS_YOUNG_GEN);
                	detailEvent.setTenuredDetail(true);
                	detailEvent.setPreUsed(preUsed);
                	detailEvent.setPostUsed(postUsed);
                	detailEvent.setTotal(currentAF.afterSOATotalBytes);
                	event.add(detailEvent);
                }

                if(currentAF.afterLOATotalBytes != -1 && currentAF.afterLOAFreeBytes != -1 && currentAF.initialLOAFreeBytes  != -1 && currentAF.initialLOATotalBytes != -1){
                	int preUsed = currentAF.initialLOATotalBytes - currentAF.initialLOAFreeBytes;
                	int postUsed = currentAF.afterLOATotalBytes - currentAF.afterLOAFreeBytes;
                	final GCEvent detailEvent = new GCEvent();
                	detailEvent.setTimestamp(currentAF.elapsedTime);
                	detailEvent.setType(AbstractGCEvent.Type.PS_OLD_GEN);
                	detailEvent.setTenuredDetail(true);
                	detailEvent.setPreUsed(preUsed);
                	detailEvent.setPostUsed(postUsed);
                	detailEvent.setTotal(currentAF.afterLOATotalBytes);
                	event.add(detailEvent);
                }
                
                
                
                model.add(event);
                currentTenured = 0;
    			currentAF = null;
			}
			else
			{
				LOG.warning("Found end <af> tag with no begin tag");
			}
			
		}	
	}

}

/**
 * Holder of GC information for standard Allocation Failures
 */
class AF{
	String type;
	String id;
	Date timestamp;
	long elapsedTime;
	double intervalms=-1;
	int minRequestedBytes=-1;
	double timeExclusiveAccessMs=-1;
	int initialFreeBytes = -1;
	int initialTotalBytes = -1;
	int initialSOAFreeBytes = -1;
	int initialSOATotalBytes = -1;
	int initialLOAFreeBytes = -1;
	int initialLOATotalBytes = -1;	
	int afterFreeBytes = -1;
	int afterTotalBytes = -1;
	int afterSOAFreeBytes = -1;
	int afterSOATotalBytes = -1;
	int afterLOAFreeBytes = -1;
	int afterLOATotalBytes = -1;
	String gcType;
	double gcIntervalms=-1;
	int gcSoftRefsCleared = -1;
	int gcWeakRefsCleared = -1;
	int gcPhantomRefsCleared = -1;
	double gcTimeMark = -1;
	double gcTimeSweep = -1;
	double gcTimeCompact = -1;
	double gcTime = -1;
	double totalTime = -1;	
}

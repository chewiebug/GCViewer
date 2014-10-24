/*
 * =================================================
 * Copyright 2007 Justin Kilimnik (IBM UK) 
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.perf.gcviewer.imp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.util.NumberParser;

/**
 * Simple (only for the -Xgcpolicy:optthruput output) IBMJ9 verbose GC reader.
 * Implemented as a SAX parser since XML based.
 * 
 * @author <a href="mailto:justink@au1.ibm.com">Justin Kilimnik (IBM)</a>
 */
public class IBMJ9SAXHandler extends DefaultHandler {
    private GCModel model;
    private GCResource gcResource;
    private DateFormat cycleStartGCFormat5 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US);
    private DateFormat cycleStartGCFormat6 = new SimpleDateFormat("MMM dd HH:mm:ss yyyy", Locale.US);
    private DateFormat current = cycleStartGCFormat5;
    protected AF currentAF;
    int currentTenured = 0; // 0 = none, 1=pre, 2=mid, 3=end
    private Date begin = null;

    public IBMJ9SAXHandler(GCResource gcResource, GCModel model) {
        this.gcResource = gcResource;
        this.model = model;
    }

    private Logger getLogger() {
        return gcResource.getLogger();
    }
    
    protected Date parseTime(String ts) throws ParseException {
        try {
            return current.parse(ts);
        } 
        catch (ParseException e) {
            if (current != cycleStartGCFormat6) {

                current = cycleStartGCFormat6;
                return parseTime(ts);
            }
            throw e;
        }
    }

    public void startElement(String namespaceURI, String sName, String qName,
            Attributes attrs) throws SAXException {
        try {
            // System.out.println("START: [" + qName + "]");
            if (currentAF == null) {

                if ("af".equals(qName)) {
                    currentAF = new AF();
                    String type = attrs.getValue("type");
                    String id = attrs.getValue("id");
                    String ts = attrs.getValue("timestamp");
                    currentAF.id = id;
                    currentAF.type = type;
                    final Date date = parseTime(ts);
                    currentAF.timestamp = date;
                    if (begin == null) {
                        begin = date;
                        currentAF.elapsedTime = 0L;
                    } 
                    else {
                        currentAF.elapsedTime = (currentAF.timestamp.getTime() - begin
                                .getTime()) / 1000;
                        System.out.println("ElapsedTime: "
                                + currentAF.elapsedTime);
                    }
                    // System.out.println("START: [af, " + type + "]");

                }
            } 
            else if (currentAF != null) {
                if ("time".equals(qName)) {
                    // String pauseStr = attrs.getValue("exclusiveaccessms");
                    // double pause = -1D;
                    // if(pauseStr != null){
                    // pause = NumberParser.parseDouble(pauseStr);
                    // currentAF.totalTime = pause/1000;
                    // }

                    String totalStr = attrs.getValue("totalms");
                    double total = -1D;
                    if (totalStr != null) {
                        total = NumberParser.parseDouble(totalStr);
                        currentAF.totalTime = total / 1000;
                    }

                } 
                else if ("gc".equals(qName)) {
                    String type = attrs.getValue("type");
                    currentAF.gcType = type;
                } 
                else if ("timesms".equals(qName)) {
                    String markStr = attrs.getValue("mark");
                    double mark = -1D;
                    if (markStr != null) {
                        mark = NumberParser.parseDouble(markStr);
                        currentAF.gcTimeMark = mark;
                    }
                    String sweepStr = attrs.getValue("sweep");
                    double sweep = -1D;
                    if (sweepStr != null) {
                        mark = NumberParser.parseDouble(sweepStr);
                        currentAF.gcTimeSweep = sweep;
                    }
                }
                else if ("tenured".equals(qName)) {
                    currentTenured++;
                    String freeStr = attrs.getValue("freebytes");
                    long free = -1;
                    if (freeStr != null) {
                        free = Long.parseLong(freeStr);
                    }
                    String totalStr = attrs.getValue("totalbytes");
                    long total = -1;
                    if (totalStr != null) {
                        total = Long.parseLong(totalStr);
                    }

                    // For now only care about Total - don't break into SOA and
                    // LOA
                    if (currentTenured == 1) {
                        currentAF.initialFreeBytes = free;
                        currentAF.initialTotalBytes = total;
                    } 
                    else if (currentTenured == 2) {
                        // ignore
                    }
                    else if (currentTenured == 3) {
                        currentAF.afterFreeBytes = free;
                        currentAF.afterTotalBytes = total;
                    } 
                    else {
                        getLogger().warning("currentTenured is > 3!");
                    }
                }
                else if ("soa".equals(qName)) {
                    String freeStr = attrs.getValue("freebytes");
                    long free = -1;
                    if (freeStr != null) {
                        free = Long.parseLong(freeStr);
                    }
                    String totalStr = attrs.getValue("totalbytes");
                    long total = -1;
                    if (totalStr != null) {
                        total = Long.parseLong(totalStr);
                    }

                    if (currentTenured == 1) {
                        currentAF.initialSOAFreeBytes = free;
                        currentAF.initialSOATotalBytes = total;
                    }
                    else if (currentTenured == 2) {
                        // ignore
                    }
                    else if (currentTenured == 3) {
                        currentAF.afterSOAFreeBytes = free;
                        currentAF.afterSOATotalBytes = total;
                    }
                    else {
                        getLogger().warning("currentTenured is > 3!");
                    }
                } 
                else if ("loa".equals(qName)) {
                    String freeStr = attrs.getValue("freebytes");
                    long free = -1;
                    if (freeStr != null) {
                        free = Long.parseLong(freeStr);
                    }
                    String totalStr = attrs.getValue("totalbytes");
                    long total = -1;
                    if (totalStr != null) {
                        total = Long.parseLong(totalStr);
                    }

                    if (currentTenured == 1) {
                        currentAF.initialLOAFreeBytes = free;
                        currentAF.initialLOATotalBytes = total;
                    } 
                    else if (currentTenured == 2) {
                        // ignore
                    } 
                    else if (currentTenured == 3) {
                        currentAF.afterLOAFreeBytes = free;
                        currentAF.afterLOATotalBytes = total;
                    } 
                    else {
                        getLogger().warning("currentTenured is > 3!");
                    }
                }
            }

        } 
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void endElement(String namespaceURI, String simpleName,
            String qualifiedName) throws SAXException {

        if ("af".equals(qualifiedName)) {
            System.out.println("In AF endElement!");
            if (currentAF != null) {
                GCEvent event = new GCEvent();
                if (!"tenured".equals(currentAF.type)) {
                    getLogger().warning("Unhandled AF type: " + currentAF.type);
                }
                if (!"global".equals(currentAF.gcType)) {
                    getLogger().warning("Different GC type: " + currentAF.gcType);
                } 
                else {
                    event.setType(AbstractGCEvent.Type.FULL_GC);
                }
                if (currentAF.initialTotalBytes != -1
                        && currentAF.initialFreeBytes != -1) {
                    event.setPreUsed(currentAF.getPreUsedInKb());
                }

                if (currentAF.afterTotalBytes != -1
                        && currentAF.afterFreeBytes != -1) {
                    event.setPostUsed(currentAF.getPostUsedInKb());
                }

                if (currentAF.afterTotalBytes != -1) {
                    event.setTotal(currentAF.getTotalInKb());
                }

                // event.setTimestamp(currentAF.timestamp.getTime());
                event.setTimestamp(currentAF.elapsedTime);

                if (currentAF.totalTime >= 0) {
                    event.setPause(currentAF.totalTime);
                }

                if (currentAF.afterSOATotalBytes != -1
                        && currentAF.afterSOAFreeBytes != -1
                        && currentAF.initialSOAFreeBytes != -1
                        && currentAF.initialSOATotalBytes != -1) {
                    
                    final GCEvent detailEvent = new GCEvent();
                    detailEvent.setTimestamp(currentAF.elapsedTime);
                    detailEvent.setType(AbstractGCEvent.Type.PS_YOUNG_GEN);
                    detailEvent.setTenuredDetail(true);
                    detailEvent.setPreUsed(currentAF.getPreUsedSoaInKb());
                    detailEvent.setPostUsed(currentAF.getPostUsedSoaInKb());
                    detailEvent.setTotal(currentAF.getTotalSoaInKb());
                    event.add(detailEvent);
                }

                if (currentAF.afterLOATotalBytes != -1
                        && currentAF.afterLOAFreeBytes != -1
                        && currentAF.initialLOAFreeBytes != -1
                        && currentAF.initialLOATotalBytes != -1) {
                    
                    final GCEvent detailEvent = new GCEvent();
                    detailEvent.setTimestamp(currentAF.elapsedTime);
                    detailEvent.setType(AbstractGCEvent.Type.PS_OLD_GEN);
                    detailEvent.setTenuredDetail(true);
                    detailEvent.setPreUsed(currentAF.getPreUsedLoaInKb());
                    detailEvent.setPostUsed(currentAF.getPostUsedLoaInKb());
                    detailEvent.setTotal(currentAF.getTotalLoaInKb());
                    event.add(detailEvent);
                }

                model.add(event);
                currentTenured = 0;
                currentAF = null;
            } 
            else {
                getLogger().warning("Found end <af> tag with no begin tag");
            }

        }
    }

}

/**
 * Holder of GC information for standard Allocation Failures
 */
class AF {
    String type;
    String id;
    Date timestamp;
    long elapsedTime;
    double intervalms = -1;
    long minRequestedBytes = -1;
    double timeExclusiveAccessMs = -1;
    long initialFreeBytes = -1;
    long initialTotalBytes = -1;
    long initialSOAFreeBytes = -1;
    long initialSOATotalBytes = -1;
    long initialLOAFreeBytes = -1;
    long initialLOATotalBytes = -1;
    long afterFreeBytes = -1;
    long afterTotalBytes = -1;
    long afterSOAFreeBytes = -1;
    long afterSOATotalBytes = -1;
    long afterLOAFreeBytes = -1;
    long afterLOATotalBytes = -1;
    String gcType;
    double gcIntervalms = -1;
    int gcSoftRefsCleared = -1;
    int gcWeakRefsCleared = -1;
    int gcPhantomRefsCleared = -1;
    double gcTimeMark = -1;
    double gcTimeSweep = -1;
    double gcTimeCompact = -1;
    double gcTime = -1;
    double totalTime = -1;

    public int getPreUsedInKb() {
        return (int) ((initialTotalBytes - initialFreeBytes) / 1024);
    }

    public int getPostUsedInKb() {
        return (int) ((afterTotalBytes - afterFreeBytes) / 1024);
    }

    public int getTotalInKb() {
        return (int) (afterTotalBytes / 1024);
    }

    public int getPreUsedSoaInKb() {
        return (int) ((initialSOATotalBytes - initialSOAFreeBytes) / 1024);
    }

    public int getPostUsedSoaInKb() {
        return (int) ((afterSOATotalBytes - afterSOAFreeBytes) / 1024);
    }

    public int getTotalSoaInKb() {
        return (int) (afterSOATotalBytes / 1024);
    }

    public int getPreUsedLoaInKb() {
        return (int) ((initialLOATotalBytes - initialLOAFreeBytes) / 1024);
    }

    public int getPostUsedLoaInKb() {
        return (int) ((afterLOATotalBytes - afterLOAFreeBytes) / 1024);
    }

    public int getTotalLoaInKb() {
        return (int) (afterLOATotalBytes / 1024);
    }
}

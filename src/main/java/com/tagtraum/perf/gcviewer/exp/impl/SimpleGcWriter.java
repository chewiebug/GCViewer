package com.tagtraum.perf.gcviewer.exp.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.exp.AbstractDataWriter;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Generation;

/**
 * <p>Exports stop-the-world events in the "simple gc log" format (compatible to GCHisto).</p>
 * <p>This writer writes every event on its own line with the following format<br/>
 * GC_TYPE START_SEC DURATION_SEC</p> 
 * 
 * @see <a href="http://mail.openjdk.java.net/pipermail/hotspot-gc-use/2012-November/001428.html">http://mail.openjdk.java.net/pipermail/hotspot-gc-use/2012-November/001428.html</a>
 * @see <a href="http://java.net/projects/gchisto">GCHisto</a>
 * @see <a href="https://svn.java.net/svn/gchisto~svn/trunk/www/index.html">GCHisto documentation</a>
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 08.12.2012</p>
 */
public class SimpleGcWriter extends AbstractDataWriter {

    public SimpleGcWriter(OutputStream outputStream) {
        super(outputStream);
    }

    /**
     * @see com.tagtraum.perf.gcviewer.exp.AbstractDataWriter#write(com.tagtraum.perf.gcviewer.model.GCModel)
     */
    @Override
    public void write(GCModel model) throws IOException {
        Iterator<AbstractGCEvent<?>> i = model.getEvents();
        while (i.hasNext()) {
            AbstractGCEvent<?> abstractEvent = i.next();
            if (abstractEvent.isStopTheWorld()) {
                GCEvent event = (GCEvent)abstractEvent;
                out.printf("%s %f %f", getSimpleType(event), event.getTimestamp(), event.getPause());
                out.println();
            }
        }

        out.flush();
    }
    
    /**
     * Simple GC Logs GC_TYPE must not contain spaces. This method makes sure they don't.
     * 
     * @param typeName name of the gc event type
     * @return name without spaces
     */
    private String getSimpleType(GCEvent event) {
        String simpleType;
        
        if (isYoungOnly(event)) {
            simpleType = "YoungGC";
        }
        else if (event.isInitialMark()) {
            simpleType = "InitialMarkGC";
        }
        else if (event.isRemark()) {
            simpleType = "RemarkGC";
        }
        else if (event.isFull()) {
            simpleType = "FullGC";
        }
        else {
            simpleType = stripBlanks(event.getTypeAsString());
        }
        
        return simpleType;
    }
    
    /**
     * Does the event consist of young generation events only (main and detail events).
     * 
     * @param event event to be analysed.
     * @return <code>true</code> if the event is only in the young generation, <code>false</code> otherwise
     */
    private boolean isYoungOnly(GCEvent event) {
        boolean isYoungOnly = false;
        if (!event.hasDetails() && event.getType().getGeneration().equals(Generation.YOUNG)) {
            isYoungOnly = true;
        }
        else if (event.getType().getGeneration().equals(Generation.YOUNG)) {
            isYoungOnly = true;
            Iterator<GCEvent> iterator = event.details();
            while (iterator.hasNext()) {
                GCEvent currentEvent = iterator.next();
                if (!currentEvent.getType().getGeneration().equals(Generation.YOUNG)) {
                    isYoungOnly = false;
                    break;
                }
            }
        }
        
        return isYoungOnly;
    }
    
    private String stripBlanks(String eventTypeName) {
        StringBuilder sb = new StringBuilder(eventTypeName);
        for (int i = sb.length()-1; i >= 0; --i) {
            if (sb.charAt(i) == ' ') {
                sb.deleteCharAt(i);
            }
        }
        
        return sb.toString();
    }

}

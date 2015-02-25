package com.tagtraum.perf.gcviewer.exp.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Locale;

import com.tagtraum.perf.gcviewer.exp.AbstractDataWriter;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Generation;
import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Exports stop-the-world events in the "simple gc log" format (compatible to GCHisto).
 * <p>
 * This writer writes every event on its own line with the following format
 * <p>
 * {@code GC_TYPE START_SEC DURATION_SEC}
 *
 * @see <a href="http://mail.openjdk.java.net/pipermail/hotspot-gc-use/2012-November/001428.html">http://mail.openjdk.java.net/pipermail/hotspot-gc-use/2012-November/001428.html</a>
 * @see <a href="http://java.net/projects/gchisto">GCHisto</a>
 * @see <a href="https://svn.java.net/svn/gchisto~svn/trunk/www/index.html">GCHisto documentation</a>
 *
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
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
        final Locale NO_LOCALE = null;
        while (i.hasNext()) {
            AbstractGCEvent<?> abstractEvent = i.next();
            if (abstractEvent.isStopTheWorld()) {
                out.printf(NO_LOCALE,
                        "%s %f %f%n",
                        getSimpleType(abstractEvent),
                        abstractEvent.getTimestamp(),
                        abstractEvent.getPause());
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
    private String getSimpleType(AbstractGCEvent<?> event) {
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
    private boolean isYoungOnly(AbstractGCEvent<?> event) {
        boolean isYoungOnly = false;
        if (!event.hasDetails() && event.getExtendedType().getGeneration().equals(Generation.YOUNG)) {
            isYoungOnly = true;
        }
        else if (event.getExtendedType().getGeneration().equals(Generation.YOUNG)) {
            isYoungOnly = true;
            @SuppressWarnings("unchecked")
            Iterator<AbstractGCEvent<?>> iterator = (Iterator<AbstractGCEvent<?>>) event.details();
            while (iterator.hasNext()) {
                AbstractGCEvent<?> currentEvent = iterator.next();
                if (!currentEvent.getExtendedType().getGeneration().equals(Generation.YOUNG)) {
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

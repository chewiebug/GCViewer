package com.tagtraum.perf.gcviewer.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.ChartRenderer;
import com.tagtraum.perf.gcviewer.ModelChartImpl;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.ConcurrentGCEvent;

/**
 * ConcurrentGcLineRenderer draws lines for every concurrent GC event, which seems to be a bit much.
 * Even every collection begin and end can be so much, that the whole chart is cyan.
 * 
 * @author <a href="mailto:jwu@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 30.10.2011</p>
 */
public class ConcurrentGcLineRenderer extends ChartRenderer {
    public static final Paint CONCURRENT_COLLECTION_BEGIN = Color.CYAN;
    public static final Paint CONCURRENT_COLLECTION_END = Color.PINK;
    
    private boolean lastEventWasStart = false;

    public ConcurrentGcLineRenderer(ModelChartImpl modelChart) {
        super(modelChart);
        setLinePaint(CONCURRENT_COLLECTION_BEGIN);
    }

    public void paintComponent(Graphics2D g2d) {
        // make sure that we ignore the AntiAliasing flag as it does not make sense for vertical lines
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        final double scaleFactor = getModelChart().getScaleFactor();
        final int height = getHeight();
        int lastScaledTimestampBegin = Integer.MIN_VALUE;
        int lastScaledTimestampEnd = Integer.MIN_VALUE;
        for (Iterator<ConcurrentGCEvent> i = getModelChart().getModel().getConcurrentGCEvents(); i.hasNext();) {
            final AbstractGCEvent event = i.next();
            if (event.isConcurrentCollectionStart()) {
                final int scaledTimestamp = (int) (scaleFactor * event.getTimestamp());
                if (scaledTimestamp != lastScaledTimestampBegin) {
                    g2d.setPaint(CONCURRENT_COLLECTION_BEGIN);
                    g2d.drawLine(scaledTimestamp, 0, scaledTimestamp, height);
                    lastScaledTimestampBegin = scaledTimestamp;
                }
            }
            else if (event.isConcurrentCollectionEnd()) {
                final int scaledTimestamp = (int) (scaleFactor * event.getTimestamp());
                if (scaledTimestamp != lastScaledTimestampEnd) {
                    g2d.setPaint(CONCURRENT_COLLECTION_END);
                    g2d.drawLine(scaledTimestamp, 0, scaledTimestamp, height);
                    lastScaledTimestampEnd = scaledTimestamp;
                }
            }
        }
    }

}

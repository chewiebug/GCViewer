package com.tagtraum.perf.gcviewer.view.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.model.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.view.ChartRenderer;
import com.tagtraum.perf.gcviewer.view.ModelChartImpl;

/**
 * ConcurrentGcStardEndRenderer draws lines for every begin and end of a concurrent GC event. Start
 * is cyan, end is pink.
 * 
 * @author <a href="mailto:jwu@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 30.10.2011</p>
 */
public class ConcurrentGcBegionEndRenderer extends ChartRenderer {
    public static final Paint CONCURRENT_COLLECTION_BEGIN = Color.CYAN;
    public static final Paint CONCURRENT_COLLECTION_END = Color.PINK;
    
    public ConcurrentGcBegionEndRenderer(ModelChartImpl modelChart) {
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
            final ConcurrentGCEvent event = i.next();
            if (event.isConcurrentCollectionStart()) {
                final int scaledTimestamp = (int) (scaleFactor * (event.getTimestamp() - getModelChart().getModel().getFirstPauseTimeStamp() - event.getPause()));
                if (scaledTimestamp != lastScaledTimestampBegin) {
                    g2d.setPaint(CONCURRENT_COLLECTION_BEGIN);
                    g2d.drawLine(scaledTimestamp, 0, scaledTimestamp, height);
                    lastScaledTimestampBegin = scaledTimestamp;
                }
            }
            else if (event.isConcurrentCollectionEnd()) {
                final int scaledTimestamp = (int) (scaleFactor * (event.getTimestamp() - getModelChart().getModel().getFirstPauseTimeStamp()));
                if (scaledTimestamp != lastScaledTimestampEnd) {
                    g2d.setPaint(CONCURRENT_COLLECTION_END);
                    g2d.drawLine(scaledTimestamp, 0, scaledTimestamp, height);
                    lastScaledTimestampEnd = scaledTimestamp;
                }
            }
        }
    }

}

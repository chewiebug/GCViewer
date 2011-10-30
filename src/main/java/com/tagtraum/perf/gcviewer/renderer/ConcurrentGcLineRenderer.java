package com.tagtraum.perf.gcviewer.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.ChartRenderer;
import com.tagtraum.perf.gcviewer.ConcurrentGCEvent;
import com.tagtraum.perf.gcviewer.ModelChartImpl;

/**
 * ConcurrentGcLineRenderer draws lines for every concurrent GC event, which seems to be a bit much.
 * 
 * @author <a href="mailto:jwu@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 30.10.2011</p>
 */
public class ConcurrentGcLineRenderer extends ChartRenderer {
    public static final Paint DEFAULT_LINEPAINT = Color.CYAN;

    public ConcurrentGcLineRenderer(ModelChartImpl modelChart) {
        super(modelChart);
        setLinePaint(DEFAULT_LINEPAINT);
    }

    public void paintComponent(Graphics2D g2d) {
        final double scaleFactor = getModelChart().getScaleFactor();
        final int height = getHeight();
        int lastScaledTimestamp = Integer.MIN_VALUE;
        for (Iterator<ConcurrentGCEvent> i = getModelChart().getModel().getConcurrentGCEvents(); i.hasNext();) {
            final ConcurrentGCEvent event = i.next();
            final int scaledTimestamp = (int) (scaleFactor * event.getTimestamp());
            if (scaledTimestamp != lastScaledTimestamp) {
                g2d.drawLine(scaledTimestamp, 0, scaledTimestamp, height);
                lastScaledTimestamp = scaledTimestamp;
            }
        }
    }

}

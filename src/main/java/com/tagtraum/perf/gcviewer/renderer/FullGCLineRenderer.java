package com.tagtraum.perf.gcviewer.renderer;

import com.tagtraum.perf.gcviewer.ChartRenderer;
import com.tagtraum.perf.gcviewer.GCEvent;
import com.tagtraum.perf.gcviewer.ModelChartImpl;

import java.awt.*;
import java.util.Iterator;

/**
 * FullGCLineRenderer.
 *
 * Date: Jun 2, 2005
 * Time: 3:31:21 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class FullGCLineRenderer extends ChartRenderer {
    public static final Paint DEFAULT_LINEPAINT = Color.BLACK;

    public FullGCLineRenderer(ModelChartImpl modelChart) {
        super(modelChart);
        setLinePaint(DEFAULT_LINEPAINT);
    }

    public void paintComponent(Graphics2D g2d) {
        // make sure that we ignore the AntiAliasing flag as it does not make sense for vertical lines
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        final double scaleFactor = getModelChart().getScaleFactor();
        final Rectangle clipBounds = g2d.getClipBounds();
        final int minX = clipBounds.x;
        final int maxX = clipBounds.x+clipBounds.width;

        final int height = getHeight();
        int lastScaledTimestamp = Integer.MIN_VALUE;
        for (Iterator<GCEvent> i = getModelChart().getModel().getFullGCEvents(); i.hasNext();) {
            GCEvent event = i.next();
            final int scaledTimestamp = (int)(scaleFactor * event.getTimestamp());
            if (scaledTimestamp != lastScaledTimestamp && scaledTimestamp >= minX && scaledTimestamp <= maxX) {
                g2d.drawLine(scaledTimestamp, 0, scaledTimestamp, height);
                lastScaledTimestamp = scaledTimestamp;
            }
        }
    }

}

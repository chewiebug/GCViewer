package com.tagtraum.perf.gcviewer.view.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.view.ChartRenderer;
import com.tagtraum.perf.gcviewer.view.ModelChartImpl;

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
        double scaleFactor = getModelChart().getScaleFactor();
        Rectangle clipBounds = g2d.getClipBounds();
        int minX = clipBounds.x;
        int maxX = clipBounds.x+clipBounds.width;

        int height = getHeight();
        int lastScaledTimestamp = Integer.MIN_VALUE;
        for (Iterator<GCEvent> i = getModelChart().getModel().getFullGCEvents(); i.hasNext();) {
            GCEvent event = i.next();
            int scaledTimestamp = (int)(scaleFactor * (event.getTimestamp() - getModelChart().getModel().getFirstPauseTimeStamp() - event.getPause()));
            if (scaledTimestamp != lastScaledTimestamp && scaledTimestamp >= minX && scaledTimestamp <= maxX) {
                g2d.drawLine(scaledTimestamp, 0, scaledTimestamp, height);
                lastScaledTimestamp = scaledTimestamp;
            }
        }
    }

}

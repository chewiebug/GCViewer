package com.tagtraum.perf.gcviewer.renderer;

import com.tagtraum.perf.gcviewer.ChartRenderer;
import com.tagtraum.perf.gcviewer.ModelChartImpl;
import com.tagtraum.perf.gcviewer.model.GCEvent;

import java.awt.*;
import java.util.Iterator;

/**
 * IncLineRenderer.
 *
 * Date: Jun 2, 2005
 * Time: 3:31:21 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class IncLineRenderer extends ChartRenderer {
    public static final Paint DEFAULT_LINEPAINT = Color.CYAN;

    public IncLineRenderer(ModelChartImpl modelChart) {
        super(modelChart);
        setLinePaint(DEFAULT_LINEPAINT);
    }

    public void paintComponent(Graphics2D g2d) {
        final double scaleFactor = getModelChart().getScaleFactor();
        final int height = getHeight();
        int lastScaledTimestamp = Integer.MIN_VALUE;
        for (Iterator<GCEvent> i = getModelChart().getModel().getGCEvents(); i.hasNext();) {
            final GCEvent event = i.next();
            if (event.isInc()) {
                final int scaledTimestamp = (int) (scaleFactor * (event.getTimestamp() - getModelChart().getModel().getFirstPauseTimeStamp()));
                if (scaledTimestamp != lastScaledTimestamp) {
                    g2d.drawLine(scaledTimestamp, 0, scaledTimestamp, height);
                    lastScaledTimestamp = scaledTimestamp;
                }
            }
        }
    }

}

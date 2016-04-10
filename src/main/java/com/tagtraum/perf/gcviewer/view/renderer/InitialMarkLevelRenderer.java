package com.tagtraum.perf.gcviewer.view.renderer;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Polygon;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.view.ModelChart;
import com.tagtraum.perf.gcviewer.view.ModelChartImpl;

/**
 * Draws a line indicating the level of memory at the initial-mark event.
 *
 * Date: Jun 2, 2005
 * Time: 3:31:21 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class InitialMarkLevelRenderer extends PolygonChartRenderer {
    public static final Paint DEFAULT_LINEPAINT = Color.YELLOW;

    public InitialMarkLevelRenderer(ModelChartImpl modelChart) {
        super(modelChart);
        setLinePaint(DEFAULT_LINEPAINT);
        setDrawPolygon(false);
        setDrawLine(true);
    }

    public Polygon computePolygon(ModelChart modelChart, GCModel model) {
        ScaledPolygon polygon = createMemoryScaledPolygon();
        for (Iterator<GCEvent> i = model.getGCEvents(); i.hasNext();) {
            GCEvent event = i.next();
            if (event.isInitialMark()) {
                polygon.addPoint(event.getTimestamp() - model.getFirstPauseTimeStamp(), event.getPreUsed());
            }
        }
        // Don't add dummy point to make the polygon complete! Just stop drawing.
        return polygon;
    }
}

package com.tagtraum.perf.gcviewer.renderer;

import com.tagtraum.perf.gcviewer.*;

import java.awt.*;
import java.util.Iterator;

/**
 * GCTimesRenderer.
 *
 * Date: Jun 2, 2005
 * Time: 3:31:21 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class GCTimesRenderer extends PolygonChartRenderer {
    public static final Paint DEFAULT_LINEPAINT = Color.GREEN;

    public GCTimesRenderer(ModelChartImpl modelChart) {
        super(modelChart);
        setLinePaint(DEFAULT_LINEPAINT);
        setDrawPolygon(false);
        setDrawLine(true);
    }

    public Polygon computePolygon(ModelChart modelChart, GCModel model) {
        ScaledPolygon polygon = createTimeScaledPolygon();
        for (Iterator i = model.getGCEvents(); i.hasNext();) {
            GCEvent event = (GCEvent) i.next();
            polygon.addPoint(event.getTimestamp(), event.getPause());
        }
        // dummy point to make the polygon complete
        polygon.addPoint(model.getRunningTime(), 0.0d);
        return polygon;
    }
}

package com.tagtraum.perf.gcviewer.renderer;

import com.tagtraum.perf.gcviewer.*;

import java.awt.*;
import java.util.Iterator;

/**
 * TotalHeapRenderer.
 *
 * Date: Jun 2, 2005
 * Time: 3:31:21 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TotalHeapRenderer extends PolygonChartRenderer {
    public static final Paint DEFAULT_LINEPAINT = Color.RED;
    public static final Paint DEFAULT_FILLPAINT = new GradientPaint(0, 0, Color.RED, 0, 0, Color.WHITE);

    public TotalHeapRenderer(ModelChartImpl modelChart) {
        super(modelChart);
        setFillPaint(DEFAULT_FILLPAINT);
        setLinePaint(DEFAULT_LINEPAINT);
        setDrawPolygon(true);
        setDrawLine(true);
    }

    public Polygon computePolygon(ModelChart modelChart, GCModel model) {
        ScaledPolygon polygon = createMemoryScaledPolygon();
        polygon.addPoint(0.0d, 0.0d);
        for (Iterator i = model.getGCEvents(); i.hasNext();) {
            GCEvent event = (GCEvent) i.next();
            polygon.addPoint(event.getTimestamp(), event.getTotal());
        }
        polygon.addPoint(model.getRunningTime(), 0.0d);
        return polygon;
    }
}

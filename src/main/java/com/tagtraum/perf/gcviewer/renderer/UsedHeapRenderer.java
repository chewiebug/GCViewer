package com.tagtraum.perf.gcviewer.renderer;

import com.tagtraum.perf.gcviewer.*;

import java.awt.*;
import java.util.Iterator;

/**
 * UsedHeapRenderer.
 *
 * Date: Jun 2, 2005
 * Time: 3:31:21 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class UsedHeapRenderer extends PolygonChartRenderer {
    public static final Paint DEFAULT_LINEPAINT = Color.BLUE;

    public UsedHeapRenderer(ModelChartImpl modelChart) {
        super(modelChart);
        setLinePaint(DEFAULT_LINEPAINT);
        setDrawPolygon(false);
        setDrawLine(true);
    }

    public Polygon computePolygon(ModelChart modelChart, GCModel model) {
        ScaledPolygon polygon = createMemoryScaledPolygon();
        for (Iterator i = model.getGCEvents(); i.hasNext();) {
            GCEvent event = (GCEvent) i.next();
            final double timestamp = event.getTimestamp();
            polygon.addPoint(timestamp, event.getPreUsed());
            polygon.addPoint(timestamp, event.getPostUsed());
        }
        // dummy point to make the polygon complete
        polygon.addPoint(model.getRunningTime(), 0.0d);
        //System.out.println("last x coord " + polygon.xpoints[polygon.npoints-1]);
        return polygon;
    }
}

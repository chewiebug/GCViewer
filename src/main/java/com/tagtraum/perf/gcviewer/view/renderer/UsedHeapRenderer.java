package com.tagtraum.perf.gcviewer.view.renderer;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Polygon;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.view.ModelChart;
import com.tagtraum.perf.gcviewer.view.ModelChartImpl;

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
        for (Iterator<AbstractGCEvent<?>> i = model.getEvents(); i.hasNext();) {
            AbstractGCEvent<?> event = i.next();
            // e.g. "GC remark" of G1 algorithm does not contain memory information
            if (event.getTotal() > 0) {
                final double timestamp = event.getTimestamp() - model.getFirstPauseTimeStamp() - event.getPause();
                polygon.addPoint(timestamp, event.getPreUsed());
                polygon.addPoint(timestamp + event.getPause(), event.getPostUsed());
            }
        }
        // dummy point to make the polygon complete
        polygon.addPoint(model.getRunningTime(), 0.0d);
        //System.out.println("last x coord " + polygon.xpoints[polygon.npoints-1]);
        return polygon;
    }
}

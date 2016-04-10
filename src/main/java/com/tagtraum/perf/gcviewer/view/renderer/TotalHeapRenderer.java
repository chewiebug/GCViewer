package com.tagtraum.perf.gcviewer.view.renderer;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Polygon;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.view.ModelChart;
import com.tagtraum.perf.gcviewer.view.ModelChartImpl;

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
        int lastTotal = 0;
        for (Iterator<AbstractGCEvent<?>> i = model.getStopTheWorldEvents(); i.hasNext();) {
            AbstractGCEvent<?> abstractGCEvent = i.next();
            if (abstractGCEvent instanceof GCEvent) {
                GCEvent event = (GCEvent) abstractGCEvent;
                if (event.getTotal() > 0) {
                    // there are events that don't have a heap size associated (like "GC remark" of G1)
                    // -> skip them
                    if (polygon.npoints == 1) {
                        // first point needs to be treated different from the rest,
                        // because otherwise the polygon would not start with a vertical line at 0,
                        // but with a slanting line between 0 and after the first pause
                        polygon.addPoint(0, (double)event.getTotal());
                    }
                    polygon.addPoint(event.getTimestamp() - model.getFirstPauseTimeStamp() + event.getPause(), event.getTotal());
                    lastTotal = event.getTotal();
                }
            }
        }
        polygon.addPointNotOptimised(model.getRunningTime(), lastTotal);
        polygon.addPointNotOptimised(model.getRunningTime(), 0.0d);
        return polygon;
    }
}

package com.tagtraum.perf.gcviewer.renderer;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Polygon;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.AbstractGCEvent.Generation;
import com.tagtraum.perf.gcviewer.GCEvent;
import com.tagtraum.perf.gcviewer.GCModel;
import com.tagtraum.perf.gcviewer.ModelChart;
import com.tagtraum.perf.gcviewer.ModelChartImpl;

/**
 * TotalTenuredRenderer.
 *
 * Date: Jun 2, 2005
 * Time: 3:31:21 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TotalTenuredRenderer extends PolygonChartRenderer {
    public static final Paint DEFAULT_LINEPAINT = Color.MAGENTA;
    public static final Paint DEFAULT_FILLPAINT = new GradientPaint(0, 0, Color.MAGENTA, 0, 0, Color.WHITE);

    public TotalTenuredRenderer(ModelChartImpl modelChart) {
        super(modelChart);
        setFillPaint(DEFAULT_FILLPAINT);
        setLinePaint(DEFAULT_LINEPAINT);
        setDrawPolygon(true);
        setDrawLine(true);
    }

    public Polygon computePolygon(ModelChart modelChart, GCModel model) {
        ScaledPolygon polygon = createMemoryScaledPolygon();
        polygon.addPoint(0.0d, 0.0d);
        double lastTotal = 0.0d;
        double fallback = 0.0d;
        double lastTenuredTotal = 0;
        for (Iterator i = model.getGCEvents(); i.hasNext();) {
            final GCEvent event = (GCEvent) i.next();
            for (Iterator iterator=event.details(); iterator.hasNext();) {
                final Object o = iterator.next();
                if (o instanceof GCEvent) {
                    final GCEvent detailEvent = (GCEvent)o;
                    if (detailEvent.getType().getGeneration() == Generation.TENURED) {
                        double total = detailEvent.getTotal();
                        if (total == 0) total = lastTenuredTotal;
                        else lastTenuredTotal = total;
                        if (polygon.npoints == 1) {
                            polygon.addPoint(0.0d, total);
                            lastTotal = total;
                        }

                        if (lastTotal != total) polygon.addPoint(detailEvent.getTimestamp(), lastTotal);
                        polygon.addPoint(detailEvent.getTimestamp()+detailEvent.getPause(), total);
                        lastTotal = total;
                    }

                    if (detailEvent.getType().getGeneration() == Generation.YOUNG && fallback == 0.0d) {
                        fallback = event.getTotal() - detailEvent.getTotal();
                    }
                }
            }
        }
        // check of we found any points at all and fall back on YOUNG computation
        if (polygon.npoints == 1 && fallback != 0.0d) {
            polygon.addPoint(0.0d, fallback);
            lastTotal = fallback;
        }
        polygon.addPoint(model.getRunningTime(), lastTotal);
        polygon.addPoint(model.getRunningTime(), 0.0d);
        return polygon;
    }
}

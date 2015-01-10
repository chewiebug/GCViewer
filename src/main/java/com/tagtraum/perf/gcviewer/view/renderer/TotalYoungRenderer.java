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
 * Renders total size of young generation.
 *
 * <br>Date: Jun 2, 2005
 * <br>Time: 3:31:21 PM<br>
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 */
public class TotalYoungRenderer extends PolygonChartRenderer {
    public static final Paint DEFAULT_LINEPAINT = Color.ORANGE;
    public static final Paint DEFAULT_FILLPAINT = new GradientPaint(0, 0, Color.ORANGE, 0, 0, Color.WHITE);

    public TotalYoungRenderer(ModelChartImpl modelChart) {
        super(modelChart);
        setFillPaint(DEFAULT_FILLPAINT);
        setLinePaint(DEFAULT_LINEPAINT);
        setDrawPolygon(true);
        setDrawLine(true);
    }

    public Polygon computePolygon(ModelChart modelChart, GCModel model) {
        ScaledPolygon polygon = createMemoryScaledPolygon();
        polygon.addPoint(0.0d, 0.0d);
        double lastTenured = 0;
        double lastYoung = 0;
        for (Iterator<AbstractGCEvent<?>> i = model.getStopTheWorldEvents(); i.hasNext();) {
            AbstractGCEvent<?> abstractGCEvent = i.next();
            if (abstractGCEvent instanceof GCEvent) {
                GCEvent event = (GCEvent) abstractGCEvent;
                double tenuredSize = 0;
                double youngSize = 0;
                GCEvent young = event.getYoung();
                GCEvent tenured = event.getTenured();
                if (hasMemoryInformation(event) && young != null && tenured != null) {
                    if (modelChart.isShowTenured()) {
                        tenuredSize = tenured.getTotal();
                    }
                    youngSize = young.getTotal();

                    if (polygon.npoints == 1) {
                        // first point needs to be treated different from the rest,
                        // because otherwise the polygon would not start with a vertical line at 0,
                        // but with a slanting line between 0 and after the first pause
                        polygon.addPoint(0, tenuredSize + youngSize);
                    }
                    polygon.addPoint(event.getTimestamp() - model.getFirstPauseTimeStamp() + event.getPause(), tenuredSize + youngSize);
                    lastYoung = youngSize;
                    lastTenured = tenuredSize;
                }
            }
        }
        polygon.addPointNotOptimised(model.getRunningTime(), lastTenured + lastYoung);
        polygon.addPointNotOptimised(model.getRunningTime(), 0.0d);
        return polygon;
    }
}

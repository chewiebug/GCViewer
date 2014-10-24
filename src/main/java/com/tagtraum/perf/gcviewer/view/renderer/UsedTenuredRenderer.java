package com.tagtraum.perf.gcviewer.view.renderer;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Polygon;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.view.ModelChart;
import com.tagtraum.perf.gcviewer.view.ModelChartImpl;

/**
 * UsedTenuredRenderer draws a line to indicate the current usage of the tenured generation.
 *  
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 22.07.2012</p>
 */
public class UsedTenuredRenderer extends PolygonChartRenderer {
    public static final Paint DEFAULT_LINEPAINT = Color.MAGENTA.darker();

    public UsedTenuredRenderer(ModelChartImpl modelChart) {
        super(modelChart);
        setLinePaint(DEFAULT_LINEPAINT);
        setDrawPolygon(false);
        setDrawLine(true);
    }

    @Override
    public Polygon computePolygon(ModelChart modelChart, GCModel model) {
        ScaledPolygon polygon = createMemoryScaledPolygon();
        for (Iterator<AbstractGCEvent<?>> i = model.getStopTheWorldEvents(); i.hasNext();) {
            AbstractGCEvent<?> abstractGCEvent = i.next();
            if (abstractGCEvent instanceof GCEvent) {
                GCEvent event = (GCEvent) abstractGCEvent;
                GCEvent tenuredEvent = event.getTenured();
                if (tenuredEvent != null) {
                    // only -XX:+PrintGCDetails adds information about generations
                    // e.g. "GC remark" of G1 algorithm does not contain memory information
                    if (tenuredEvent.getTotal() > 0) {
                        final double timestamp = event.getTimestamp() - model.getFirstPauseTimeStamp();
                        polygon.addPoint(timestamp, tenuredEvent.getPreUsed());
                        polygon.addPoint(timestamp + event.getPause(), tenuredEvent.getPostUsed());
                    }
                }
            }
        }
        // dummy point to make the polygon complete
        polygon.addPoint(model.getRunningTime(), 0.0d);
        return polygon;
    }

}

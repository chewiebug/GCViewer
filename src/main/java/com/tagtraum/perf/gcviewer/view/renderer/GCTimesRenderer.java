package com.tagtraum.perf.gcviewer.view.renderer;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Polygon;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.view.ModelChart;
import com.tagtraum.perf.gcviewer.view.ModelChartImpl;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Renders all stop the world event pauses.
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
        for (Iterator<AbstractGCEvent<?>> i = model.getStopTheWorldEvents(); i.hasNext();) {
            AbstractGCEvent<?> event = i.next();
            polygon.addPoint(event.getTimestamp() - model.getFirstPauseTimeStamp(), event.getPause());
        }
        // dummy point to make the polygon complete
        polygon.addPoint(model.getRunningTime(), 0.0d);
        return polygon;
    }
}

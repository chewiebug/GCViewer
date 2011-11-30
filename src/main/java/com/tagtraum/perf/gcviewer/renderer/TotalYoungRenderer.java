package com.tagtraum.perf.gcviewer.renderer;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Polygon;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.Generation;
import com.tagtraum.perf.gcviewer.ModelChart;
import com.tagtraum.perf.gcviewer.ModelChartImpl;

/**
 * TotalYoungRenderer.
 *
 * Date: Jun 2, 2005
 * Time: 3:31:21 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
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
        for (Iterator i = model.getGCEvents(); i.hasNext();) {
            GCEvent event = (GCEvent) i.next();
            double tenured = 0;
            double young = 0;
            for (Iterator iterator=event.details(); iterator.hasNext();) {
                final Object o = iterator.next();
                if (o instanceof GCEvent) {
                    GCEvent detailEvent = (GCEvent)o;
                    if (modelChart.isShowTenured() && detailEvent.getType().getGeneration() == Generation.TENURED) {
                        double total = detailEvent.getTotal();
                        if (total == 0) total = tenured;
                        else tenured = total;
                    }
                    if (detailEvent.getType().getGeneration() == Generation.YOUNG) {
                        double total = detailEvent.getTotal();
                        if (total == 0) total = young;
                        else young = total;
                    }
                }
            }
            if (modelChart.isShowTenured() && tenured == 0 && young != 0) {
                tenured = event.getTotal() - young;
            }
            if (young != 0) {
                polygon.addPoint(event.getTimestamp(), lastTenured + lastYoung);
                polygon.addPoint(event.getTimestamp()+event.getPause(), tenured + young);
                lastYoung = young;
            }
            if (tenured != 0) {
                lastTenured = tenured;
            }
        }
        polygon.addPoint(model.getRunningTime(), lastTenured + lastYoung);
        polygon.addPoint(model.getRunningTime(), 0.0d);
        return polygon;
    }
}

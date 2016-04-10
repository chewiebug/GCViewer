package com.tagtraum.perf.gcviewer.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;

import javax.swing.JComponent;

/**
 * ChartRenderer.
 *
 * Date: Jun 2, 2005
 * Time: 5:03:34 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public abstract class ChartRenderer extends JComponent {
    private ModelChartImpl modelChart;
    private boolean drawLine;
    private Paint linePaint;

    public ChartRenderer(ModelChartImpl modelChart) {
        this.modelChart = modelChart;
    }

    public void setLinePaint(Paint linePaint) {
        this.linePaint = linePaint;
    }

    public void setDrawLine(boolean drawLine) {
        this.drawLine = drawLine;
    }

    public ModelChartImpl getModelChart() {
        return modelChart;
    }

    public void setModelChart(ModelChartImpl modelChart) {
        this.modelChart = modelChart;
    }

    public boolean isDrawLine() {
        return drawLine;
    }

    public Paint getLinePaint() {
        return linePaint;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        Paint oldPaint = g2d.getPaint();
        Object oldAAHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        if (modelChart.isAntiAlias()) g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setPaint(getLinePaint());
        paintComponent(g2d);
        g2d.setPaint(oldPaint);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAAHint);
    }

    public abstract void paintComponent(Graphics2D g2d);


}

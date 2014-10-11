package com.tagtraum.perf.gcviewer.action;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.tagtraum.perf.gcviewer.GCViewerGui;
import com.tagtraum.perf.gcviewer.ModelChartImpl;

/**
 * @author <a href="mailto:serafin.sedano@gmail.com">Serafin Sedano</a>
 */
public class ZoomMouseListener implements MouseWheelListener
{
    private final GCViewerGui gui;

    private ModelChartImpl chart;

    public ZoomMouseListener(GCViewerGui gui, final ModelChartImpl chart)
    {
        this.gui = gui;
        this.chart = chart;
    }

    /*
     * (non-Javadoc)
     * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        // In OSX track pad horizontal scrolls yields true
		// In *nix shift zoom is horizontal scroll
        if(e.isShiftDown()) 
            return;
        
        double factor = 1d;
		if (e.getWheelRotation() > 0)
        {
            factor = chart.getScaleFactor() / 1.1d;
        }
        else
        {
            factor = chart.getScaleFactor() * 1.1d;
        }
        // The Listener does the magic
        gui.setZoomValue(factor);
    }
}

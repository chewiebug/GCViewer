package com.tagtraum.perf.gcviewer.view.renderer;

import com.tagtraum.perf.gcviewer.model.AbstractGCEvent.GcPattern;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.view.ChartRenderer;
import com.tagtraum.perf.gcviewer.view.ModelChart;
import com.tagtraum.perf.gcviewer.view.ModelChartImpl;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Arrays;

/**
 * Superclass for components rendering model data as polygon, polyline
 * or both.
 *
 * Date: Jun 2, 2005
 * Time: 2:53:36 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public abstract class PolygonChartRenderer extends ChartRenderer {
    private boolean drawPolygon;
    private Paint fillPaint;
    private Polygon polygon;
    private Polygon clippedPolygon = new Polygon();

    public PolygonChartRenderer(ModelChartImpl modelChart) {
        super(modelChart);
        setOpaque(false);
    }

    public void setDrawPolygon(boolean drawPolygon) {
        this.drawPolygon = drawPolygon;
    }

    public void setFillPaint(Paint fillPaint) {
        this.fillPaint = fillPaint;
    }

    /**
     * Reset the internally cached polygon. Should always be done when size of chart is changed
     * in some way (zoom, window resize, reload), but not more often.
     */
    public void resetPolygon() {
        polygon = null;
    }

    /**
     * @see com.tagtraum.perf.gcviewer.view.ChartRenderer#paintComponent(java.awt.Graphics2D)
     */
    public void paintComponent(Graphics2D g2d) {
        if ((!drawPolygon) && (!isDrawLine())) return;
        if (polygon == null) {
            // don't recompute polygon for each paint event
            polygon = computePolygon(getModelChart(), getModelChart().getModel());
        }
        clippedPolygon = initClippedPolygon(polygon, g2d.getClip());
        if (drawPolygon) {
            // don't antialias the polygon, if we are going to antialias the bounding lines
            Object oldAAHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            if (isDrawLine()) {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
            g2d.setPaint(createPaint(polygon));
            g2d.fillPolygon(clippedPolygon);
            if (isDrawLine()) {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAAHint);
            }
        }
        if (isDrawLine()) {
            g2d.setPaint(getLinePaint());
            g2d.drawPolyline(clippedPolygon.xpoints, clippedPolygon.ypoints, clippedPolygon.npoints-1);
        }
    }

    /**
     * Updates the <code>clippedPolygon</code> instance in this class to contain only the points
     * needed to fill the current clipped area.
     *
     * @param polygon full polygon with all points of the gc log file.
     * @param clip size of the current clipping area
     * @return the updated <code>clippedPolygon</code> with only the points relevant to draw
     * the area inside the <code>clip</code> area.
     */
    private Polygon initClippedPolygon(Polygon polygon, Shape clip) {
        int xMin = (int) clip.getBounds2D().getMinX();
        int xMax = (int) Math.ceil(clip.getBounds2D().getMaxX());

        // only clip if there are enough points in the polygon
        if (polygon.npoints > 1000 && (xMin > polygon.xpoints[0] || xMax < polygon.xpoints[polygon.npoints-1])) {
            InsertionBoundary insertionPoint = findInsertionBoundary(polygon, xMin, xMax);

            int[] xpoints = new int[insertionPoint.getDistance() + 2];
            int[] ypoints = new int[insertionPoint.getDistance() + 2];

            System.arraycopy(polygon.xpoints,
                    insertionPoint.getStartX(),
                    xpoints,
                    1,
                    insertionPoint.getDistance() + 1);

            System.arraycopy(polygon.ypoints,
                    insertionPoint.getStartX(),
                    ypoints,
                    1,
                    insertionPoint.getDistance() + 1);

            xpoints[0] = xpoints[1]-1;
            if (drawPolygon) {
                // for polygons the last point needs to be in the bottom of the chart
                // (otherwise filling will not be correct)...
                ypoints[0] = (int) Math.ceil(clip.getBounds2D().getMaxY());
            }
            else {
                // ...for lines it must not (otherwise vertical lines will appear where they shouldn't)
                ypoints[0] = ypoints[1];
            }

            clippedPolygon.xpoints = xpoints;
            clippedPolygon.ypoints = ypoints;
            clippedPolygon.npoints = insertionPoint.getDistance() + 2;
            clippedPolygon.addPoint(xpoints[xpoints.length-1]+1, ypoints[0]);
        }
        else {
            clippedPolygon.xpoints = polygon.xpoints;
            clippedPolygon.ypoints = polygon.ypoints;
            clippedPolygon.npoints = polygon.npoints;
        }

        clippedPolygon.invalidate();

        return clippedPolygon;
    }

    /**
     * Finds boundaries in <code>xpoints</code> of <code>polygon</code> so that <code>startX</code>
     * is smaller or equal to <code>xMin</code> and <code>endX</code> is greater or equal to
     * <code>xMax</code>;
     *
     * @param polygon polygon where the boundaries have to be found
     * @param xMin min value of x
     * @param xMax max value of x
     * @return InsertionBoundary containing startX as index of first element in xpoints being
     * smaller than xMin and endX as index of first element in xpoints being greater than xMax.
     */
    private InsertionBoundary findInsertionBoundary(Polygon polygon, int xMin, int xMax) {
        InsertionBoundary insertionBoundary = new InsertionBoundary(polygon.npoints);
        // find zero based index of elements to the right and the left of "xMin" and "xMax" in the polygon
        insertionBoundary.setStartX(Math.abs(Arrays.binarySearch(polygon.xpoints, 0, polygon.npoints, xMin))-2);
        insertionBoundary.setEndX(Math.abs(Arrays.binarySearch(polygon.xpoints, 0, polygon.npoints, xMax))-1);

        // if the resulting point is not between two points in the array, make sure that the range
        // is extended until the insertion point is found
        while (polygon.xpoints[insertionBoundary.getStartX()] > xMin) {
            if (!insertionBoundary.decreaseStartX()) {
                break;
            }
        }
        while (polygon.xpoints[insertionBoundary.getEndX()] < xMax) {
            if (!insertionBoundary.increaseEndX()) {
                break;
            }
        }

        return insertionBoundary;
    }

    public abstract Polygon computePolygon(ModelChart modelChart, GCModel model);

    protected Paint createPaint(Polygon polygon) {
        if (fillPaint instanceof GradientPaint) {
            GradientPaint gradientPaint = (GradientPaint)fillPaint;
            Point2D point1 = new Point(0, getLowestY(polygon));
            Point2D point2 = new Point(0, getHeight());
            return new GradientPaint(point1, gradientPaint.getColor1(), point2, gradientPaint.getColor2(), false);
        }
        else {
            return fillPaint;
        }
    }

    protected ScaledPolygon createTimeScaledPolygon() {
        return new ScaledPolygon(getModelChart().getScaleFactor(), getHeight()/(getModelChart().getMaxPause()), getHeight());
    }

    protected ScaledPolygon createMemoryScaledPolygon() {
        return new ScaledPolygon(getModelChart().getScaleFactor(), getHeight()/((double)getModelChart().getFootprint()), getHeight());
    }

    private static int getLowestY(Polygon polygon) {
        int[] y = polygon.ypoints;
        int min = Integer.MAX_VALUE;
        for (int i=0, max=polygon.npoints; i<max; i++) {
            min = Math.min(min, y[i]);
        }
        return min;
    }

    /**
     * Returns <code>true</code> if <code>event</code> is of a type that contains memory information.
     *
     * @param event event to be analysed
     * @return <code>true</code> if <code>event</code> contains memory information
     */
    protected boolean hasMemoryInformation(GCEvent event) {
        return event.getExtendedType().getPattern().equals(GcPattern.GC_MEMORY)
                || event.getExtendedType().getPattern().equals(GcPattern.GC_MEMORY_PAUSE);
    }

    /**
     * Polygon that scales points upon addition.
     */
    public static class ScaledPolygon extends Polygon {
        private double xScaleFactor;
        private double yScaleFactor;
        private int yOffset;
        private Point lastPointOfOptimisation = new Point();
        private boolean lastPointWasOptimised = false;

        public ScaledPolygon(double xScaleFactor, double yScaleFactor, int yOffset) {
            this.xScaleFactor = xScaleFactor;
            this.yScaleFactor = yScaleFactor;
            this.yOffset = yOffset;
        }

        /**
         * Adds Point to the polygon optimising the polygon so as not to add points that can't
         * be seen in the graph because they are on the same pixel after scaling.
         *
         * @param x x-value
         * @param y y-value
         */
        public void addPoint(double x, double y) {
            int scaledY = getScaledYValue(y);
            int scaledX = getScaledXValue(x);
            // optimize the polygon as we add points.
            if (!lastPointWasOptimised
                    && npoints>2
                    && ypoints[npoints-2] == scaledY
                    && ypoints[npoints-1] == ypoints[npoints-2]) {

                xpoints[npoints-1] = scaledX;
            }
            else if (npoints>2 && xpoints[npoints-2] == scaledX && xpoints[npoints-1] == xpoints[npoints-2]) {
                if (!lastPointWasOptimised) {
                    // first point to be optimised in this sequence
                    // duplicate second last point to make sure that the connecting line ends right
                    addPoint(xpoints[npoints-1], ypoints[npoints-1]);
                    ypoints[npoints-2] = ypoints[npoints-3];
                }

                // optimise the subsequent points to make sure the resulting vertical line is
                // as long as the greatest distance between any two points of the sequence
                if (ypoints[npoints-2] < ypoints[npoints-1]) {
                    ypoints[npoints-2] = Math.min(ypoints[npoints-2], scaledY);
                    ypoints[npoints-1] = Math.max(ypoints[npoints-1], scaledY);
                }
                else {
                    ypoints[npoints-2] = Math.max(ypoints[npoints-2], scaledY);
                    ypoints[npoints-1] = Math.min(ypoints[npoints-1], scaledY);
                }
                lastPointOfOptimisation.setLocation(scaledX, scaledY);
                lastPointWasOptimised = true;
            }
            else {
                if (lastPointWasOptimised) {
                    // the last point of the sequence must be exactly the last point before
                    // this optimisation sequence stops; again to make sure that the connecting
                    // line starting from this point will start from the right position
                    addPoint(lastPointOfOptimisation.x, lastPointOfOptimisation.y);
                }
                addPoint(scaledX, scaledY);
                lastPointWasOptimised = false;
            }
        }

        /**
         * Adds the given point without optimisation.
         *
         * @param x x-Value
         * @param y y-Value
         */
        public void addPointNotOptimised(double x, double y) {
            addPoint(getScaledXValue(x), getScaledYValue(y));
        }

        private int getScaledXValue(double x) {
            return (int)(xScaleFactor * x);
        }

        private int getScaledYValue(double y) {
            return yOffset - (int)(yScaleFactor * y);
        }

    }

    /**
     * InsertionBoundary holds the boundaries (index in polygon.xpoints) in a polygon array.
     * This class makes sure that the boundary indexes are allways within the size of the
     * polygon arrays size.
     *
     * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
     * <p>created on: 04.06.2012</p>
     */
    private static class InsertionBoundary {
        private int startX;
        private int endX;

        /** max value that insertion point may have */
        private int maxX;

        public InsertionBoundary(int arrayLength) {
            super();

            if (arrayLength == 0) {
                throw new IllegalArgumentException("arrayLength must be > 0");
            }

            this.maxX = arrayLength-1;
        }

        /**
         * Decreases start index by one
         * @return <code>true</code> if current start index was &gt;0 before, <code>false</code> otherwise
         */
        public boolean decreaseStartX() {
            if (startX > 0) {
                --startX;
                return true;
            }

            return false;
        }

        /**
         * Increases end index by one
         * @return <code>true</code> if current end index was &lt; max value before, <code>false</code> otherwise
         */
        public boolean increaseEndX() {
            if (endX < maxX) {
                ++endX;
                return true;
            }

            return false;
        }

        public int getDistance() {
            return endX - startX;
        }

        public int getStartX() {
            return startX;
        }

        public void setStartX(int startX) {
            this.startX = startX < 0 ? 0 : startX;
        }

        public int getEndX() {
            return endX;
        }

        public void setEndX(int endX) {
            if (endX < 0) {
                endX = 0;
            }
            this.endX = endX > maxX ? maxX : endX;
        }

    }
}

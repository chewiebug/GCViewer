package com.tagtraum.perf.gcviewer.math;

import java.io.Serializable;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 4:04:43 PM
 *
 */
public class RegressionLine implements Serializable {

    private double sumX;
    private double sumY;
    private double sumXSquare;
    private double sumXY;
    private int n;

    public void addPoint(double x, double y) {
        sumX += x;
        sumY += y;
        sumXSquare += x*x;
        sumXY += x*y;
        n++;
    }

    public int getPointCount() {
        return n;
    }

    public boolean isLine() {
        return n>1;
    }

    public boolean hasPoints() {
        return n != 0;
    }

    public double slope() {
        return slope(n, sumX, sumY, sumXY, sumXSquare);
    }

    public void reset() {
        sumX = 0;
        sumY = 0;
        sumXSquare = 0;
        sumXY = 0;
        n = 0;
    }

    public static double slope(double[] x, double[] y) {
        double n = x.length;
        double sumX = Sum.sum(x);
        double sumY = Sum.sum(y);
        double sumXY = Sum.sumOfProducts(x, y);
        double sumXSquare = Sum.sumOfSquares(x);
        return slope(n, sumX, sumY, sumXY, sumXSquare);
    }

    private static double slope(double n, double sumX, double sumY, double sumXY, double sumXSquare) {
        return (n * sumXY - sumX * sumY) / (n * sumXSquare - sumX * sumX);
    }

}

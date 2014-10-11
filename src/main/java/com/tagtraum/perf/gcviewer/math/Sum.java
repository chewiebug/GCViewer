package com.tagtraum.perf.gcviewer.math;

import java.io.Serializable;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 5:06:34 PM
 *
 */
public class Sum implements Serializable {
    private Sum(){}


    public static double sum(double[] n) {
        double sum = 0;
        for (int i=0; i<n.length; i++) {
            sum += n[i];
        }
        return sum;
    }

    public static double sumOfSquares(double[] n) {
        double sumXSquare = 0;
        for (int i=0; i<n.length; i++) {
            sumXSquare += n[i]*n[i];
        }
        return sumXSquare;
    }

    public static double sumOfProducts(double[] x, double[] y) {
        double sumXY = 0;
        for (int i=0; i<x.length; i++) {
            sumXY += x[i]*y[i];
        }
        return sumXY;
    }
}

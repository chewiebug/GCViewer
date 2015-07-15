package com.tagtraum.perf.gcviewer.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Extends {@link DoubleData} with the functionality to calculate percentiles.
 */
public class DoubleDataPercentile extends DoubleData {
    private List<Double> doubleSet = new ArrayList<>();
    private boolean isSorted = false;

    @Override
    public void add(double x) {
        super.add(x);
        doubleSet.add(x);
        isSorted = false;
    }

    /**
     * return the n-th percentile of the list.
     * @param percentile percentile as floating point number (median = 50.0)
     * @return value at n-th percentile
     */
    public double getPercentile(double percentile) {
        // https://matheguru.com/stochastik/quantil-perzentil.html
        if (!isSorted) {
            Collections.sort(doubleSet);
        }

        if (percentile < 10) {
            percentile = 10;
        }
        else if (percentile > 100) {
            percentile = 100;
        }

        double position = percentile / 100 * doubleSet.size();
        if ((position == Math.rint(position)) && !Double.isInfinite(position)) {
            position = (position + position + 1) / 2;
        } else {
            position = Math.ceil(position);
        }
        return doubleSet.get((int)position-1);
    }
}

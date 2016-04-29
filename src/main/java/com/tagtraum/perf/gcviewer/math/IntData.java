package com.tagtraum.perf.gcviewer.math;

import java.io.Serializable;

/**
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * Date: May 20, 2005
 * Time: 5:08:33 PM
 *
 */
public class IntData implements Serializable  {

    private int n;
    private long sum;
    private long sumSquares;
    private int min = Integer.MAX_VALUE;
	private int max = Integer.MIN_VALUE;

    public void add(int x) {
        sum += x;
        sumSquares += ((long)x)*((long)x);
        n++;
        min = Math.min(min, x);
        max = Math.max(max, x);
    }

    public void add(int x, int weight) {
        sum += x * weight;
        n += weight;
        sumSquares += ((long)x)*((long)x)*(weight);
        min = Math.min(min, x);
        max = Math.max(max, x);
    }

    public int getN() {
        return n;
    }

    public long getSum() {
        return sum;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public double average() {
        if (n == 0) throw new IllegalStateException("n == 0");
        return ((double)sum) / ((double)n);
    }

    public double standardDeviation() {
        if (n == 0) throw new IllegalStateException("n == 0");
        if (n==1) return 0;
        return Math.sqrt(variance());
    }

    public double variance() {
        if (n == 0) throw new IllegalStateException("n == 0");
        if (n==1) return 0;
        final double dsum = sum;
        final double dn = n;
        return (sumSquares - dsum*dsum/dn)/(dn-1);
    }

    public void reset() {
        sum = 0;
        sumSquares = 0;
        n = 0;
    }

    public static long weightedAverage(long[] n, int[] weight) {
        long sum = 0;
        int m = 0;
        for (int i=0; i<n.length; i++) {
            sum += n[i]*weight[i];
            m += weight[i];
        }
        return sum / m;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        IntData intData = (IntData) o;

        if (n != intData.n)
            return false;
        if (sum != intData.sum)
            return false;
        if (sumSquares != intData.sumSquares)
            return false;
        if (min != intData.min)
            return false;
        return max == intData.max;

    }

    @Override
    public int hashCode() {
        int result = n;
        result = 31 * result + (int) (sum ^ (sum >>> 32));
        result = 31 * result + (int) (sumSquares ^ (sumSquares >>> 32));
        result = 31 * result + min;
        result = 31 * result + max;
        return result;
    }
}

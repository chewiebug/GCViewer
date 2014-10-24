package com.tagtraum.perf.gcviewer.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestDoubleData {

    @Test
    public void testSimpleAverage() throws Exception {
        double[] x = {1.0, 2.0};
        assertEquals("Simple average", 1.5, DoubleData.average(x), 0.0);
    }

    public void testSimpleStandardDeviation() throws Exception {
        DoubleData doubleData = new DoubleData();
        doubleData.add(1);
        doubleData.add(1);
        doubleData.add(-1);
        doubleData.add(-1);

        assertEquals("Simple std deviation", 1.1547005383792515, doubleData.standardDeviation(), 0.0000001);
    }

}

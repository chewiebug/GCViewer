package com.tagtraum.perf.gcviewer.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
class TestDoubleData {

    @Test
    void testSimpleAverage() {
        double[] x = {1.0, 2.0};
        assertEquals( 1.5, DoubleData.average(x), 0.0, "Simple average");
    }

    @Test
    void testSimpleStandardDeviation() {
        DoubleData doubleData = new DoubleData();
        doubleData.add(1);
        doubleData.add(1);
        doubleData.add(-1);
        doubleData.add(-1);

        assertEquals(1.1547005383792515, doubleData.standardDeviation(), 0.0000001, "Simple std deviation");
    }

}

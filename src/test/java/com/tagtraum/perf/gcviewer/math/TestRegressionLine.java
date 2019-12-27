package com.tagtraum.perf.gcviewer.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
class TestRegressionLine {

    @Test
    void testSimpleSlope() {
        double[] x = {0.0, 1.0, 2.0, 3.0};
        double[] y = {0.0, 1.0, 2.0, 3.0};
        
        assertEquals(1.0, RegressionLine.slope(x, y), 0.0, "Simple regression line slope test");
    }

}

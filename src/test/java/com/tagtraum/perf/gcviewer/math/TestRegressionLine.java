package com.tagtraum.perf.gcviewer.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestRegressionLine {

    @Test
    public void testSimpleSlope() throws Exception {
        double[] x = {0.0, 1.0, 2.0, 3.0};
        double[] y = {0.0, 1.0, 2.0, 3.0};
        
        assertEquals("Simple regression line slope test", 1.0, RegressionLine.slope(x, y), 0.0);
    }

}

package com.tagtraum.perf.gcviewer.math;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class TestRegressionLine extends TestCase {

    public TestRegressionLine(String name) {
        super(name);
    }

    public void testSimpleSlope() throws Exception {
        double[] x = {0.0, 1.0, 2.0, 3.0};
        double[] y = {0.0, 1.0, 2.0, 3.0};
        assertEquals("Simple regression line slope test", 1.0, RegressionLine.slope(x, y), 0.0);
    }

    public static TestSuite suite() {
        return new TestSuite(TestRegressionLine.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

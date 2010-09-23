package com.tagtraum.perf.gcviewer.math;

import junit.framework.TestSuite;
import junit.textui.TestRunner;
import com.tagtraum.perf.gcviewer.TestDataReaderFactory;

public class TestAll {

    /** Source-Version: <code>$Id: TestAll.java,v 1.2 2002/01/20 02:08:41 hendriks73 Exp $</code> */
    public static String vcid = "$Id: TestAll.java,v 1.2 2002/01/20 02:08:41 hendriks73 Exp $";

    /**
     * Returns the TestSuite for TestAll.
     */
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestRegressionLine.class);
        suite.addTestSuite(TestDoubleData.class);
        suite.addTestSuite(TestIntData.class);
        // subpackages
        //suite.addTest(com.tagtraum.perf.gcviewer.math.TestAll.suite());
        return suite;
    }

    /**
     * Runs the textui JUnit-TestRunner for TestAll.
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

}

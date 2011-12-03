package com.tagtraum.perf.gcviewer.imp;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class TestAll {

    /** Source-Version: <code>$Id: TestAll.java,v 1.2 2002/01/20 02:08:41 hendriks73 Exp $</code> */
    public static String vcid = "$Id: TestAll.java,v 1.2 2002/01/20 02:08:41 hendriks73 Exp $";

    /**
     * Returns the TestSuite for TestAll.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("all datareader tests");
        suite.addTestSuite(TestDataReaderFactory.class);

        suite.addTestSuite(TestDataReaderSun1_2_2.class);
        suite.addTestSuite(TestDataReaderSun1_3_1.class);
        // suite.addTestSuite(TestDataReaderSun1_3_1_19.class);
        suite.addTestSuite(TestDataReaderSun1_4_0.class);
        suite.addTestSuite(TestDataReaderSun1_5_0.class);
        suite.addTestSuite(TestDataReaderSun1_6_0.class);
        suite.addTestSuite(TestDataReaderSun1_6_0G1.class);

        suite.addTestSuite(TestDataReaderIBM1_2_2.class);
        suite.addTestSuite(TestDataReaderIBM1_3_1.class);
        suite.addTestSuite(TestDataReaderIBMi5OS1_4_2.class);
        suite.addTestSuite(TestDataReaderHPUX1_2.class);
        suite.addTestSuite(TestDataReaderJRockit1_4_2.class);
        suite.addTestSuite(TestDataReaderJRockit1_5_0.class);

        return suite;
    }

    /**
     * Runs the textui JUnit-TestRunner for TestAll.
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

}

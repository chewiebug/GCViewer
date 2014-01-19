package com.tagtraum.perf.gcviewer.imp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * TestSuite in JUnit 4 style; points to all testcases, that should be run.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 25.01.2012</p>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestAbstractDataReaderSun.class,
    
    TestDataReaderFacade.class,
    TestDataReaderFactory.class,
    TestDataReaderSun1_2_2.class,
    TestDataReaderSun1_3_1.class,
    // TestDataReaderSun1_3_1_19.class,
    TestDataReaderSun1_4_0.class,
    TestDataReaderSun1_5_0.class,
    TestDataReaderSun1_6_0.class,
    TestDataReaderSun1_6_0G1.class,
    TestDataReaderSun1_7_0.class,
    TestDataReaderSun1_7_0G1.class,
    TestDataReaderSun1_8_0.class,

    TestDataReaderIBM1_2_2.class,
    TestDataReaderIBM1_3_1.class,
    TestDataReaderIBM1_4_2.class,
    TestDataReaderIBMi5OS1_4_2.class,
    TestDataReaderIBMJ9_5_0.class,
    
    TestDataReaderHPUX1_2.class,
    TestDataReaderHPUX1_4_1.class,
    
    TestDataReaderJRockit1_4_2.class,
    TestDataReaderJRockit1_5_0.class,
    TestDataReaderJRockit1_6_0.class,
})
public class TestAllImp {
}

package com.tagtraum.perf.gcviewer.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * TestSuite in JUnit 4 style; points to all testcases, that should be run.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 04.02.2012</p>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    TestGcEvent.class,
    TestAbstractGCEvent.class
})
public class TestAll {
}

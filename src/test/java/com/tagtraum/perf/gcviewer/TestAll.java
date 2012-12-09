package com.tagtraum.perf.gcviewer;

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
    ModelPanelTest.class,
    TestBuildInfoReader.class,
    com.tagtraum.perf.gcviewer.exp.TestAll.class,
    com.tagtraum.perf.gcviewer.imp.TestAll.class,
    com.tagtraum.perf.gcviewer.math.TestAll.class,
    com.tagtraum.perf.gcviewer.model.TestAll.class
})
public class TestAll {
}

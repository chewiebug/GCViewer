package com.tagtraum.perf.gcviewer.view;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * TestSuite in JUnit 4 style; points to all testcases, that should be run.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 06.08.2014</p>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ModelChartImplTest.class,
    ModelMetricsPanelTest.class,
})
public class TestAllView {
}

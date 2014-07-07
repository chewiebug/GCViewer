package com.tagtraum.perf.gcviewer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.tagtraum.perf.gcviewer.ctrl.TestAllCtrl;
import com.tagtraum.perf.gcviewer.exp.TestAllExp;
import com.tagtraum.perf.gcviewer.imp.TestAllImp;
import com.tagtraum.perf.gcviewer.math.TestAllMath;
import com.tagtraum.perf.gcviewer.model.TestAllModel;
import com.tagtraum.perf.gcviewer.util.TestAllUtil;

/**
 * TestSuite in JUnit 4 style; points to all testcases, that should be run.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 25.01.2012</p>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ModelMetricsPanelTest.class,
    TestBuildInfoReader.class,
    TestAllExp.class,
    TestAllImp.class,
    TestAllMath.class,
    TestAllModel.class,
    TestAllUtil.class,
    TestAllCtrl.class,
})
public class TestAll {
}

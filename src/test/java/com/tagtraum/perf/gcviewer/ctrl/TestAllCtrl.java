package com.tagtraum.perf.gcviewer.ctrl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Testsuite for all controller classes.
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 05.01.2014</p>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
TestGCModelLoader.class,
TestGCModelLoaderController.class,
})
public class TestAllCtrl {
}

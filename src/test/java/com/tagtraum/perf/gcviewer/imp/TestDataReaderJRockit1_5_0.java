/*
 * =================================================
 * Copyright 2006 tagtraum industries incorporated
 * All rights reserved.
 * =================================================
 */
package com.tagtraum.perf.gcviewer.imp;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.InputStream;


/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class TestDataReaderJRockit1_5_0 extends TestCase {

    public TestDataReaderJRockit1_5_0(String name) {
        super(name);
    }

    public void test() throws Exception {
        InputStream in = getClass().getResourceAsStream("SampleJRockit1_5_0.txt");
        DataReader reader = new DataReaderJRockit1_4_2(in);
        reader.read();
    }

    public static TestSuite suite() {
        return new TestSuite(TestDataReaderJRockit1_5_0.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestDataReaderJRockit1_5_0.suite());
    }
}

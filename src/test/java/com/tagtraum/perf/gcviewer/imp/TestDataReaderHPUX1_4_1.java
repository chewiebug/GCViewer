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
public class TestDataReaderHPUX1_4_1 extends TestCase {

    public TestDataReaderHPUX1_4_1(final String name) {
        super(name);
    }

    public void testParse1() throws Exception {
        final InputStream in = getClass().getResourceAsStream("SampleHP-UX1_4_1.txt");
        final DataReader reader = new DataReaderHPUX1_4_1(in);
        reader.read();
    }

    public static TestSuite suite() {
        return new TestSuite(TestDataReaderHPUX1_4_1.class);
    }

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(TestDataReaderHPUX1_4_1.suite());
    }
}

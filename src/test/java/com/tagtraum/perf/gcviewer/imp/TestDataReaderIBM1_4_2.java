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
public class TestDataReaderIBM1_4_2 extends TestCase {

    public TestDataReaderIBM1_4_2(final String name) {
        super(name);
    }

    public void testParse1() throws Exception {
        final InputStream in = getClass().getResourceAsStream("SampleIBM1_4_2.txt");
        final DataReader reader = new DataReaderIBM1_4_2(in);
        reader.read();
    }

    public static TestSuite suite() {
        return new TestSuite(TestDataReaderIBM1_4_2.class);
    }

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(TestDataReaderIBM1_4_2.suite());
    }
}

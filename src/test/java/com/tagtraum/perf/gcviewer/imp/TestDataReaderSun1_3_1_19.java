package com.tagtraum.perf.gcviewer.imp;

import java.io.InputStream;

import junit.framework.TestCase;

import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @version $Id: $
 */
public class TestDataReaderSun1_3_1_19 extends TestCase {

    public TestDataReaderSun1_3_1_19(String name) {
        super(name);
    }

    public void testCMSPrintGCDetails() throws Exception {
    	// does not seem to be implemented at all
    	
    	final InputStream in = getClass().getResourceAsStream("SampleSun1_3_1_19SunOS.txt");
        final DataReader reader = new DataReaderSun1_3_1(in, GcLogType.SUN1_3_1);
        GCModel model = reader.read();
        
        assertEquals("throughput", 95.21, model.getThroughput(), 0.01);
    }

}
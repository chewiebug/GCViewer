package com.tagtraum.perf.gcviewer.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class TestIntData {

    @Test
    public void testSimpleAverage() throws Exception {
        IntData intData = new IntData();
        intData.add(1);
        intData.add(2);
        
        assertEquals("Simple average", 1.5, intData.average(), 0.0);
    }

    public void testSimpleStandardDeviation() throws Exception {
        IntData intData = new IntData();
        intData.add(1);
        intData.add(1);
        intData.add(-1);
        intData.add(-1);
        
        assertEquals("Simple std deviation", 1.1547005383792515, intData.standardDeviation(), 0.0000001);
    }

}

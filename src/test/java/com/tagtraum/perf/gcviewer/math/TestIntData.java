package com.tagtraum.perf.gcviewer.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * Date: Jan 30, 2002
 * Time: 5:53:55 PM
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
class TestIntData {

    @Test
    void testSimpleAverage() {
        IntData intData = new IntData();
        intData.add(1);
        intData.add(2);
        
        assertEquals(1.5, intData.average(), 0.0, "Simple average");
    }

    @Test
    void testSimpleStandardDeviation() {
        IntData intData = new IntData();
        intData.add(1);
        intData.add(1);
        intData.add(-1);
        intData.add(-1);
        
        assertEquals(1.1547005383792515, intData.standardDeviation(), 0.0000001, "Simple std deviation");
    }

}

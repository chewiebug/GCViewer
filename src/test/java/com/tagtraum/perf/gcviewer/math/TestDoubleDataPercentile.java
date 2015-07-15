package com.tagtraum.perf.gcviewer.math;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a> <p>created on: 15.07.2015</p>
 */
public class TestDoubleDataPercentile {
    private DoubleDataPercentile ddpInteger;
    private DoubleDataPercentile ddp;


    @Before
    public void setup() throws Exception {
        ddpInteger = new DoubleDataPercentile();
        ddpInteger.add(15);
        ddpInteger.add(20);
        ddpInteger.add(35);
        ddpInteger.add(40);
        ddpInteger.add(50);

        ddp = new DoubleDataPercentile();
        ddp.add(1.5);
        ddp.add(2.3);
        ddp.add(3.5);
        ddp.add(4.1);
        ddp.add(5.2);
    }

    @Test
    public void integerZero() throws Exception {
        assertThat("integer 10 percentile", ddpInteger.getPercentile(10), closeTo(15, 0.1));
    }

    @Test
    public void integerFifty() throws Exception {
        assertThat("integer 50 percentile", ddpInteger.getPercentile(50), closeTo(35, 0.1));
    }

    @Test
    public void integerSeventyfive() throws Exception {
        assertThat("integer 75 percentile", ddpInteger.getPercentile(75), closeTo(40, 0.1));
    }

    @Test
    public void integerHundred() throws Exception {
        assertThat("integer 100 percentile", ddpInteger.getPercentile(100), closeTo(50, 0.1));
    }

    @Test
    public void zero() {
        assertThat("10 percentile", ddp.getPercentile(10), closeTo(1.5, 0.001));
    }

    @Test
    public void median() {
        assertThat("median", ddp.getPercentile(50), closeTo(3.5, 0.001));
    }

    @Test
    public void seventyFife() {
        assertThat("75 percentile", ddp.getPercentile(75), closeTo(4.1, 0.001));
    }

    @Test
    public void hundred() {
        assertThat("100 percentile", ddp.getPercentile(100), closeTo(5.2, 0.001));
    }
}

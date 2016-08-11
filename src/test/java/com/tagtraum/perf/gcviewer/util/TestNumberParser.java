package com.tagtraum.perf.gcviewer.util;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test methods of {@link NumberParser}.
 */
public class TestNumberParser {

    @Test
    public void testParseIntString() throws Exception {
        int result = NumberParser.parseInt("1442450944");
        Assert.assertThat("string int", result, Matchers.is(1442450944));
    }

    @Test
    public void testParseIntStringNegative() throws Exception {
        int result = NumberParser.parseInt("-1442450944");
        Assert.assertThat("string negative int", result, Matchers.is(-1442450944));
    }

    @Test
    public void testParseIntChar() throws Exception {
        int result = NumberParser.parseInt(new char[] {'1', '4', '4', '2', '4', '5', '0', '9', '4', '4'}, 0, 10);
        Assert.assertThat("char int", result, Matchers.is(1442450944));
    }

    @Test
    public void testParseIntCharNegative() throws Exception {
        int result = NumberParser.parseInt(new char[] {'-', '1', '4', '4', '2', '4', '5', '0', '9', '4', '4'}, 0, 11);
        Assert.assertThat("char negative int", result, Matchers.is(-1442450944));
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntTooLarge() throws Exception {
        NumberParser.parseInt("6442450944");
    }

    @Test
    public void testParseLongString() throws Exception {
        long result = NumberParser.parseLong("6442450944");
        Assert.assertThat("string long", result, Matchers.is(6442450944L));
    }

    @Test
    public void testParseLongStringNegative() throws Exception {
        long result = NumberParser.parseLong("-6442450944");
        Assert.assertThat("string negative long", result, Matchers.is(-6442450944L));

    }

    @Test
    public void testParseLongChar() throws Exception {
        long result = NumberParser.parseLong(new char[] {'6', '4', '4', '2', '4', '5', '0', '9', '4', '4'}, 0, 10);
        Assert.assertThat("char long", result, Matchers.is(6442450944L));
    }

    @Test
    public void testParseLongCharNegative() throws Exception {
        long result = NumberParser.parseLong(new char[] {'-', '6', '4', '4', '2', '4', '5', '0', '9', '4', '4'}, 0, 11);
        Assert.assertThat("char negative long", result, Matchers.is(-6442450944L));
    }

}

package com.tagtraum.perf.gcviewer.util;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test methods of {@link NumberParser}.
 */
class TestNumberParser {

    @Test
    void testParseIntString() {
        int result = NumberParser.parseInt("1442450944");
        assertThat("string int", result, Matchers.is(1442450944));
    }

    @Test
    void testParseIntStringNegative() {
        int result = NumberParser.parseInt("-1442450944");
        assertThat("string negative int", result, Matchers.is(-1442450944));
    }

    @Test
    void testParseIntChar() {
        int result = NumberParser.parseInt(new char[] {'1', '4', '4', '2', '4', '5', '0', '9', '4', '4'}, 0, 10);
        assertThat("char int", result, Matchers.is(1442450944));
    }

    @Test
    void testParseIntCharNegative() {
        int result = NumberParser.parseInt(new char[] {'-', '1', '4', '4', '2', '4', '5', '0', '9', '4', '4'}, 0, 11);
        assertThat("char negative int", result, Matchers.is(-1442450944));
    }

    @Test
    void testParseIntTooLarge() {
        assertThrows(NumberFormatException.class, () -> NumberParser.parseInt("6442450944"));
    }

    @Test
    void testParseLongString() {
        long result = NumberParser.parseLong("6442450944");
        assertThat("string long", result, Matchers.is(6442450944L));
    }

    @Test
    void testParseLongStringNegative() {
        long result = NumberParser.parseLong("-6442450944");
        assertThat("string negative long", result, Matchers.is(-6442450944L));
    }

    @Test
    void testParseLongChar() {
        long result = NumberParser.parseLong(new char[] {'6', '4', '4', '2', '4', '5', '0', '9', '4', '4'}, 0, 10);
        assertThat("char long", result, Matchers.is(6442450944L));
    }

    @Test
    void testParseLongCharNegative() throws Exception {
        long result = NumberParser.parseLong(new char[] {'-', '6', '4', '4', '2', '4', '5', '0', '9', '4', '4'}, 0, 11);
        assertThat("char negative long", result, Matchers.is(-6442450944L));
    }

}

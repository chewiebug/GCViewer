package com.tagtraum.perf.gcviewer.util;

/**
 * Fast methods for parsing ints and longs.
 * <p>
 * This class originally stems form the book "Performant Java Programmieren" by Hendrik Schreiber
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * @see <a href="http://www.tagtraum.com/performance/">"Performant Java Programmieren" by Hendrik Schreiber</a>
 */
public class NumberParser {
    // replace parseInt(String) with parseInt(CharSequence) in later version
    // replace parseLong(String) with parseLong(CharSequence) in later version

    public static final int MAX_NEGATIVE_INTEGER_CHARS = Integer.toString(Integer.MIN_VALUE).length();
    public static final int MAX_POSITIVE_INTEGER_CHARS = Integer.toString(Integer.MAX_VALUE).length();

    public static final int MAX_NEGATIVE_LONG_CHARS = Long.toString(Long.MIN_VALUE).length();
    public static final int MAX_POSITIVE_LONG_CHARS = Long.toString(Long.MAX_VALUE).length();

    public static int parseInt(char[] cb, int offset, int length ) throws NumberFormatException {
        if (cb == null) throw new NumberFormatException("null");
        int result = 0;
        boolean negative = false;
        int i = 0;
        int limit;
        int digit;

        if (length > 0) {
            if (cb[offset] == '-') {
                if (length > MAX_NEGATIVE_INTEGER_CHARS) throw new NumberFormatException(new String(cb, offset, length));
                negative = true;
                limit = Integer.MIN_VALUE;
                i++;
            } else {
                if (length > MAX_POSITIVE_INTEGER_CHARS) throw new NumberFormatException(new String(cb, offset, length));
                limit = -Integer.MAX_VALUE;
            }
            while (i < length) {
                digit = cb[offset + i++]-'0';
                if (digit < 0 || digit > 9) {
                    throw new NumberFormatException(new String(cb, offset, length));
                }
                result *= 10;
                if (result < limit + digit) {
                    throw new NumberFormatException(new String(cb, offset, length));
                }
                result -= digit;
            }
        } else {
            throw new NumberFormatException(new String(cb, offset, length));
        }
        if (negative) {
            if (i > 1) {
                return result;
            } else {
                throw new NumberFormatException(new String(cb, offset, length));
            }
        } else {
            return -result;
        }
    }

    public static int parseInt(String s) throws NumberFormatException {
        return parseInt(s, 0, s.length());
    }

    public static int parseInt(String s, int offset, int length) throws NumberFormatException {
        // for speed this is a copy of parseInt(string) instead of just using toCharArrays()...
        if (s == null) throw new NumberFormatException("null");
        int result = 0;
        boolean negative = false;
        int i = 0;
        int limit;
        int digit;

        if (length > 0) {
            if (s.charAt(offset) == '-') {
                if (length > MAX_NEGATIVE_INTEGER_CHARS) throw new NumberFormatException(s);
                negative = true;
                limit = Integer.MIN_VALUE;
                i++;
            } else {
                if (length > MAX_POSITIVE_INTEGER_CHARS) throw new NumberFormatException(s);
                limit = -Integer.MAX_VALUE;
            }
            while (i < length) {
                digit = s.charAt(offset + i++)-'0';
                if (digit < 0 || digit > 9) {
                    throw new NumberFormatException(s);
                }
                result *= 10;
                if (result < limit + digit) {
                    throw new NumberFormatException(s);
                }
                result -= digit;
            }
        } else {
            throw new NumberFormatException(s);
        }
        if (negative) {
            if (i > 1) {
                return result;
            } else {
                throw new NumberFormatException(s);
            }
        } else {
            return -result;
        }
    }

    public static long parseLong(String s) throws NumberFormatException {
        return parseLong(s, 0, s.length());
    }

    public static long parseLong(String s, int offset, int length) throws NumberFormatException {
        if (s == null) throw new NumberFormatException("null");
        long result = 0;
        boolean negative = false;
        int i = 0;
        long limit;
        int digit;

        if (length > 0) {
            if (s.charAt(offset) == '-') {
                // shortcut for ints
                if (length <= MAX_NEGATIVE_INTEGER_CHARS) return parseInt(s, offset, length);
                if (length > MAX_NEGATIVE_LONG_CHARS) throw new NumberFormatException(s);
                negative = true;
                limit = Long.MIN_VALUE;
                i++;
            } else {
                // shortcut for ints
                if (length <= MAX_POSITIVE_INTEGER_CHARS) return parseInt(s, offset, length);
                if (length > MAX_POSITIVE_LONG_CHARS) throw new NumberFormatException(s);
                limit = -Long.MAX_VALUE;
            }
            while (i < length) {
                digit = s.charAt(offset + i++)-'0';
                if (digit < 0 || digit > 9) {
                    throw new NumberFormatException(s);
                }
                result *= 10L;
                if (result < limit + digit) {
                    throw new NumberFormatException(s);
                }
                result -= digit;
            }
        } else {
            throw new NumberFormatException(s);
        }
        if (negative) {
            if (i > 1) {
                return result;
            } else {
                throw new NumberFormatException(s);
            }
        } else {
            return -result;
        }
    }

    public static long parseLong(char[] cb, int offset, int length) throws NumberFormatException {
        // for speed this is a copy of parseLong(string) instead of just using toCharArrays()...
        if (cb == null) throw new NumberFormatException("null");
        long result = 0;
        boolean negative = false;
        int i = 0;
        long limit;
        int digit;

        if (length > 0) {
            if (cb[offset] == '-') {
                // shortcut for ints
                if (length <= MAX_NEGATIVE_INTEGER_CHARS) return parseInt(cb, offset, length);
                if (length > MAX_NEGATIVE_LONG_CHARS) throw new NumberFormatException(new String(cb, offset, length));
                negative = true;
                limit = Long.MIN_VALUE;
                i++;
            } else {
                // shortcut for ints
                if (length <= MAX_POSITIVE_INTEGER_CHARS) return parseInt(cb, offset, length);
                if (length > MAX_POSITIVE_LONG_CHARS) throw new NumberFormatException(new String(cb, offset, length));
                limit = -Long.MAX_VALUE;
            }
            while (i < length) {
                digit = cb[offset + i++]-'0';
                if (digit < 0 || digit > 9) {
                    throw new NumberFormatException(new String(cb, offset, length));
                }
                result *= 10L;
                if (result < limit + digit) {
                    throw new NumberFormatException(new String(cb, offset, length));
                }
                result -= digit;
            }
        } else {
            throw new NumberFormatException(new String(cb, offset, length));
        }
        if (negative) {
            if (i > 1) {
                return result;
            } else {
                throw new NumberFormatException(new String(cb, offset, length));
            }
        } else {
            return -result;
        }
    }
    
    public static double parseDouble(String s, int offset, int length) {
        // Currently, this method is unlikely to be as efficient as those above.
        // This logic is factored out so as to allow future optimisations. For
        // example, we might want to make some simplifying assumptions about the
        // kind of doubles present in GC logs.
        
        return parseDouble(s.substring(offset, offset + length));
    }
    
    public static double parseDouble (String s) {
        // replace "," with "." because doubles may only contain "."
        // some localized gc logs contain "," in pauses
        return Double.parseDouble(s.replace(",", "."));
    }
}

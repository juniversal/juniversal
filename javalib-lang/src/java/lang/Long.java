/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang;

/**
 * The wrapper for the primitive type {@code long}.
 * <p>
 * As with the specification, this implementation relies on code laid out in <a
 * href="http://www.hackersdelight.org/">Henry S. Warren, Jr.'s Hacker's Delight, (Addison Wesley,
 * 2002)</a> as well as <a href="http://aggregate.org/MAGIC/">The Aggregate's Magic Algorithms</a>.
 * 
 * JUniversal REMOVED:
 * 
 * getLong since System.getProperty isn't supported
 * 
 * TYPE since there are no Class objects currently
 *
 * longValue
 * 
 * @see java.lang.Number
 * @since 1.0
 */
public final class Long extends Number implements Comparable<Long> {
    /**
     * The value which the receiver represents.
     */
    private final long value;

    /**
     * Constant for the maximum {@code long} value, 2<sup>63</sup>-1.
     */
    public static final long MAX_VALUE = 0x7FFFFFFFFFFFFFFFL;

    /**
     * Constant for the minimum {@code long} value, -2<sup>63</sup>.
     */
    public static final long MIN_VALUE = 0x8000000000000000L;

    // Note: This can't be set to "long.class", since *that* is
    // defined to be "java.lang.Long.TYPE";

    /**
     * Constant for the number of bits needed to represent a {@code long} in
     * two's complement form.
     *
     * @since 1.5
     */
    public static final int SIZE = 64;


    /**
     * Constructs a new {@code Long} with the specified primitive long value.
     * 
     * @param value
     *            the primitive long value to store in the new instance.
     */
    public Long(long value) {
        this.value = value;
    }

    /**
     * Constructs a new {@code Long} from the specified string.
     * 
     * @param string
     *            the string representation of a long value.
     * @throws NumberFormatException
     *             if {@code string} can not be decoded into a long value.
     * @see #parseLong(String)
     */
    public Long(String string) throws NumberFormatException {
        this.value = parseLong(string);
    }

    @Override
    public byte byteValue() {
        return (byte) value;
    }

    /**
     * Compares this object to the specified long object to determine their
     * relative order.
     * 
     * @param object
     *            the long object to compare this object to.
     * @return a negative value if the value of this long is less than the value
     *         of {@code object}; 0 if the value of this long and the value of
     *         {@code object} are equal; a positive value if the value of this
     *         long is greater than the value of {@code object}.
     * @see java.lang.Comparable
     * @since 1.2
     */
    public int compareTo(Long object) {
        return value > object.value ? 1 : (value < object.value ? -1 : 0);
    }

    /**
     * Parses the specified string and returns a {@code Long} instance if the
     * string can be decoded into a long value. The string may be an optional
     * minus sign "-" followed by a hexadecimal ("0x..." or "#..."), octal
     * ("0..."), or decimal ("...") representation of a long.
     * 
     * @param string
     *            a string representation of a long value.
     * @return a {@code Long} containing the value represented by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} can not be parsed as a long value.
     */
    public static Long decode(String string) throws NumberFormatException {
        int length = string.length(), i = 0;
        if (length == 0) {
            throw new NumberFormatException();
        }
        char firstDigit = string.charAt(i);
        boolean negative = firstDigit == '-';
        if (negative) {
            if (length == 1) {
                throw new NumberFormatException(string);
            }
            firstDigit = string.charAt(++i);
        }

        int base = 10;
        if (firstDigit == '0') {
            if (++i == length) {
                return valueOf(0L);
            }
            if ((firstDigit = string.charAt(i)) == 'x' || firstDigit == 'X') {
                if (i == length) {
                    throw new NumberFormatException(string);
                }
                i++;
                base = 16;
            } else {
                base = 8;
            }
        } else if (firstDigit == '#') {
            if (i == length) {
                throw new NumberFormatException(string);
            }
            i++;
            base = 16;
        }

        long result = parse(string, i, base, negative);
        return valueOf(result);
    }

    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Compares this instance with the specified object and indicates if they
     * are equal. In order to be equal, {@code o} must be an instance of
     * {@code Long} and have the same long value as this object.
     * 
     * @param o
     *            the object to compare this long with.
     * @return {@code true} if the specified object is equal to this
     *         {@code Long}; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        return (o instanceof Long)
                && (value == ((Long) o).value);
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    /**
     * Parses the specified string as a signed decimal long value. The ASCII
     * character \u002d ('-') is recognized as the minus sign.
     * 
     * @param string
     *            the string representation of a long value.
     * @return the primitive long value represented by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null}, has a length of zero or
     *             can not be parsed as a long value.
     */
    public static long parseLong(String string) throws NumberFormatException {
        return parseLong(string, 10);
    }

    /**
     * Parses the specified string as a signed long value using the specified
     * radix. The ASCII character \u002d ('-') is recognized as the minus sign.
     * 
     * @param string
     *            the string representation of a long value.
     * @param radix
     *            the radix to use when parsing.
     * @return the primitive long value represented by {@code string} using
     *         {@code radix}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null} or has a length of zero,
     *             {@code radix < Character.MIN_RADIX},
     *             {@code radix > Character.MAX_RADIX}, or if {@code string}
     *             can not be parsed as a long value.
     */
    public static long parseLong(String string, int radix)
            throws NumberFormatException {
        if (string == null || radix < Character.MIN_RADIX
                || radix > Character.MAX_RADIX) {
            throw new NumberFormatException();
        }
        int length = string.length(), i = 0;
        if (length == 0) {
            throw new NumberFormatException(string);
        }
        boolean negative = string.charAt(i) == '-';
        if (negative && ++i == length) {
            throw new NumberFormatException(string);
        }

        return parse(string, i, radix, negative);
    }

    private static long parse(String string, int offset, int radix,
            boolean negative) {
        long max = Long.MIN_VALUE / radix;
        long result = 0, length = string.length();
        while (offset < length) {
            int digit = Character.digit(string.charAt(offset++), radix);
            if (digit == -1) {
                throw new NumberFormatException(string);
            }
            if (max > result) {
                throw new NumberFormatException(string);
            }
            long next = result * radix - digit;
            if (next > result) {
                throw new NumberFormatException(string);
            }
            result = next;
        }
        if (!negative) {
            result = -result;
            if (result < 0) {
                throw new NumberFormatException(string);
            }
        }
        return result;
    }

    @Override
    public short shortValue() {
        return (short) value;
    }

    /**
     * Converts the specified long value into its binary string representation.
     * The returned string is a concatenation of '0' and '1' characters.
     * 
     * @param l
     *            the long value to convert.
     * @return the binary string representation of {@code l}.
     */
    public static String toBinaryString(long l) {
        int count = 1;
        long j = l;

        if (l < 0) {
            count = 64;
        } else {
            while ((j >>= 1) != 0) {
                count++;
            }
        }

        String buffer = new String(count);
        char[] bufferValue = buffer.getValue();
        do {
            bufferValue[--count] = (char) ((l & 1) + '0');
            l >>= 1;
        } while (count > 0);
        return buffer;
    }

    /**
     * Converts the specified long value into its hexadecimal string
     * representation. The returned string is a concatenation of characters from
     * '0' to '9' and 'a' to 'f'.
     * 
     * @param l
     *            the long value to convert.
     * @return the hexadecimal string representation of {@code l}.
     */
    public static String toHexString(long l) {
        int count = 1;
        long j = l;

        if (l < 0) {
            count = 16;
        } else {
            while ((j >>= 4) != 0) {
                count++;
            }
        }

        String buffer = new String(count);
        char[] bufferValue = buffer.getValue();
        do {
            int t = (int) (l & 15);
            if (t > 9) {
                t = t - 10 + 'a';
            } else {
                t += '0';
            }
            bufferValue[--count] = (char) t;
            l >>= 4;
        } while (count > 0);
        return buffer;
    }

    /**
     * Converts the specified long value into its octal string representation.
     * The returned string is a concatenation of characters from '0' to '7'.
     * 
     * @param l
     *            the long value to convert.
     * @return the octal string representation of {@code l}.
     */
    public static String toOctalString(long l) {
        int count = 1;
        long j = l;

        if (l < 0) {
            count = 22;
        } else {
            while ((j >>>= 3) != 0) {
                count++;
            }
        }

        String buffer = new String(count);
        char[] bufferValue = buffer.getValue();
        do {
            bufferValue[--count] = (char) ((l & 7) + '0');
            l >>>= 3;
        } while (count > 0);
        return buffer;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    /**
     * Converts the specified long value into its decimal string representation.
     * The returned string is a concatenation of a minus sign if the number is
     * negative and characters from '0' to '9'.
     * 
     * @param l
     *            the long to convert.
     * @return the decimal string representation of {@code l}.
     */
    public static String toString(long l) {
        return toString(l, 10);
    }

    /**
     * Converts the specified long value into a string representation based on
     * the specified radix. The returned string is a concatenation of a minus
     * sign if the number is negative and characters from '0' to '9' and 'a' to
     * 'z', depending on the radix. If {@code radix} is not in the interval
     * defined by {@code Character.MIN_RADIX} and {@code Character.MAX_RADIX}
     * then 10 is used as the base for the conversion.
     * 
     * @param l
     *            the long to convert.
     * @param radix
     *            the base to use for the conversion.
     * @return the string representation of {@code l}.
     */
    public static String toString(long l, int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            radix = 10;
        }
        if (l == 0) {
            return "0"; //$NON-NLS-1$
        }

        int count = 2;
        long j = l;
        boolean negative = l < 0;
        if (!negative) {
            count = 1;
            j = -l;
        }
        while ((l /= radix) != 0) {
            count++;
        }

        String buffer = new String(count);
        char[] bufferValue = buffer.getValue();
        do {
            int ch = 0 - (int) (j % radix);
            if (ch > 9) {
                ch = ch - 10 + 'a';
            } else {
                ch += '0';
            }
            bufferValue[--count] = (char) ch;
        } while ((j /= radix) != 0);
        if (negative) {
            bufferValue[0] = '-';
        }
        return buffer;
    }

    /**
     * Parses the specified string as a signed decimal long value.
     * 
     * @param string
     *            the string representation of a long value.
     * @return a {@code Long} instance containing the long value represented by
     *         {@code string}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null}, has a length of zero or
     *             can not be parsed as a long value.
     * @see #parseLong(String)
     */
    public static Long valueOf(String string) throws NumberFormatException {
        return valueOf(parseLong(string));
    }

    /**
     * Parses the specified string as a signed long value using the specified
     * radix.
     * 
     * @param string
     *            the string representation of a long value.
     * @param radix
     *            the radix to use when parsing.
     * @return a {@code Long} instance containing the long value represented by
     *         {@code string} using {@code radix}.
     * @throws NumberFormatException
     *             if {@code string} is {@code null} or has a length of zero,
     *             {@code radix < Character.MIN_RADIX},
     *             {@code radix > Character.MAX_RADIX}, or if {@code string}
     *             can not be parsed as a long value.
     * @see #parseLong(String, int)
     */
    public static Long valueOf(String string, int radix)
            throws NumberFormatException {
        return valueOf(parseLong(string, radix));
    }

    /**
     * Determines the highest (leftmost) bit of the specified long value that is
     * 1 and returns the bit mask value for that bit. This is also referred to
     * as the Most Significant 1 Bit. Returns zero if the specified long is
     * zero.
     * 
     * @param lng
     *            the long to examine.
     * @return the bit mask indicating the highest 1 bit in {@code lng}.
     * @since 1.5
     */
    public static long highestOneBit(long lng) {
        lng |= (lng >> 1);
        lng |= (lng >> 2);
        lng |= (lng >> 4);
        lng |= (lng >> 8);
        lng |= (lng >> 16);
        lng |= (lng >> 32);
        return (lng & ~(lng >>> 1));
    }

    /**
     * Determines the lowest (rightmost) bit of the specified long value that is
     * 1 and returns the bit mask value for that bit. This is also referred to
     * as the Least Significant 1 Bit. Returns zero if the specified long is
     * zero.
     * 
     * @param lng
     *            the long to examine.
     * @return the bit mask indicating the lowest 1 bit in {@code lng}.
     * @since 1.5
     */
    public static long lowestOneBit(long lng) {
        return (lng & (-lng));
    }

    /**
     * Determines the number of leading zeros in the specified long value prior
     * to the {@link #highestOneBit(long) highest one bit}.
     *
     * @param lng
     *            the long to examine.
     * @return the number of leading zeros in {@code lng}.
     * @since 1.5
     */
    public static int numberOfLeadingZeros(long lng) {
        lng |= lng >> 1;
        lng |= lng >> 2;
        lng |= lng >> 4;
        lng |= lng >> 8;
        lng |= lng >> 16;
        lng |= lng >> 32;
        return bitCount(~lng);
    }

    /**
     * Determines the number of trailing zeros in the specified long value after
     * the {@link #lowestOneBit(long) lowest one bit}.
     *
     * @param lng
     *            the long to examine.
     * @return the number of trailing zeros in {@code lng}.
     * @since 1.5
     */
    public static int numberOfTrailingZeros(long lng) {
        return bitCount((lng & -lng) - 1);
    }

    /**
     * Counts the number of 1 bits in the specified long value; this is also
     * referred to as population count.
     *
     * @param lng
     *            the long to examine.
     * @return the number of 1 bits in {@code lng}.
     * @since 1.5
     */
    public static int bitCount(long lng) {
        lng = (lng & 0x5555555555555555L) + ((lng >> 1) & 0x5555555555555555L);
        lng = (lng & 0x3333333333333333L) + ((lng >> 2) & 0x3333333333333333L);
        // adjust for 64-bit integer
        int i = (int) ((lng >>> 32) + lng);
        i = (i & 0x0F0F0F0F) + ((i >> 4) & 0x0F0F0F0F);
        i = (i & 0x00FF00FF) + ((i >> 8) & 0x00FF00FF);
        i = (i & 0x0000FFFF) + ((i >> 16) & 0x0000FFFF);
        return i;
    }

    /**
     * Rotates the bits of the specified long value to the left by the specified
     * number of bits.
     *
     * @param lng
     *            the long value to rotate left.
     * @param distance
     *            the number of bits to rotate.
     * @return the rotated value.
     * @since 1.5
     */
    public static long rotateLeft(long lng, int distance) {
        if (distance == 0) {
            return lng;
        }
        /*
         * According to JLS3, 15.19, the right operand of a shift is always
         * implicitly masked with 0x3F, which the negation of 'distance' is
         * taking advantage of.
         */
        return ((lng << distance) | (lng >>> (-distance)));
    }

    /**
     * <p>
     * Rotates the bits of the specified long value to the right by the
     * specified number of bits.
     *
     * @param lng
     *            the long value to rotate right.
     * @param distance
     *            the number of bits to rotate.
     * @return the rotated value.
     * @since 1.5
     */
    public static long rotateRight(long lng, int distance) {
        if (distance == 0) {
            return lng;
        }
        /*
         * According to JLS3, 15.19, the right operand of a shift is always
         * implicitly masked with 0x3F, which the negation of 'distance' is
         * taking advantage of.
         */
        return ((lng >>> distance) | (lng << (-distance)));
    }

    /**
     * Reverses the order of the bytes of the specified long value.
     * 
     * @param lng
     *            the long value for which to reverse the byte order.
     * @return the reversed value.
     * @since 1.5
     */
    public static long reverseBytes(long lng) {
        long b7 = lng >>> 56;
        long b6 = (lng >>> 40) & 0xFF00L;
        long b5 = (lng >>> 24) & 0xFF0000L;
        long b4 = (lng >>> 8) & 0xFF000000L;
        long b3 = (lng & 0xFF000000L) << 8;
        long b2 = (lng & 0xFF0000L) << 24;
        long b1 = (lng & 0xFF00L) << 40;
        long b0 = lng << 56;
        return (b0 | b1 | b2 | b3 | b4 | b5 | b6 | b7);
    }

    /**
     * Reverses the order of the bits of the specified long value.
     * 
     * @param lng
     *            the long value for which to reverse the bit order.
     * @return the reversed value.
     * @since 1.5
     */
    public static long reverse(long lng) {
        // From Hacker's Delight, 7-1, Figure 7-1
        lng = (lng & 0x5555555555555555L) << 1 | (lng >> 1)
                & 0x5555555555555555L;
        lng = (lng & 0x3333333333333333L) << 2 | (lng >> 2)
                & 0x3333333333333333L;
        lng = (lng & 0x0F0F0F0F0F0F0F0FL) << 4 | (lng >> 4)
                & 0x0F0F0F0F0F0F0F0FL;
        return reverseBytes(lng);
    }

    /**
     * Returns the value of the {@code signum} function for the specified long
     * value.
     * 
     * @param lng
     *            the long value to check.
     * @return -1 if {@code lng} is negative, 1 if {@code lng} is positive, 0 if
     *         {@code lng} is zero.
     * @since 1.5
     */
    public static int signum(long lng) {
        return (lng == 0 ? 0 : (lng < 0 ? -1 : 1));
    }

    /**
     * Returns a {@code Long} instance for the specified long value.
     * <p>
     * If it is not necessary to get a new {@code Long} instance, it is
     * recommended to use this method instead of the constructor, since it
     * maintains a cache of instances which may result in better performance.
     *
	 * JUniversal CHANGE: Removed the valueOfCache, which cached -128 to 127, in
	 * order to make things a little simpler & since static initializers aren't
	 * currently supported.
	 * 
     * @param lng
     *            the long value to store in the instance.
     * @return a {@code Long} instance containing {@code lng}.
     * @since 1.5
     */
    public static Long valueOf(long lng) {
        return new Long(lng);
    }
}

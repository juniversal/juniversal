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
 * An immutable sequence of characters/code units ({@code char}s). A {@code String} is represented
 * by array of UTF-16 values, such that Unicode supplementary characters (code points) are
 * stored/encoded as surrogate pairs via Unicode code units ({@code char}).
 * 
 * JUniversal REMOVED:
 * 
 * Made not derive from Serializable nor have serialVersionUID
 * 
 * ConsolePrintStream
 * 
 * codePoint related methods
 * 
 * Formatter methods
 * 
 * Regular expression methods
 * 
 * Methods that convert to/from byte arrays
 * 
 * Methods that take a StringBuffer (only StringBuilder is supported)
 * 
 * Deprecated constructors converting from a byte array with no charset specified
 * 
 * valueOf methods for Double and Float
 * 
 * CASE_INSENSITIVE_ORDER
 * 
 * intern method
 * 
 * versions of toUpperCase and toLowerCase that took a Locale
 * 
 * optimized private indexOf method taking extra parameters & supposedly for use by special JITs
 * that support it when the string being searched for is a constant string
 * 
 * @see StringBuilder
 * @since 1.0
 */
public final class String implements Comparable<String>, CharSequence {
    private final char[] value;
    private final int count;
    private int hashCode;

    /**
     * Creates an empty string.
     */
    public String() {
        value = new char[0];
        count = 0;
    }

    /*
     * Private constructor used for JIT optimization.
     */
    @SuppressWarnings("unused")
    private String(String s, char c) {
        value = new char[s.count + 1];
        count = s.count + 1;
        JUniversalHelper.arraycopy(s.value, value, s.count);
        value[s.count] = c;
    }

    /**
     * Initializes this string to contain the characters in the specified
     * character array. Modifying the character array after creating the string
     * has no effect on the string.
     * 
     * @param data
     *            the array of characters.
     * @throws NullPointerException
     *             when {@code data} is {@code null}.
     */
    public String(char[] data) {
    	count = data.length;
    	value = new char[count];
        JUniversalHelper.arraycopy(data, value, count);
    }

    /**
     * Initializes this string to contain the specified characters in the
     * character array. Modifying the character array after creating the string
     * has no effect on the string.
     * 
     * @param data
     *            the array of characters.
     * @param start
     *            the starting offset in the character array.
     * @param length
     *            the number of characters to use.
     * @throws NullPointerException
     *             when {@code data} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code length < 0, start < 0} or {@code start + length >
     *             data.length}
     */
    public String(char[] data, int start, int length) {
        // range check everything so a new char[] is not created
        // start + length could overflow, start/length maybe MaxInt
        if (start >= 0 && 0 <= length && length <= data.length - start) {
            value = new char[length];
            count = length;
            JUniversalHelper.arraycopy(data, start, value, 0, count);
        } else {
            throw new StringIndexOutOfBoundsException();
        }
    }

    /*
     * JUniversal ADDITION
     * 
	 * Internal version of string constructor; does not range check or null check. Constructs a new
	 * String that's a substring of the specified String, starting at position start with length
	 * characters.
	 */
    String(int start, int length, String sourceString) {
        value = new char[length];
        count = length;
        JUniversalHelper.arraycopy(sourceString.value, start, value, 0, count);
    }

    /*
	 * JUniversal ADDITION
	 * 
	 * Internal version of string constructor. Constructs a new String with a value buffer of the
	 * specified length. The value may modified by the caller to initialize it, but the String
	 * shouldn't be changed after it's completely constructed and returned to client code.
	 */
    String(int length) {
        value = new char[length];
        count = length;
    }

    /**
     * Creates a {@code String} that is a copy of the specified string.
     * 
     * @param string
     *            the string to copy.
     */
    public String(String string) {
        value = string.value;
        count = string.count;
    }

    /*
     * Private constructor useful for JIT optimization.
     */
    @SuppressWarnings( { "unused", "nls" })
    private String(String s1, String s2) {
        if (s1 == null) {
            s1 = "null";
        }
        if (s2 == null) {
            s2 = "null";
        }
        count = s1.count + s2.count;
        value = new char[count];
        JUniversalHelper.arraycopy(s1.value, value, s1.count);
        JUniversalHelper.arraycopy(s2.value, 0, value, s1.count, s2.count);
    }

    /*
     * Private constructor useful for JIT optimization.
     */
    @SuppressWarnings( { "unused", "nls" })
    private String(String s1, String s2, String s3) {
        if (s1 == null) {
            s1 = "null";
        }
        if (s2 == null) {
            s2 = "null";
        }
        if (s3 == null) {
            s3 = "null";
        }
        count = s1.count + s2.count + s3.count;
        value = new char[count];
        JUniversalHelper.arraycopy(s1.value, value, s1.count);
        JUniversalHelper.arraycopy(s2.value, 0, value, s1.count, s2.count);
        JUniversalHelper.arraycopy(s3.value, 0, value, s1.count + s2.count,
                s3.count);
    }

    /**
     * Creates a {@code String} from the contents of the specified {@code
     * StringBuilder}.
     * 
     * @param sb
     *            the {@code StringBuilder} to copy the contents from.
     * @throws NullPointerException
     *             if {@code sb} is {@code null}.
     * @since 1.5
     */
    public String(StringBuilder sb) {
        if (sb == null) {
            throw new NullPointerException();
        }
        this.count = sb.length();
        this.value = new char[this.count];
        sb.getChars(0, this.count, this.value, 0);
    }

    /*
     * Creates a {@code String} that is s1 + v1. May be used by JIT code.
     */
    @SuppressWarnings("unused")
    private String(String s1, int v1) {
        if (s1 == null) {
            s1 = "null"; //$NON-NLS-1$
        }
        String s2 = String.valueOf(v1);
        int len = s1.count + s2.count;
        value = new char[len];
        JUniversalHelper.arraycopy(s1.value, value, s1.count);
        JUniversalHelper.arraycopy(s2.value, 0, value, s1.count, s2.count);
        count = len;
    }

    /**
     * Returns the character at the specified offset in this string.
     * 
     * @param index
     *            the zero-based index in this string.
     * @return the character at the index.
     * @throws IndexOutOfBoundsException
     *             if {@code index < 0} or {@code index >= length()}.
     */
    public char charAt(int index) {
        if (0 <= index && index < count) {
            return value[index];
        }
        throw new StringIndexOutOfBoundsException();
    }

    // Optimized for ASCII
    private char compareValue(char ch) {
        if (ch < 128) {
            if ('A' <= ch && ch <= 'Z') {
                return (char) (ch + ('a' - 'A'));
            }
            return ch;
        }
        return Character.toLowerCase(Character.toUpperCase(ch));
    }

    /**
     * Compares the specified string to this string using the Unicode values of
     * the characters. Returns 0 if the strings contain the same characters in
     * the same order. Returns a negative integer if the first non-equal
     * character in this string has a Unicode value which is less than the
     * Unicode value of the character at the same position in the specified
     * string, or if this string is a prefix of the specified string. Returns a
     * positive integer if the first non-equal character in this string has a
     * Unicode value which is greater than the Unicode value of the character at
     * the same position in the specified string, or if the specified string is
     * a prefix of this string.
     * 
     * @param string
     *            the string to compare.
     * @return 0 if the strings are equal, a negative integer if this string is
     *         before the specified string, or a positive integer if this string
     *         is after the specified string.
     * @throws NullPointerException
     *             if {@code string} is {@code null}.
     */
    public int compareTo(String string) {
        // Code adapted from K&R, pg 101
        int o1 = 0, o2 = 0, result;
        int end = count < string.count ? count : string.count;
        char[] target = string.value;
        while (o1 < end) {
            if ((result = value[o1++] - target[o2++]) != 0) {
                return result;
            }
        }
        return count - string.count;
    }

    /**
     * Compares the specified string to this string using the Unicode values of
     * the characters, ignoring case differences. Returns 0 if the strings
     * contain the same characters in the same order. Returns a negative integer
     * if the first non-equal character in this string has a Unicode value which
     * is less than the Unicode value of the character at the same position in
     * the specified string, or if this string is a prefix of the specified
     * string. Returns a positive integer if the first non-equal character in
     * this string has a Unicode value which is greater than the Unicode value
     * of the character at the same position in the specified string, or if the
     * specified string is a prefix of this string.
     * 
     * @param string
     *            the string to compare.
     * @return 0 if the strings are equal, a negative integer if this string is
     *         before the specified string, or a positive integer if this string
     *         is after the specified string.
     * @throws NullPointerException
     *             if {@code string} is {@code null}.
     */
    public int compareToIgnoreCase(String string) {
        int o1 = 0, o2 = 0, result;
        int end = count < string.count ? count : string.count;
        char c1, c2;
        char[] target = string.value;
        while (o1 < end) {
            if ((c1 = value[o1++]) == (c2 = target[o2++])) {
                continue;
            }
            c1 = compareValue(c1);
            c2 = compareValue(c2);
            if ((result = c1 - c2) != 0) {
                return result;
            }
        }
        return count - string.count;
    }

    /**
     * Concatenates this string and the specified string.
     * 
     * @param string
     *            the string to concatenate
     * @return a new string which is the concatenation of this string and the
     *         specified string.
     */
    public String concat(String string) {
        if (string.count > 0 && count > 0) 
        {
            String buffer = new String(count + string.count);
            JUniversalHelper.arraycopy(value, buffer.getValue(), count);
			JUniversalHelper.arraycopy(string.value, 0, buffer.getValue(), count, string.count);
            return buffer;
        }
        return count == 0 ? string : this;
    }

    /**
     * Creates a new string containing the characters in the specified character
     * array. Modifying the character array after creating the string has no
     * effect on the string.
     * 
     * @param data
     *            the array of characters.
     * @return the new string.
     * @throws NullPointerException
     *             if {@code data} is {@code null}.
     */
    public static String copyValueOf(char[] data) {
        return new String(data, 0, data.length);
    }

    /**
     * Creates a new string containing the specified characters in the character
     * array. Modifying the character array after creating the string has no
     * effect on the string.
     * 
     * @param data
     *            the array of characters.
     * @param start
     *            the starting offset in the character array.
     * @param length
     *            the number of characters to use.
     * @return the new string.
     * @throws NullPointerException
     *             if {@code data} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code length < 0, start < 0} or {@code start + length >
     *             data.length}.
     */
    public static String copyValueOf(char[] data, int start, int length) {
        return new String(data, start, length);
    }

    /**
     * Compares the specified string to this string to determine if the
     * specified string is a suffix.
     * 
     * @param suffix
     *            the suffix to look for.
     * @return {@code true} if the specified string is a suffix of this string,
     *         {@code false} otherwise.
     * @throws NullPointerException
     *             if {@code suffix} is {@code null}.
     */
    public boolean endsWith(String suffix) {
        return regionMatches(count - suffix.count, suffix, 0, suffix.count);
    }

    /**
     * Compares the specified object to this string and returns true if they are
     * equal. The object must be an instance of string with the same characters
     * in the same order.
     * 
     * @param object
     *            the object to compare.
     * @return {@code true} if the specified object is equal to this string,
     *         {@code false} otherwise.
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof String) {
            String s = (String) object;
            int hash = hashCode;  // Single read on hashCodes as they may change
            int shash = s.hashCode;
            if (count != s.count || (hash != shash && hash != 0 && shash != 0)) {
                return false;
            }
            for (int i = 0; i < count; ++i) {
                if (value[i] != s.value[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Compares the specified string to this string ignoring the case of the
     * characters and returns true if they are equal.
     * 
     * @param string
     *            the string to compare.
     * @return {@code true} if the specified string is equal to this string,
     *         {@code false} otherwise.
     */
    public boolean equalsIgnoreCase(String string) {
        if (string == this) {
            return true;
        }
        if (string == null || count != string.count) {
            return false;
        }

        int o1 = 0, o2 = 0;
        int end = count;
        char c1, c2;
        char[] target = string.value;
        while (o1 < end) {
            if ((c1 = value[o1++]) != (c2 = target[o2++])
                    && Character.toUpperCase(c1) != Character.toUpperCase(c2)
                    // Required for unicode that we test both cases
                    && Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Copies the specified characters in this string to the character array
     * starting at the specified offset in the character array.
     * 
     * @param start
     *            the starting offset of characters to copy.
     * @param end
     *            the ending offset of characters to copy.
     * @param buffer
     *            the destination character array.
     * @param index
     *            the starting offset in the character array.
     * @throws NullPointerException
     *             if {@code buffer} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code start < 0}, {@code end > length()}, {@code start >
     *             end}, {@code index < 0}, {@code end - start > buffer.length -
     *             index}
     */
    public void getChars(int start, int end, char[] buffer, int index) {
        // NOTE last character not copied!
        // Fast range check.
        if (0 <= start && start <= end && end <= count) {
            JUniversalHelper.arraycopy(value, start, buffer, index, end - start);
        } else {
            throw new StringIndexOutOfBoundsException();
        }
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            if (count == 0) {
                return 0;
            }
            int hash = 0;
            for (int i = 0; i < count; i++) {
                hash = value[i] + ((hash << 5) - hash);
            }
            hashCode = hash;
        }
        return hashCode;
    }

    /**
     * Searches in this string for the first index of the specified character.
     * The search for the character starts at the beginning and moves towards
     * the end of this string.
     * 
     * @param c
     *            the character to find.
     * @return the index in this string of the specified character, -1 if the
     *         character isn't found.
     */
    public int indexOf(int c) {
        return indexOf(c, 0);
    }

    /**
     * Searches in this string for the index of the specified character. The
     * search for the character starts at the specified offset and moves towards
     * the end of this string.
     * 
     * @param c
     *            the character to find.
     * @param start
     *            the starting offset.
     * @return the index in this string of the specified character, -1 if the
     *         character isn't found.
     */
    public int indexOf(int c, int start) {
        if (start < count) {
            if (start < 0) {
                start = 0;
            }
            if (c >= 0 && c <= Character.MAX_VALUE) {
                for (int i = start; i < count; i++) {
                    if (value[i] == c) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Searches in this string for the first index of the specified string. The
     * search for the string starts at the beginning and moves towards the end
     * of this string.
     * 
     * @param string
     *            the string to find.
     * @return the index of the first character of the specified string in this
     *         string, -1 if the specified string is not a substring.
     * @throws NullPointerException
     *             if {@code string} is {@code null}.
     */
    public int indexOf(String string) {
        return indexOf(string, 0);
    }

    /**
     * Searches in this string for the index of the specified string. The search
     * for the string starts at the specified offset and moves towards the end
     * of this string.
     * 
     * @param subString
     *            the string to find.
     * @param start
     *            the starting offset.
     * @return the index of the first character of the specified string in this
     *         string, -1 if the specified string is not a substring.
     * @throws NullPointerException
     *             if {@code subString} is {@code null}.
     */
    public int indexOf(String subString, int start) {
        if (start < 0) {
            start = 0;
        }
        int subCount = subString.count;
        if (subCount > 0) {
            if (subCount + start > count) {
                return -1;
            }
            char[] target = subString.value;
            char firstChar = target[0];
            int end = subCount;
            while (true) {
                int i = indexOf(firstChar, start);
                if (i == -1 || subCount + i > count) {
                    return -1; // handles subCount > count || start >= count
                }
                int o1 = i, o2 = 0;
                while (++o2 < end && value[++o1] == target[o2]) {
                    // Intentionally empty
                }
                if (o2 == end) {
                    return i;
                }
                start = i + 1;
            }
        }
        return start < count ? start : count;
    }

    /**
     * Searches in this string for the last index of the specified character.
     * The search for the character starts at the end and moves towards the
     * beginning of this string.
     * 
     * @param c
     *            the character to find.
     * @return the index in this string of the specified character, -1 if the
     *         character isn't found.
     */
    public int lastIndexOf(int c) {
        return lastIndexOf(c, count - 1);
    }

    /**
     * Searches in this string for the index of the specified character. The
     * search for the character starts at the specified offset and moves towards
     * the beginning of this string.
     * 
     * @param c
     *            the character to find.
     * @param start
     *            the starting offset.
     * @return the index in this string of the specified character, -1 if the
     *         character isn't found.
     */
    public int lastIndexOf(int c, int start) {
        if (start >= 0) {
            if (start >= count) {
                start = count - 1;
            }
            if (c >= 0 && c <= Character.MAX_VALUE) {
                for (int i = start; i >= 0; --i) {
                    if (value[i] == c) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Searches in this string for the last index of the specified string. The
     * search for the string starts at the end and moves towards the beginning
     * of this string.
     * 
     * @param string
     *            the string to find.
     * @return the index of the first character of the specified string in this
     *         string, -1 if the specified string is not a substring.
     * @throws NullPointerException
     *             if {@code string} is {@code null}.
     */
    public int lastIndexOf(String string) {
        // Use count instead of count - 1 so lastIndexOf("") answers count
        return lastIndexOf(string, count);
    }

    /**
     * Searches in this string for the index of the specified string. The search
     * for the string starts at the specified offset and moves towards the
     * beginning of this string.
     * 
     * @param subString
     *            the string to find.
     * @param start
     *            the starting offset.
     * @return the index of the first character of the specified string in this
     *         string , -1 if the specified string is not a substring.
     * @throws NullPointerException
     *             if {@code subString} is {@code null}.
     */
    public int lastIndexOf(String subString, int start) {
        int subCount = subString.count;
        if (subCount <= count && start >= 0) {
            if (subCount > 0) {
                if (start > count - subCount) {
                    start = count - subCount;
                }
                // count and subCount are both >= 1
                char[] target = subString.value;
                char firstChar = target[0];
                int end = subCount;
                while (true) {
                    int i = lastIndexOf(firstChar, start);
                    if (i == -1) {
                        return -1;
                    }
                    int o1 = i, o2 = 0;
                    while (++o2 < end && value[++o1] == target[o2]) {
                        // Intentionally empty
                    }
                    if (o2 == end) {
                        return i;
                    }
                    start = i - 1;
                }
            }
            return start < count ? start : count;
        }
        return -1;
    }

    /**
     * Returns the size of this string.
     * 
     * @return the number of characters in this string.
     */
    public int length() {
        return count;
    }
    
    /**
     * Answers if the size of this String is zero.
     * 
     * @return true if the size of this String is zero, false otherwise
     * @since 1.6
     */
    public boolean isEmpty() {
        return 0 == count;
    }

    /**
     * Compares the specified string to this string and compares the specified
     * range of characters to determine if they are the same.
     * 
     * @param thisStart
     *            the starting offset in this string.
     * @param string
     *            the string to compare.
     * @param start
     *            the starting offset in the specified string.
     * @param length
     *            the number of characters to compare.
     * @return {@code true} if the ranges of characters are equal, {@code false}
     *         otherwise
     * @throws NullPointerException
     *             if {@code string} is {@code null}.
     */
    public boolean regionMatches(int thisStart, String string, int start,
            int length) {
        if (string == null) {
            throw new NullPointerException();
        }
        if (start < 0 || string.count - start < length) {
            return false;
        }
        if (thisStart < 0 || count - thisStart < length) {
            return false;
        }
        if (length <= 0) {
            return true;
        }
        int o1 = thisStart, o2 = start;
        for (int i = 0; i < length; ++i) {
            if (value[o1 + i] != string.value[o2 + i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the specified string to this string and compares the specified
     * range of characters to determine if they are the same. When ignoreCase is
     * true, the case of the characters is ignored during the comparison.
     * 
     * @param ignoreCase
     *            specifies if case should be ignored.
     * @param thisStart
     *            the starting offset in this string.
     * @param string
     *            the string to compare.
     * @param start
     *            the starting offset in the specified string.
     * @param length
     *            the number of characters to compare.
     * @return {@code true} if the ranges of characters are equal, {@code false}
     *         otherwise.
     * @throws NullPointerException
     *             if {@code string} is {@code null}.
     */
    public boolean regionMatches(boolean ignoreCase, int thisStart,
            String string, int start, int length) {
        if (!ignoreCase) {
            return regionMatches(thisStart, string, start, length);
        }

        if (string != null) {
            if (thisStart < 0 || length > count - thisStart) {
                return false;
            }
            if (start < 0 || length > string.count - start) {
                return false;
            }

            int end = thisStart + length;
            char c1, c2;
            char[] target = string.value;
            while (thisStart < end) {
                if ((c1 = value[thisStart++]) != (c2 = target[start++])
                        && Character.toUpperCase(c1) != Character.toUpperCase(c2)
                        // Required for unicode that we test both cases
                        && Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
                    return false;
                }
            }
            return true;
        }
        throw new NullPointerException();
    }

    /**
     * Copies this string replacing occurrences of the specified character with
     * another character.
     * 
     * @param oldChar
     *            the character to replace.
     * @param newChar
     *            the replacement character.
     * @return a new string with occurrences of oldChar replaced by newChar.
     */
    public String replace(char oldChar, char newChar) {
        int index = indexOf(oldChar, 0);
        if (index == -1) {
            return this;
        }

        String buffer = new String(count);
        char[] bufferValue = buffer.getValue();
        JUniversalHelper.arraycopy(value, bufferValue, count);
        do {
        	bufferValue[index++] = newChar;
        } while ((index = indexOf(oldChar, index)) != -1);
        return buffer;
    }
    
    /**
     * Copies this string replacing occurrences of the specified target sequence
     * with another sequence. The string is processed from the beginning to the
     * end.
     * 
     * @param target
     *            the sequence to replace.
     * @param replacement
     *            the replacement sequence.
     * @return the resulting string.
     * @throws NullPointerException
     *             if {@code target} or {@code replacement} is {@code null}.
     */
    public String replace(CharSequence target, CharSequence replacement) {
        if (target == null) {
            throw new NullPointerException("target should not be null");
        }
        if (replacement == null) {
            throw new NullPointerException("replacement should not be null");
        }
        String ts = target.toString();
        int index = indexOf(ts, 0);

        if (index == -1)
            return this;

        String rs = replacement.toString();

        // special case if the string to match is empty then
        // match at the start, inbetween each character and at the end
        if ("".equals(ts)) {
            StringBuilder buffer = new StringBuilder(count + (rs.length() * (count + 1)));
            buffer.append(rs);
            for(int i=0; i<count; i++) {
                buffer.append(value[i]);
                buffer.append(rs);
            }
            return buffer.toString();
        }

        StringBuilder buffer = new StringBuilder(count + rs.length());
        int tl = target.length();
        int tail = 0;
        do {
            buffer.append(value, tail, index - tail);
            buffer.append(rs);
            tail = index + tl;
        } while ((index = indexOf(ts, tail)) != -1);
        //append trailing chars 
        buffer.append(value, tail, count - tail);

        return buffer.toString();
    }

    /**
     * Compares the specified string to this string to determine if the
     * specified string is a prefix.
     * 
     * @param prefix
     *            the string to look for.
     * @return {@code true} if the specified string is a prefix of this string,
     *         {@code false} otherwise
     * @throws NullPointerException
     *             if {@code prefix} is {@code null}.
     */
    public boolean startsWith(String prefix) {
        return startsWith(prefix, 0);
    }

    /**
     * Compares the specified string to this string, starting at the specified
     * offset, to determine if the specified string is a prefix.
     * 
     * @param prefix
     *            the string to look for.
     * @param start
     *            the starting offset.
     * @return {@code true} if the specified string occurs in this string at the
     *         specified offset, {@code false} otherwise.
     * @throws NullPointerException
     *             if {@code prefix} is {@code null}.
     */
    public boolean startsWith(String prefix, int start) {
        return regionMatches(start, prefix, 0, prefix.count);
    }

    /**
	 * Copies a range of characters into a new string.
	 * 
	 * JUniversal CHANGE: Changed to make a full copy of the substring, not point into the existing
	 * char array
	 * 
	 * @param start
	 *            the offset of the first character.
	 * @return a new string containing the characters from start to the end of the string.
	 * @throws IndexOutOfBoundsException
	 *             if {@code start < 0} or {@code start > length()}.
	 */
    public String substring(int start) {
        if (start == 0) {
            return this;
        }
        if (0 <= start && start <= count) {
            return new String(start, count - start, this);
        }
        throw new StringIndexOutOfBoundsException(start);
    }

    /**
     * Copies a range of characters into a new string.
     * 
	 * JUniversal CHANGE: Changed to make a full copy of the substring, not point into the existing
	 * char array
	 * 
     * @param start
     *            the offset of the first character.
     * @param end
     *            the offset one past the last character.
     * @return a new string containing the characters from start to end - 1
     * @throws IndexOutOfBoundsException
     *             if {@code start < 0}, {@code start > end} or {@code end >
     *             length()}.
     */
    public String substring(int start, int end) {
        if (start == 0 && end == count) {
            return this;
        }
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        } else if (start > end) {
            throw new StringIndexOutOfBoundsException(end - start);
        } else if (end > count) {
            throw new StringIndexOutOfBoundsException(end);
        }
        // NOTE last character not copied!
        return new String(start, end - start, this);
    }

    /**
     * Copies the characters in this string to a character array.
     * 
     * @return a character array containing the characters of this string.
     */
    public char[] toCharArray() {
        char[] buffer = new char[count];
        JUniversalHelper.arraycopy(value, buffer, count);
        return buffer;
    }

    /**
	 * Converts the characters in this string to lowercase, using the default Locale.
	 * 
	 * JUniversal CHANGE: No Locale is taken into account and just ASCII letters are potentially
	 * changed.
	 * 
	 * @return a new string containing the lowercase characters equivalent to the characters in this
	 *         string.
	 */
    public String toLowerCase() {
        for (int o = 0, end = count; o < end; o++) {
            char ch = value[o];
            if (ch != Character.toLowerCase(ch)) {
                String buffer = new String(count);
                char[] bufferValue = buffer.getValue();
                int i = o;
                // Not worth checking for i == 0 case
                JUniversalHelper.arraycopy(value, bufferValue, i);
                while (i < count) {
                    bufferValue[i++] = Character.toLowerCase(value[o++]);
                }
                return buffer;
            }
        }
        return this;
    }

    /**
     * Returns this string.
     *
     * @return this string.
     */
    @Override
    public String toString() {
        return this;
    }

    /**
	 * Converts the characters in this string to uppercase, using the default Locale.
	 * 
	 * JUniversal CHANGE: No Locale is taken into account and just ASCII letters are potentially
	 * changed. Also rewrote this method to be the same as toLowerCase. I'm not sure why toUpperCase
	 * was implemented differently than toLowerCase (in a more complex way, using a lookup table) in
	 * the Harmony code.
	 * 
	 * @return a new string containing the uppercase characters equivalent to the characters in this
	 *         string.
	 */
    public String toUpperCase() {
        for (int o = 0, end = count; o < end; o++) {
            char ch = value[o];
            if (ch != Character.toUpperCase(ch)) {
                String buffer = new String(count);
                char[] bufferValue = buffer.getValue();
                int i = o;
                // Not worth checking for i == 0 case
                JUniversalHelper.arraycopy(value, bufferValue, i);
                while (i < count) {
                    bufferValue[i++] = Character.toUpperCase(value[o++]);
                }
                return buffer;
            }
        }
        return this;
    }

    /**
     * Copies this string removing white space characters from the beginning and
     * end of the string.
     * 
     * @return a new string with characters <code><= \\u0020</code> removed from
     *         the beginning and the end.
     */
    public String trim() {
        int start = 0, last = count - 1;
        int end = last;
        while ((start <= end) && (value[start] <= ' ')) {
            start++;
        }
        while ((end >= start) && (value[end] <= ' ')) {
            end--;
        }
        if (start == 0 && end == last) {
            return this;
        }
        return new String(start, end - start + 1, this);
    }

    /**
     * Creates a new string containing the characters in the specified character
     * array. Modifying the character array after creating the string has no
     * effect on the string.
     * 
     * @param data
     *            the array of characters.
     * @return the new string.
     * @throws NullPointerException
     *             if {@code data} is {@code null}.
     */
    public static String valueOf(char[] data) {
        return new String(data, 0, data.length);
    }

    /**
     * Creates a new string containing the specified characters in the character
     * array. Modifying the character array after creating the string has no
     * effect on the string.
     * 
     * @param data
     *            the array of characters.
     * @param start
     *            the starting offset in the character array.
     * @param length
     *            the number of characters to use.
     * @return the new string.
     * @throws IndexOutOfBoundsException
     *             if {@code length < 0}, {@code start < 0} or {@code start +
     *             length > data.length}
     * @throws NullPointerException
     *             if {@code data} is {@code null}.
     */
    public static String valueOf(char[] data, int start, int length) {
        return new String(data, start, length);
    }

    /**
     * Converts the specified character to its string representation.
     * 
     * JUniversal CHANGE: Changed to not use cache characters < 128
     * 
     * @param value
     *            the character.
     * @return the character converted to a string.
     */
    public static String valueOf(char value) {
        String s = new String(1);
        s.getValue()[0] = value;
        s.hashCode = value;
        return s;
    }

    /**
     * Converts the specified integer to its string representation.
     * 
     * @param value
     *            the integer.
     * @return the integer converted to a string.
     */
    public static String valueOf(int value) {
        return Integer.toString(value);
    }

    /**
     * Converts the specified long to its string representation.
     * 
     * @param value
     *            the long.
     * @return the long converted to a string.
     */
    public static String valueOf(long value) {
        return Long.toString(value);
    }

    /**
     * Converts the specified object to its string representation. If the object
     * is null return the string {@code "null"}, otherwise use {@code
     * toString()} to get the string representation.
     * 
     * @param value
     *            the object.
     * @return the object converted to a string, or the string {@code "null"}.
     */
    public static String valueOf(Object value) {
        return value != null ? value.toString() : "null"; //$NON-NLS-1$
    }

    /**
     * Converts the specified boolean to its string representation. When the
     * boolean is {@code true} return {@code "true"}, otherwise return {@code
     * "false"}.
     * 
     * @param value
     *            the boolean.
     * @return the boolean converted to a string.
     */
    public static String valueOf(boolean value) {
        return value ? "true" : "false"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Compares a {@code CharSequence} to this {@code String} to determine if
     * their contents are equal.
     *
     * @param cs
     *            the character sequence to compare to.
     * @return {@code true} if equal, otherwise {@code false}
     * @since 1.5
     */
    public boolean contentEquals(CharSequence cs) {
        if (cs == null) {
            throw new NullPointerException();
        }

        int len = cs.length();

        if (len != count) {
            return false;
        }

        if (len == 0 && count == 0) {
            return true; // since both are empty strings
        }

        return regionMatches(0, cs.toString(), 0, len);
    }

    /**
     * Has the same result as the substring function, but is present so that
     * string may implement the CharSequence interface.
     * 
     * @param start
     *            the offset the first character.
     * @param end
     *            the offset of one past the last character to include.
     * @return the subsequence requested.
     * @throws IndexOutOfBoundsException
     *             if {@code start < 0}, {@code end < 0}, {@code start > end} or
     *             {@code end > length()}.
     * @see java.lang.CharSequence#subSequence(int, int)
     * @since 1.4
     */
    public CharSequence subSequence(int start, int end) {
        return substring(start, end);
    }

    /**
     * Determines if this {@code String} contains the sequence of characters in
     * the {@code CharSequence} passed.
     *
     * @param cs
     *            the character sequence to search for.
     * @return {@code true} if the sequence of characters are contained in this
     *         string, otherwise {@code false}.
     * @since 1.5
     */
    public boolean contains(CharSequence cs) {
        if (cs == null) {
            throw new NullPointerException();
        }
        return indexOf(cs.toString()) >= 0;
    }

    /*
     * Returns the character array for this string.
     */
    char[] getValue() {
        return value;
    }
}

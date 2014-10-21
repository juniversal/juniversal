/*
 * Copyright (c) 2011-2014, Microsoft Mobile
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.juniversal.translator.all;

import org.junit.Test;
import org.juniversal.translator.TranslateNodeTest;

public class TranslateLiteralTest extends TranslateNodeTest {
    @Test
    public void testTranslateIntegerLiterals() {
        testTranslateIntExpression("3", null, null);

        // TODO: Fix this up when Swift fixes its bugs here
        //testTranslateLongExpression("3L", "UInt64(3)");

        testTranslateLongExpression("3", null, null);
        testTranslateLongExpression("3_123", "3123", null);
        testTranslateLongExpression("0xA1b2", null, null);
        testTranslateLongExpression("0XA1b2", null, null);
        testTranslateLongExpression("0b1", "NOT-SUPPORTED: Binary literals aren't currently supported; change the source to use hex instead", null);
        testTranslateLongExpression("0B1", "NOT-SUPPORTED: Binary literals aren't currently supported; change the source to use hex instead", null);
        testTranslateLongExpression("0", null, null);

        // Octal
        testTranslateLongExpression("0010101", "NOT-SUPPORTED: Octal literals aren't currently supported; change the source to use hex instead", "0o010101");

        //testTranslateLongExpression("3L", "Int64(3)");
        //testTranslateLongExpression("3l", "Int64(3)");
       }

    @Test
    public void testTranslateCharLiterals() {
        // TODO: Handle escape sequences

        testTranslateCharExpression("'c'", "'c'", "Character(\"c\")");
    }

    @Test
    public void testTranslateOtherLiterals() {
        testTranslateStringExpression("null", null, "nil");

        testTranslateBooleanExpression("true", null, null);
        testTranslateBooleanExpression("false", null, null);
    }
}

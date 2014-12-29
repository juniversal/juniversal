/*
 * Copyright (c) 2012-2014, Microsoft Mobile
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

public class TranslatePrimitiveTypeTest extends TranslateNodeTest {
    @Test public void testTranslatePrimitiveTypes() {
        testTranslateStatement("byte foo;", "sbyte foo;", "FILL IN");
        testTranslateStatement("short foo;", null, "FILL IN");
        testTranslateStatement("char foo;", null, "FILL IN");
        testTranslateStatement("int foo;", null, "FILL IN");
        testTranslateStatement("long foo;", null, "FILL IN");
        testTranslateStatement("float foo;", null, "FILL IN");
        testTranslateStatement("double foo;", null, "FILL IN");
        testTranslateStatement("boolean foo;", "bool foo;", "FILL IN");
    }
}

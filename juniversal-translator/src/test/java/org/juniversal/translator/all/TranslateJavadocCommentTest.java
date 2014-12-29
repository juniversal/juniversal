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

/**
 * @author Chris Ingram
 * @since 12/11/2014
 */
public class TranslateJavadocCommentTest extends TranslateNodeTest {
    @Test public void testSimpleMethodJD() {
        testTranslateJavadocComment(
                "/**\n" +
                " * A method that does blah\n" +
                " *\n" +
                " * @param p1 The first parameter\n" +
                " * @param p2 The second parameter\n" +
                " * @returns int Description of return value\n" +
                " * @since 1.0\n" +
                " * @author cingram\n" +
                " */", null);
    }

    /**
     * Test method
     */
    @Test public void testMultilineTextNoPeriodsJD() {
        testTranslateJavadocComment(
                "/**\n" +
                " * A method that does blah\n" +
                " * More about the method in question\n" +
                " *\n" +
                " * @param p1 The first parameter\n" +
                " *        More detail about the first parameter\n" +
                " * @param p2 The second parameter\n" +
                " * @returns int Description of return value\n" +
                " *          More about the return value\n" +
                " * @since 1.0\n" +
                " * @author cingram\n" +
                " */", null);
    }

    @Test public void testMultilineTextWithPeriodsJD() {
        testTranslateJavadocComment(
                "/**\n" +
                " * A method that does blah.\n" +
                " * More about the method in question.\n" +
                " *\n" +
                " * @param p1 The first parameter.\n" +
                " *        More detail about the first parameter.\n" +
                " * @param p2 The second parameter\n" +
                " * @returns int Description of return value.\n" +
                " *          More about the return value.\n" +
                " * @since 1.0\n" +
                " * @author cingram\n" +
                " */", null);
    }

    @Test public void testMultipleLongParagraphsNoPTagsJD() {
        testTranslateJavadocComment(
                "/**\n" +
                " * A method that does blah.\n" +
                " * More about the method in question using very verbose and descriptive language such that the line\n" +
                " * must be wrapped to be reasonable like this text right here.\n" +
                " *\n" +
                " * This text represents a second paragraph but without any silly \"p\" tags cluttering things up.  Does this line which is fairly long need to be wrapped as well?\n" +
                " *\n" +
                " * @param p1 The first parameter.\n" +
                " *        More detail about the first parameter.\n" +
                " * @param p2 The second parameter\n" +
                " * @returns int Description of return value.\n" +
                " *          More about the return value.\n" +
                " * @since 1.0\n" +
                " * @author cingram\n" +
                " */", null);
    }

    @Test public void testEmbeddedCodeTagJD() {
        testTranslateJavadocComment(
                "/**\n" +
                " * Constructs a new {@code HashMap} instance with the specified capacity and load factor.  A method that does blah.\n" +
                " * More about the method {@see ABC} in question {@link DEF} using very verbose and descriptive language such that the line\n" +
                " * must be wrapped to be reasonable like this text right here.\n" +
                " *\n" +
                " * This text represents a second paragraph but without any silly \"p\" tags cluttering things up.  Does this line which is fairly long need to be wrapped as well?\n" +
                " *\n" +
                " * @param p1 The first parameter.\n" +
                " *        More detail about the first parameter.\n" +
                " * @param p2 The second parameter\n" +
                " * @returns int Description of return value.\n" +
                " *          More about the return value.\n" +
                " * @since 1.0\n" +
                " * @author cingram\n" +
                " */", null);
    }
}

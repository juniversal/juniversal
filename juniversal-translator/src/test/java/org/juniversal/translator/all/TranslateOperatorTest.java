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

public class TranslateOperatorTest extends TranslateNodeTest {
    @Test
    public void testTranslatePrefixOperators() {
        testTranslateIntExpression("-3", null, null);
        testTranslateIntExpression("- 3", null, "-3");
        testTranslateIntExpression("- /* foo */\n 3", null, "-3");

        testTranslateIntExpression("+ /* foo */\n 3", null, "+3");

        testTranslateBooleanExpression("! true", null, "!true");
        testTranslateBooleanExpression("~ 12", null, "~12");
    }

    @Test
    public void testTranslateInfixOperators() {
        testTranslateIntExpression("-3 * 4", null, null);
        testTranslateIntExpression("4 /*x*/ / /*y*/ 2", null, null);

        /*
        equivalentOperators.put(InfixExpression.Operator.TIMES, "*");
        equivalentOperators.put(InfixExpression.Operator.DIVIDE, "/");
        equivalentOperators.put(InfixExpression.Operator.REMAINDER, "%");
        equivalentOperators.put(InfixExpression.Operator.PLUS, "+");
        equivalentOperators.put(InfixExpression.Operator.MINUS, "-");

        // TODO: Test signed / unsigned semantics here
        equivalentOperators.put(InfixExpression.Operator.LEFT_SHIFT, "<<");
        equivalentOperators.put(InfixExpression.Operator.RIGHT_SHIFT_SIGNED, ">>");
        //cppOperators.put(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED, ">>>");

        equivalentOperators.put(InfixExpression.Operator.LESS, "<");
        equivalentOperators.put(InfixExpression.Operator.GREATER, ">");
        equivalentOperators.put(InfixExpression.Operator.LESS_EQUALS, "<=");
        equivalentOperators.put(InfixExpression.Operator.GREATER_EQUALS, ">=");
        equivalentOperators.put(InfixExpression.Operator.EQUALS, "==");
        equivalentOperators.put(InfixExpression.Operator.NOT_EQUALS, "!=");

        equivalentOperators.put(InfixExpression.Operator.AND, "&");
        equivalentOperators.put(InfixExpression.Operator.XOR, "^");
        equivalentOperators.put(InfixExpression.Operator.OR, "|");

        equivalentOperators.put(InfixExpression.Operator.CONDITIONAL_AND, "&&");
        equivalentOperators.put(InfixExpression.Operator.CONDITIONAL_OR, "||");
        */
    }

    // TODO: Test -- and ++, prefix and postfix versions, when a bit more test plumbing in place
    @Test
    public void testTranslatePostfixOperator() {
    }

    @Test
    public void testTranslateConditionalOperator() {
        testTranslateIntExpression("true   ? /*foo*/  1 :  0", null, null);
    }
}

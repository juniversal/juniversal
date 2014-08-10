package org.juniversal.translator.all;

import org.junit.Test;
import org.juniversal.translator.TranslateNodeTest;

public class TranslateOperatorTest extends TranslateNodeTest {
    @Test
    public void testTranslatePrefixOperators() {
        testTranslateIntExpression("-3", null);
        testTranslateIntExpression("- 3", "-3");
        testTranslateIntExpression("- /* foo */\n 3", "-3");

        testTranslateIntExpression("+ /* foo */\n 3", "+3");

        testTranslateBooleanExpression("! true", "!true");
        testTranslateBooleanExpression("~ 12", "~12");
    }

    @Test
    public void testTranslateInfixOperators() {
        testTranslateIntExpression("-3 * 4", null);
        testTranslateIntExpression("4 /*x*/ / /*y*/ 2", null);

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
        testTranslateIntExpression("true   ? /*foo*/  1 :  0", null);
    }
}

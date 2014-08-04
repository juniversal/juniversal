package org.juniversal.translator.all;

import org.junit.Test;
import org.juniversal.translator.TranslateNodeTest;

public class TranslateOperatorTest extends TranslateNodeTest {
    @Test public void testTranslatePrefixOperator() {
        testTranslateIntExpression("-3", null);
        testTranslateIntExpression("- 3", "-3");
        testTranslateIntExpression("- /* foo */\n 3", "-3");

        testTranslateIntExpression("+ /* foo */\n 3", "+3");

        testTranslateBooleanExpression("! true", "!true");
        testTranslateBooleanExpression("~ 12", "~12");
    }

    // TODO: Test -- and ++, prefix and postfix versions, when a bit more test plumbing in place
    @Test public void testTranslatePostfixOperator() {
    }

    @Test public void testTranslateConditionalOperator() {
        testTranslateIntExpression("true   ? /*foo*/  1 :  0", null);
    }
}

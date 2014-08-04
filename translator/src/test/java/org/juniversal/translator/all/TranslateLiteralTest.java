package org.juniversal.translator.all;

import org.junit.Test;
import org.juniversal.translator.TranslateNodeTest;

public class TranslateLiteralTest extends TranslateNodeTest {
    @Test public void testTranslateIntegerLiterals() {
        testTranslateIntExpression("3", null);

        // TODO: Fix this up when Swift fixes its bugs here
        //testTranslateLongExpression("3L", "UInt64(3)");

        testTranslateLongExpression("3", null);
        testTranslateLongExpression("3_123", null);
        testTranslateLongExpression("0xA1b2", null);
        testTranslateLongExpression("0XA1b2", null);
        testTranslateLongExpression("0b1", null);
        testTranslateLongExpression("0B1", null);
        testTranslateLongExpression("0", null);

        // Octal
        testTranslateLongExpression("0010101", "0o010101");

        //testTranslateLongExpression("3L", "Int64(3)");
        //testTranslateLongExpression("3l", "Int64(3)");
    }

    @Test public void testTranslateCharLiterals() {
        // TODO: Handle escape sequences

        testTranslateCharExpression("'c'", "Character(\"c\")");
    }

    @Test public void testTranslateOtherLiterals() {
        testTranslateStringExpression("null", "nil");

        testTranslateBooleanExpression("true", null);
        testTranslateBooleanExpression("false", null);
    }
}

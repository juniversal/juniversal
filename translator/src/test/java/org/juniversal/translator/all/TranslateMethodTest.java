package org.juniversal.translator.all;

import org.junit.Test;
import org.juniversal.translator.TranslateNodeTest;

public class TranslateMethodTest extends TranslateNodeTest {
    @Test public void testTranslateMethod() {
        testTranslateMethod("int foo() { return 3; }", "func foo() -> Int { return 3; }");
        testTranslateMethod("int foo(int a, int b) { return 3; }", "func foo(a: Int, b: Int) -> Int { return 3; }");
        testTranslateMethod("void foo(int a, int b) { }", "func foo(a: Int, b: Int) { }");
    }
}

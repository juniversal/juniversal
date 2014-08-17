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

public class TranslateStatementTest extends TranslateNodeTest {
    @Test
    public void testTranslateIfStatement() {
        // Test different combinations around the condition
        testTranslateStatement("if (true) { return; }", null, "if true { return; }");
        testTranslateStatement("if /*x*/ ( /*y*/ true /*y*/ ) /*z*/  { return; }", null, "if /*x*/ true /*z*/  { return; }");
        testTranslateStatement("if(true){ return; }", null, "if true{ return; }");

        // Test existing braces, on separate line
        testTranslateStatement(
                "if (true) {/*abc*/\n" +
                "    return; /*def*/}",

                null,

                "if true {/*abc*/\n" +
                "    return; /*def*/}");

        // Test adding braces
        testTranslateStatement("if (true) return;", null, "if true { return; }");
        testTranslateStatement("if (true)    return;", null, "if true    { return; }");
        testTranslateStatement("if(true)return;", null, "if true { return; }");
        testTranslateStatement("if (true) /*before*/ return;", null, "if true /*before*/ { return; }");
        testTranslateStatement("if  (   true   )  /*before*/ return;", null, "if  true  /*before*/ { return; }");

        // Test adding braces, when then part is on separate line

        testTranslateStatement(
                "if (true)\n" +
                "    return;",
                null,
                "if true {\n" +
                "    return;\n" +
                "}");

        testTranslateStatement(
                "if (true) // trailing\n" +
                "    /* pre */return; /*post*/",

                "if (true) // trailing\n" +
                "    /* pre */return;",

                "if true { // trailing\n" +
                "    /* pre */return; /*post*/\n" +
                "}");

        testTranslateStatement(
                "if (true)\n" +
                "      \n" +
                "       return;",

                "if (true)\n" +
                "\n" +
                "       return;",

                "if true {\n" +
                "\n" +
                "       return;\n" +
                "}");

        testTranslateStatement(
                "if (true) /*x*/\n" +
                "  /*abc*/    \n" +
                "       return;  /*def*/ return;",

                "if (true) /*x*/\n" +
                "  /*abc*/\n" +
                "       return;",

                "if true { /*x*/\n" +
                "  /*abc*/\n" +
                "       return;  /*def*/ \n" +
                "}");
    }

    @Test
    public void testTranslateIfElseStatement() {
        // Test with existing braces
        testTranslateStatement("if (true) {return;} else {return;}", null, "if true {return;} else {return;}");
        testTranslateStatement("if (true) {return;} else  /*abc*/  {/*def*/return;}", null, "if true {return;} else  /*abc*/  {/*def*/return;}");

        // Test adding braces

        testTranslateStatement(
                "if (true) {return;} else return;",

                null,

                "if true {return;} else {\n" +
                "    return;\n" +
                "}");

        testTranslateStatement(
                "if (true) {return;} else/*abc*/return;/*def*/",

                "if (true) {return;} else/*abc*/return;",

                "if true {return;} else {\n" +
                "    /*abc*/return;/*def*/\n" +
                "}");

        testTranslateStatement(
                "if (true) /*abc*/return; else/*def*/ return;",

                null,

                "if true /*abc*/{ return; } else {\n" +
                "    /*def*/ return;\n" +
                "}");

        testTranslateStatement(
                "if (true) {return;} else  /*abc*/       return;  /*def*/",

                "if (true) {return;} else  /*abc*/       return;",

                "if true {return;} else {\n" +
                "    /*abc*/       return;  /*def*/\n" +
                "}");

        testTranslateStatement(
                "if (true) {return;} else  /*abc*/       return;  /*def*/ return;\n" +
                "/*ghi*/",

                "if (true) {return;} else  /*abc*/       return;",

                "if true {return;} else {\n" +
                "    /*abc*/       return;  /*def*/ \n" +
                "}");

        testTranslateStatement(
                "if (true) {return;} else   /*foo*/\n" +
                "    \n" +
                "    /*abc*/       return;  /*def*/ return;",

                "if (true) {return;} else   /*foo*/\n" +
                "\n" +
                "    /*abc*/       return;",

                "if true {return;} else {   /*foo*/\n" +
                "\n" +
                "    /*abc*/       return;  /*def*/ \n" +
                "}");
    }

    @Test
    public void testTranslateWhileStatement() {
        // Test with existing braces

        testTranslateStatement("while /*abc*/(/*def*/true) {return;}", null, "while /*abc*/true {return;}");

        testTranslateStatement(
                "while (true)\n" +
                " /*abc*/ {return; /*def*/}  return;",

                "while (true)\n" +
                " /*abc*/ {return; /*def*/}",

                "while true\n" +
                " /*abc*/ {return; /*def*/}");

        // Test adding braces

        testTranslateStatement(
                "while (true) return;",
                null,
                "while (true) {\n" +
                "    return;\n" +
                "}\n");

        testTranslateStatement(
                "while (true) /*abc*/\n" +
                "    /*def*/\n" +
                "    return;",
                null,
                "while true { /*abc*/\n" +
                "    /*def*/\n" +
                "    return;\n" +
                "}");
    }

    @Test
    public void testTranslateDoStatement() {
        // Test with existing braces

        testTranslateStatement(
                "do {return;} while/*abc*/(/*def*/true) /*ghi*/  ;", null,
                "do {return;} while/*abc*/true /*ghi*/  ;");

        testTranslateStatement(
                "do  /*xx*/   \n" +
                "    {return;}\n" +
                "  while/*abc*/(/*def*/true) /*ghi*/  ;",
                null,
                "do  /*xx*/\n" +
                "    {return;}\n" +
                "  while/*abc*/true /*ghi*/  ;");


        // Test adding braces

        testTranslateStatement(
                "do   /*abc*/  return;   /*def*/  while  /*x*/(true) /*y*/ ;",
                null,
                "do   /*abc*/  { return; }   /*def*/  while  /*x*/true /*y*/ ;");

        testTranslateStatement(
                "do /*a*/ \n" +
                "  //abc\n" +
                "    return  \n" +
                "  //xx\n" +
                "  ;\n" +
                "  while (true)\n" +
                " /*y*/ ;",
                null,
                "do { /*a*/\n" +
                "  //abc\n" +
                "    return\n" +
                "  //xx\n" +
                "  ;\n" +
                "}\n" +
                "  while true\n" +
                " /*y*/ ;");
    }

    @Test
    public void testCurr() {
    }
}

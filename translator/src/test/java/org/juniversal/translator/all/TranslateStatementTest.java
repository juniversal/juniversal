package org.juniversal.translator.all;

import org.junit.Test;
import org.juniversal.translator.TranslateNodeTest;

public class TranslateStatementTest extends TranslateNodeTest {
    @Test
    public void testTranslateIfStatement() {
        // Test different combinations around the condition
        testTranslateStatement("if (true) { return; }", "if true { return; }");
        testTranslateStatement("if /*x*/ ( /*y*/ true /*y*/ ) /*z*/  { return; }", "if /*x*/ true /*z*/  { return; }");
        testTranslateStatement("if(true){ return; }", "if true{ return; }");

        // Test existing braces, on separate line
        testTranslateStatement(
                "if (true) {/*abc*/\n" +
                "    return; /*def*/}",

                "if true {/*abc*/\n" +
                "    return; /*def*/}");

        // Test adding braces
        testTranslateStatement("if (true) return;", "if true { return; }");
        testTranslateStatement("if (true)    return;", "if true    { return; }");
        testTranslateStatement("if(true)return;", "if true { return; }");
        testTranslateStatement("if (true) /*before*/ return;", "if true /*before*/ { return; }");
        testTranslateStatement("if  (   true   )  /*before*/ return;", "if  true  /*before*/ { return; }");

        // Test adding braces, when then part is on separate line

        testTranslateStatement(
                "if (true)\n" +
                "    return;",

                "if true {\n" +
                "    return;\n" +
                "}");

        testTranslateStatement(
                "if (true) // trailing\n" +
                "    /* pre */return; /*post*/",

                "if true { // trailing\n" +
                "    /* pre */return; /*post*/\n" +
                "}");

        testTranslateStatement(
                "if (true)\n" +
                "      \n" +
                "       return;",

                "if true {\n" +
                "\n" +
                "       return;\n" +
                "}");

        testTranslateStatement(
                "if (true) /*x*/\n" +
                "  /*abc*/    \n" +
                "       return;  /*def*/ return;",

                "if true { /*x*/\n" +
                "  /*abc*/\n" +
                "       return;  /*def*/ \n" +
                "}");
    }

    @Test
    public void testTranslateIfElseStatement() {
        // Test with existing braces
        testTranslateStatement("if (true) {return;} else {return;}", "if true {return;} else {return;}");
        testTranslateStatement("if (true) {return;} else  /*abc*/  {/*def*/return;}", "if true {return;} else  /*abc*/  {/*def*/return;}");

        // Test adding braces

        testTranslateStatement(
                "if (true) {return;} else return;",

                "if true {return;} else {\n" +
                "    return;\n" +
                "}");

        testTranslateStatement(
                "if (true) {return;} else/*abc*/return;/*def*/",

                "if true {return;} else {\n" +
                "    /*abc*/return;/*def*/\n" +
                "}");

        testTranslateStatement(
                "if (true) {return;} else  /*abc*/       return;  /*def*/",

                "if true {return;} else {\n" +
                "    /*abc*/       return;  /*def*/\n" +
                "}");

        testTranslateStatement(
                "if (true) {return;} else  /*abc*/       return;  /*def*/ return;\n" +
                "/*ghi*/",

                "if true {return;} else {\n" +
                "    /*abc*/       return;  /*def*/ \n" +
                "}");

        testTranslateStatement(
                "if (true) {return;} else   /*foo*/\n" +
                "    \n" +
                "    /*abc*/       return;  /*def*/ return;",

                "if true {return;} else {   /*foo*/\n" +
                "\n" +
                "    /*abc*/       return;  /*def*/ \n" +
                "}");
    }

    @Test
    public void testTranslateWhileStatement() {
        // Test with existing braces

        testTranslateStatement("while /*abc*/(/*def*/true) {return;}", "while /*abc*/true {return;}");

        testTranslateStatement(
                "while (true)\n" +
                " /*abc*/ {return; /*def*/}  return;",

                "while true\n" +
                " /*abc*/ {return; /*def*/}");

        // Test adding braces

        testTranslateStatement(
                "while (true) return;",

                "while (true) {\n" +
                "    return;\n" +
                "}\n");

        testTranslateStatement(
                "while (true) /*abc*/\n" +
                "    /*def*/\n" +
                "    return;",

                "while true { /*abc*/\n" +
                "    /*def*/\n" +
                "    return;\n" +
                "}");
    }

    @Test
    public void testTranslateDoStatement() {
        // Test with existing braces

        testTranslateStatement(
                "do {return;} while/*abc*/(/*def*/true) /*ghi*/  ;",
                "do {return;} while/*abc*/true /*ghi*/  ;");

        testTranslateStatement(
                "do  /*xx*/   \n" +
                "    {return;}\n" +
                "  while/*abc*/(/*def*/true) /*ghi*/  ;",

                "do  /*xx*/\n" +
                "    {return;}\n" +
                "  while/*abc*/true /*ghi*/  ;");


        // Test adding braces

        testTranslateStatement(
                "do   /*abc*/  return;   /*def*/  while  /*x*/(true) /*y*/ ;",

                "do   /*abc*/  { return; }   /*def*/  while  /*x*/true /*y*/ ;");

        testTranslateStatement(
                "do /*a*/ \n" +
                "  //abc\n" +
                "    return  \n" +
                "  //xx\n" +
                "  ;\n" +
                "  while (true)\n" +
                " /*y*/ ;",

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

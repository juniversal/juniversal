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

import static org.junit.Assert.fail;


public class WriteTest {
    // TODO: Move this test somewwhere else

    @Test public void returnTest() {
        sourceTabStop = 4;
        destTabStop = -1;
        testWriteStatement("return 3;");
        testWriteStatement("return\r\n\t3;", "return\r\n    3;");
        testWriteStatement("return\t3\t\t;", "return  3       ;");

        sourceTabStop = 4;
        destTabStop = 4;  // Return to default settings
        testWriteStatement("return 3;");
        testWriteStatement("return\r\n\t3;");
        testWriteStatement("return\r\n   \t3;", "return\r\n\t3;");
        testWriteStatement("return\r\n  \t  \t 3;", "return\r\n\t\t 3;");
        testWriteStatement("return\t3\t\t;", "return  3       ;");
    }

/*
    @Test public void ifTest() {
        testWriteStatement("if (false) return 3;");
        testWriteStatement("if (true) return 3; else return 7;");
        testWriteStatement("if ( true ) { return 3 ; } else { return 7 ; }");
        testWriteStatement("if ( true )\r\n\t\t{ return 3 ; }\r\n\t\telse { return 7 ; }");
    }

    @Test public void variableDeclarationTest() {
        testWriteStatement("int i = 3;");
        testWriteStatement("boolean *//* comment 1 *//* b *//* comment 2 *//* ;",
                "bool *//* comment 1 *//* b *//* comment 2 *//* ;");
        testWriteStatement("char c = 25 , d = 25 ;",
                "unsigned short c = 25 , d = 25 ;");
        testWriteStatement("byte foo;", "char foo;");
        testWriteStatement("short foo;");
        testWriteStatement("char foo;", "unsigned short foo;");
        testWriteStatement("int foo;");
        try {
            testWriteStatement("long foo;");
        } catch (SourceNotSupportedException e) {
            assertEquals(
                    "SourceNotSupportedException: <unknown-file> (line 2, col 1): long type isn't supported by default; need to specify target C++ type for 64 bit int",
                    e.toString());
        }
        testWriteStatement("float foo;");
        testWriteStatement("double foo;");
        testWriteStatement("boolean foo;", "bool foo;");
    }

    @Test public void blockTest() {
        testWriteStatement("{ int i = 3; boolean b = false; if ( b ) \r\n return 5; else return 6; }",
                "{ int i = 3; bool b = false; if ( b )\r\n return 5; else return 6; }");
    }

    @Test public void classTest() {
        testWriteClass(
                "class Foo {\r\n" +
                        "	private int abc;\r\n" +
                        "}\r\n",
                "class Foo {\r\n" +
                        "private:\r\n" +
                        "    int abc;\r\n" +
                        "}\r\n");
    }*/

    public void testWriteStatement(String javaExpressionAndCpp) {
/*
        testWriteStatement(javaExpressionAndCpp, javaExpressionAndCpp);
*/
    }

    public void testWriteStatement(String javaExpression, String cpp) {
/*
        testWriteStatement(javaExpressionAndCpp, javaExpressionAndCpp);
*/
    }

/*
    public void testWriteNode(ASTNode node, String javaSource, CompilationUnit compilationUnit,
                              int sourceTabStop, @Nullable String expectedSwift) {
        StringWriter writer = new StringWriter();
        CPPProfile profile = new CPPProfile();
        profile.setTabStop(destTabStop);

        TargetWriter targetWriter = new TargetWriter(writer, profile);

        Context context = new Context(new SourceFile(compilationUnit, null, javaSource), this.sourceTabStop, profile,
                targetWriter, OutputType.SOURCE);

        context.setPosition(node.getStartPosition());
        writeSwift.writeNode(node, context);

        String cppOutput = writer.getBuffer().toString();

        if (!cppOutput.equals(expectedSwift))
            fail("Output doesn't match expected output.\r\nEXPECTED:\r\n" + expectedSwift +
                    "\r\nACUAL:\r\n" + cppOutput);
    }

    private SwiftTranslator swiftTranslator = new SwiftTranslator();
    private CSharpTranslator swiftTranslator = new SwiftTranslator();
*/

    // Data
    private int sourceTabStop = 4;
    private int destTabStop = 4;
}

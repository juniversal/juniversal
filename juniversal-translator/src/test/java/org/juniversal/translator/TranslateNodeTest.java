/*
 * Copyright (c) 2012-2015, Microsoft Mobile
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

package org.juniversal.translator;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.JavaSourceFile;
import org.xuniversal.translator.core.SourceFile;
import org.xuniversal.translator.core.SourceNotSupportedException;
import org.juniversal.translator.core.Translator;
import org.juniversal.translator.csharp.CSharpTranslator;

import java.util.Map;

import static org.junit.Assert.assertEquals;


public class TranslateNodeTest {
    protected int sourceTabStop = 4;
    protected int destTabStop = -1;

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

    public void testWriteStatement(String javaStatement, @Nullable String expectedCSharp,
                                   @Nullable String expectedSwift) {
        String javaFullSource = "class TestClass{ int testMethod() {\n" + javaStatement + "\n} }";

        CompilationUnit compilationUnit = parseCompilationUnit(javaFullSource);

        Block block = getFirstMethodBlock(compilationUnit);
        ASTNode firstStatement = (ASTNode) block.statements().get(0);

        testTranslateNode(firstStatement, javaFullSource, javaStatement, compilationUnit, expectedCSharp,
                expectedSwift);
    }

    public CompilationUnit parseCompilationUnit(String java) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        parser.setSource(java.toCharArray()); // set source
        parser.setResolveBindings(true); // we need bindings later on
        parser.setEnvironment(new String[0], new String[0], null, true);
        parser.setUnitName("TestClass.java");

        // In order to parse 1.8 code, some compiler options need to be set to 1.8
        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
        parser.setCompilerOptions(options);

        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null /* IProgressMonitor */);

        IProblem[] problems = compilationUnit.getProblems();
        if (problems.length > 0) {
            StringBuilder problemsText = new StringBuilder();

            for (IProblem problem : problems) {
                if (problem.isError()) {
                    problemsText.append(problem.getMessage() + "\n");
                }
            }

            if (problemsText.length() > 0)
                throw new RuntimeException(problemsText.toString());
        }

        return compilationUnit;
    }

    public static MethodDeclaration getFirstMethod(CompilationUnit compilationUnit) {
        TypeDeclaration clazz = (TypeDeclaration) compilationUnit.types().get(0);
        return clazz.getMethods()[0];
    }

    public static Block getFirstMethodBlock(CompilationUnit compilationUnit) {
        return getFirstMethod(compilationUnit).getBody();
    }

    protected void testTranslateIntExpression(String javaExpression, @Nullable String expectedCSharpExpression,
                                              @Nullable String expectedSwiftExpression) {
        testTranslateExpression("int", javaExpression, expectedCSharpExpression, expectedSwiftExpression);
    }

    protected void testTranslateLongExpression(String javaExpression, @Nullable String expectedCSharpExpression,
                                               @Nullable String expectedSwiftExpression) {
        testTranslateExpression("long", javaExpression, expectedCSharpExpression, expectedSwiftExpression);
    }

    protected void testTranslateBooleanExpression(String javaExpression, @Nullable String expectedCSharpExpression,
                                                  @Nullable String expectedSwiftExpression) {
        testTranslateExpression("boolean", javaExpression, expectedCSharpExpression, expectedSwiftExpression);
    }

    protected void testTranslateCharExpression(String javaExpression, @Nullable String expectedCSharpExpression,
                                               @Nullable String expectedSwiftExpression) {
        testTranslateExpression("char", javaExpression, expectedCSharpExpression, expectedSwiftExpression);
    }

    protected void testTranslateStringExpression(String javaExpression, @Nullable String expectedCSharpExpression,
                                                 @Nullable String expectedSwiftExpression) {
        testTranslateExpression("String", javaExpression, expectedCSharpExpression, expectedSwiftExpression);
    }

    protected void testTranslateExpression(String javaType, String javaExpression,
                                           @Nullable String expectedCSharpExpression,
                                           @Nullable String expectedSwiftExpression) {
        String javaClass =
                "class TestClass{\n" +
                        "    private int intField;\n" +
                        "    private int[] intArrayField;\n" +
                        "    void testMethod() {" + javaType + " foo = " + javaExpression + "; }\n" +
                        "}";

        CompilationUnit compilationUnit = parseCompilationUnit(javaClass);

        Block block = getFirstMethodBlock(compilationUnit);
        VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) block.statements().get(0);
        VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);

        testTranslateNode(variableDeclarationFragment.getInitializer(), javaClass, javaExpression, compilationUnit,
                expectedCSharpExpression, expectedSwiftExpression);
    }

    protected void testTranslateStatement(String javaStatement, @Nullable String expectedCSharpStatement,
                                          @Nullable String expectedSwiftStatement) {
        String javaClass = "class TestClass{ void testMethod() {" + javaStatement + "} }";

        CompilationUnit compilationUnit = parseCompilationUnit(javaClass);

        Block block = getFirstMethodBlock(compilationUnit);
        Statement statement = (Statement) block.statements().get(0);

        testTranslateNode(statement, javaClass, javaStatement, compilationUnit, expectedCSharpStatement,
                expectedSwiftStatement);
    }

    protected void testTranslateMethod(String javaMethod, @Nullable String expectedCSharpMethod,
                                       @Nullable String expectedSwiftMethod) {
        String javaClass = "final class TestClass{ " + javaMethod + " }";

        CompilationUnit compilationUnit = parseCompilationUnit(javaClass);

        MethodDeclaration methodDeclaration = getFirstMethod(compilationUnit);

        testTranslateNode(methodDeclaration, javaClass, javaMethod, compilationUnit, expectedCSharpMethod,
                expectedSwiftMethod);
    }

    protected void testTranslateJavadocComment(String javadocComment, @Nullable String expectedCSharpComment) {
        String javaClass = "class TestClass{\n" + javadocComment + "\n  void testMethod() {\n  }\n}";

        CompilationUnit compilationUnit = parseCompilationUnit(javaClass);

        Javadoc commentObj = getFirstJavadoc(compilationUnit);
        testTranslateNode(commentObj, javaClass, javadocComment, compilationUnit, expectedCSharpComment,
                expectedCSharpComment);
    }

    public static Javadoc getFirstJavadoc(CompilationUnit compilationUnit) {
        return (Javadoc) compilationUnit.getCommentList().get(0);
    }

    protected void testTranslateNode(ASTNode node, String javaFullSource, String javaNodeSource,
                                     CompilationUnit compilationUnit, @Nullable String expectedCSharp,
                                     @Nullable String expectedSwift) {
/*
        CPPProfile profile = new CPPProfile();
        profile.setTabStop(destTabStop);
*/

        cSharpTranslator.setDestTabStop(destTabStop);

        JavaSourceFile sourceFile = new JavaSourceFile(compilationUnit, javaFullSource, sourceTabStop);

        testTranslate(cSharpTranslator, sourceFile, node, javaNodeSource, expectedCSharp);
        //testTranslate(swiftTranslator, sourceFile, node, javaNodeSource, expectedSwift);


/*
        if (!actualSwift.equals(expectedSwift == null ? java : expectedSwift))
            fail("Output doesn't match expected output.\r\nEXPECTED:\r\n" + (expectedSwift == null ? java : expectedSwift) +
                    "\r\nACUAL:\r\n" + actualSwift);
*/
    }

    private void testTranslate(Translator translator, JavaSourceFile sourceFile, ASTNode node, String javaNodeSource,
                               @Nullable String expectedOutput) {
        String effectiveExpectedOutput = expectedOutput == null ? javaNodeSource : expectedOutput;

        if (effectiveExpectedOutput.startsWith(("NOT-SUPPORTED:"))) {
            try {
                translator.translateNode(sourceFile, node);
            } catch (SourceNotSupportedException e) {
                String expectedError = effectiveExpectedOutput.substring("NOT-SUPPORTED: ".length());

                String actutalError = e.getMessage().replace("\r", "");
                int newlineIndex = actutalError.indexOf('\n');
                if (newlineIndex != -1)
                    actutalError = actutalError.substring(0, newlineIndex);

                assertEquals(expectedError, actutalError);
            }
        } else {
            String actualOutput = translator.translateNode(sourceFile, node);
            assertEqualsNormalizeNewlines(effectiveExpectedOutput, actualOutput);
        }
    }

    private void assertEqualsNormalizeNewlines(String expected, String actual) {
        assertEquals(expected.replace("\r", ""), actual.replace("\r", ""));
    }

    private CSharpTranslator cSharpTranslator = new CSharpTranslator();
    //private SwiftTranslator swiftTranslator = new SwiftTranslator();
}

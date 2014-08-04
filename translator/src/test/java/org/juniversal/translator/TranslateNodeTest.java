package org.juniversal.translator;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.juniversal.translator.core.Context;
import org.juniversal.translator.core.SourceFile;
import org.juniversal.translator.core.TargetWriter;
import org.juniversal.translator.cplusplus.CPPProfile;
import org.juniversal.translator.cplusplus.OutputType;

import java.io.StringWriter;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class TranslateNodeTest {
    private int sourceTabStop = 4;
    private int destTabStop = -1;

    @Test
    public void returnTest() {
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

    public void testWriteStatement(String javaStatement, @Nullable String expectedSwift) {
        String javaFullSource = "class TestClass{ void testMethod() {\n" + javaStatement + "\n} }";

        CompilationUnit compilationUnit = parseCompilationUnit(javaFullSource);

        Block block = getFirstMethodBlock(compilationUnit);
        ASTNode firstStatement = (ASTNode) block.statements().get(0);

        testTranslateNode(firstStatement, javaFullSource, javaStatement, compilationUnit, expectedSwift);
    }

    public void testWriteClass(String java, String expectedCPPClassHeader) {
        CompilationUnit compilationUnit = parseCompilationUnit(java);

        Block block = getFirstMethodBlock(compilationUnit);
        ASTNode firstStatement = (ASTNode) block.statements().get(0);

        testTranslateNode(firstStatement, java, java, compilationUnit, expectedCPPClassHeader);
    }

    public void testWriteStatement(String javaExpressionAndCpp) {
        testWriteStatement(javaExpressionAndCpp, javaExpressionAndCpp);
    }

    public void testWriteCompilationUnit(String java, String cpp) {
        CompilationUnit compilationUnit = parseCompilationUnit(java);
        Block block = getFirstMethodBlock(compilationUnit);
        testTranslateNode(block, java, java, compilationUnit, cpp);
    }

    public CompilationUnit parseCompilationUnit(String java) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        parser.setSource(java.toCharArray()); // set source
        parser.setResolveBindings(true); // we need bindings later on

        // In order to parse 1.8 code, some compiler options need to be set to 1.8
        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
        parser.setCompilerOptions(options);

        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null /* IProgressMonitor */);

        IProblem[] problems = compilationUnit.getProblems();
        if (problems.length > 0) {
            StringBuilder problemsText = new StringBuilder();

            for (IProblem problem : problems) {
                problemsText.append(problem.getMessage() + "\n");
            }

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

    protected void testTranslateIntExpression(String javaExpression, @Nullable String expectedSwiftExpression) {
        testTranslateExpression("int", javaExpression, expectedSwiftExpression);
    }

    protected void testTranslateLongExpression(String javaExpression, @Nullable String expectedSwiftExpression) {
        testTranslateExpression("long", javaExpression, expectedSwiftExpression);
    }

    protected void testTranslateBooleanExpression(String javaExpression, @Nullable String expectedSwiftExpression) {
        testTranslateExpression("boolean", javaExpression, expectedSwiftExpression);
    }

    protected void testTranslateCharExpression(String javaExpression, @Nullable String expectedSwiftExpression) {
        testTranslateExpression("char", javaExpression, expectedSwiftExpression);
    }

    protected void testTranslateStringExpression(String javaExpression, @Nullable String expectedSwiftExpression) {
        testTranslateExpression("String", javaExpression, expectedSwiftExpression);
    }

    protected void testTranslateExpression(String javaType, String javaExpression,
                                           @Nullable String expectedSwiftExpression) {
        String javaClass = "class TestClass{ void testMethod() {" + javaType + " foo = " + javaExpression + "; } }";

        CompilationUnit compilationUnit = parseCompilationUnit(javaClass);

        Block block = getFirstMethodBlock(compilationUnit);
        VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) block.statements().get(0);
        VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);

        testTranslateNode(variableDeclarationFragment.getInitializer(), javaClass, javaExpression, compilationUnit, expectedSwiftExpression);
    }

    protected void testTranslateStatement(String javaStatement, @Nullable String expectedSwiftStatement) {
        String javaClass = "class TestClass{ void testMethod() {" + javaStatement + "} }";

        CompilationUnit compilationUnit = parseCompilationUnit(javaClass);

        Block block = getFirstMethodBlock(compilationUnit);
        Statement statement = (Statement) block.statements().get(0);

        testTranslateNode(statement, javaClass, javaStatement, compilationUnit, expectedSwiftStatement);
    }

    protected void testTranslateMethod(String javaMethod, @Nullable String expectedSwiftMethod) {
        String javaClass = "class TestClass{ " + javaMethod + " }";

        CompilationUnit compilationUnit = parseCompilationUnit(javaClass);

        MethodDeclaration methodDeclaration = getFirstMethod(compilationUnit);

        testTranslateNode(methodDeclaration, javaClass, javaMethod, compilationUnit, expectedSwiftMethod);
    }

    protected void testTranslateNode(ASTNode node, String javaFullSource, String javaNodeSource, CompilationUnit compilationUnit,
                                     @Nullable String expectedSwift) {
        StringWriter writer = new StringWriter();
        CPPProfile profile = new CPPProfile();
        profile.setTabStop(destTabStop);

        TargetWriter targetWriter = new TargetWriter(writer, profile);

        Context context = new Context(new SourceFile(compilationUnit, null, javaFullSource), this.sourceTabStop, profile,
                targetWriter, OutputType.SOURCE);

        context.setPosition(node.getStartPosition());
        writeSwift.writeNode(node, context);

        String actualSwift = writer.getBuffer().toString();

        assertEqualsNormalizeNewlines(expectedSwift == null ? javaNodeSource : expectedSwift, actualSwift);

/*
        if (!actualSwift.equals(expectedSwift == null ? java : expectedSwift))
            fail("Output doesn't match expected output.\r\nEXPECTED:\r\n" + (expectedSwift == null ? java : expectedSwift) +
                    "\r\nACUAL:\r\n" + actualSwift);
*/
    }

    private void assertEqualsNormalizeNewlines(String expected, String actual) {
        assertEquals(expected.replace("\r", ""), actual.replace("\r", ""));
    }

    private org.juniversal.translator.swift.astwriters.ASTWriters writeSwift = new org.juniversal.translator.swift.astwriters.ASTWriters();
}

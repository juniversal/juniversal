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

package org.juniversal.translator.cplusplus;

import org.eclipse.jdt.core.dom.*;
import org.junit.Ignore;
import org.junit.Test;
import org.juniversal.translator.core.JavaSourceFile;
import org.xuniversal.translator.core.SourceNotSupportedException;
import org.xuniversal.translator.cplusplus.CPlusPlusProfile;

import static org.junit.Assert.*;


public @Ignore class WriteCPPTest {
    private int m_sourceTabStop = 4;
    private int m_destTabStop = 4;
    private CPlusPlusTranslator cPlusPlusTranslator = new CPlusPlusTranslator();

    @Test public void variableDeclarationTest() {
        testWriteStatement("int i = 3;");
        testWriteStatement("boolean /* comment 1 */ b /* comment 2 */ ;",
                "bool /* comment 1 */ b /* comment 2 */ ;");
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
    }

    public void testWriteStatement(String javaStatement, String expectedCPPStatement) {
        String java = "class TestClass{ void testMethod() {\n" + javaStatement + "\n} }";

        CompilationUnit compilationUnit = parseCompilationUnit(java);

        Block block = getFirstMethodBlock(compilationUnit);
        ASTNode firstStatement = (ASTNode) block.statements().get(0);

        testWriteNode(firstStatement, java, compilationUnit, 4, expectedCPPStatement);
    }

    public void testWriteClass(String java, String expectedCPPClassHeader) {
        CompilationUnit compilationUnit = parseCompilationUnit(java);

        Block block = getFirstMethodBlock(compilationUnit);
        ASTNode firstStatement = (ASTNode) block.statements().get(0);

        testWriteNode(firstStatement, java, compilationUnit, 4, expectedCPPClassHeader);
    }

    public void testWriteStatement(String javaExpressionAndCpp) {
        testWriteStatement(javaExpressionAndCpp, javaExpressionAndCpp);
    }

    public void testWriteCompilationUnit(String java, String cpp) {
        CompilationUnit compilationUnit = parseCompilationUnit(java);
        Block block = getFirstMethodBlock(compilationUnit);
        testWriteNode(block, java, compilationUnit, 4, cpp);
    }

    public CompilationUnit parseCompilationUnit(String java) {

        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(java.toCharArray()); // set source
        parser.setResolveBindings(true); // we need bindings later on

        return (CompilationUnit) parser.createAST(null /* IProgressMonitor */);
    }

    public static Block getFirstMethodBlock(CompilationUnit compilationUnit) {
        TypeDeclaration clazz = (TypeDeclaration) compilationUnit.types().get(0);

        return clazz.getMethods()[0].getBody();
    }

    public void testWriteNode(ASTNode node, String javaSource, CompilationUnit compilationUnit, int sourceTabStop,
                              String expectedOutput) {
        CPlusPlusProfile profile = new CPlusPlusProfile();
        profile.setTabStop(m_destTabStop);

        JavaSourceFile sourceFile = new JavaSourceFile(compilationUnit, javaSource, m_sourceTabStop);

        String cppOutput = cPlusPlusTranslator.translateNode(sourceFile, node);

        if (!cppOutput.equals(expectedOutput))
            fail("Output doesn't match expected output.\r\nEXPECTED:\r\n" + expectedOutput +
                 "\r\nACUAL:\r\n" + cppOutput);
    }
}

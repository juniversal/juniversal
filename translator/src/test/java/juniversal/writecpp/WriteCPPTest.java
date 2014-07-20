package juniversal.writecpp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.StringWriter;

import juniversal.SourceFile;
import juniversal.SourceNotSupportedException;
import juniversal.cplusplus.CPPProfile;
import juniversal.cplusplus.CPPWriter;
import juniversal.cplusplus.Context;
import juniversal.cplusplus.OutputType;
import juniversal.cplusplus.astwriters.ASTWriters;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.Test;


public class WriteCPPTest {
	@Test public void returnTest() {	
		m_sourceTabStop = 4; m_destTabStop = -1;
		testWriteStatement("return 3;");
		testWriteStatement("return\r\n\t3;", "return\r\n    3;");
		testWriteStatement("return\t3\t\t;", "return  3       ;");

		m_sourceTabStop = 4; m_destTabStop = 4;  // Return to default settings
		testWriteStatement("return 3;");
		testWriteStatement("return\r\n\t3;");
		testWriteStatement("return\r\n   \t3;", "return\r\n\t3;");
		testWriteStatement("return\r\n  \t  \t 3;", "return\r\n\t\t 3;");
		testWriteStatement("return\t3\t\t;", "return  3       ;");
	}

	@Test public void ifTest() {
		testWriteStatement("if (false) return 3;");
		testWriteStatement("if (true) return 3; else return 7;");
		testWriteStatement("if ( true ) { return 3 ; } else { return 7 ; }");
		testWriteStatement("if ( true )\r\n\t\t{ return 3 ; }\r\n\t\telse { return 7 ; }");
	}

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
		}
		catch (SourceNotSupportedException e) {
			assertEquals(
					"juniversal.SourceNotSupportedException: <unknown-file> (line 2, col 1): long type isn't supported by default; need to specify target C++ type for 64 bit int",
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

		ASTParser parser = ASTParser.newParser(AST.JLS3); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(java.toCharArray()); // set source
		parser.setResolveBindings(true); // we need bindings later on

		return (CompilationUnit) parser.createAST(null /* IProgressMonitor */);
	}

	public static Block getFirstMethodBlock(CompilationUnit compilationUnit)
	{
		TypeDeclaration clazz = (TypeDeclaration) compilationUnit.types().get(0);
	
		return clazz.getMethods()[0].getBody();
	}

	public void testWriteNode(ASTNode node, String javaSource, CompilationUnit compilationUnit,
			int sourceTabStop, String expectedCPPOutput)
	{
		StringWriter writer = new StringWriter();
		CPPProfile profile = new CPPProfile();
		profile.setTabStop(m_destTabStop);
		
		CPPWriter cppWriter = new CPPWriter(writer, profile);

		Context context = new Context(new SourceFile(compilationUnit, null, javaSource), m_sourceTabStop, profile,
				cppWriter, OutputType.SOURCE);

		context.setPosition(node.getStartPosition());
		getWriteCPP().writeNode(node, context);

		String cppOutput = writer.getBuffer().toString();

		if (! cppOutput.equals(expectedCPPOutput))
			fail("Output doesn't match expected output.\r\nEXPECTED:\r\n" + expectedCPPOutput +
					"\r\nACUAL:\r\n" + cppOutput);
	}

	static ASTWriters m_writeCPP = null; 
	ASTWriters getWriteCPP() {
		if (m_writeCPP == null)
			m_writeCPP = new ASTWriters();
		return m_writeCPP;
	}

	// Data
	private int m_sourceTabStop = 4;
	private int m_destTabStop = 4;
}

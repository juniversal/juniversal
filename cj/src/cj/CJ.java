package cj;

import org.eclipse.jdt.core.dom.*;

public class CJ {

	public static void main(String[] args) {

		ASTParser parser = ASTParser.newParser(AST.JLS3); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource("class Foo{ void foo() { x = 3; \n\n   /* X */\n\n/* abc */   y = 7; /* a*/\n}}".toCharArray()); // set source
		// parser.setResolveBindings(true); // we need bindings later on

		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null /* IProgressMonitor */);
		System.out.println(compilationUnit);

		Block block = ASTUtil.getFirstMethodBlock(compilationUnit);
		for (Object statementObj : block.statements()) {
			ASTNode statement = (ASTNode) statementObj;
			
			System.out.println(statement.getStartPosition() + ", " +
					statement.getLength() + "; Comments: " +
					compilationUnit.getExtendedStartPosition(statement) + " - " +
					compilationUnit.getExtendedLength(statement));
		}
	}
}

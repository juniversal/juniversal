package cj.writecpp;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * This method 
 * @author bret
 *
 */
public class WriteTypeVisitor extends WriteCPPVisitor {
	private int someData;

	/**
	 * Some java doc
	 */
	@Override
	public void write(ASTNode node, WriteCPPContext context) {
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;

		if (context.getOutputType() == OutputType.HEADER)
			writeHeader(typeDeclaration, context);
		else writeSource(typeDeclaration, context);
	}

	// Some doc
	private void writeHeader(TypeDeclaration typeDeclaration, WriteCPPContext context) {
		
		// Javadoc, if any, before the type declaration is included in the source range of
		// this node; copy it if present
		context.copySpaceAndComments();

		// Skip the modifiers and the space/comments following them
		context.skipModifiers(typeDeclaration.modifiers());
		context.skipSpaceAndComments();

		context.write("class");

		// TODO: Handle super classes & super interfaces
		// TODO: Handle generics

		if (context.getCPPProfile().getClassBraceOnSameLine())
			context.writeln(" {");
		else {
			context.writeln();
			context.writeln("{");
		}
		context.matchAndWrite("{");

		context.getCPPWriter().incrementByPreferredIndent();
		
		


		/*
		 * // START HERE; SWITCH BETWEEN HEADER, SOURCE, AND BOTH OUTPUT if
		 * (typeDeclaration.isInterface()) context.match
		 */


		/*
		 * for (Object statementObject : block.statements()) { Statement statement =
		 * (Statement) statementObject; context.copySpaceAndComments(); writeNode(statement,
		 * context); }
		 */

		context.copySpaceAndComments();
		context.matchAndWrite("}");
	}

	private void writeSource(TypeDeclaration typeDeclaration, WriteCPPContext context) {
		
	}
}

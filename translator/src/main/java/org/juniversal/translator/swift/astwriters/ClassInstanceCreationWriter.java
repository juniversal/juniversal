package org.juniversal.translator.swift.astwriters;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.juniversal.translator.core.Context;

import java.util.List;


public class ClassInstanceCreationWriter extends ASTWriter {
	public ClassInstanceCreationWriter(ASTWriters astWriters) {
		super(astWriters);
	}

	@Override
	public void write(ASTNode node, Context context) {
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) node;

		//TODO: Handle type arguments
		//TODO: Handle different reference operator used for stack objects

		// TODO: Support inner class creation via object.new
		if (classInstanceCreation.getExpression() != null)
			context.throwSourceNotSupported("Inner classes not yet supported");

		context.matchAndWrite("new");
		context.copySpaceAndComments();

		getASTWriters().writeNode(classInstanceCreation.getType(), context);
		context.copySpaceAndComments();

		context.matchAndWrite("(");
		context.copySpaceAndComments();

		List<?> arguments = classInstanceCreation.arguments();

		boolean first = true;
		for (Object object : arguments) {
			Expression argument = (Expression) object;

			if (! first) {
				context.matchAndWrite(",");
				context.copySpaceAndComments();
			}

			getASTWriters().writeNode(argument, context);
			context.copySpaceAndComments();

			first = false;
		}

		context.matchAndWrite(")");
	}
}

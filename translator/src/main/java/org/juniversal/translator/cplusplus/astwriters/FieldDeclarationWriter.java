package org.juniversal.translator.cplusplus.astwriters;

import org.juniversal.translator.core.ASTUtil;
import org.juniversal.translator.core.Context;
import org.juniversal.translator.cplusplus.OutputType;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;


public class FieldDeclarationWriter extends ASTWriter {
	public FieldDeclarationWriter(ASTWriters astWriters) {
		super(astWriters);
	}

	@Override
	public void write(ASTNode node, Context context) {
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;

		// TODO: Handle final/const

		boolean isStatic = ASTUtil.containsStatic(fieldDeclaration.modifiers());

		if (context.getOutputType() == OutputType.HEADER && isStatic)
			context.write("static ");
		context.skipModifiers(fieldDeclaration.modifiers());

		// Write the type
		context.skipSpaceAndComments();
		getASTWriters().writeType(fieldDeclaration.getType(), context, false);

		boolean first = true;
		for (Object fragment : fieldDeclaration.fragments()) {
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragment;

			if (! first) {
				context.copySpaceAndComments();
				context.matchAndWrite(",");
			}

			context.copySpaceAndComments();
			writeVariableDeclarationFragment(variableDeclarationFragment, context,
					context.getOutputType() == OutputType.SOURCE);

			first = false;
		}

		context.copySpaceAndComments();
		context.matchAndWrite(";");
	}

	private void writeVariableDeclarationFragment(VariableDeclarationFragment variableDeclarationFragment,
			Context context, boolean writingSourceFile) {

		// TODO: Handle syntax with extra dimensions on array
		if (variableDeclarationFragment.getExtraDimensions() > 0)
			context.throwSourceNotSupported("\"int foo[]\" syntax not currently supported; use \"int[] foo\" instead");

		if (context.isWritingVariableDeclarationNeedingStar())
			context.write("*");

		if (writingSourceFile)
			context.write(context.getTypeDeclaration().getName().getIdentifier() + "::");
		getASTWriters().writeNode(variableDeclarationFragment.getName(), context);

		// Only write out the initializer when writing to the source file; in that case the field
		// must be static
		Expression initializer = variableDeclarationFragment.getInitializer();
		if (initializer != null) {
			if (!writingSourceFile)
				context.setPosition(ASTUtil.getEndPosition(initializer));
			else {
				context.copySpaceAndComments();
				context.matchAndWrite("=");

				context.copySpaceAndComments();
				getASTWriters().writeNode(initializer, context);
			}
		}
	}
}

package org.juniversal.translator.cplusplus.astwriters;

import org.juniversal.translator.core.Context;
import org.juniversal.translator.cplusplus.OutputType;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeDeclaration;


public class TypeDeclarationWriter extends ASTWriter {
	public TypeDeclarationWriter(ASTWriters astWriters) {
		super(astWriters);
	}

	public void write(ASTNode node, Context context) {
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;

		TypeDeclaration oldTypeDeclaration = context.getTypeDeclaration();
		context.setTypeDeclaration(typeDeclaration);

		if (context.getOutputType() == OutputType.HEADER)
			new WriteTypeDeclarationHeader(typeDeclaration, context, getASTWriters());
		else new WriteTypeDeclarationSource(typeDeclaration, getASTWriters(), context);

		context.setTypeDeclaration(oldTypeDeclaration);
	}
}

package org.juniversal.translator.swift.astwriters;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.juniversal.translator.core.Context;


public class TypeDeclarationWriter extends ASTWriter {
	public TypeDeclarationWriter(ASTWriters astWriters) {
		super(astWriters);
	}

	public void write(ASTNode node, Context context) {
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;

		TypeDeclaration oldTypeDeclaration = context.getTypeDeclaration();
		context.setTypeDeclaration(typeDeclaration);

		new WriteTypeDeclaration(typeDeclaration, context, getASTWriters());

		context.setTypeDeclaration(oldTypeDeclaration);
	}
}

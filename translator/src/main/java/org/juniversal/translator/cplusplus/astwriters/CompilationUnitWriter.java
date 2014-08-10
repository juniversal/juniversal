/*
 * Copyright (c) 2011-2014, Microsoft Mobile
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

package org.juniversal.translator.cplusplus.astwriters;
import org.juniversal.translator.core.ASTUtil;
import org.juniversal.translator.core.Context;
import org.juniversal.translator.cplusplus.OutputType;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;


public class CompilationUnitWriter extends ASTWriter {
	public CompilationUnitWriter(ASTWriters astWriters) {
		super(astWriters);
	}

	public void write(ASTNode node, Context context) {
		CompilationUnit compilationUnit = (CompilationUnit) node;

		TypeDeclaration mainTypeDeclaration = ASTUtil.getFirstTypeDeclaration(compilationUnit);

		context.setPosition(mainTypeDeclaration.getStartPosition());

		if (context.getOutputType() == OutputType.HEADER)
			writeHeader(compilationUnit, mainTypeDeclaration, context);
		else writeSource(compilationUnit, mainTypeDeclaration, context);
	}

	private void writeHeader(CompilationUnit compilationUnit, TypeDeclaration mainTypeDeclaration, Context context) {
		String name = mainTypeDeclaration.getName().getIdentifier();
		String multiIncludeDefine = name.toUpperCase() + "_H";
		Type superclassType = mainTypeDeclaration.getSuperclassType();

		context.writeln("#ifndef " + multiIncludeDefine);
		context.writeln("#define " + multiIncludeDefine);
		context.writeln();

		context.writeln("#include \"juniversal.h\"");
		if (superclassType != null) {
			if (superclassType instanceof SimpleType)
				ASTWriterUtil.writeIncludeForTypeName(((SimpleType) superclassType).getName(), context);
			else if (superclassType instanceof ParameterizedType) {
				// TODO: Finish this; make check for dependencies everywhere in all code via visitor
			}
		}
		context.writeln();

		context.writeln("namespace " + getPackageNamespaceName(compilationUnit) + " {");
		context.writeln("JU_USING_STD_NAMESPACES");
		context.writeln();

		// Copy class Javadoc or other comments before the class starts
		context.copySpaceAndComments();

		getASTWriters().writeNode(mainTypeDeclaration, context);

		context.copySpaceAndComments();

		context.writeln();
		context.writeln("}");   // Close namespace definition

		context.writeln("#endif // " + multiIncludeDefine);
	}

	private void writeSource(CompilationUnit compilationUnit, TypeDeclaration mainTypeDeclaration, Context context) {
		ASTWriterUtil.writeIncludeForTypeName(mainTypeDeclaration.getName(), context);
		context.writeln();
		
		context.writeln("JU_USING_STD_NAMESPACES");
		context.writeln("using namespace " + getPackageNamespaceName(compilationUnit) + ";");
		context.writeln();

		context.setPosition(mainTypeDeclaration.getStartPosition());
		context.skipSpaceAndComments();   // Skip any Javadoc included in the node

		getASTWriters().writeNode(mainTypeDeclaration, context);

		context.skipSpaceAndComments();
	}

	private String getPackageNamespaceName(CompilationUnit compilationUnit) {
		PackageDeclaration packageDeclaration = compilationUnit.getPackage();
		return ASTWriterUtil.getNamespaceNameForPackageName(packageDeclaration == null ? null : packageDeclaration
				.getName());
	}
}

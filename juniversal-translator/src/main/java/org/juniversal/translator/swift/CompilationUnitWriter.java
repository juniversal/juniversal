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

package org.juniversal.translator.swift;

import org.eclipse.jdt.core.dom.*;
import org.juniversal.translator.core.ASTUtil;
import org.juniversal.translator.core.JavaSourceContext;


public class CompilationUnitWriter extends SwiftASTNodeWriter {
    private SwiftTranslator swiftASTWriters;

    public CompilationUnitWriter(SwiftTranslator swiftASTWriters) {
        super(swiftASTWriters);
    }
    
    public void write(ASTNode node) {
		CompilationUnit compilationUnit = (CompilationUnit) node;

		AbstractTypeDeclaration mainTypeDeclaration = ASTUtil.getFirstTypeDeclaration(compilationUnit);

		setPosition(mainTypeDeclaration.getStartPosition());

/*
		if (context.getOutputType() == OutputType.HEADER_FILE)
			writeHeader(compilationUnit, mainTypeDeclaration, context);
		else writeSource(compilationUnit, mainTypeDeclaration, context);
*/
	}

	private void writeHeader(CompilationUnit compilationUnit, TypeDeclaration mainTypeDeclaration, JavaSourceContext javaSourceContext) {
		String name = mainTypeDeclaration.getName().getIdentifier();
		String multiIncludeDefine = name.toUpperCase() + "_H";
		Type superclassType = mainTypeDeclaration.getSuperclassType();

		writeln("#ifndef " + multiIncludeDefine);
		writeln("#define " + multiIncludeDefine);
		writeln();

		writeln("#include \"juniversal.h\"");
		if (superclassType != null) {
			if (superclassType instanceof SimpleType)
				//writeIncludeForTypeName(((SimpleType) superclassType).getName());
                ;
			else if (superclassType instanceof ParameterizedType) {
				// TODO: Finish this; make check for dependencies everywhere in all code via visitor
			}
		}
		writeln();

		writeln("namespace " + getPackageNamespaceName(compilationUnit) + " {");
		writeln("JU_USING_STD_NAMESPACES");
		writeln();

		// Copy class Javadoc or other comments before the class starts
		copySpaceAndComments();

        swiftASTWriters.writeNode(mainTypeDeclaration);

		copySpaceAndComments();

		writeln();
		writeln("}");   // Close namespace definition

		writeln("#endif // " + multiIncludeDefine);
	}

	private void writeSource(CompilationUnit compilationUnit, TypeDeclaration mainTypeDeclaration, JavaSourceContext javaSourceContext) {
		//writeIncludeForTypeName(mainTypeDeclaration.getName(), context);
		writeln();
		
		writeln("JU_USING_STD_NAMESPACES");
		writeln("using namespace " + getPackageNamespaceName(compilationUnit) + ";");
		writeln();

		setPosition(mainTypeDeclaration.getStartPosition());
		skipSpaceAndComments();   // Skip any Javadoc included in the node

        swiftASTWriters.writeNode(mainTypeDeclaration);

		skipSpaceAndComments();
	}

	private String getPackageNamespaceName(CompilationUnit compilationUnit) {
		PackageDeclaration packageDeclaration = compilationUnit.getPackage();
		return ASTWriterUtil.getNamespaceNameForPackageName(packageDeclaration == null ? null : packageDeclaration
				.getName());
	}
}

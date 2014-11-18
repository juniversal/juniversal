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

import java.util.List;

import org.juniversal.translator.core.ASTUtil;
import org.juniversal.translator.core.Context;

import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class WriteTypeDeclarationSource {
	private final CPlusPlusASTWriters astWriters;
	private final Context context;
	private boolean outputSomething;

	@SuppressWarnings("unchecked")
	public WriteTypeDeclarationSource(TypeDeclaration typeDeclaration, CPlusPlusASTWriters astWriters, Context context) {
		this.astWriters = astWriters;
		this.context = context;
		outputSomething = false;

		// Write the static fields, if any
		for (Object bodyDeclaration : typeDeclaration.bodyDeclarations()) {
			if (bodyDeclaration instanceof FieldDeclaration) {
				FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;

				if (!ASTUtil.containsStatic(fieldDeclaration.modifiers()))
					continue;

				// Skip any Javadoc comments for the field; field comments are just output in the
				// header
				context.setPositionToStartOfNode(fieldDeclaration);

				astWriters.writeNode(fieldDeclaration);
                context.writeln();
				outputSomething = true;
			}
		}

		for (BodyDeclaration bodyDeclaration : (List<BodyDeclaration>) typeDeclaration.bodyDeclarations()) {
			if (bodyDeclaration instanceof MethodDeclaration) {
				MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
				if (methodDeclaration.getBody() == null)
					continue;

				writeMethod(methodDeclaration);
			}
			else if (bodyDeclaration instanceof TypeDeclaration) {
				TypeDeclaration nestedTypeDeclaration = (TypeDeclaration) bodyDeclaration;

				writeNestedType(astWriters, context, nestedTypeDeclaration);
			}
		}

		context.setPosition(ASTUtil.getEndPosition(typeDeclaration));
	}

	private void writeNestedType(CPlusPlusASTWriters astWriters, Context context, TypeDeclaration nestedTypeDeclaration) {
		if (outputSomething)
            context.writeln(2);

        context.writeln("/**");
        context.writeln(" *    " + nestedTypeDeclaration.getName());
        context.writeln(" */");
        context.writeln();

		context.setPositionToStartOfNode(nestedTypeDeclaration);
		astWriters.writeNode(nestedTypeDeclaration);
		outputSomething = true;
	}

	private void writeMethod(MethodDeclaration methodDeclaration) {
		// We assume that the first non-whitespace text on the first line of the method
		// isn't indented at all--there's nothing in the method left of it. Unindent the
		// whole method by that amount, since methods aren't indented in the C++ source.
		CompilationUnit compilationUnit = context.getCompilationUnit();
		int methodLine = compilationUnit.getLineNumber(methodDeclaration.getStartPosition());
		int methodLineStartPosition = compilationUnit.getPosition(methodLine, 0);
		context.setPosition(methodLineStartPosition);
		context.skipSpacesAndTabs();
		int additionalIndent = context.getSourceLogicalColumn(context.getPosition());

		int previousIndent = context.getTargetWriter().setAdditionalIndentation(-1 * additionalIndent);

		context.setWritingMethodImplementation(true);

		// Skip back to the beginning of the comments, ignoring any comments associated with
		// the previous node
		context.setPositionToStartOfNodeSpaceAndComments(methodDeclaration);

		// If we haven't output anything yet, don't include the separator blank lines
		if (! outputSomething)
			context.skipBlankLines();

        context.copySpaceAndComments();
		astWriters.writeNode(methodDeclaration);

		context.setWritingMethodImplementation(false);
		context.getTargetWriter().setAdditionalIndentation(previousIndent);

        context.copySpaceAndCommentsUntilEOL();
        context.writeln();

		outputSomething = true;
	}
}

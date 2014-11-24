/*
 * Copyright (c) 2012-2014, Microsoft Mobile
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

package org.juniversal.translator.cplusplus;

import java.util.List;

import org.juniversal.translator.core.ASTUtil;
import org.juniversal.translator.core.Context;

import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class WriteTypeDeclarationSource {
	private final CPlusPlusSourceFileWriter sourceFileWriter;
	private final Context context;
	private boolean outputSomething;

	@SuppressWarnings("unchecked")
	public WriteTypeDeclarationSource(TypeDeclaration typeDeclaration, CPlusPlusSourceFileWriter sourceFileWriter, Context context) {
		this.sourceFileWriter = sourceFileWriter;
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
				sourceFileWriter.setPositionToStartOfNode(fieldDeclaration);

				sourceFileWriter.writeNode(fieldDeclaration);
				sourceFileWriter.writeln();
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

				writeNestedType(nestedTypeDeclaration);
			}
		}

		sourceFileWriter.setPosition(ASTUtil.getEndPosition(typeDeclaration));
	}

	private void writeNestedType(TypeDeclaration nestedTypeDeclaration) {
		if (outputSomething)
            sourceFileWriter.writeln(2);

        sourceFileWriter.writeln("/**");
        sourceFileWriter.writeln(" *    " + nestedTypeDeclaration.getName());
        sourceFileWriter.writeln(" */");
        sourceFileWriter.writeln();

		sourceFileWriter.setPositionToStartOfNode(nestedTypeDeclaration);
		sourceFileWriter.writeNode(nestedTypeDeclaration);
		outputSomething = true;
	}

	private void writeMethod(MethodDeclaration methodDeclaration) {
		// We assume that the first non-whitespace text on the first line of the method
		// isn't indented at all--there's nothing in the method left of it. Unindent the
		// whole method by that amount, since methods aren't indented in the C++ source.
		CompilationUnit compilationUnit = sourceFileWriter.getCompilationUnit();
		int methodLine = compilationUnit.getLineNumber(methodDeclaration.getStartPosition());
		int methodLineStartPosition = compilationUnit.getPosition(methodLine, 0);
		sourceFileWriter.setPosition(methodLineStartPosition);
		sourceFileWriter.skipSpacesAndTabs();
		int additionalIndent = sourceFileWriter.getSourceLogicalColumn(sourceFileWriter.getPosition());

		int previousIndent = sourceFileWriter.getTargetWriter().setAdditionalIndentation(-1 * additionalIndent);

		context.setWritingMethodImplementation(true);

		// Skip back to the beginning of the comments, ignoring any comments associated with
		// the previous node
		sourceFileWriter.setPositionToStartOfNodeSpaceAndComments(methodDeclaration);

		// If we haven't output anything yet, don't include the separator blank lines
		if (! outputSomething)
			sourceFileWriter.skipBlankLines();

        sourceFileWriter.copySpaceAndComments();
		sourceFileWriter.writeNode(methodDeclaration);

		context.setWritingMethodImplementation(false);
		sourceFileWriter.getTargetWriter().setAdditionalIndentation(previousIndent);

        sourceFileWriter.copySpaceAndCommentsUntilEOL();
        sourceFileWriter.writeln();

		outputSomething = true;
	}
}

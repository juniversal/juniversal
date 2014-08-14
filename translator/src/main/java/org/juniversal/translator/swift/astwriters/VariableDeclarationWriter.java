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

package org.juniversal.translator.swift.astwriters;

import org.eclipse.jdt.core.dom.*;
import org.juniversal.translator.core.ASTUtil;
import org.juniversal.translator.core.ASTWriter;
import org.juniversal.translator.core.Context;

import java.util.List;


public class VariableDeclarationWriter extends ASTWriter {
    private SwiftASTWriters swiftASTWriters;

    public VariableDeclarationWriter(SwiftASTWriters swiftASTWriters) {
        this.swiftASTWriters = swiftASTWriters;
    }

    @Override
	public void write(ASTNode node, Context context) {
		// Variable declaration statements & expressions are quite similar, so we handle them both
		// here together

		if (node instanceof VariableDeclarationStatement) {
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) node;

			writeVariableDeclaration(variableDeclarationStatement.modifiers(), variableDeclarationStatement.getType(),
					variableDeclarationStatement.fragments(), context);
			context.copySpaceAndComments();

			context.matchAndWrite(";");
		} else {
			VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) node;

			writeVariableDeclaration(variableDeclarationExpression.modifiers(),
					variableDeclarationExpression.getType(), variableDeclarationExpression.fragments(), context);
		}
	}

	private void writeVariableDeclaration(List<?> modifiers, Type type, List<?> fragments, Context context) {
		// Turn "final" into "const"
		if (ASTUtil.containsFinal(modifiers)) {
			context.write("const");
			context.skipModifiers(modifiers);

			context.copySpaceAndComments();
		}

		// Write the type
        swiftASTWriters.writeType(type, context, false);

		boolean needStar = false;
		context.setWritingVariableDeclarationNeedingStar(needStar);

		// Write the variable declaration(s)
		boolean first = true;
		for (Object fragment : fragments) {
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragment;

			context.copySpaceAndComments();
			if (!first) {
				context.matchAndWrite(",");
				context.copySpaceAndComments();
			}
            swiftASTWriters.writeNode(variableDeclarationFragment, context);

			first = false;
		}

		context.setWritingVariableDeclarationNeedingStar(false);
	}
}

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

import org.juniversal.translator.core.Context;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;


public class ForStatementWriter extends ASTWriter {
	public ForStatementWriter(ASTWriters astWriters) {
		super(astWriters);
	}

	@Override
	public void write(ASTNode node, Context context) {
		ForStatement forStatement = (ForStatement) node;

		context.matchAndWrite("for");
		context.copySpaceAndComments();

		context.matchAndWrite("(");
		context.copySpaceAndComments();

		boolean first = true;
		for (Object initializerExpressionObject : forStatement.initializers()) {
			Expression initializerExpression = (Expression) initializerExpressionObject;

			if (! first) {
				context.matchAndWrite(",");
				context.copySpaceAndComments();
			}

			getASTWriters().writeNode(initializerExpression, context);
			context.copySpaceAndComments();

			first = false;
		}

		context.matchAndWrite(";");
		context.copySpaceAndComments();

		Expression forExpression = forStatement.getExpression();
		if (forExpression != null) {
			getASTWriters().writeNode(forStatement.getExpression(), context);
			context.copySpaceAndComments();
		}

		context.matchAndWrite(";");
		context.copySpaceAndComments();

		first = true;
		for (Object updaterExpressionObject : forStatement.updaters()) {
			Expression updaterExpression = (Expression) updaterExpressionObject;

			if (! first) {
				context.matchAndWrite(",");
				context.copySpaceAndComments();
			}

			getASTWriters().writeNode(updaterExpression, context);
			context.copySpaceAndComments();

			first = false;
		}

		context.matchAndWrite(")");
		context.copySpaceAndComments();

		getASTWriters().writeNode(forStatement.getBody(), context);
	}
}

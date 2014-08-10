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

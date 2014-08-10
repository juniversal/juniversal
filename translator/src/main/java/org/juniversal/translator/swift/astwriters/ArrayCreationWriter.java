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
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Expression;
import org.juniversal.translator.core.ASTUtil;
import org.juniversal.translator.core.Context;
import org.juniversal.translator.core.JUniversalException;

import java.util.List;


public class ArrayCreationWriter extends ASTWriter {
	public ArrayCreationWriter(ASTWriters astWriters) {
		super(astWriters);
	}

	@Override
	public void write(ASTNode node, Context context) {
		ArrayCreation arrayCreation = (ArrayCreation) node;

		context.matchAndWrite("new");

		List<?> dimensions = arrayCreation.dimensions();
		// TODO: Support multidimensional arrays
		if (dimensions.size() > 1)
			throw new JUniversalException("Multidimensional arrays not currently supported");

		// TODO: Support array initializers
		if (arrayCreation.getInitializer() != null)
			throw new JUniversalException("Array initializers not currently supported");

		Expression dimensionSizeExpression = (Expression) dimensions.get(0);

		context.setPosition(dimensionSizeExpression.getStartPosition());

		context.write("(");
		getASTWriters().writeNode(dimensionSizeExpression, context);
		context.copySpaceAndComments();
		context.write(") ");

		ArrayType arrayType = arrayCreation.getType();
		context.setPosition(arrayType.getStartPosition());

		context.write("Array<");
		getASTWriters().writeNode(arrayType.getElementType(), context);
		context.skipSpaceAndComments();
		context.write(">");

		context.setPosition(ASTUtil.getEndPosition(dimensionSizeExpression));
		context.skipSpaceAndComments();
		context.match("]");
	}
}

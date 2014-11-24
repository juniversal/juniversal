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

package org.juniversal.translator.swift.astwriters;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

import java.util.List;


public class ClassInstanceCreationWriter extends SwiftASTNodeWriter {
    private SwiftSourceFileWriter swiftASTWriters;

    public ClassInstanceCreationWriter(SwiftSourceFileWriter swiftASTWriters) {
        super(swiftASTWriters);
    }

    @Override
	public void write(ASTNode node) {
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) node;

		//TODO: Handle type arguments
		//TODO: Handle different reference operator used for stack objects

		// TODO: Support inner class creation via object.new
		if (classInstanceCreation.getExpression() != null)
			throw sourceNotSupported("Inner classes not yet supported");

		matchAndWrite("new");
		copySpaceAndComments();

        swiftASTWriters.writeNode(classInstanceCreation.getType());
		copySpaceAndComments();

		matchAndWrite("(");
		copySpaceAndComments();

		List<?> arguments = classInstanceCreation.arguments();

		boolean first = true;
		for (Object object : arguments) {
			Expression argument = (Expression) object;

			if (! first) {
				matchAndWrite(",");
				copySpaceAndComments();
			}

            swiftASTWriters.writeNode(argument);
			copySpaceAndComments();

			first = false;
		}

		matchAndWrite(")");
	}
}

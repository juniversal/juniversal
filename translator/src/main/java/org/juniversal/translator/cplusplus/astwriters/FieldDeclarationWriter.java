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
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;


public class FieldDeclarationWriter extends CPlusPlusASTWriter {
    public FieldDeclarationWriter(CPlusPlusASTWriters cPlusPlusASTWriters) {
        super(cPlusPlusASTWriters);
    }

    @Override
	public void write(Context context, ASTNode node) {
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;

		// TODO: Handle final/const

		boolean isStatic = ASTUtil.containsStatic(fieldDeclaration.modifiers());

		if (context.getOutputType() == OutputType.HEADER && isStatic)
			context.write("static ");
		context.skipModifiers(fieldDeclaration.modifiers());

		// Write the type
		context.skipSpaceAndComments();
        writeType(fieldDeclaration.getType(), context, false);

		boolean first = true;
		for (Object fragment : fieldDeclaration.fragments()) {
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragment;

			if (! first) {
				context.copySpaceAndComments();
				context.matchAndWrite(",");
			}

			context.copySpaceAndComments();
			writeVariableDeclarationFragment(variableDeclarationFragment, context,
					context.getOutputType() == OutputType.SOURCE);

			first = false;
		}

		context.copySpaceAndComments();
		context.matchAndWrite(";");
	}

	private void writeVariableDeclarationFragment(VariableDeclarationFragment variableDeclarationFragment,
			Context context, boolean writingSourceFile) {

		// TODO: Handle syntax with extra dimensions on array
		if (variableDeclarationFragment.getExtraDimensions() > 0)
			context.throwSourceNotSupported("\"int foo[]\" syntax not currently supported; use \"int[] foo\" instead");

		if (context.isWritingVariableDeclarationNeedingStar())
			context.write("*");

		if (writingSourceFile)
			context.write(context.getTypeDeclaration().getName().getIdentifier() + "::");
        writeNode(context, variableDeclarationFragment.getName());

		// Only write out the initializer when writing to the source file; in that case the field
		// must be static
		Expression initializer = variableDeclarationFragment.getInitializer();
		if (initializer != null) {
			if (!writingSourceFile)
				context.setPosition(ASTUtil.getEndPosition(initializer));
			else {
				context.copySpaceAndComments();
				context.matchAndWrite("=");

				context.copySpaceAndComments();
                writeNode(context, initializer);
			}
		}
	}
}

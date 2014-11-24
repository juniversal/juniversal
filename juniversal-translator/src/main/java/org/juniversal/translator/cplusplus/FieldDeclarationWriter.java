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

import org.juniversal.translator.core.ASTUtil;
import org.juniversal.translator.cplusplus.OutputType;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;


public class FieldDeclarationWriter extends CPlusPlusASTNodeWriter {
    public FieldDeclarationWriter(CPlusPlusSourceFileWriter cPlusPlusASTWriters) {
        super(cPlusPlusASTWriters);
    }

    @Override
	public void write(ASTNode node) {
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;

		// TODO: Handle final/const

		boolean isStatic = ASTUtil.containsStatic(fieldDeclaration.modifiers());

		if (getContext().getOutputType() == OutputType.HEADER && isStatic)
			write("static ");
		skipModifiers(fieldDeclaration.modifiers());

		// Write the type
		skipSpaceAndComments();
        writeType(fieldDeclaration.getType(), false);

		boolean first = true;
		for (Object fragment : fieldDeclaration.fragments()) {
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragment;

			if (! first) {
				copySpaceAndComments();
				matchAndWrite(",");
			}

			copySpaceAndComments();
			writeVariableDeclarationFragment(variableDeclarationFragment,
                    getContext().getOutputType() == OutputType.SOURCE);

			first = false;
		}

		copySpaceAndComments();
		matchAndWrite(";");
	}

	private void writeVariableDeclarationFragment(VariableDeclarationFragment variableDeclarationFragment,
                                                  boolean writingSourceFile) {

		// TODO: Handle syntax with extra dimensions on array
		if (variableDeclarationFragment.getExtraDimensions() > 0)
			throw sourceNotSupported("\"int foo[]\" syntax not currently supported; use \"int[] foo\" instead");

		if (getContext().isWritingVariableDeclarationNeedingStar())
			write("*");

		if (writingSourceFile)
			write(getContext().getTypeDeclaration().getName().getIdentifier() + "::");
        writeNode(variableDeclarationFragment.getName());

		// Only write out the initializer when writing to the source file; in that case the field
		// must be static
		Expression initializer = variableDeclarationFragment.getInitializer();
		if (initializer != null) {
			if (!writingSourceFile)
				setPosition(ASTUtil.getEndPosition(initializer));
			else {
				copySpaceAndComments();
				matchAndWrite("=");

				copySpaceAndComments();
                writeNode(initializer);
			}
		}
	}
}

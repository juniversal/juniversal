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

package org.juniversal.translator.csharp.astwriters;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.juniversal.translator.core.Context;

import java.util.List;


// TODO: Finish this
public class FieldDeclarationWriter extends CSharpASTNodeWriter<FieldDeclaration> {
    public FieldDeclarationWriter(CSharpSourceFileWriter cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override
    public void write(FieldDeclaration fieldDeclaration) {
        // TODO: Handle final/const

        List<?> modifiers = fieldDeclaration.modifiers();
        boolean[] wroteModifier = new boolean[1];

        writeAccessModifier(modifiers);

        // Skip the modifiers
        skipModifiers(modifiers);
        skipSpaceAndComments();

        // Write the type
        if (wroteModifier[0])
            write(" ");
        writeNode(fieldDeclaration.getType());

        writeCommaDelimitedNodes(fieldDeclaration.fragments());

        copySpaceAndComments();
        matchAndWrite(";");
    }

    private void writeVariableDeclarationFragment(Context context, VariableDeclarationFragment variableDeclarationFragment) {
        // TODO: Check for syntax with extra dimensions on array
        // TODO: Handle check for int foo[] syntax instead of int[] foo

        writeNode(variableDeclarationFragment.getName());

        // Only write out the initializer when writing to the source file; in that case the field must be static
        Expression initializer = variableDeclarationFragment.getInitializer();
        if (initializer != null) {
            copySpaceAndComments();
            matchAndWrite("=");

            copySpaceAndComments();
            writeNode(initializer);
        }
    }
}

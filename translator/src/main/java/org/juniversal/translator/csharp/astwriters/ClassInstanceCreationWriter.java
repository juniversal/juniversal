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

package org.juniversal.translator.csharp.astwriters;

import org.eclipse.jdt.core.dom.*;

import static org.juniversal.translator.core.ASTUtil.*;


public class ClassInstanceCreationWriter extends CSharpASTWriter<ClassInstanceCreation> {
    public ClassInstanceCreationWriter(CSharpASTWriters cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override public void write(ClassInstanceCreation classInstanceCreation) {
        //TODO: Handle type arguments

        // TODO: Support inner class creation via object.new
        if (classInstanceCreation.getExpression() != null)
            throw sourceNotSupported("Inner classes not yet supported");

        if (classInstanceCreation.getAnonymousClassDeclaration() != null) {
            writeAnonymousInnerClassFunction(classInstanceCreation);
        } else {
            matchAndWrite("new");

            copySpaceAndComments();
            writeNode(classInstanceCreation.getType());

            copySpaceAndComments();
            matchAndWrite("(");

            writeCommaDelimitedNodes(classInstanceCreation.arguments());

            if (classInstanceCreation.getAnonymousClassDeclaration() != null) {
                throw sourceNotSupported("Anonymous classes aren't yet supported");
            }

            copySpaceAndComments();
            matchAndWrite(")");
        }
    }

    private void writeAnonymousInnerClassFunction(ClassInstanceCreation classInstanceCreation) {
        Type type = classInstanceCreation.getType();

        if (! isFunctionalInterfaceImplementation(getContext(), type))
            throw sourceNotSupported("Anonymous inner classes are only supported when they implement a functional interface--an interface with a single abstract method and no constants");

        MethodDeclaration functionalMethod = (MethodDeclaration) classInstanceCreation.getAnonymousClassDeclaration().bodyDeclarations().get(0);

        write("(");
        boolean first = true;
        for (Object parameterObject : functionalMethod.parameters()) {
            SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameterObject;

            if (! first)
                write(", ");
            write(parameter.getName().getIdentifier());
            first = false;
        }
        write(") => ");

        setPosition(functionalMethod.getBody().getStartPosition());
        writeNode(functionalMethod.getBody());

        setPositionToEndOfNode(classInstanceCreation);
    }
}

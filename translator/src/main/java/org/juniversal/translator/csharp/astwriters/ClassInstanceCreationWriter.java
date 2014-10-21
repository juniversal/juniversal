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
import org.juniversal.translator.core.Context;

import java.util.List;

import static org.juniversal.translator.core.ASTUtil.*;


public class ClassInstanceCreationWriter extends CSharpASTWriter<ClassInstanceCreation> {
    public ClassInstanceCreationWriter(CSharpASTWriters cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override public void write(Context context, ClassInstanceCreation classInstanceCreation) {
        //TODO: Handle type arguments

        // TODO: Support inner class creation via object.new
        if (classInstanceCreation.getExpression() != null)
            context.throwSourceNotSupported("Inner classes not yet supported");

        if (classInstanceCreation.getAnonymousClassDeclaration() != null) {
            writeAnonymousInnerClassFunction(context, classInstanceCreation);
        } else {
            context.matchAndWrite("new");

            context.copySpaceAndComments();
            writeNode(context, classInstanceCreation.getType());

            context.copySpaceAndComments();
            context.matchAndWrite("(");

            writeCommaDelimitedNodes(context, classInstanceCreation.arguments());

            if (classInstanceCreation.getAnonymousClassDeclaration() != null) {
                context.throwSourceNotSupported("Anonymous classes aren't yet supported");
            }

            context.copySpaceAndComments();
            context.matchAndWrite(")");
        }
    }

    private void writeAnonymousInnerClassFunction(Context context, ClassInstanceCreation classInstanceCreation) {
        Type type = classInstanceCreation.getType();

        if (! isFunctionalInterfaceImplementation(context, type))
            context.throwSourceNotSupported("Anonymous inner classes are only supported when they implement a functional interface--an interface with a single abstract method and no constants");

        MethodDeclaration functionalMethod = (MethodDeclaration) classInstanceCreation.getAnonymousClassDeclaration().bodyDeclarations().get(0);

        context.write("(");
        boolean first = true;
        for (Object parameterObject : functionalMethod.parameters()) {
            SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameterObject;

            if (! first)
                context.write(", ");
            context.write(parameter.getName().getIdentifier());
            first = false;
        }
        context.write(") => ");

        context.setPosition(functionalMethod.getBody().getStartPosition());
        writeNode(context, functionalMethod.getBody());

        context.setPositionToEndOfNode(classInstanceCreation);
    }
}

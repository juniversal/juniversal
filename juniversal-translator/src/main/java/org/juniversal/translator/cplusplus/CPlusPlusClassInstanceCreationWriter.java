/*
 * Copyright (c) 2012-2015, Microsoft Mobile
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

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.juniversal.translator.core.ClassInstanceCreationWriter;

import static org.juniversal.translator.core.ASTUtil.forEach;


public class CPlusPlusClassInstanceCreationWriter extends ClassInstanceCreationWriter {
    public CPlusPlusClassInstanceCreationWriter(CPlusPlusTranslator translator) {
        super(translator);
    }

    @Override protected void writeAnonymousInnerClassFunction(ClassInstanceCreation classInstanceCreation) {
        MethodDeclaration functionalMethod = (MethodDeclaration) classInstanceCreation.getAnonymousClassDeclaration().bodyDeclarations().get(0);

        // TODO: Fix this up to only use auto when we think it'll work, to pay attention to pass/return by reference
        // for auto, and to include right captures

        write("(");
        forEach(functionalMethod.parameters(), (SingleVariableDeclaration parameter, boolean first) -> {
            if (! first)
                write(", ");
            write("auto ");
            write(parameter.getName().getIdentifier());
        });
        write(") -> auto");

        setPosition(functionalMethod.getBody().getStartPosition());
        writeNode(functionalMethod.getBody());

        setPositionToEndOfNode(classInstanceCreation);
    }
}

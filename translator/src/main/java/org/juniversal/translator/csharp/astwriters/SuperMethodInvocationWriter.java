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

import org.eclipse.jdt.core.dom.SuperMethodInvocation;

public class SuperMethodInvocationWriter extends MethodInvocationWriterBase<SuperMethodInvocation> {
    public SuperMethodInvocationWriter(CSharpASTWriters cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override
    public void write(SuperMethodInvocation superMethodInvocation) {
        // TODO: Support this
        if (superMethodInvocation.getQualifier() != null)
            throw sourceNotSupported("Qualified super invocations aren't currently supported");

        matchAndWrite("super", "base");

        copySpaceAndComments();
        matchAndWrite(".");

        copySpaceAndComments();
        writeMethodInvocation(superMethodInvocation, superMethodInvocation.getName().getIdentifier(),
                superMethodInvocation.typeArguments(), superMethodInvocation.arguments(),
                superMethodInvocation.resolveMethodBinding());
    }
}

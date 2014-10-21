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

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Expression;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.ASTUtil;
import org.juniversal.translator.core.Context;
import org.juniversal.translator.core.JUniversalException;

import java.util.List;


public class ArrayCreationWriter extends CSharpASTWriter<ArrayCreation> {
    public ArrayCreationWriter(CSharpASTWriters cPlusPlusASTWriters) {
        super(cPlusPlusASTWriters);
    }

    @Override
    public void write(Context context, ArrayCreation arrayCreation) {
        List<?> dimensions = arrayCreation.dimensions();
        // TODO: Support multidimensional arrays
        if (dimensions.size() > 1)
            throw new JUniversalException("Multidimensional arrays not currently supported");

        context.matchAndWrite("new");

        context.copySpaceAndComments();
        writeNode(context, arrayCreation.getType().getElementType());

        context.copySpaceAndComments();
        context.matchAndWrite("[");

        writeNodes(context, arrayCreation.dimensions());

        context.copySpaceAndComments();
        context.matchAndWrite("]");

        // TODO: Check all syntax combinations here
        @Nullable ArrayInitializer arrayInitializer = arrayCreation.getInitializer();
        if (arrayInitializer != null) {
            context.copySpaceAndComments();
            writeNode(context, arrayInitializer);
        }
    }
}

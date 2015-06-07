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

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Expression;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.JUniversalException;
import org.xuniversal.translator.cplusplus.ReferenceKind;

import java.util.List;


public class ArrayCreationWriter extends CPlusPlusASTNodeWriter<ArrayCreation> {
    public ArrayCreationWriter(CPlusPlusTranslator cPlusPlusASTWriters) {
        super(cPlusPlusASTWriters);
    }

    @Override
    public void write(ArrayCreation arrayCreation) {
        @Nullable ArrayInitializer arrayInitializer = arrayCreation.getInitializer();

        List<?> dimensions = arrayCreation.dimensions();
        // TODO: Support multidimensional arrays
        if (dimensions.size() > 1)
            throw new JUniversalException("Multidimensional arrays not currently supported");

/*
        if (dimensions.length() == 1) {
            Expression dimensionSizeExpression = (Expression) dimensions.get(0);

            setPosition(dimensionSizeExpression.getStartPosition());

            write("(");
            writeNode(dimensionSizeExpression);
            copySpaceAndComments();
            write(") ");
        }
        else {
            if (arrayInitializer == null)
                throw sourceNotSupported("Unexpectedly, neither an array length nor an array initializer is specified");

            int length = arrayInitializer.expressions().length();

            write("(");
            write(Integer.toString(length));
            write(") ");
        }
*/

        ArrayType arrayType = arrayCreation.getType();

        matchAndWrite("new", "xuniv::Array<");

        skipSpaceAndComments();
        writeTypeReference(arrayType.getElementType(), ReferenceKind.SharedPtr);

        skipSpaceAndComments();
        write(">::make");

        if (dimensions.size() == 1) {
            matchAndWrite("[", "(");

            copySpaceAndComments();
            writeNode((Expression) dimensions.get(0));

            copySpaceAndComments();
            matchAndWrite("]", ")");
        }
        else {
            if (arrayInitializer == null)
                throw sourceNotSupported("Unexpectedly, neither an array size nor an array initializer is specified");

            match("[");

            skipSpaceAndComments();
            int size = arrayInitializer.expressions().size();
            write(Integer.toString(size));

            skipSpaceAndComments();
            matchAndWrite("]", ")");
        }

        // TODO: Check all syntax combinations here
        if (arrayInitializer != null) {
            write(" = ");

            copySpaceAndComments();
            writeNode(arrayInitializer);
        }
    }
}

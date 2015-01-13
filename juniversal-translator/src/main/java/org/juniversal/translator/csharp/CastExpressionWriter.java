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

package org.juniversal.translator.csharp;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.juniversal.translator.core.ASTUtil.isGenericArrayCreation;
import static org.juniversal.translator.core.ASTUtil.isNumericPrimitiveType;

/**
 * Created by Bret on 12/31/2014.
 */
public class CastExpressionWriter extends CSharpASTNodeWriter<CastExpression> {
    public CastExpressionWriter(CSharpSourceFileWriter cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override public void write(CastExpression castExpression) {
        Expression expression = castExpression.getExpression();
        Type type = castExpression.getType();

        if (isGenericArrayCreation(castExpression))
            writeGenericArrayCreation((ArrayType) type, (ArrayCreation) expression);
        else {
            boolean makeUncheckedCast = isNumericPrimitiveType(type) && expression instanceof NumberLiteral;

            if (makeUncheckedCast)
                write("unchecked(");

            matchAndWrite("(");

            copySpaceAndComments();
            writeNode(type);

            copySpaceAndComments();
            matchAndWrite(")");

            copySpaceAndComments();
            writeNode(expression);

            if (makeUncheckedCast)
                write(")");
        }
    }

    private void writeGenericArrayCreation(ArrayType type, ArrayCreation arrayCreation) {
        List<?> dimensions = arrayCreation.dimensions();
        // TODO: Support multidimensional arrays
        if (dimensions.size() > 1)
            throw sourceNotSupported("Multidimensional arrays not currently supported");

        setPositionToStartOfNode(arrayCreation);
        matchAndWrite("new");

        // Skip the non-generic type in the new expression & write the type from the cast expression instead
        copySpaceAndComments();
        setPositionToEndOfNode(arrayCreation.getType().getElementType());
        writeNodeAtDifferentPosition(type.getElementType());

        copySpaceAndComments();
        matchAndWrite("[");

        writeNodes(arrayCreation.dimensions());

        copySpaceAndComments();
        matchAndWrite("]");

        // TODO: Check all syntax combinations here
        @Nullable ArrayInitializer arrayInitializer = arrayCreation.getInitializer();
        if (arrayInitializer != null) {
            copySpaceAndComments();
            writeNode(arrayInitializer);
        }
    }
}


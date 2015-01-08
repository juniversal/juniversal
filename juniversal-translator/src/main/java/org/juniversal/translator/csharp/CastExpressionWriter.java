package org.juniversal.translator.csharp;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.juniversal.translator.core.ASTUtil.isGenericArrayCreation;

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
            matchAndWrite("(");

            copySpaceAndComments();
            writeNode(type);

            copySpaceAndComments();
            matchAndWrite(")");

            copySpaceAndComments();
            writeNode(expression);
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


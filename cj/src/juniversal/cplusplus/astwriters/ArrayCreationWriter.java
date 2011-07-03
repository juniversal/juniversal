package juniversal.cplusplus.astwriters;

import java.util.List;

import juniversal.ASTUtil;
import juniversal.CJException;
import juniversal.cplusplus.Context;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Expression;


public class ArrayCreationWriter extends ASTWriter {
	public ArrayCreationWriter(ASTWriters astWriters) {
		super(astWriters);
	}

	@Override
	public void write(ASTNode node, Context context) {
		ArrayCreation arrayCreation = (ArrayCreation) node;

		context.matchAndWrite("new");

		List<?> dimensions = arrayCreation.dimensions();
		// TODO: Support multidimensional arrays
		if (dimensions.size() > 1)
			throw new CJException("Multidimensional arrays not currently supported");
		Expression dimensionSizeExpression = (Expression) dimensions.get(0);

		// TODO: Support array initializers
		if (arrayCreation.getInitializer() != null)
			throw new CJException("Array initializers not currently supported");

		context.setPosition(dimensionSizeExpression.getStartPosition());

		context.write("(");
		getASTWriters().writeNode(dimensionSizeExpression, context);
		context.copySpaceAndComments();
		context.write(") ");

		ArrayType arrayType = arrayCreation.getType();
		context.setPosition(arrayType.getStartPosition());

		context.write("array<");
		getASTWriters().writeNode(arrayType.getComponentType(), context);
		context.skipSpaceAndComments();
		context.write(">");

		context.setPosition(ASTUtil.getEndPosition(dimensionSizeExpression));
		context.skipSpaceAndComments();
		context.match("]");
	}
}

package org.juniversal.translator.cplusplus.astwriters;

import org.juniversal.translator.core.Context;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;


public class ForStatementWriter extends ASTWriter {
	public ForStatementWriter(ASTWriters astWriters) {
		super(astWriters);
	}

	@Override
	public void write(ASTNode node, Context context) {
		ForStatement forStatement = (ForStatement) node;

		context.matchAndWrite("for");
		context.copySpaceAndComments();

		context.matchAndWrite("(");
		context.copySpaceAndComments();

		boolean first = true;
		for (Object initializerExpressionObject : forStatement.initializers()) {
			Expression initializerExpression = (Expression) initializerExpressionObject;

			if (! first) {
				context.matchAndWrite(",");
				context.copySpaceAndComments();
			}

			getASTWriters().writeNode(initializerExpression, context);
			context.copySpaceAndComments();

			first = false;
		}

		context.matchAndWrite(";");
		context.copySpaceAndComments();

		Expression forExpression = forStatement.getExpression();
		if (forExpression != null) {
			getASTWriters().writeNode(forStatement.getExpression(), context);
			context.copySpaceAndComments();
		}

		context.matchAndWrite(";");
		context.copySpaceAndComments();

		first = true;
		for (Object updaterExpressionObject : forStatement.updaters()) {
			Expression updaterExpression = (Expression) updaterExpressionObject;

			if (! first) {
				context.matchAndWrite(",");
				context.copySpaceAndComments();
			}

			getASTWriters().writeNode(updaterExpression, context);
			context.copySpaceAndComments();

			first = false;
		}

		context.matchAndWrite(")");
		context.copySpaceAndComments();

		getASTWriters().writeNode(forStatement.getBody(), context);
	}
}

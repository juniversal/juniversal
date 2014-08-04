package org.juniversal.translator.swift.astwriters;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.juniversal.translator.core.Context;


public class AssignmentWriter extends ASTWriter {
	public AssignmentWriter(ASTWriters astWriters) {
		super(astWriters);
	}

	@Override
	public void write(ASTNode node, Context context) {
		Assignment assignment = (Assignment) node;
		
		Assignment.Operator operator = assignment.getOperator();
		
		if (operator == Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN) {
			getASTWriters().writeNode(assignment.getLeftHandSide(), context);

			context.copySpaceAndComments();
			context.matchAndWrite(">>>=", "=");

			context.copySpaceAndComments();
			context.write("rightShiftUnsigned(");
			getASTWriters().writeNodeAtDifferentPosition(assignment.getLeftHandSide(), context);
			context.write(", ");
			getASTWriters().writeNode(assignment.getRightHandSide(), context);
			context.write(")");
		}
		else {
			getASTWriters().writeNode(assignment.getLeftHandSide(), context);

			context.copySpaceAndComments();
			if (operator == Assignment.Operator.ASSIGN)
				context.matchAndWrite("=");
			else if (operator == Assignment.Operator.PLUS_ASSIGN)
				context.matchAndWrite("+=");
			else if (operator == Assignment.Operator.MINUS_ASSIGN)
				context.matchAndWrite("-=");
			else if (operator == Assignment.Operator.TIMES_ASSIGN)
				context.matchAndWrite("*=");
			else if (operator == Assignment.Operator.DIVIDE_ASSIGN)
				context.matchAndWrite("/=");
			else if (operator == Assignment.Operator.BIT_AND_ASSIGN)
				context.matchAndWrite("&=");
			else if (operator == Assignment.Operator.BIT_OR_ASSIGN)
				context.matchAndWrite("|=");
			else if (operator == Assignment.Operator.BIT_XOR_ASSIGN)
				context.matchAndWrite("^=");
			else if (operator == Assignment.Operator.REMAINDER_ASSIGN)
				context.matchAndWrite("%=");
			else if (operator == Assignment.Operator.LEFT_SHIFT_ASSIGN)
				context.matchAndWrite("<<=");
			else if (operator == Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN)
				context.matchAndWrite(">>=");
			else if (operator == Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN)
				context.matchAndWrite(">>>=");
	
			context.copySpaceAndComments();
			getASTWriters().writeNode(assignment.getRightHandSide(), context);
		}
	}
}

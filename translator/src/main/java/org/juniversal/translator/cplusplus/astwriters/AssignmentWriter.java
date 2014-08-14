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

package org.juniversal.translator.cplusplus.astwriters;

import org.juniversal.translator.core.ASTWriter;
import org.juniversal.translator.core.Context;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;


public class AssignmentWriter extends ASTWriter {
    private CPlusPlusASTWriters cPlusPlusASTWriters;

    public AssignmentWriter(CPlusPlusASTWriters cPlusPlusASTWriters) {
        this.cPlusPlusASTWriters = cPlusPlusASTWriters;
    }

    @Override
	public void write(ASTNode node, Context context) {
		Assignment assignment = (Assignment) node;
		
		Assignment.Operator operator = assignment.getOperator();
		
		if (operator == Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN) {
            cPlusPlusASTWriters.writeNode(assignment.getLeftHandSide(), context);

			context.copySpaceAndComments();
			context.matchAndWrite(">>>=", "=");

			context.copySpaceAndComments();
			context.write("rightShiftUnsigned(");
            cPlusPlusASTWriters.writeNodeAtDifferentPosition(assignment.getLeftHandSide(), context);
			context.write(", ");
            cPlusPlusASTWriters.writeNode(assignment.getRightHandSide(), context);
			context.write(")");
		}
		else {
            cPlusPlusASTWriters.writeNode(assignment.getLeftHandSide(), context);

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
            cPlusPlusASTWriters.writeNode(assignment.getRightHandSide(), context);
		}
	}
}

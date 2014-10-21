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

import java.util.HashMap;
import java.util.List;

import org.juniversal.translator.core.Context;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;


public class InfixExpressionWriter extends CPlusPlusASTWriter {
    private CPlusPlusASTWriters cPlusPlusASTWriters;
	private HashMap<InfixExpression.Operator, String> equivalentOperators;  // Operators that have the same token in both Java & C++


	public InfixExpressionWriter(CPlusPlusASTWriters cPlusPlusASTWriters) {
		super(cPlusPlusASTWriters);

		equivalentOperators = new HashMap<>();
		equivalentOperators.put(InfixExpression.Operator.TIMES, "*");
		equivalentOperators.put(InfixExpression.Operator.DIVIDE, "/");
		equivalentOperators.put(InfixExpression.Operator.REMAINDER, "%");
		equivalentOperators.put(InfixExpression.Operator.PLUS, "+");
		equivalentOperators.put(InfixExpression.Operator.MINUS, "-");

		// TODO: Test signed / unsigned semantics here
		equivalentOperators.put(InfixExpression.Operator.LEFT_SHIFT, "<<");
		equivalentOperators.put(InfixExpression.Operator.RIGHT_SHIFT_SIGNED, ">>");
		//cppOperators.put(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED, "==");

		equivalentOperators.put(InfixExpression.Operator.LESS, "<");
		equivalentOperators.put(InfixExpression.Operator.GREATER, ">");
		equivalentOperators.put(InfixExpression.Operator.LESS_EQUALS, "<=");
		equivalentOperators.put(InfixExpression.Operator.GREATER_EQUALS, ">=");
		equivalentOperators.put(InfixExpression.Operator.EQUALS, "==");
		equivalentOperators.put(InfixExpression.Operator.NOT_EQUALS, "!=");

		equivalentOperators.put(InfixExpression.Operator.XOR, "^");
		equivalentOperators.put(InfixExpression.Operator.AND, "&");
		equivalentOperators.put(InfixExpression.Operator.OR, "|");

		equivalentOperators.put(InfixExpression.Operator.CONDITIONAL_AND, "&&");
		equivalentOperators.put(InfixExpression.Operator.CONDITIONAL_OR, "||");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void write(Context context, ASTNode node) {
		InfixExpression infixExpression = (InfixExpression) node;
		
		InfixExpression.Operator operator = infixExpression.getOperator();

		if (operator == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED) {
			context.write("rightShiftUnsigned(");
            writeNode(context, infixExpression.getLeftOperand());

			// Skip spaces before the >>> but if there's a newline (or comments) there, copy them
			context.skipSpacesAndTabs();
			context.copySpaceAndComments();
			context.matchAndWrite(">>>", ",");

			context.copySpaceAndComments();
            writeNode(context, infixExpression.getRightOperand());
			context.write(")");
		}
		else {
            writeNode(context, infixExpression.getLeftOperand());

			context.copySpaceAndComments();
			String operatorToken = this.equivalentOperators.get(infixExpression.getOperator());
			context.matchAndWrite(operatorToken);
	
			context.copySpaceAndComments();
            writeNode(context, infixExpression.getRightOperand());

			if (infixExpression.hasExtendedOperands()) {
				for (Expression extendedOperand : (List<Expression>) infixExpression.extendedOperands()) {
					
					context.copySpaceAndComments();
					context.matchAndWrite(operatorToken);
	
					context.copySpaceAndComments();
                    writeNode(context, extendedOperand);
				}
			}
		}
	}
}

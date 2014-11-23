/*
 * Copyright (c) 2012-2014, Microsoft Mobile
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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;

import java.util.HashMap;
import java.util.List;


public class InfixExpressionWriter extends CSharpASTWriter {
    private HashMap<InfixExpression.Operator, String> equivalentOperators;  // Operators that have the same token in both Java & C#

    public InfixExpressionWriter(CSharpASTWriters cSharpASTWriters) {
        super(cSharpASTWriters);

        /*
        Java binary operator precedence, from: http://docs.oracle.com/javase/tutorial/java/nutsandbolts/operators.html
        postfix	expr++ expr--
        unary	++expr --expr +expr -expr ~ !
        multiplicative	* / %
        additive	+ -
        shift	<< >> >>>
        relational	< > <= >= instanceof
        equality	== !=
        bitwise AND	&
        bitwise exclusive OR	^
        bitwise inclusive OR	|
        logical AND	&&
        logical OR	||
        ternary	? :
        assignment	= += -= *= /= %= &= ^= |= <<= >>= >>>=


        C# operator precendence, from C# 5.0 language spec
        Primary	x.y  f(x)  a[x]  x++  x--  new
        typeof  default  checked  unchecked  delegate
        7.7
        Unary	+  -  !  ~  ++x  --x  (T)x
        7.8
        Multiplicative	*  /  %
        7.8
        Additive	+  -
        7.9
        Shift	<<  >>
        7.10
        Relational and type testing	<  >  <=  >=  is  as
        7.10
        Equality	==  !=
        7.11
        Logical AND	&
        7.11
        Logical XOR	^
        7.11
        Logical OR	|
        7.12
        Conditional AND	&&
        7.12
        Conditional OR	||
        7.13
        Null coalescing	??
        7.14
        Conditional	?:
        7.17, 7.15
        Assignment and lambda expression	=  *=  /=  %=  +=  -=  <<=  >>=  &=  ^=  |=
        =>
        */

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
    public void write(ASTNode node) {
        InfixExpression infixExpression = (InfixExpression) node;

        InfixExpression.Operator operator = infixExpression.getOperator();

        if (operator == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED) {
            // TODO: Handle >>>

            write("rightShiftUnsigned(");
            writeNode(infixExpression.getLeftOperand());

            // Skip spaces before the >>> but if there's a newline (or comments) there, copy them
            skipSpacesAndTabs();
            copySpaceAndComments();
            matchAndWrite(">>>", ",");

            copySpaceAndComments();
            writeNode(infixExpression.getRightOperand());
            write(")");
        } else {
            writeNode(infixExpression.getLeftOperand());

            copySpaceAndComments();
            String operatorToken = this.equivalentOperators.get(infixExpression.getOperator());
            matchAndWrite(operatorToken);

            copySpaceAndComments();
            writeNode(infixExpression.getRightOperand());

            if (infixExpression.hasExtendedOperands()) {
                for (Expression extendedOperand : (List<Expression>) infixExpression.extendedOperands()) {

                    copySpaceAndComments();
                    matchAndWrite(operatorToken);

                    copySpaceAndComments();
                    writeNode(extendedOperand);
                }
            }
        }
    }
}

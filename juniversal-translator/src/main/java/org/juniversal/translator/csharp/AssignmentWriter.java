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

package org.juniversal.translator.csharp;

import org.eclipse.jdt.core.dom.Assignment;


// TODO: Finish this
public class AssignmentWriter extends CSharpASTNodeWriter<Assignment> {
    public AssignmentWriter(CSharpSourceFileWriter cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override
    public void write(Assignment assignment) {
        Assignment.Operator operator = assignment.getOperator();

        if (operator == Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN) {
            writeNode(assignment.getLeftHandSide());

            copySpaceAndComments();
            matchAndWrite(">>>=", "=");

            copySpaceAndComments();
            write("rightShiftUnsigned(");
            writeNodeAtDifferentPosition(assignment.getLeftHandSide());
            write(", ");
            writeNode(assignment.getRightHandSide());
            write(")");
        } else {
            writeNode(assignment.getLeftHandSide());

            copySpaceAndComments();
            if (operator == Assignment.Operator.ASSIGN)
                matchAndWrite("=");
            else if (operator == Assignment.Operator.PLUS_ASSIGN)
                matchAndWrite("+=");
            else if (operator == Assignment.Operator.MINUS_ASSIGN)
                matchAndWrite("-=");
            else if (operator == Assignment.Operator.TIMES_ASSIGN)
                matchAndWrite("*=");
            else if (operator == Assignment.Operator.DIVIDE_ASSIGN)
                matchAndWrite("/=");
            else if (operator == Assignment.Operator.BIT_AND_ASSIGN)
                matchAndWrite("&=");
            else if (operator == Assignment.Operator.BIT_OR_ASSIGN)
                matchAndWrite("|=");
            else if (operator == Assignment.Operator.BIT_XOR_ASSIGN)
                matchAndWrite("^=");
            else if (operator == Assignment.Operator.REMAINDER_ASSIGN)
                matchAndWrite("%=");
            else if (operator == Assignment.Operator.LEFT_SHIFT_ASSIGN)
                matchAndWrite("<<=");
            else if (operator == Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN)
                matchAndWrite(">>=");
            else if (operator == Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN)
                matchAndWrite(">>>=");

            copySpaceAndComments();
            writeNode(assignment.getRightHandSide());
        }
    }
}

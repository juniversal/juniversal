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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;


public class ForStatementWriter extends CPlusPlusASTWriter {
    public ForStatementWriter(CPlusPlusASTWriters cPlusPlusASTWriters) {
        super(cPlusPlusASTWriters);
    }

    @Override
    public void write(ASTNode node) {
        ForStatement forStatement = (ForStatement) node;

        matchAndWrite("for");
        copySpaceAndComments();

        matchAndWrite("(");
        copySpaceAndComments();

        boolean first = true;
        for (Object initializerExpressionObject : forStatement.initializers()) {
            Expression initializerExpression = (Expression) initializerExpressionObject;

            if (!first) {
                matchAndWrite(",");
                copySpaceAndComments();
            }

            writeNode(initializerExpression);
            copySpaceAndComments();

            first = false;
        }

        matchAndWrite(";");
        copySpaceAndComments();

        Expression forExpression = forStatement.getExpression();
        if (forExpression != null) {
            writeNode(forStatement.getExpression());
            copySpaceAndComments();
        }

        matchAndWrite(";");
        copySpaceAndComments();

        first = true;
        for (Object updaterExpressionObject : forStatement.updaters()) {
            Expression updaterExpression = (Expression) updaterExpressionObject;

            if (!first) {
                matchAndWrite(",");
                copySpaceAndComments();
            }

            writeNode(updaterExpression);
            copySpaceAndComments();

            first = false;
        }

        matchAndWrite(")");
        copySpaceAndComments();

        writeNode(forStatement.getBody());
    }
}

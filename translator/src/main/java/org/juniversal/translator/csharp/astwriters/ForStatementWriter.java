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

package org.juniversal.translator.csharp.astwriters;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.juniversal.translator.core.Context;


public class ForStatementWriter extends CSharpASTWriter<ForStatement> {
    public ForStatementWriter(CSharpASTWriters cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override
    public void write(Context context, ForStatement forStatement) {
        context.matchAndWrite("for");

        context.copySpaceAndComments();
        context.matchAndWrite("(");

        writeCommaDelimitedNodes(context, forStatement.initializers());

        context.copySpaceAndComments();
        context.matchAndWrite(";");

        Expression forExpression = forStatement.getExpression();
        if (forExpression != null) {
            context.copySpaceAndComments();
            writeNode(context, forStatement.getExpression());
        }

        context.copySpaceAndComments();
        context.matchAndWrite(";");

        writeCommaDelimitedNodes(context, forStatement.updaters());

        context.copySpaceAndComments();
        context.matchAndWrite(")");

        context.copySpaceAndComments();
        writeNode(context, forStatement.getBody());
    }
}

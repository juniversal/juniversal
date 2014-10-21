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

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.juniversal.translator.core.Context;


public class SwitchStatementWriter extends CSharpASTWriter<SwitchStatement> {
    public SwitchStatementWriter(CSharpASTWriters cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override
    public void write(Context context, SwitchStatement switchStatement) {
        // TODO: Handle string swtich statements
        // TODO: Check for fall through of cases (disallowed in C#)

        context.matchAndWrite("switch");

        context.copySpaceAndComments();
        context.matchAndWrite("(");

        writeNode(context, switchStatement.getExpression());

        context.copySpaceAndComments();
        context.matchAndWrite(")");

        context.copySpaceAndComments();
        context.matchAndWrite("{");

        for (Object statementObject : switchStatement.statements()) {
            Statement statement = (Statement) statementObject;

            if (statement instanceof SwitchCase) {
                context.copySpaceAndComments();
                writeSwitchCase(context, (SwitchCase) statement);
            } else {
                context.copySpaceAndComments();
                writeNode(context, statement);
            }
        }

        context.copySpaceAndComments();
        context.matchAndWrite("}");
    }

    private void writeSwitchCase(Context context, SwitchCase switchCase) {
        if (switchCase.isDefault()) {
            context.matchAndWrite("default");

            context.copySpaceAndComments();
            context.matchAndWrite(":");
        } else {
            context.matchAndWrite("case");

            context.copySpaceAndComments();
            writeNode(context, switchCase.getExpression());

            context.copySpaceAndComments();
            context.matchAndWrite(":");
        }
    }
}

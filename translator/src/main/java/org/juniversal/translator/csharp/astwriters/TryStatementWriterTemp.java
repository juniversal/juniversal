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

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.Context;

import java.util.List;


public class TryStatementWriterTemp extends CSharpASTWriter<TryStatement> {
    public TryStatementWriterTemp(CSharpASTWriters cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    // TODO: Possibly check that catch statements are ordered from more specific to more general, as that's enforced in C#
    // TODO: Possibly change catch(Throwable t) where t is unused to just catch in c#
    @Override
    public void write(Context context, TryStatement tryStatement) {
        if (tryStatement.resources().size() > 0)
                writeTryWithResources(context, tryStatement);
        else {
            context.matchAndWrite("try");

            context.copySpaceAndComments();
            writeNode(context, tryStatement.getBody());

            List<?> catchClauses = tryStatement.catchClauses();
            for (Object catchClauseObject : catchClauses) {
                CatchClause catchClause = (CatchClause) catchClauseObject;

                context.copySpaceAndComments();
                context.matchAndWrite("catch");

                context.copySpaceAndComments();
                context.matchAndWrite("(");

                context.copySpaceAndComments();
                writeNode(context, catchClause.getException());

                context.copySpaceAndComments();
                context.matchAndWrite(")");

                context.copySpaceAndComments();
                writeNode(context, catchClause.getBody());
            }

            @Nullable Block finallyBlock = tryStatement.getFinally();
            if (finallyBlock != null) {
                context.copySpaceAndComments();
                context.matchAndWrite("finally");

                context.copySpaceAndComments();
                writeNode(context, finallyBlock);
            }
        }
    }

    private void writeTryWithResources(Context context, TryStatement tryStatement) {
        context.matchAndWrite("try", "using");

        context.copySpaceAndComments();
        context.matchAndWrite("(");

        boolean first = true;
        for (Object resourceDeclarationObject : tryStatement.resources()) {
            // TODO: Check handling multiple variables, with same type, included as part of single declaration
            VariableDeclarationExpression resourceDeclaration = (VariableDeclarationExpression) resourceDeclarationObject;

            if (! first) {
                context.copySpaceAndComments();
                context.matchAndWrite(";", ",");
            }

            writeNode(context, resourceDeclaration);

            first = false;
        }

        context.copySpaceAndComments();
        context.matchAndWrite(")");

        context.copySpaceAndComments();
        writeNode(context, tryStatement.getBody());

        if (tryStatement.catchClauses().size() > 0)
            context.throwSourceNotSupported("try-with-resources with a catch clause isn't supported; use nested try statements instead");

        if (tryStatement.getFinally() != null)
            context.throwSourceNotSupported("try-with-resources with a finally clause isn't supported; use nested try statements instead");
    }
}

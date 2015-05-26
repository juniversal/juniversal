/*
 * Copyright (c) 2012-2015, Microsoft Mobile
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

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.jetbrains.annotations.Nullable;

import static org.juniversal.translator.core.ASTUtil.forEach;


public class TryStatementWriter extends CSharpASTNodeWriter<TryStatement> {
    public TryStatementWriter(CSharpTranslator cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    // TODO: Possibly check that catch statements are ordered from more specific to more general, as that's enforced in C#
    // TODO: Possibly change catch(Throwable t) where t is unused to just catch in c#
    @Override
    public void write(TryStatement tryStatement) {
        if (tryStatement.resources().size() > 0)
            writeTryWithResources(tryStatement);
        else {
            matchAndWrite("try");

            copySpaceAndComments();
            writeNode(tryStatement.getBody());

            forEach(tryStatement.catchClauses(), (CatchClause catchClause) -> {
                copySpaceAndComments();
                matchAndWrite("catch");

                copySpaceAndComments();
                matchAndWrite("(");

                copySpaceAndComments();
                writeNode(catchClause.getException());

                copySpaceAndComments();
                matchAndWrite(")");

                copySpaceAndComments();
                writeNode(catchClause.getBody());
            });

            @Nullable Block finallyBlock = tryStatement.getFinally();
            if (finallyBlock != null) {
                copySpaceAndComments();
                matchAndWrite("finally");

                copySpaceAndComments();
                writeNode(finallyBlock);
            }
        }
    }

    private void writeTryWithResources(TryStatement tryStatement) {
        matchAndWrite("try", "using");

        copySpaceAndComments();
        matchAndWrite("(");

        // TODO: Check handling multiple variables, with same type, included as part of single declaration
        forEach(tryStatement.resources(), (VariableDeclarationExpression resourceDeclaration, boolean first) -> {
            if (!first) {
                copySpaceAndComments();
                matchAndWrite(";", ",");
            }

            writeNode(resourceDeclaration);
        });

        copySpaceAndComments();
        matchAndWrite(")");

        copySpaceAndComments();
        writeNode(tryStatement.getBody());

        if (tryStatement.catchClauses().size() > 0)
            throw sourceNotSupported("try-with-resources with a catch clause isn't supported; use nested try statements instead");

        if (tryStatement.getFinally() != null)
            throw sourceNotSupported("try-with-resources with a finally clause isn't supported; use nested try statements instead");
    }
}

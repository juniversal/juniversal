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
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.juniversal.translator.core.ASTWriter;
import org.juniversal.translator.core.ASTWriters;
import org.juniversal.translator.core.Context;


class TypeDeclarationWriter extends ASTWriter {
    private CSharpASTWriters cSharpASTWriters;

    TypeDeclarationWriter(CSharpASTWriters cSharpASTWriters) {
        this.cSharpASTWriters = cSharpASTWriters;
    }

    public void write(ASTNode node, Context context) {
        TypeDeclaration typeDeclaration = (TypeDeclaration) node;

        TypeDeclaration oldTypeDeclaration = context.getTypeDeclaration();
        context.setTypeDeclaration(typeDeclaration);

        new WriteTypeDeclaration(typeDeclaration, context, cSharpASTWriters);

        context.setTypeDeclaration(oldTypeDeclaration);
    }
}

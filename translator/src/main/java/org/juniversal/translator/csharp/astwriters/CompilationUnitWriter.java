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

package
        org /*abc*/.juniversal.
                translator.csharp.
                astwriters;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.Context;


// TODO: FInish this

class CompilationUnitWriter extends CSharpASTWriter<CompilationUnit> {
    CompilationUnitWriter(CSharpASTWriters cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    public void write(Context context, CompilationUnit compilationUnit) {
        context.copySpaceAndComments();

        // TODO: This results in an extra newline normally; distinguish between case where package is only think on line & multiple things on the line
        @Nullable PackageDeclaration packageDeclaration = compilationUnit.getPackage();
        if (packageDeclaration != null) {
            context.setPositionToEndOfNodeSpaceAndComments(packageDeclaration);
        }

        for (Object importDeclarationObject : compilationUnit.imports()) {
            ImportDeclaration importDeclaration = (ImportDeclaration) importDeclarationObject;

            context.copySpaceAndComments();
            //TODO: Process import statements; but for now just skip
            context.setPositionToEndOfNodeSpaceAndComments(importDeclaration);
        }

        int previousIndent = 0;
        if (packageDeclaration != null) {
            int previousPosition = context.getPosition();
            context.setPositionToStartOfNode(packageDeclaration);
            context.matchAndWrite("package", "namespace");

            context.copySpaceAndComments();
            writeNode(context, packageDeclaration.getName());

            context.write(" {");
            previousIndent = context.getTargetWriter().incrementAdditionalIndentation(context.getPreferredIndent());
            context.writeln();

            context.setPosition(previousPosition);
        }

/*

        if (packageDeclaration != null)
        return getNamespaceNameForPackageName(packageDeclaration == null ? null : packageDeclaration
                .getName());
*/

        AbstractTypeDeclaration firstTypeDeclaration = (AbstractTypeDeclaration) compilationUnit.types().get(0);

        context.copySpaceAndComments();
        writeNode(context, firstTypeDeclaration);
        context.copySpaceAndComments();

        if (packageDeclaration != null) {
            previousIndent = context.getTargetWriter().setAdditionalIndentation(previousIndent);
            context.writeln();
            context.writeln("}");
        }

/*
        context.writeln();

        context.setPosition(firstTypeDeclaration.getStartPosition());
        context.copySpaceAndComments();   // Skip any Javadoc included in the node

        writeNode(context, firstTypeDeclaration);
*/
    }
}

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

package
        org /*abc*/.juniversal.
                translator.csharp.
                astwriters;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;


// TODO: Finish this

class CompilationUnitWriter extends CSharpASTNodeWriter<CompilationUnit> {
    CompilationUnitWriter(CSharpSourceFileWriter cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    public void write(CompilationUnit compilationUnit) {
        copySpaceAndComments();

        // TODO: This results in an extra newline normally; distinguish between case where package is only thing on line & multiple things on that line
        @Nullable PackageDeclaration packageDeclaration = compilationUnit.getPackage();
        if (packageDeclaration != null) {
            setPositionToEndOfNodeSpaceAndComments(packageDeclaration);
        }

        for (Object importDeclarationObject : compilationUnit.imports()) {
            ImportDeclaration importDeclaration = (ImportDeclaration) importDeclarationObject;
            Name importDeclarationName = importDeclaration.getName();

            if (importDeclaration.isStatic())
                throw sourceNotSupported("Static imports aren't currently supported");

            copySpaceAndComments();
            matchAndWrite("import", "using");

            if (importDeclaration.isOnDemand()) {
                copySpaceAndComments();
                writeNode(importDeclarationName);
            }
            else {
                if (! (importDeclarationName instanceof QualifiedName))
                    throw sourceNotSupported("Class import is unexpectedly not a fully qualified name (with a '.' in it)");
                QualifiedName qualifiedName = (QualifiedName) importDeclarationName;

                copySpaceAndComments();
                writeNodeAtDifferentPosition(qualifiedName.getName());
                write(" = ");
                writeNode(qualifiedName);
            }

            copySpaceAndComments();
            matchAndWrite(";");
        }

        copySpaceAndComments();

        int previousIndent = 0;
        if (packageDeclaration != null) {
            int previousPosition = getPosition();
            setPositionToStartOfNode(packageDeclaration);
            matchAndWrite("package", "namespace");

            copySpaceAndComments();
            writeNode(packageDeclaration.getName());

            write(" {");
            previousIndent = getTargetWriter().incrementAdditionalIndentation(getPreferredIndent());
            writeln();

            setPosition(previousPosition);
        }

/*

        if (packageDeclaration != null)
        return getNamespaceNameForPackageName(packageDeclaration == null ? null : packageDeclaration
                .getName());
*/

        AbstractTypeDeclaration firstTypeDeclaration = (AbstractTypeDeclaration) compilationUnit.types().get(0);

        //copySpaceAndComments();
        writeNode(firstTypeDeclaration);
        copySpaceAndComments();

        if (packageDeclaration != null) {
            getTargetWriter().setAdditionalIndentation(previousIndent);
            writeln();
            writeln("}");
        }

/*
        writeln();

        context.setPosition(firstTypeDeclaration.getStartPosition());
        copySpaceAndComments();   // Skip any Javadoc included in the node

        writeNode(context, firstTypeDeclaration);
*/
    }
}

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

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.BufferTargetWriter;
import org.juniversal.translator.core.SourceFileWriter;

import static org.juniversal.translator.core.ASTUtil.isThisName;


// TODO: Finish this

class CompilationUnitWriter extends CSharpASTNodeWriter<CompilationUnit> {
    CompilationUnitWriter(CSharpSourceFileWriter cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    public void write(CompilationUnit compilationUnit) {
        copySpaceAndComments();

        // First visit & write out the guts of the class, using a BufferTargetWriter to capture all output.   We do
        // this first to capture the usage of anything that needs an explicit "using" statement in C# but doesn't
        // have an import in Java.   The top of the file, with the "using" statements, is written later, down below
        BufferTargetWriter bufferTargetWriter = new BufferTargetWriter(getSourceFileWriter().getTargetWriter());
        try (SourceFileWriter.RestoreTargetWriter ignored = getSourceFileWriter().setTargetWriter(bufferTargetWriter)) {
            writeNamespaceAndTypeDeclaration(compilationUnit);
        }

        @Nullable PackageDeclaration packageDeclaration = compilationUnit.getPackage();
        if (packageDeclaration != null) {
            // TODO: This results in an extra newline normally; distinguish between case where package is only thing on line & multiple things on that line
            setPositionToEndOfNodeSpaceAndComments(packageDeclaration);
        }
        else {
            setPositionToStartOfNode(compilationUnit);
            skipSpaceAndComments();
        }

        // Write
        copySpaceAndComments();
        writeUsingStatements(compilationUnit);

        copySpaceAndComments();
        write(bufferTargetWriter);

        setPositionToEndOfNode(compilationUnit);
    }

    private void writeUsingStatements(CompilationUnit compilationUnit) {
        for (Object importDeclarationObject : compilationUnit.imports()) {
            ImportDeclaration importDeclaration = (ImportDeclaration) importDeclarationObject;
            Name importDeclarationName = importDeclaration.getName();

            // Skip imports for the Nullable annotation
            if (isThisName(importDeclarationName, "org.jetbrains.annotations.Nullable")) {
                setPositionToEndOfNode(importDeclaration);
                continue;
            }

            if (importDeclaration.isStatic())
                throw sourceNotSupported("Static imports aren't currently supported");

            copySpaceAndComments();
            matchAndWrite("import", "using");

            if (importDeclaration.isOnDemand()) {
                copySpaceAndComments();
                writeNode(importDeclarationName);
            } else {
                if (!(importDeclarationName instanceof QualifiedName))
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

        for (String extraUsing : getContext().getExtraUsings()) {
            write("using ");
            write(extraUsing);
            writeln(";");
        }
    }

    private void writeNamespaceAndTypeDeclaration(CompilationUnit compilationUnit) {
        @Nullable PackageDeclaration packageDeclaration = compilationUnit.getPackage();

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
        setPositionToStartOfNode(firstTypeDeclaration);
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

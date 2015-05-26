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

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.xuniversal.translator.core.TargetWriter;
import org.xuniversal.translator.core.Var;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.juniversal.translator.core.ASTUtil.forEach;
import static org.juniversal.translator.core.ASTUtil.getFirstTypeDeclaration;
import static org.juniversal.translator.core.ASTUtil.isGenericImport;


// TODO: Finish this

class CompilationUnitWriter extends CSharpASTNodeWriter<CompilationUnit> {
    CompilationUnitWriter(CSharpTranslator translator) {
        super(translator);
    }

    public void write(CompilationUnit compilationUnit) {
        AbstractTypeDeclaration typeDeclaration = getFirstTypeDeclaration(compilationUnit);
        setPositionToStartOfNodeSpaceAndComments(typeDeclaration);

        // First visit & write out the guts of the type, using a BufferTargetWriter to capture all output.   We do
        // this first to capture the usage of anything that needs an import.   The imports themselves are written
        // after, via writeEntireFile
        String typeBuffer;
        try (TargetWriter.BufferedWriter bufferedWriter = getTargetWriter().startBuffering()) {
            copySpaceAndComments();
            writeType(compilationUnit, typeDeclaration);
            copySpaceAndComments();

            typeBuffer = bufferedWriter.getBufferContents();
        }

        setPositionToStartOfNode(compilationUnit);
        writeEntireFile(compilationUnit, typeBuffer);
    }

    protected void writeType(CompilationUnit compilationUnit, AbstractTypeDeclaration typeDeclaration) {
        @Nullable PackageDeclaration packageDeclaration = compilationUnit.getPackage();

        int previsionAdditionalIndentation = 0;
        if (packageDeclaration != null) {
            previsionAdditionalIndentation = getTargetWriter().incrementAdditionalIndentation(getPreferredIndent());
            writeln();
        }

        copySpaceAndComments();
        writeNode(typeDeclaration);
        copySpaceAndComments();

        if (packageDeclaration != null) {
            getTargetWriter().setAdditionalIndentation(previsionAdditionalIndentation);
            writeln();
        }
    }

    protected void writeEntireFile(CompilationUnit compilationUnit, String typeBuffer) {
        @Nullable PackageDeclaration packageDeclaration = compilationUnit.getPackage();

        if (packageDeclaration != null) {
            setPositionToStartOfNode(packageDeclaration);
            matchAndWrite("package", "namespace");

            copySpaceAndComments();
            writeNode(packageDeclaration.getName());

            write(" {");
        }

        writeUsings(compilationUnit);

        // Write out the body contents
        copySpaceAndComments();
        write(typeBuffer);

        if (packageDeclaration != null)
            writeln("}");

        setPositionToEndOfNode(compilationUnit);
    }

    private void writeUsings(CompilationUnit compilationUnit) {
        Map<String, String> annotationMap = ((CSharpTranslator) getTranslator()).getAnnotationMap();
        Set<String> genericUsings = new HashSet<>();

        Var<Boolean> wroteUsing = new Var<>(false);
        forEach(compilationUnit.imports(), (ImportDeclaration importDeclaration) -> {
            Name importDeclarationName = importDeclaration.getName();

            String importDeclarationFullyQualifiedName = importDeclarationName.getFullyQualifiedName();

            // Skip imports for the Nullable annotation and mapped annotations
            if (importDeclarationFullyQualifiedName.equals("org.jetbrains.annotations.Nullable") ||
                annotationMap.containsKey(importDeclarationFullyQualifiedName)) {
                setPositionToEndOfNode(importDeclaration);
                return;
            }

            if (importDeclaration.isStatic())
                throw sourceNotSupported("Static imports aren't currently supported");

            if (!(importDeclarationName instanceof QualifiedName))
                throw sourceNotSupported("Class import is unexpectedly not a fully qualified name (with a '.' in it)");
            QualifiedName qualifiedName = (QualifiedName) importDeclarationName;

            @Nullable String namespaceToImportForGenericClass = null;
            if (isGenericImport(importDeclaration)) {
                namespaceToImportForGenericClass = qualifiedName.getQualifier().getFullyQualifiedName();
                if (genericUsings.contains(namespaceToImportForGenericClass))
                    return;
            }

            copySpaceAndComments();
            matchAndWrite("import", "using");

            copySpaceAndComments();
            if (importDeclaration.isOnDemand()) {
                writeNode(importDeclarationName);
                skipSpaceAndComments();
                match(".");
                skipSpaceAndComments();
                match("*");
            } else {
                if (namespaceToImportForGenericClass != null) {
                    write(namespaceToImportForGenericClass);
                    genericUsings.add(namespaceToImportForGenericClass);
                    setPositionToEndOfNode(qualifiedName);
                } else {
                    writeNodeAtDifferentPosition(qualifiedName.getName());
                    write(" = ");
                    writeNode(qualifiedName);
                }
            }

            copySpaceAndComments();
            matchAndWrite(";");
            wroteUsing.set(true);
        });

        if (wroteUsing.value())
            writeln();

        for (String extraUsing : ((CSharpContext) getContext()).getExtraUsings()) {
            write("using ");
            write(extraUsing);
            writeln(";");
        }
    }
}

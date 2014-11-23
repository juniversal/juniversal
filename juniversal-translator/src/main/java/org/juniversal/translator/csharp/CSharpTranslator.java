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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.juniversal.translator.core.*;
import org.juniversal.translator.cplusplus.CPPProfile;
import org.juniversal.translator.cplusplus.OutputType;
import org.juniversal.translator.csharp.astwriters.CSharpASTWriters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

public class CSharpTranslator extends Translator {
    private CPPProfile cppProfile = new CPPProfile();

    @Override public void translateFile(SourceFile sourceFile) {
        CompilationUnit compilationUnit = sourceFile.getCompilationUnit();
        AbstractTypeDeclaration mainTypeDeclaration = (AbstractTypeDeclaration) compilationUnit.types().get(0);

        ;

        String packageName = mainTypeDeclaration.getName().getFullyQualifiedName();
        String typeName = mainTypeDeclaration.getName().getIdentifier();

        String fileName = typeName + ".cs";

        File file = new File(getPackageDirectory(mainTypeDeclaration), fileName);
        try (FileWriter writer = new FileWriter(file)) {
            TargetWriter targetWriter = new TargetWriter(writer, cppProfile);
            Context context = new Context(sourceFile, targetWriter, OutputType.SOURCE);
            CSharpASTWriters cSharpASTWriters = new CSharpASTWriters(context, this);

            cSharpASTWriters.writeRootNode(compilationUnit);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public String translateNode(SourceFile sourceFile, ASTNode astNode) {
        try (StringWriter writer = new StringWriter()) {
            TargetWriter targetWriter = new TargetWriter(writer, cppProfile);
            Context context = new Context(sourceFile, targetWriter, OutputType.SOURCE);

            context.setPosition(astNode.getStartPosition());
            CSharpASTWriters cSharpASTWriters = new CSharpASTWriters(context, this);

            cSharpASTWriters.writeRootNode(astNode);

            return writer.getBuffer().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

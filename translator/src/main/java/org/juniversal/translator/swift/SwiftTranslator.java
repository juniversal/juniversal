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

package org.juniversal.translator.swift;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.juniversal.translator.core.*;
import org.juniversal.translator.cplusplus.CPPProfile;
import org.juniversal.translator.cplusplus.OutputType;
import org.juniversal.translator.swift.astwriters.SwiftASTWriters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class SwiftTranslator extends Translator {
    private SwiftASTWriters swiftASTWriters;
    private CPPProfile cppProfile = new CPPProfile();

    public SwiftTranslator() {
        this.swiftASTWriters = new SwiftASTWriters(this);
    }

    @Override public SwiftASTWriters getASTWriters() {
        return swiftASTWriters;
    }

    @Override protected void translateAST(SourceFile sourceFile) {
        writeCSharpFile(sourceFile);
    }

    public Context createContext(SourceFile sourceFile, Writer outputWriter) {
        TargetWriter targetWriter = new TargetWriter(outputWriter, cppProfile);
        return new Context(swiftASTWriters, sourceFile, cppProfile, targetWriter, OutputType.SOURCE);
    }

    private void writeCSharpFile(SourceFile sourceFile) {
        CompilationUnit compilationUnit = sourceFile.getCompilationUnit();
        TypeDeclaration mainTypeDeclaration = ASTUtil.getFirstTypeDeclaration(compilationUnit);

        String typeName = mainTypeDeclaration.getName().getIdentifier();

        String fileName = typeName + ".cs";
        File file = new File(getOutputDirectory(), fileName);

        FileWriter writer;
        try {
            writer = new FileWriter(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Context context = createContext(sourceFile, writer);

        try {
            swiftASTWriters.writeNode(context, compilationUnit);
        } catch (UserViewableException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (RuntimeException e) {
            if (e instanceof ContextPositionMismatchException)
                throw e;
            else
                throw new JUniversalException(e.getMessage() + "\nError occurred with context at position\n"
                                              + context.getPositionDescription(context.getPosition()), e);
        }

        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

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

package org.juniversal.translator.cplusplus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.juniversal.translator.cplusplus.astwriters.CPlusPlusASTWriters;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.juniversal.translator.core.*;

public class CPlusPlusTranslator extends Translator {
	private CPlusPlusASTWriters cPlusPlusASTWriters;
	private CPPProfile cppProfile = new CPPProfile();

	public CPlusPlusTranslator() {
		this.cPlusPlusASTWriters = new CPlusPlusASTWriters(this);
	}

	@Override protected void translateAST(SourceFile sourceFile) {
		writeCPPFile(sourceFile, OutputType.HEADER);
		writeCPPFile(sourceFile, OutputType.SOURCE);
	}

    public Context createContext(SourceFile sourceFile, Writer outputWriter, OutputType outputType) {
        TargetWriter targetWriter = new TargetWriter(outputWriter, cppProfile);
        return new Context(sourceFile, cppProfile, targetWriter, outputType);
    }

    private void writeCPPFile(SourceFile sourceFile, OutputType outputType) {
		CompilationUnit compilationUnit = sourceFile.getCompilationUnit();
		TypeDeclaration mainTypeDeclaration = ASTUtil.getFirstTypeDeclaration(compilationUnit);

		String typeName = mainTypeDeclaration.getName().getIdentifier();

		String fileName = outputType == OutputType.HEADER ? typeName + ".h" : typeName + ".cpp";
		File file = new File(getOutputDirectory(), fileName);

		FileWriter writer;
		try {
			writer = new FileWriter(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Context context = createContext(sourceFile, writer, OutputType.SOURCE);

		try {
			cPlusPlusASTWriters.writeNode(context, compilationUnit);
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

    @Override
    public CPlusPlusASTWriters getASTWriters() {
        return cPlusPlusASTWriters;
    }
}

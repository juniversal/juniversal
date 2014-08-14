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

import org.juniversal.translator.cplusplus.astwriters.CPlusPlusASTWriters;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.juniversal.translator.core.*;

public class Translator {
	private JUniversal jUniversal;
	private CPlusPlusASTWriters astWriters;
	private CPPProfile cppProfile = new CPPProfile();
	String s;

	public Translator(JUniversal jUniversal) {
		this.jUniversal = jUniversal;
		this.astWriters = new CPlusPlusASTWriters();
	}

	public void translate() {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(new String[0], new String[0], null, false);
		parser.setResolveBindings(true);

		FileASTRequestor astRequestor = new FileASTRequestor() {
			public void acceptAST(String sourceFilePath, CompilationUnit compilationUnit) {
				System.out.println("Called to accept AST for " + sourceFilePath);
				translateAST(sourceFilePath, compilationUnit);
			}
		};

		parser.createASTs(jUniversal.getJavaFiles(), null, new String[0], astRequestor, null);

		/*
		 * String source = readFile(jUniversal.getJavaProjectDirectories().get(0).getPath());
		 * parser.setSource(source.toCharArray());
		 * 
		 * CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		 * 
		 * 
		 * TypeDeclaration typeDeclaration = ASTUtil.getFirstTypeDeclaration(compilationUnit);
		 * 
		 * FileWriter writer; try { writer = new FileWriter(jUniversal.getOutputDirectory()); }
		 * catch (IOException e) { throw new RuntimeException(e); }
		 * 
		 * CPPProfile profile = new CPPProfile(); // profile.setTabStop(4);
		 * 
		 * CPPWriter cppWriter = new CPPWriter(writer, profile);
		 * 
		 * Context context = new Context((CompilationUnit) compilationUnit.getRoot(), source, 8,
		 * profile, cppWriter, OutputType.SOURCE);
		 * 
		 * context.setPosition(typeDeclaration.getStartPosition());
		 * 
		 * ASTWriters astWriters = new ASTWriters();
		 * 
		 * try { context.setPosition(typeDeclaration.getStartPosition());
		 * context.skipSpaceAndComments();
		 * 
		 * astWriters.writeNode(typeDeclaration, context); } catch (UserViewableException e) {
		 * System.err.println(e.getMessage()); System.exit(1); } catch (RuntimeException e) { if (e
		 * instanceof ContextPositionMismatchException) throw e; else throw new
		 * JUniversalException(e.getMessage() + "\nError occurred with context at position\n" +
		 * context.getPositionDescription(context.getPosition()), e); }
		 * 
		 * try { writer.close(); } catch (IOException e) { throw new RuntimeException(e); }
		 */
	}

	private void translateAST(String sourceFilePath, CompilationUnit compilationUnit) {
		// profile.setTabStop(4);
		
		SourceFile sourceFile = new SourceFile(compilationUnit, sourceFilePath);
		
		writeCPPFile(sourceFile, OutputType.HEADER);
		writeCPPFile(sourceFile, OutputType.SOURCE);
	}

	private void writeCPPFile(SourceFile sourceFile, OutputType outputType) {
		CompilationUnit compilationUnit = sourceFile.getCompilationUnit();
		TypeDeclaration mainTypeDeclaration = ASTUtil.getFirstTypeDeclaration(compilationUnit);

		String typeName = mainTypeDeclaration.getName().getIdentifier();

		String fileName = outputType == OutputType.HEADER ? typeName + ".h" : typeName + ".cpp";
		File file = new File(jUniversal.getOutputDirectory(), fileName);

		FileWriter writer;
		try {
			writer = new FileWriter(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		TargetWriter targetWriter = new TargetWriter(writer, cppProfile);

		Context context = new Context(sourceFile, 4, cppProfile, targetWriter, outputType);

		context.setPosition(compilationUnit.getStartPosition());

		try {
			astWriters.writeNode(compilationUnit, context);
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

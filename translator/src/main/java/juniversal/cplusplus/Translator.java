package juniversal.cplusplus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import juniversal.ASTUtil;
import juniversal.ContextPositionMismatchException;
import juniversal.JUniversal;
import juniversal.JUniversalException;
import juniversal.SourceFile;
import juniversal.UserViewableException;
import juniversal.cplusplus.astwriters.ASTWriters;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class Translator {
	private JUniversal jUniversal;
	private ASTWriters astWriters;
	private CPPProfile cppProfile = new CPPProfile();
	String s;

	public Translator(JUniversal jUniversal) {
		this.jUniversal = jUniversal;
		this.astWriters = new ASTWriters();
	}

	public void translate() {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
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
		CPPWriter cppWriter = new CPPWriter(writer, cppProfile);

		Context context = new Context(sourceFile, 4, cppProfile, cppWriter, outputType);

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

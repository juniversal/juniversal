package juniversal.cplusplus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import juniversal.ASTUtil;
import juniversal.ContextPositionMismatchException;
import juniversal.JUniversal;
import juniversal.JUniversalException;
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
		String source = readFile(jUniversal.getJavaProjectDirectories().get(0).getPath());
		parser.setSource(source.toCharArray());

		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		

		TypeDeclaration typeDeclaration = ASTUtil.getFirstTypeDeclaration(compilationUnit);

		FileWriter writer;
		try {
			writer = new FileWriter(jUniversal.getOutputDirectory());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		CPPProfile profile = new CPPProfile();
		// profile.setTabStop(4);

		CPPWriter cppWriter = new CPPWriter(writer, profile);

		Context context = new Context((CompilationUnit) compilationUnit.getRoot(), source, 8, profile, cppWriter,
				OutputType.SOURCE);

		context.setPosition(typeDeclaration.getStartPosition());

		ASTWriters astWriters = new ASTWriters();

		try {
			context.setPosition(typeDeclaration.getStartPosition());
			context.skipSpaceAndComments();

			astWriters.writeNode(typeDeclaration, context);
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
		*/
	}

	private void translateAST(String sourceFilePath, CompilationUnit compilationUnit) {
		TypeDeclaration typeDeclaration = ASTUtil.getFirstTypeDeclaration(compilationUnit);

		FileWriter writer;
		try {
			writer = new FileWriter(jUniversal.getOutputDirectory());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		CPPProfile profile = new CPPProfile();
		// profile.setTabStop(4);

		CPPWriter cppWriter = new CPPWriter(writer, profile);

		String source = readFile(sourceFilePath);
		Context context = new Context((CompilationUnit) compilationUnit.getRoot(), source, 8, profile, cppWriter,
				OutputType.SOURCE);

		context.setPosition(typeDeclaration.getStartPosition());

		try {
			context.setPosition(typeDeclaration.getStartPosition());
			context.skipSpaceAndComments();

			astWriters.writeNode(typeDeclaration, context);
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

	public static String readFile(String filePath) {
		File file = new File(filePath);

		FileReader fileReader = null;
		try {
			StringBuilder stringBuilder = new StringBuilder();

			fileReader = new FileReader(file);

			char[] contentsBuffer = new char[1024];
			int charsRead = 0;
			while ((charsRead = fileReader.read(contentsBuffer)) != -1)
				stringBuilder.append(contentsBuffer, 0, charsRead);

			fileReader.close();

			return stringBuilder.toString();
		} catch (FileNotFoundException e) {
			throw new JUniversalException(e);
		} catch (IOException ioe) {
			throw new JUniversalException(ioe);
		}
	}
}

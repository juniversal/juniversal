package juniversal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import juniversal.cplusplus.CPPWriter;
import juniversal.cplusplus.Context;
import juniversal.cplusplus.ContextPositionMismatchException;
import juniversal.cplusplus.OutputType;
import juniversal.cplusplus.astwriters.ASTWriters;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;


public class CJ {

	public static void main(String[] args) {

		ASTParser parser = ASTParser.newParser(AST.JLS3); 
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		if (args.length != 2) {
			System.out.println("Usage: CJ <input-file> <output-directory>");
			System.exit(1);
		}

		String source = readFile(args[0]);
		parser.setSource(source.toCharArray());

		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null /* IProgressMonitor */);
		
		TypeDeclaration typeDeclaration = ASTUtil.getFirstTypeDeclaration(compilationUnit);


		StringWriter writer = new StringWriter();
		CPPProfile profile = new CPPProfile();
		//profile.setTabStop(4);

		CPPWriter cppWriter = new CPPWriter(writer, profile);

		Context context = new Context((CompilationUnit) compilationUnit.getRoot(),
			source, 8, profile, cppWriter, OutputType.SOURCE);

		context.setPosition(typeDeclaration.getStartPosition());

		ASTWriters astWriters = new ASTWriters();

		try {
			context.setPosition(typeDeclaration.getStartPosition());
			context.skipSpaceAndComments();

			astWriters.writeNode(typeDeclaration, context);
		}
		catch (UserViewableException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		catch (RuntimeException e) {
			if (e instanceof ContextPositionMismatchException)
				throw e;
			else throw new CJException(e.getMessage() + "\nError occurred with context at position\n"
					+ context.getPositionDescription(context.getPosition()), e);
		}

		String cppOutput = writer.getBuffer().toString();

		System.out.println("Output:");
		System.out.println(cppOutput);
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
			throw new CJException(e);
		} catch (IOException ioe) {
			throw new CJException(ioe);
		}
	}
}

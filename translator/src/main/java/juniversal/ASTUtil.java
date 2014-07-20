package juniversal;

import java.io.File;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class ASTUtil {

	public static void parseJava(List<File> javaProjectDirectories) {
		
		/*
		ArrayList<File> files = new ArrayList<File>();
		for (File javaProjectDirectory : javaProjectDirectories)
			Util.getFilesRecursive(javaProjectDirectory, ".java", files);
	
		ArrayList<String> filePathsList = new ArrayList<String>();
		for (File file : files) {
			filePathsList.add(file.getPath());
		}

		String[] filePaths = filePathsList.toArray(new String[filePathsList.size()]);

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		parser.createASTs(filePaths, encodings, bindingKeys, requestor, monitor)

		String source = readFile(args[0]);
		parser.setSource(source.toCharArray());

		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null /xx* IProgressMonitor *xx/);

		TypeDeclaration typeDeclaration = ASTUtil.getFirstTypeDeclaration(compilationUnit);

		StringWriter writer = new StringWriter();
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

		String cppOutput = writer.getBuffer().toString();

		System.out.println("Output:");
		System.out.println(cppOutput);
		*/
		
	}

	public static TypeDeclaration getFirstTypeDeclaration(CompilationUnit compilationUnit) {
		return (TypeDeclaration) compilationUnit.types().get(0);
	}

	public static Block getFirstMethodBlock(CompilationUnit compilationUnit) {
		return getFirstTypeDeclaration(compilationUnit).getMethods()[0].getBody();
	}

	/**
	 * Returns true if the list of extended modifiers (modifiers & annotations) includes "final".
	 * 
	 * @param extendedModifiers
	 *            extended modifiers to check
	 * @return true if and only if list contains "final"
	 */
	public static boolean containsFinal(List<?> extendedModifiers) {
		for (Object extendedModifierObject : extendedModifiers) {
			IExtendedModifier extendedModifier = (IExtendedModifier) extendedModifierObject;
			if (extendedModifier.isModifier() && ((Modifier) extendedModifier).isFinal())
				return true;
		}
		return false;
	}

	/**
	 * Returns true if the list of extended modifiers (modifiers & annotations) includes "static".
	 * 
	 * @param extendedModifiers
	 *            extended modifiers to check
	 * @return true if and only if list contains "static"
	 */
	public static boolean containsStatic(List<?> extendedModifiers) {
		for (Object extendedModifierObject : extendedModifiers) {
			IExtendedModifier extendedModifier = (IExtendedModifier) extendedModifierObject;
			if (extendedModifier.isModifier() && ((Modifier) extendedModifier).isStatic())
				return true;
		}
		return false;
	}

	/**
	 * Returns true if the list of extended modifiers (modifiers & annotations) includes "abstract".
	 * 
	 * @param extendedModifiers
	 *            extended modifiers to check
	 * @return true if and only if list contains "static"
	 */
	public static boolean containsAbstract(List<?> extendedModifiers) {
		for (Object extendedModifierObject : extendedModifiers) {
			IExtendedModifier extendedModifier = (IExtendedModifier) extendedModifierObject;
			if (extendedModifier.isModifier() && ((Modifier) extendedModifier).isAbstract())
				return true;
		}
		return false;
	}

	/**
	 * Determines the access modifier specified in list of modifiers. If no access modifier is
	 * specified, the default access of Package is returned.
	 * 
	 * @param extendedModifiers
	 *            extended modifiers to check
	 * @return AccessModifier specified or Package by default
	 */
	public static AccessLevel getAccessModifier(List<?> extendedModifiers) {
		for (Object extendedModifierObject : extendedModifiers) {
			IExtendedModifier extendedModifier = (IExtendedModifier) extendedModifierObject;
			if (extendedModifier.isModifier()) {
				Modifier modifier = (Modifier) extendedModifier;
				if (modifier.isPublic())
					return AccessLevel.PUBLIC;
				else if (modifier.isProtected())
					return AccessLevel.PROTECTED;
				else if (modifier.isPrivate())
					return AccessLevel.PRIVATE;
			}
		}

		return AccessLevel.PACKAGE;
	}

	/**
	 * Returns the position following the last character in the node; just a shortcut for adding the
	 * length to the start position. Note that the end position may be (one) past the end of the
	 * source.
	 * 
	 * @param node
	 *            ASTNode in question
	 * @return last position in the node + 1
	 */
	public static int getEndPosition(ASTNode node) {
		return node.getStartPosition() + node.getLength();
	}

	/**
	 * Returns the position following the last character in the last node in the list. Note that the
	 * end position may be (one) past the end of the source.
	 * 
	 * @param nodes
	 *            list of nodes; each item in the list should actually be of a type that's a
	 *            subclass of ASTNode
	 * @return last position of the last node in the list + 1
	 */
	public static int getEndPosition(List<?> nodes) {
		Object lastNodeObject = null;
		for (Object node : nodes)
			lastNodeObject = node;

		return getEndPosition((ASTNode) lastNodeObject);
	}

	public static String simpleNameFromQualifiedName(String qualifiedName) {
		int lastPeriodIndex = qualifiedName.lastIndexOf('.');
		if (lastPeriodIndex == -1)
			return qualifiedName;
		else return qualifiedName.substring(lastPeriodIndex) + 1;
	}
}

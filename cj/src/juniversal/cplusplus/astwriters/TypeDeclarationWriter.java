package juniversal.cplusplus.astwriters;
import java.util.ArrayList;
import java.util.List;

import juniversal.ASTUtil;
import juniversal.AccessLevel;
import juniversal.CJException;
import juniversal.cplusplus.Context;
import juniversal.cplusplus.OutputType;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;


public class TypeDeclarationWriter extends ASTWriter {
	public TypeDeclarationWriter(ASTWriters astWriters) {
		super(astWriters);
	}

	public void write(ASTNode node, Context context) {
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;

		if (context.getOutputType() == OutputType.HEADER)
			writeHeader(typeDeclaration, context);
		else writeSource(typeDeclaration, context);
	}

	private void writeHeader(TypeDeclaration typeDeclaration, Context context) {
		String name = typeDeclaration.getName().getIdentifier();
		String multiIncludeDefine = name.toUpperCase() + "_H";

		context.writeln("#ifndef " + multiIncludeDefine);
		context.writeln("#define " + multiIncludeDefine);
		context.writeln();

		// Javadoc, if any, before the type declaration is included in the source range of
		// this node; copy it if present
		context.copySpaceAndComments();

		// Skip the modifiers and the space/comments following them
		context.skipModifiers(typeDeclaration.modifiers());
		context.skipSpaceAndComments();
		context.matchAndWrite("class");

		// TODO: Handle super classes & super interfaces
		// TODO: Handle generics

		context.copySpaceAndComments();
		context.matchAndWrite(typeDeclaration.getName().getIdentifier());

		context.copySpaceAndComments();
		context.matchAndWrite("{");

		context.copySpaceAndCommentsUntilEOL();
		context.writeln();

		//context.getCPPWriter().incrementByPreferredIndent();

		writeMethodDeclarations(typeDeclaration, context);
		writeFieldDeclarations(typeDeclaration, context);

		/*
		 * // START HERE; SWITCH BETWEEN HEADER, SOURCE, AND BOTH OUTPUT if
		 * (typeDeclaration.isInterface()) context.match
		 */

		context.writeln("}");
		context.writeln();
		context.writeln("#endif // " + multiIncludeDefine);

		context.setPosition(ASTUtil.getEndPosition(typeDeclaration));
	}

	private void writeFieldDeclarations(TypeDeclaration typeDeclaration, Context context) {

		AccessLevel lastAccessLevel = null;
		for (Object bodyDeclaration : typeDeclaration.bodyDeclarations()) {
			if (bodyDeclaration instanceof FieldDeclaration) {

				FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;
				AccessLevel accessLevel = ASTUtil.getAccessModifier(fieldDeclaration.modifiers());

				// Write out the access level header, if it's changed
				if (accessLevel != lastAccessLevel) {
					if (lastAccessLevel != null)
						context.getCPPWriter().decrementByPreferredIndent();

					if (accessLevel == AccessLevel.PUBLIC || accessLevel == AccessLevel.PACKAGE)
						context.writeln("public:");
					else if (accessLevel == AccessLevel.PROTECTED)
						context.writeln("protected:");
					else if (accessLevel == AccessLevel.PRIVATE)
						context.writeln("private:");

					context.getCPPWriter().incrementByPreferredIndent();
				}

				context.setPosition(fieldDeclaration.getStartPosition());

				// TODO: Handle non-Javadoc comments as well

				// Javadoc, if any, before the type declaration is included in the source range of
				// this node; copy it if present
				context.copySpaceAndComments();

				getASTWriters().writeNode(fieldDeclaration, context);

				context.copySpaceAndCommentsUntilEOL();
				context.writeln();

				lastAccessLevel = accessLevel;
			}
		}

		context.getCPPWriter().decrementByPreferredIndent();
	}

	private void writeMethodDeclarations(TypeDeclaration typeDeclaration, Context context) {
		ArrayList<MethodDeclaration> publicMethods = new ArrayList<MethodDeclaration>();
		ArrayList<MethodDeclaration> protectedMethods = new ArrayList<MethodDeclaration>();
		ArrayList<MethodDeclaration> privateMethods = new ArrayList<MethodDeclaration>();

		for (Object bodyDeclaration : typeDeclaration.bodyDeclarations()) {
			if (bodyDeclaration instanceof MethodDeclaration) {
				MethodDeclaration MethodDeclaration = (MethodDeclaration) bodyDeclaration;
				AccessLevel accessLevel = ASTUtil.getAccessModifier(MethodDeclaration.modifiers());

				if (accessLevel == AccessLevel.PUBLIC || accessLevel == AccessLevel.PACKAGE)
					publicMethods.add(MethodDeclaration);
				else if (accessLevel == AccessLevel.PROTECTED)
					protectedMethods.add(MethodDeclaration);
				else privateMethods.add(MethodDeclaration);
			}
			else if (! (bodyDeclaration instanceof FieldDeclaration)) 
				throw new CJException("Unexpected bodyDeclaration type" + bodyDeclaration.getClass());
		}

		writeMethodDeclarationsForAccessLevel(publicMethods, context, AccessLevel.PUBLIC);
		writeMethodDeclarationsForAccessLevel(protectedMethods, context, AccessLevel.PROTECTED);
		writeMethodDeclarationsForAccessLevel(privateMethods, context, AccessLevel.PRIVATE);
	}

	private void writeMethodDeclarationsForAccessLevel(List<MethodDeclaration> methodDeclarations, Context context, AccessLevel accessLevel) {
		if (methodDeclarations.size() == 0)
			return;

		if (accessLevel == AccessLevel.PUBLIC || accessLevel == AccessLevel.PACKAGE)
			context.writeln("public:");
		else if (accessLevel == AccessLevel.PROTECTED)
			context.writeln("protected:");
		else if (accessLevel == AccessLevel.PRIVATE)
			context.writeln("private:");

		context.getCPPWriter().incrementByPreferredIndent();

		for (MethodDeclaration methodDeclaration : methodDeclarations) {
			context.setPosition(methodDeclaration.getStartPosition());

			// TODO: Handle non-Javadoc comments as well

			// Javadoc, if any, before the method declaration is included in the source range of this
			// node; copy it if present
			context.copySpaceAndComments();

			getASTWriters().writeNode(methodDeclaration, context);

			context.copySpaceAndCommentsUntilEOL();
			context.writeln();
		}

		context.getCPPWriter().decrementByPreferredIndent();
	}

	private void writeSource(TypeDeclaration typeDeclaration, Context context) {
		for (Object bodyDeclaration : typeDeclaration.bodyDeclarations()) {
			if (bodyDeclaration instanceof MethodDeclaration) {
				MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;

				if (methodDeclaration.getBody() == null)
					continue; 
				
				context.setPosition(methodDeclaration.getStartPosition());

				int additionalIndent = context.getSourceLogicalColumn(methodDeclaration.getStartPosition());

				int previousIndent = context.getCPPWriter().setAdditionalIndentation(-1 * additionalIndent);
				context.setWritingMethodImplementation(true);

				// TODO: Handle non-Javadoc comments as well. Copy comments maybe in node writer for
				// this since Javadoc is expected there. Also, write blank lines between methods.

				// Javadoc, if any, before the method declaration is included in the source range of this
				// node; copy it if present
				context.copySpaceAndComments();

				getASTWriters().writeNode(methodDeclaration, context);

				context.setWritingMethodImplementation(false);
				context.getCPPWriter().setAdditionalIndentation(previousIndent);

				context.copySpaceAndCommentsUntilEOL();
				context.writeln();
			}
		}

		context.setPosition(ASTUtil.getEndPosition(typeDeclaration));
	}
}

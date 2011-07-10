package juniversal.cplusplus.astwriters;
import java.util.ArrayList;
import java.util.List;

import juniversal.ASTUtil;
import juniversal.AccessLevel;
import juniversal.JUniversalException;
import juniversal.UserViewableException;
import juniversal.cplusplus.Context;
import juniversal.cplusplus.OutputType;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;


public class TypeDeclarationWriter extends ASTWriter {
	public TypeDeclarationWriter(ASTWriters astWriters) {
		super(astWriters);
	}

	public void write(ASTNode node, Context context) {
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		
		// TODO: Support nested classes
		if (typeDeclaration.getTypes().length != 0)
			context.throwSourceNotSupported("Classes with inner classes/types aren't currently supported");

		SimpleName oldTypeName = context.getTypeDeclarationName();
		context.setTypeDeclarationName(typeDeclaration.getName());

		if (context.getOutputType() == OutputType.HEADER)
			writeHeader(typeDeclaration, context);
		else writeSource(typeDeclaration, context);

		context.setTypeDeclarationName(oldTypeName);
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
		context.copySpaceAndComments();

		// TODO: Handle super classes & super interfaces
		// TODO: Handle generics

		context.matchAndWrite(typeDeclaration.getName().getIdentifier());
		context.copySpaceAndComments();
		
		context.matchAndWrite("{");
		context.copySpaceAndCommentsUntilEOL();
		context.writeln();

		//context.getCPPWriter().incrementByPreferredIndent();

		boolean[] outputSomethingForClass = new boolean[1];
		outputSomethingForClass[0] = false;
		
		writeMethodDeclarations(typeDeclaration, context, outputSomethingForClass);
		writeFieldDeclarations(typeDeclaration, context, outputSomethingForClass);

		/*
		 * // START HERE; SWITCH BETWEEN HEADER, SOURCE, AND BOTH OUTPUT if
		 * (typeDeclaration.isInterface()) context.match
		 */

		context.writeln("};");
		context.writeln();
		context.writeln("#endif // " + multiIncludeDefine);

		context.setPosition(ASTUtil.getEndPosition(typeDeclaration));
	}

	private void writeMethodDeclarations(TypeDeclaration typeDeclaration, Context context,
			boolean[] outputSomethingForClass) {

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
				else
					privateMethods.add(MethodDeclaration);
			} else if (!(bodyDeclaration instanceof FieldDeclaration))
				throw new JUniversalException("Unexpected bodyDeclaration type" + bodyDeclaration.getClass());
		}

		writeMethodDeclarationsForAccessLevel(publicMethods, context, AccessLevel.PUBLIC, outputSomethingForClass);
		writeMethodDeclarationsForAccessLevel(protectedMethods, context, AccessLevel.PROTECTED, outputSomethingForClass);
		writeMethodDeclarationsForAccessLevel(privateMethods, context, AccessLevel.PRIVATE, outputSomethingForClass);
	}

	private void writeMethodDeclarationsForAccessLevel(List<MethodDeclaration> methodDeclarations, Context context,
			AccessLevel accessLevel, boolean[] outputSomethingForClass) {
		if (methodDeclarations.size() == 0)
			return;

		// If we've already output something for the class, add a blank line separator 
		if (outputSomethingForClass[0])
			context.writeln();

		if (accessLevel == AccessLevel.PUBLIC || accessLevel == AccessLevel.PACKAGE)
			context.writeln("public:");
		else if (accessLevel == AccessLevel.PROTECTED)
			context.writeln("protected:");
		else if (accessLevel == AccessLevel.PRIVATE)
			context.writeln("private:");
		
		outputSomethingForClass[0] = true;

		for (MethodDeclaration methodDeclaration : methodDeclarations) {

			// When writing the class definition, don't include method comments. So skip over the
			// Javadoc, which will just be by the implementation.
			context.setPosition(methodDeclaration.getStartPosition());
			context.skipSpaceAndComments();

			// If there's nothing else on the line before the method declaration besides whitespace,
			// then indent by that amount; that's the typical case. However, if there's something on
			// the line before the method (e.g. a comment or previous declaration of something else
			// in the class), then the source is in some odd format, so just ignore that and use the
			// standard indent.
			context.skipSpacesAndTabsBackward();
			if (context.getSourceLogicalColumn() == 0)
				context.copySpaceAndComments();
			else
				context.writeSpaces(context.getPreferredIndent());

			getASTWriters().writeNode(methodDeclaration, context);

			// Copy any trailing comment associated with the method; that's sometimes there for
			// one-liners.
			context.copySpaceAndCommentsUntilEOL();

			context.writeln();
		}
	}

	private void writeFieldDeclarations(TypeDeclaration typeDeclaration, Context context,
			boolean[] outputSomethingForClass) {

		AccessLevel lastAccessLevel = null;
		for (Object bodyDeclaration : typeDeclaration.bodyDeclarations()) {
			if (bodyDeclaration instanceof FieldDeclaration) {

				FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;
				AccessLevel accessLevel = ASTUtil.getAccessModifier(fieldDeclaration.modifiers());

				// Write out the access level header, if it's changed
				if (accessLevel != lastAccessLevel) {

					// If we've already output something for the class, add a blank line separator 
					if (outputSomethingForClass[0])
						context.writeln();

					if (accessLevel == AccessLevel.PUBLIC || accessLevel == AccessLevel.PACKAGE)
						context.writeln("public: // Data");
					else if (accessLevel == AccessLevel.PROTECTED)
						context.writeln("protected: // Data");
					else if (accessLevel == AccessLevel.PRIVATE)
						context.writeln("private: // Data");

					outputSomethingForClass[0] = true;
				}

				// Skip back to the beginning of the comments, ignoring any comments associated with
				// the previous node
				context.setPosition(fieldDeclaration.getStartPosition());
				context.backwardSkipSpaceAndComments();
				context.skipSpaceAndCommentsUntilEOL();
				context.skipNewline();

				context.copySpaceAndComments();

				// If the member is on the same line as other members, for some unusual reason, then
				// the above code won't indent it. So indent it here since in our output every
				// member/method is on it's own line in the class definition.
				if (context.getSourceLogicalColumn() == 0)
					context.writeSpaces(context.getPreferredIndent());

				getASTWriters().writeNode(fieldDeclaration, context);

				context.copySpaceAndCommentsUntilEOL();
				context.writeln();

				lastAccessLevel = accessLevel;
			}
		}
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

				// Skip back to the beginning of the comments, ignoring any comments associated with
				// the previous node
				context.setPosition(methodDeclaration.getStartPosition());
				context.backwardSkipSpaceAndComments();
				context.skipSpaceAndCommentsUntilEOL();
				context.skipNewline();

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

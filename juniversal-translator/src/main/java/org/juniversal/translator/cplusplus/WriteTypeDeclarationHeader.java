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

package org.juniversal.translator.cplusplus;

import java.util.ArrayList;
import java.util.List;

import org.juniversal.translator.core.*;

import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;

public class WriteTypeDeclarationHeader {
	private final TypeDeclaration typeDeclaration;
	private final Context context;
	private final SourceFileWriter sourceFileWriter;
	private final int typeIndent;
	private boolean outputSomethingForType;

	public WriteTypeDeclarationHeader(TypeDeclaration typeDeclaration, Context context, SourceFileWriter sourceFileWriter) {
		this.typeDeclaration = typeDeclaration;
		this.context = context;
		this.sourceFileWriter = sourceFileWriter;

		// Skip the modifiers and the space/comments following them
		context.skipModifiers(typeDeclaration.modifiers());
        context.skipSpaceAndComments();

		// Remember how much the type is indented (typically only nested types are indented), so we
		// can use that in determining the "natural" indent for some things inside the type
		// declaration.
		typeIndent = context.getTargetWriter().getCurrColumn();

		boolean isInterface = typeDeclaration.isInterface();

		@SuppressWarnings("unchecked")
		List<TypeParameter> typeParameters = (List<TypeParameter>) typeDeclaration.typeParameters();

		boolean isGeneric = !typeParameters.isEmpty();

		if (isGeneric) {
            context.write("template ");
			//sCPlusPlusASTWriter.writeTypeParameters(typeParameters, true);
            context.write(" ");
		}

		if (isInterface)
            context.matchAndWrite("interface", "class");
		else
            context.matchAndWrite("class");

        context.copySpaceAndComments();
        context.matchAndWrite(typeDeclaration.getName().getIdentifier());

		// Skip past the type parameters
		if (isGeneric) {
			context.setPosition(ASTUtil.getEndPosition(typeParameters));
            context.skipSpaceAndComments();
            context.match(">");
		}

		writeSuperClassAndInterfaces();

        context.copySpaceAndComments();
        context.matchAndWrite("{");
        context.copySpaceAndCommentsUntilEOL();
        context.writeln();

		// context.getTargetWriter().incrementByPreferredIndent();

		outputSomethingForType = false;
		writeNestedTypes();
		writeMethods();
		writeSuperDefinition();
		writeFields();

        context.writeSpaces(typeIndent);
        context.write("};");

		context.setPosition(ASTUtil.getEndPosition(typeDeclaration));
	}

	private void writeSuperClassAndInterfaces() {
		Type superclassType = typeDeclaration.getSuperclassType();

		@SuppressWarnings("unchecked")
		List<Type> superInterfaceTypes = (List<Type>) typeDeclaration.superInterfaceTypes();

		if (superclassType == null)
            context.write(" : public Object");
		else {
            context.copySpaceAndComments();
            context.matchAndWrite("extends", ": public");

            context.copySpaceAndComments();
			sourceFileWriter.writeNode(superclassType);
		}

		// Write out the super interfaces, if any
		boolean firstInterface = true;
		for (Object superInterfaceTypeObject : superInterfaceTypes) {
			Type superInterfaceType = (Type) superInterfaceTypeObject;

			if (firstInterface) {
                context.skipSpaceAndComments();
                context.matchAndWrite("implements", ", public");
			} else {
                context.copySpaceAndComments();
                context.matchAndWrite(",", ", public");
			}

			// Ensure there's at least a space after the "public" keyword (not required in Java
			// which just has the comma there)
			int originalPosition = context.getPosition();
            context.copySpaceAndComments();
			if (context.getPosition() == originalPosition)
                context.write(" ");

			sourceFileWriter.writeNode(superInterfaceType);
			firstInterface = false;
		}
	}

	@SuppressWarnings("unchecked")
	private void writeNestedTypes() {
		ArrayList<TypeDeclaration> publicTypes = new ArrayList<TypeDeclaration>();
		ArrayList<TypeDeclaration> protectedTypes = new ArrayList<TypeDeclaration>();
		ArrayList<TypeDeclaration> privateTypes = new ArrayList<TypeDeclaration>();

		for (BodyDeclaration bodyDeclaration : (List<BodyDeclaration>) typeDeclaration.bodyDeclarations()) {
			if (bodyDeclaration instanceof TypeDeclaration) {
				TypeDeclaration nestedTypeDeclaration = (TypeDeclaration) bodyDeclaration;
				AccessLevel accessLevel = ASTUtil.getAccessModifier(nestedTypeDeclaration.modifiers());

				if (accessLevel == AccessLevel.PUBLIC || accessLevel == AccessLevel.PACKAGE)
					publicTypes.add(nestedTypeDeclaration);
				else if (accessLevel == AccessLevel.PROTECTED)
					protectedTypes.add(nestedTypeDeclaration);
				else
					privateTypes.add(nestedTypeDeclaration);
			}
		}

		writeNestedTypeDeclarationsForAccessLevel(publicTypes, AccessLevel.PUBLIC);
		writeNestedTypeDeclarationsForAccessLevel(protectedTypes, AccessLevel.PROTECTED);
		writeNestedTypeDeclarationsForAccessLevel(privateTypes, AccessLevel.PRIVATE);
	}

	private void writeNestedTypeDeclarationsForAccessLevel(List<TypeDeclaration> typeDeclarations, AccessLevel accessLevel) {
		if (typeDeclarations.size() == 0)
			return;

		// If we've already output something for the class, add a blank line separator
		if (outputSomethingForType)
            context.writeln();

		writeAccessLevelGroup(accessLevel, " // Nested class(es)");

		outputSomethingForType = true;

		for (TypeDeclaration nestedTypeDeclaration : typeDeclarations) {

			context.setPositionToStartOfNodeSpaceAndComments(nestedTypeDeclaration);
            context.copySpaceAndComments();

			sourceFileWriter.writeNode(nestedTypeDeclaration);

			// Copy any trailing comment associated with the class, on the same line as the closing
			// brace; rare but possible
            context.copySpaceAndCommentsUntilEOL();

            context.writeln();
		}
	}

	private void writeAccessLevelGroup(AccessLevel accessLevel, String headerComment) {
        context.writeSpaces(typeIndent);
		
		String headerText;
		if (accessLevel == AccessLevel.PUBLIC || accessLevel == AccessLevel.PACKAGE)
			headerText = "public:";
		else if (accessLevel == AccessLevel.PROTECTED)
			headerText = "protected:";
		else if (accessLevel == AccessLevel.PRIVATE)
			headerText = "private:";
		else throw new JUniversalException("Unknown access level: " + accessLevel);

        context.write(headerText);
		if (headerComment != null)
            context.write(headerComment);
        context.writeln();
	}

	private void writeMethods() {
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
			} else if (!(bodyDeclaration instanceof FieldDeclaration || bodyDeclaration instanceof TypeDeclaration))
				throw new JUniversalException("Unexpected bodyDeclaration type" + bodyDeclaration.getClass());
		}

		writeMethodsForAccessLevel(publicMethods, AccessLevel.PUBLIC);
		writeMethodsForAccessLevel(protectedMethods, AccessLevel.PROTECTED);
		writeMethodsForAccessLevel(privateMethods, AccessLevel.PRIVATE);
	}

	private void writeMethodsForAccessLevel(List<MethodDeclaration> methodDeclarations, AccessLevel accessLevel) {
		if (methodDeclarations.size() == 0)
			return;

		// If we've already output something for the class, add a blank line separator
		if (outputSomethingForType)
            context.writeln();

		writeAccessLevelGroup(accessLevel, null);

		outputSomethingForType = true;

		for (MethodDeclaration methodDeclaration : methodDeclarations) {

			// When writing the class definition, don't include method comments. So skip over the
			// Javadoc, which will just be by the implementation.
			context.setPositionToStartOfNode(methodDeclaration);

			// If there's nothing else on the line before the method declaration besides whitespace,
			// then indent by that amount; that's the typical case. However, if there's something on
			// the line before the method (e.g. a comment or previous declaration of something else
			// in the class), then the source is in some odd format, so just ignore that and use the
			// standard indent.
			context.skipSpacesAndTabsBackward();
			if (context.getSourceLogicalColumn() == 0)
                context.copySpaceAndComments();
			else {
                context.writeSpaces(context.getPreferredIndent());
				context.skipSpacesAndTabs();
			}

			sourceFileWriter.writeNode(methodDeclaration);

			// Copy any trailing comment associated with the method; that's sometimes there for
			// one-liners
            context.copySpaceAndCommentsUntilEOL();

            context.writeln();
		}
	}

	private void writeSuperDefinition() {
		// If we've already output something for the class, add a blank line separator
		if (outputSomethingForType)
            context.writeln();

		writeAccessLevelGroup(AccessLevel.PRIVATE, null);

        context.writeSpaces(typeIndent + context.getPreferredIndent());

        context.write("typedef ");

		Type superclassType = typeDeclaration.getSuperclassType();
		if (superclassType == null)
            context.write("Object");
		else sourceFileWriter.writeNodeAtDifferentPosition(superclassType, context);

        context.writeln(" super;");
	}

	private void writeFields() {
		AccessLevel lastAccessLevel = null;

		for (Object bodyDeclaration : typeDeclaration.bodyDeclarations()) {
			if (bodyDeclaration instanceof FieldDeclaration) {

				FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;
				AccessLevel accessLevel = ASTUtil.getAccessModifier(fieldDeclaration.modifiers());

				// Write out the access level header, if it's changed
				boolean firstItemForAccessLevel = accessLevel != lastAccessLevel;
				
				if (firstItemForAccessLevel) {

					// If we've already output something for the class, add a blank line separator
					if (outputSomethingForType)
                        context.writeln();

					writeAccessLevelGroup(accessLevel, " // Data");
				}

				// Skip back to the beginning of the comments, ignoring any comments associated with
				// the previous node
				context.setPosition(fieldDeclaration.getStartPosition());
                context.skipSpaceAndCommentsBackward();
                context.skipSpaceAndCommentsUntilEOL();
				context.skipNewline();

				if (firstItemForAccessLevel)
					context.skipBlankLines();

                context.copySpaceAndComments();

				// If the member is on the same line as other members, for some unusual reason, then
				// the above code won't indent it. So indent it here since in our output every
				// member/method is on it's own line in the class definition.
				if (context.getSourceLogicalColumn() == 0)
                    context.writeSpaces(context.getPreferredIndent());

				sourceFileWriter.writeNode(fieldDeclaration);

                context.copySpaceAndCommentsUntilEOL();
                context.writeln();

				outputSomethingForType = true;
				lastAccessLevel = accessLevel;
			}
		}
	}
}

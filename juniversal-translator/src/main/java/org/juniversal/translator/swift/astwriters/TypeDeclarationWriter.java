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

package org.juniversal.translator.swift.astwriters;

import org.eclipse.jdt.core.dom.*;
import org.juniversal.translator.core.ASTUtil;
import org.juniversal.translator.core.AccessLevel;
import org.juniversal.translator.core.Context;
import org.juniversal.translator.core.JUniversalException;

import java.util.ArrayList;
import java.util.List;


public class TypeDeclarationWriter extends SwiftASTWriter {
    private SwiftASTWriters swiftASTWriters;

    public TypeDeclarationWriter(SwiftASTWriters swiftASTWriters) {
        super(swiftASTWriters);
    }

    public void write(ASTNode node) {
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;

		TypeDeclaration oldTypeDeclaration = getContext().getTypeDeclaration();
        getContext().setTypeDeclaration(typeDeclaration);

        //new WriteTypeDeclaration(typeDeclaration, context, swiftASTWriters);

        getContext().setTypeDeclaration(oldTypeDeclaration);
	}

/*
    public WriteTypeDeclaration(TypeDeclaration typeDeclaration, Context context, SwiftASTWriters astWriters) {
        this.typeDeclaration = typeDeclaration;
        this.context = context;
        this.astWriters = astWriters;

        // Skip the modifiers and the space/comments following them
        context.skipModifiers(typeDeclaration.modifiers());
        skipSpaceAndComments();

        // Remember how much the type is indented (typically only nested types are indented), so we can use that in determining the "natural" indent for some things inside the type declaration.
        typeIndent = context.getTargetWriter().getCurrColumn();

        boolean isInterface = typeDeclaration.isInterface();

        @SuppressWarnings("unchecked")
        List<TypeParameter> typeParameters = (List<TypeParameter>) typeDeclaration.typeParameters();

        boolean isGeneric = !typeParameters.isEmpty();

        // TODO: Handle generics
        if (isGeneric) {
            write("template ");
            ASTWriterUtil.writeTypeParameters(typeParameters, true, context);
            write(" ");
        }

        // TODO: Handle interfaces
        if (isInterface)
            matchAndWrite("interface", "class");
        else
            matchAndWrite("class");

        copySpaceAndComments();
        matchAndWrite(typeDeclaration.getName().getIdentifier());

        // Skip past the type parameters
        if (isGeneric) {
            context.setPosition(ASTUtil.getEndPosition(typeParameters));
            skipSpaceAndComments();
            match(">");
        }

        // TODO: Implement this
        writeSuperClassAndInterfaces();

        copySpaceAndComments();
        matchAndWrite("{");
        copySpaceAndCommentsUntilEOL();
        writeln();

        // context.getTargetWriter().incrementByPreferredIndent();

        outputSomethingForType = false;
        writeNestedTypes();    // TODO: Implement this
        writeSuperDefinition();   // TODO: Implement this

        for (Object bodyDeclaration : this.typeDeclaration.bodyDeclarations()) {
            if (bodyDeclaration instanceof FieldDeclaration) {
                writeField((FieldDeclaration) bodyDeclaration);
            }
            else if (bodyDeclaration instanceof MethodDeclaration) {
                writeMethod((MethodDeclaration) bodyDeclaration);
            }
        }

        writeSpaces(typeIndent);
        write("};");

        context.setPosition(ASTUtil.getEndPosition(typeDeclaration));
    }

    private void writeSuperClassAndInterfaces() {
        Type superclassType = typeDeclaration.getSuperclassType();

        @SuppressWarnings("unchecked")
        List<Type> superInterfaceTypes = (List<Type>) typeDeclaration.superInterfaceTypes();

        if (superclassType == null)
            write(" : public Object");
        else {
            copySpaceAndComments();
            matchAndWrite("extends", ": public");

            copySpaceAndComments();
            astWriters.writeNode(superclassType);
        }

        // Write out the super interfaces, if any
        boolean firstInterface = true;
        for (Object superInterfaceTypeObject : superInterfaceTypes) {
            Type superInterfaceType = (Type) superInterfaceTypeObject;

            if (firstInterface) {
                skipSpaceAndComments();
                matchAndWrite("implements", ", public");
            } else {
                copySpaceAndComments();
                matchAndWrite(",", ", public");
            }

            // Ensure there's at least a space after the "public" keyword (not required in Java
            // which just has the comma there)
            int originalPosition = context.getPosition();
            copySpaceAndComments();
            if (context.getPosition() == originalPosition)
                write(" ");

            astWriters.writeNode(superInterfaceType);
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
            writeln();

        writeAccessLevelGroup(accessLevel, " // Nested class(es)");

        outputSomethingForType = true;

        for (TypeDeclaration nestedTypeDeclaration : typeDeclarations) {

            context.setPositionToStartOfNodeSpaceAndComments(nestedTypeDeclaration);
            copySpaceAndComments();

            astWriters.writeNode(nestedTypeDeclaration);

            // Copy any trailing comment associated with the class, on the same line as the closing
            // brace; rare but possible
            copySpaceAndCommentsUntilEOL();

            writeln();
        }
    }

    private void writeAccessLevelGroup(AccessLevel accessLevel, String headerComment) {
        writeSpaces(typeIndent);

        String headerText;
        if (accessLevel == AccessLevel.PUBLIC || accessLevel == AccessLevel.PACKAGE)
            headerText = "public:";
        else if (accessLevel == AccessLevel.PROTECTED)
            headerText = "protected:";
        else if (accessLevel == AccessLevel.PRIVATE)
            headerText = "private:";
        else throw new JUniversalException("Unknown access level: " + accessLevel);

        write(headerText);
        if (headerComment != null)
            write(headerComment);
        writeln();
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
            writeln();

        writeAccessLevelGroup(accessLevel, null);

        outputSomethingForType = true;

        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            writeMethod(methodDeclaration);
        }
    }

    private void writeMethod(MethodDeclaration methodDeclaration) {
        //AccessLevel accessLevel = ASTUtil.getAccessModifier(MethodDeclaration.modifiers());

        // Copy the Javadoc for the method
        copySpaceAndComments();

*/
/*
        // When writing the class definition, don't include method comments. So skip over the
        // Javadoc, which will just be by the implementation.
        context.setPositionToStartOfNode(methodDeclaration);
*//*


        astWriters.writeNode(methodDeclaration);

        // Copy any trailing comment associated with the method; that's sometimes there for
        // one-liners
        copySpaceAndCommentsUntilEOL();

        writeln();
    }

    private void writeSuperDefinition() {
        // If we've already output something for the class, add a blank line separator
        if (outputSomethingForType)
            writeln();

        writeAccessLevelGroup(AccessLevel.PRIVATE, null);

        writeSpaces(typeIndent + context.getPreferredIndent());

        write("typedef ");

        Type superclassType = typeDeclaration.getSuperclassType();
        if (superclassType == null)
            write("Object");
        else astWriters.writeNodeAtDifferentPosition(superclassType, context);

        writeln(" super;");
    }

    private void writeField(FieldDeclaration fieldDeclaration) {
        AccessLevel accessLevel = ASTUtil.getAccessModifier(fieldDeclaration.modifiers());

        // Write out the access level header, if it's changed
        boolean firstItemForAccessLevel = true; // accessLevel != lastAccessLevel;

        if (firstItemForAccessLevel) {

            // If we've already output something for the class, add a blank line separator
            if (outputSomethingForType)
                writeln();

            writeAccessLevelGroup(accessLevel, " // Data");
        }

        // Skip back to the beginning of the comments, ignoring any comments associated with
        // the previous node
        context.setPosition(fieldDeclaration.getStartPosition());
        skipSpaceAndCommentsBackward();
        skipSpaceAndCommentsUntilEOL();
        context.skipNewline();

        if (firstItemForAccessLevel)
            context.skipBlankLines();

        copySpaceAndComments();

        // If the member is on the same line as other members, for some unusual reason, then
        // the above code won't indent it. So indent it here since in our output every
        // member/method is on it's own line in the class definition.
        if (context.getSourceLogicalColumn() == 0)
            writeSpaces(context.getPreferredIndent());

        astWriters.writeNode(fieldDeclaration);

        copySpaceAndCommentsUntilEOL();
        writeln();

        outputSomethingForType = true;
    }
*/
}

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

package org.juniversal.translator.csharp;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.juniversal.translator.core.ASTUtil.*;

public class TypeDeclarationWriter extends CSharpASTNodeWriter<TypeDeclaration> {
    public TypeDeclarationWriter(CSharpSourceFileWriter cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override
    public void write(TypeDeclaration typeDeclaration) {
        @Nullable AbstractTypeDeclaration outerTypeDeclaration = getContext().getTypeDeclaration();
        getContext().setTypeDeclaration(typeDeclaration);

        try {
            if (isFunctionalInterface(typeDeclaration))   {
                writeFunctionalInterfaceAsDelegate(typeDeclaration);
                return;
            }

            List<?> modifiers = typeDeclaration.modifiers();
            if (outerTypeDeclaration != null) {
                if (! containsStatic(modifiers))
                    throw sourceNotSupported("Only static nested classes are supported, as C# doesn't support inner (non static) classes.  Make the nested class static and pass in the outer instance to the constructor, to simulate an inner class.");

                if (isInterface(outerTypeDeclaration))
                    throw sourceNotSupported("C# doesn't support nested types inside an interface (nesting in a class is OK, but not an interface).   Move the nested type outside the interface.");
            }

            boolean isInterface = typeDeclaration.isInterface();

            List typeParameters = typeDeclaration.typeParameters();

            boolean isGeneric = !typeParameters.isEmpty();

            writeAccessModifier(modifiers);

            if (isFinal(typeDeclaration))
                writeSealedModifier();
            else if (isAbstract(typeDeclaration))
                writeAbstractModifier();

            // Skip the modifiers
            skipModifiers(modifiers);

            if (isInterface)
                matchAndWrite("interface");
            else
                matchAndWrite("class");

            copySpaceAndComments();
            writeNode(typeDeclaration.getName());

            if (isGeneric) {
                copySpaceAndComments();
                matchAndWrite("<");

                writeCommaDelimitedNodes(typeParameters, (TypeParameter typeParameter) -> {
                    copySpaceAndComments();
                    writeNode(typeParameter.getName());

                    // Skip any constraints; they'll be written out at the end of the declaration line
                    setPositionToEndOfNode(typeParameter);
                });

                copySpaceAndComments();
                matchAndWrite(">");
            }

            // TODO: Implement this
            writeSuperClassAndInterfaces(typeDeclaration);

            if (isGeneric)
                writeTypeParameterConstraints(typeParameters);

            copySpaceAndComments();
            matchAndWrite("{");

            forEach(typeDeclaration.bodyDeclarations(), (BodyDeclaration bodyDeclaration) -> {
                copySpaceAndCommentsTranslatingJavadoc(bodyDeclaration.getJavadoc());
                writeNode(bodyDeclaration);
            });

            copySpaceAndComments();
            matchAndWrite("}");
        } finally {
            getContext().setTypeDeclaration(outerTypeDeclaration);
        }
    }

    private void writeFunctionalInterfaceAsDelegate(TypeDeclaration typeDeclaration) {
        MethodDeclaration functionalInterfaceMethod = getFunctionalInterfaceMethod(typeDeclaration);

        List typeDeclarationModifiers = typeDeclaration.modifiers();
        skipModifiers(typeDeclarationModifiers);
        writeAccessModifier(typeDeclarationModifiers);

        skipSpaceAndComments();
        matchAndWrite("interface", "delegate");

        copySpaceAndComments();

        setPositionToStartOfNode(functionalInterfaceMethod);
        skipModifiers(functionalInterfaceMethod.modifiers());
        skipSpaceAndComments();
        writeNode(functionalInterfaceMethod.getReturnType2());

        copySpaceAndComments();
        // Write the interface name, not the method name, as the name of the delegate
        matchAndWrite(
                functionalInterfaceMethod.getName().getIdentifier(),
                typeDeclaration.getName().getIdentifier());

        copySpaceAndComments();
        matchAndWrite("(");

        forEach(functionalInterfaceMethod.parameters(), (SingleVariableDeclaration singleVariableDeclaration, boolean first) -> {
            if (!first) {
                copySpaceAndComments();
                matchAndWrite(",");
            }

            copySpaceAndComments();
            writeNode(singleVariableDeclaration);
        });

        copySpaceAndComments();
        matchAndWrite(")");

        copySpaceAndComments();
        matchAndWrite(";");

        setPositionToEndOfNode(typeDeclaration);
    }

    private void writeSuperClassAndInterfaces(TypeDeclaration typeDeclaration) {
        Type superclassType = typeDeclaration.getSuperclassType();
        boolean isInterface = typeDeclaration.isInterface();

        if (superclassType != null) {
            copySpaceAndComments();
            matchAndWrite("extends", ":");

            copySpaceAndComments();
            writeNode(superclassType);
        }

        // Write out the super interfaces, if any
        forEach(typeDeclaration.superInterfaceTypes(), (Type superInterfaceType, boolean first) -> {
            if (first) {
                if (superclassType == null) {
                    copySpaceAndComments();

                    // An interface extends another interface whereas a class implements interfaces
                    matchAndWrite(isInterface ? "extends" : "implements", ":");
                } else {
                    skipSpaceAndComments();
                    matchAndWrite("implements", ", ");
                }
            } else {
                copySpaceAndComments();
                matchAndWrite(",");
            }

            // Ensure there's at least a space after the "public" keyword (not required in Java which just has the
            // comma there)
            int originalPosition = getPosition();
            copySpaceAndComments();
            if (getPosition() == originalPosition)
                write(" ");

            writeNode(superInterfaceType);
        });
    }
}

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

package org.juniversal.translator.csharp.astwriters;

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.ASTUtil;

import java.util.List;

import static org.juniversal.translator.core.ASTUtil.*;

public class TypeDeclarationWriter extends CSharpASTWriter<TypeDeclaration> {
    public TypeDeclarationWriter(CSharpASTWriters cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override
    public void write(TypeDeclaration typeDeclaration) {
        @Nullable TypeDeclaration outerTypeDeclaration = getContext().getTypeDeclaration();
        getContext().setTypeDeclaration(typeDeclaration);

        try {
            List<?> modifiers = typeDeclaration.modifiers();
            if (outerTypeDeclaration != null && ! containsStatic(modifiers))
                throw sourceNotSupported("Only static nested classes are supported, as C# and Swift don't support inner (non static) classes.  Make the nested class static and pass in the outer instance to the constructor, to simulate an inner class.");

            boolean isInterface = typeDeclaration.isInterface();

            List typeParameters = typeDeclaration.typeParameters();

            boolean isGeneric = !typeParameters.isEmpty();

            writeAccessModifier(modifiers);

            if (ASTUtil.containsFinal(modifiers))
                writeSealedModifier();

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

            writeNodes(typeDeclaration.bodyDeclarations());

            copySpaceAndComments();
            matchAndWrite("}");
        } finally {
            getContext().setTypeDeclaration(outerTypeDeclaration);
        }
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

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
import org.juniversal.translator.core.Context;

import java.util.List;

import static org.juniversal.translator.core.ASTUtil.*;

public class TypeDeclarationWriter extends CSharpASTWriter<TypeDeclaration> {
    public TypeDeclarationWriter(CSharpASTWriters cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override
    public void write(Context context, TypeDeclaration typeDeclaration) {
        @Nullable TypeDeclaration outerTypeDeclaration = context.getTypeDeclaration();
        context.setTypeDeclaration(typeDeclaration);

        try {
            List<?> modifiers = typeDeclaration.modifiers();
            if (outerTypeDeclaration != null && ! containsStatic(modifiers))
                context.throwSourceNotSupported("Only static nested classes are supported, as C# and Swift don't support inner (non static) classes.  Make the nested class static and pass in the outer instance to the constructor, to simulate an inner class.");

            boolean isInterface = typeDeclaration.isInterface();

            List typeParameters = typeDeclaration.typeParameters();

            boolean isGeneric = !typeParameters.isEmpty();

            writeAccessModifier(context, modifiers);

            if (ASTUtil.containsFinal(modifiers))
                writeSealedModifier(context);

            // Skip the modifiers
            context.skipModifiers(modifiers);

            if (isInterface)
                context.matchAndWrite("interface");
            else
                context.matchAndWrite("class");

            context.copySpaceAndComments();
            writeNode(context, typeDeclaration.getName());

            if (isGeneric) {
                context.copySpaceAndComments();
                context.matchAndWrite("<");

                writeCommaDelimitedNodes(context, typeParameters, (TypeParameter typeParameter) -> {
                    context.copySpaceAndComments();
                    writeNode(context, typeParameter.getName());

                    // Skip any constraints; they'll be written out at the end of the declaration line
                    context.setPositionToEndOfNode(typeParameter);
                });

                context.copySpaceAndComments();
                context.matchAndWrite(">");
            }

            // TODO: Implement this
            writeSuperClassAndInterfaces(context, typeDeclaration);

            if (isGeneric)
                writeTypeParameterConstraints(context, typeParameters);

            context.copySpaceAndComments();
            context.matchAndWrite("{");

            writeNodes(context, typeDeclaration.bodyDeclarations());

            context.copySpaceAndComments();
            context.matchAndWrite("}");
        } finally {
            context.setTypeDeclaration(outerTypeDeclaration);
        }
    }

    private void writeSuperClassAndInterfaces(Context context, TypeDeclaration typeDeclaration) {
        Type superclassType = typeDeclaration.getSuperclassType();
        boolean isInterface = typeDeclaration.isInterface();

        if (superclassType != null) {
            context.copySpaceAndComments();
            context.matchAndWrite("extends", ":");

            context.copySpaceAndComments();
            writeNode(context, superclassType);
        }

        // Write out the super interfaces, if any
        forEach(typeDeclaration.superInterfaceTypes(), (Type superInterfaceType, boolean first) -> {
            if (first) {
                if (superclassType == null) {
                    context.copySpaceAndComments();

                    // An interface extends another interface whereas a class implements interfaces
                    context.matchAndWrite(isInterface ? "extends" : "implements", ":");
                } else {
                    context.skipSpaceAndComments();
                    context.matchAndWrite("implements", ", ");
                }
            } else {
                context.copySpaceAndComments();
                context.matchAndWrite(",");
            }

            // Ensure there's at least a space after the "public" keyword (not required in Java which just has the
            // comma there)
            int originalPosition = context.getPosition();
            context.copySpaceAndComments();
            if (context.getPosition() == originalPosition)
                context.write(" ");

            writeNode(context, superInterfaceType);
        });
    }
}

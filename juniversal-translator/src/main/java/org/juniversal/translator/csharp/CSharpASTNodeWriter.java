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
import org.juniversal.translator.core.ASTNodeWriter;
import org.juniversal.translator.core.AccessLevel;
import org.juniversal.translator.core.JUniversalException;

import java.util.ArrayList;
import java.util.List;

import static org.juniversal.translator.core.ASTUtil.getAccessModifier;


public abstract class CSharpASTNodeWriter<T extends ASTNode> extends ASTNodeWriter<T> {
    private CSharpSourceFileWriter cSharpASTWriters;

    protected CSharpASTNodeWriter(CSharpSourceFileWriter cSharpASTWriters) {
        this.cSharpASTWriters = cSharpASTWriters;
    }

    @Override
    protected CSharpSourceFileWriter getSourceFileWriter() {
        return cSharpASTWriters;
    }

    public void writeAccessModifier(List<?> modifiers) {
        AccessLevel accessLevel = getAccessModifier(modifiers);

        switch (accessLevel) {
            case PRIVATE:
                writeModifier("private");
                break;
            case PACKAGE:
                writeModifier("internal");
                break;
            case PROTECTED:
                writeModifier("protected internal");
                break;
            case PUBLIC:
                writeModifier("public");
                break;
        }
    }

    public void writeSealedModifier() {
        writeModifier("sealed");
    }

    public void writeReadonlyModifier() {
        writeModifier("readonly");
    }

    public void writeOverrideModifier() {
        writeModifier("override");
    }

    public void writeStaticModifier() {
        writeModifier("static");
    }

    public void writeModifier(String modifier) {
        write(modifier);
        write(" ");
    }

    public void writeVariableDeclaration(List<?> modifiers, Type type, List<?> fragments) {
        ensureModifiersJustFinalOrAnnotations(modifiers);
        skipModifiers(modifiers);

        // Write the type
        writeNode(type);

        // Write the variable declaration(s)
        writeCommaDelimitedNodes(fragments, (VariableDeclarationFragment variableDeclarationFragment) -> {
            copySpaceAndComments();
            writeNode(variableDeclarationFragment);
        });
    }

    public void writeMethodInvocationArgumentList(List<?> typeArguments, List<?> arguments) {
        // TODO: Handle type arguments
        if (!typeArguments.isEmpty())
            throw sourceNotSupported("Type arguments not currently supported on a method invocation");

        matchAndWrite("(");
        writeCommaDelimitedNodes(arguments);

        copySpaceAndComments();
        matchAndWrite(")");
    }

    public void writeTypeParameterConstraints(List typeParameters) {
        for (Object typeParameterObject : typeParameters) {
            TypeParameter typeParameter = (TypeParameter) typeParameterObject;

            boolean firstBound = true;
            for (Object typeBoundObject : typeParameter.typeBounds()) {
                Type typeBound = (Type) typeBoundObject;

                if (firstBound) {
                    write(" where ");
                    write(typeParameter.getName().getIdentifier());
                    write(" : ");
                } else write(", ");

                writeNodeFromOtherPosition(typeBound);

                firstBound = false;
            }
        }
    }

    public void writeWildcardTypeSyntheticName(ArrayList<WildcardType> wildcardTypes, WildcardType wildcardType) {
        int index = wildcardTypes.indexOf(wildcardType);
        if (index == -1)
            throw new JUniversalException("Wildcard type not found in list");

        if (wildcardTypes.size() == 1)
            write("TWildcard");
        else write("TWildcard" + (index + 1));
    }

    /*
        public void write(ASTNode node, Context context) {
            Modifier modifier = (Modifier) node;

            if (modifier.isPublic()) {
                matchAndWrite("public");
            } else if (modifier.isProtected()) {
                matchAndWrite("protected", "protected internal");
            } else if (modifier.isPrivate()) {
                matchAndWrite("private");
            } else if (modifier.isStatic()) {
                matchAndWrite("static");
            } else if (modifier.isAbstract()) {
                matchAndWrite("abstract");
            } else if (modifier.isFinal()) {   // TODO: Work thru different kinds of final here
                matchAndWrite("final");
            } else if (modifier.isNative()) {
                context.throwSourceNotSupported("native methods aren't supported");
            } else if (modifier.isSynchronized()) {  // TODO: Handle this
                matchAndWrite("synchronized");
            } else if (modifier.isTransient()) {  // TODO: Handle this
                matchAndWrite("transient");
            } else if (modifier.isVolatile()) {  // TODO: Handle this
                matchAndWrite("volatile");
            } else if (modifier.isStrictfp()) {  // TODO: Handle this
                context.throwSourceNotSupported("strictfp isn't supported");
            } else context.throwInvalidAST("Unknown modifier type: " + modifier);
        }
    });
    */

    public static String getNamespaceNameForPackageName(Name packageName) {
        if (packageName == null)
            return getNamespaceNameForPackageName((String) null);
        else return getNamespaceNameForPackageName(packageName.getFullyQualifiedName());
    }

    public static String getNamespaceNameForPackageName(String packageName) {
        if (packageName == null)
            return "unnamed";
        else return packageName.replace('.', '_');
    }
}

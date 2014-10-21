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
import org.juniversal.translator.core.ASTWriter;
import org.juniversal.translator.core.AccessLevel;
import org.juniversal.translator.core.Context;

import java.util.List;

import static org.juniversal.translator.core.ASTUtil.*;


public abstract class CSharpASTWriter<T extends ASTNode> extends ASTWriter<T> {
    private CSharpASTWriters cSharpASTWriters;

    protected CSharpASTWriter(CSharpASTWriters cSharpASTWriters) {
        super(cSharpASTWriters);
        this.cSharpASTWriters = cSharpASTWriters;
    }

    @Override
    protected CSharpASTWriters getASTWriters() {
        return cSharpASTWriters;
    }

    public static void writeAccessModifier(Context context, List<?> modifiers) {
        AccessLevel accessLevel = getAccessModifier(modifiers);
        
        switch (accessLevel) {
            case PRIVATE:
                writeModifier(context, "private");
                break;
            case PACKAGE:
                writeModifier(context, "internal");
                break;
            case PROTECTED:
                writeModifier(context, "protected internal");
                break;
            case PUBLIC:
                writeModifier(context, "public");
                break;
        }
    }

    public static void writeSealedModifier(Context context) {
        writeModifier(context, "sealed");
    }

    public static void writeOverrideModifier(Context context) {
        writeModifier(context, "override");
    }

    public final void writeStaticModifier(Context context, List<?> modifiers) {
        if (containsStatic(modifiers))
            writeModifier(context, "static");
    }

    public static void writeModifier(Context context, String modifier) {
        context.write(modifier);
        context.write(" ");
    }

    public void writeVariableDeclaration(Context context, List<?> modifiers, Type type, List<?> fragments) {
        context.ensureModifiersJustFinalOrAnnotations(modifiers);
        context.skipModifiers(modifiers);

        // Write the type
        writeNode(context, type);

        // Write the variable declaration(s)
        writeCommaDelimitedNodes(context, fragments, (VariableDeclarationFragment variableDeclarationFragment) -> {
            context.copySpaceAndComments();
            writeNode(context, variableDeclarationFragment);
        });
    }

    public void writeMethodInvocationArgumentList(Context context, List<?> typeArguments, List<?> arguments) {
        // TODO: Handle type arguments
        if (!typeArguments.isEmpty())
            context.throwSourceNotSupported("Type arguments not currently supported on a method invocation");

        context.matchAndWrite("(");
        writeCommaDelimitedNodes(context, arguments);

        context.copySpaceAndComments();
        context.matchAndWrite(")");
    }

    public void writeTypeParameterConstraints(Context context, List typeParameters) {
        for (Object typeParameterObject : typeParameters) {
            TypeParameter typeParameter = (TypeParameter) typeParameterObject;

            boolean firstBound = true;
            for (Object typeBoundObject : typeParameter.typeBounds()) {
                Type typeBound = (Type) typeBoundObject;

                if (firstBound) {
                    context.write(" where ");
                    context.write(typeParameter.getName().getIdentifier());
                    context.write(" : ");
                } else context.write(", ");

                writeNodeFromOtherPosition(context, typeBound);

                firstBound = false;
            }
        }
    }


    /*
        public void write(ASTNode node, Context context) {
            Modifier modifier = (Modifier) node;

            if (modifier.isPublic()) {
                context.matchAndWrite("public");
            } else if (modifier.isProtected()) {
                context.matchAndWrite("protected", "protected internal");
            } else if (modifier.isPrivate()) {
                context.matchAndWrite("private");
            } else if (modifier.isStatic()) {
                context.matchAndWrite("static");
            } else if (modifier.isAbstract()) {
                context.matchAndWrite("abstract");
            } else if (modifier.isFinal()) {   // TODO: Work thru different kinds of final here
                context.matchAndWrite("final");
            } else if (modifier.isNative()) {
                context.throwSourceNotSupported("native methods aren't supported");
            } else if (modifier.isSynchronized()) {  // TODO: Handle this
                context.matchAndWrite("synchronized");
            } else if (modifier.isTransient()) {  // TODO: Handle this
                context.matchAndWrite("transient");
            } else if (modifier.isVolatile()) {  // TODO: Handle this
                context.matchAndWrite("volatile");
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

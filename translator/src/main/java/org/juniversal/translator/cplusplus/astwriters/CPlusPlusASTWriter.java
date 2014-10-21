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

package org.juniversal.translator.cplusplus.astwriters;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.juniversal.translator.core.ASTWriter;
import org.juniversal.translator.core.Context;

import java.util.List;


public abstract class CPlusPlusASTWriter<T extends ASTNode> extends ASTWriter<T> {
    private CPlusPlusASTWriters cPlusPlusASTWriters;

    protected CPlusPlusASTWriter(CPlusPlusASTWriters cPlusPlusASTWriters) {
        this.cPlusPlusASTWriters = cPlusPlusASTWriters;
    }

    @Override
    protected CPlusPlusASTWriters getASTWriters() {
        return cPlusPlusASTWriters;
    }

    /**
     * Write out a type, when it's used (as opposed to defined).
     *
     * @param type    type to write
     * @param context context
     */
    public void writeType(Type type, Context context, boolean useRawPointer) {
        boolean referenceType = !type.isPrimitiveType();

        if (!referenceType)
            writeNode(context, type);
        else {
            if (useRawPointer) {
                writeNode(context, type);
                context.write("*");
            } else {
                context.write("ptr< ");
                writeNode(context, type);
                context.write(" >");
            }
        }
    }

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

    public static void writeIncludeForTypeName(Name typeName, Context context) {
        String typeNameString = typeName.getFullyQualifiedName();
        String includePath = typeNameString.replace('.', '/');

        context.writeln("#include \"" + includePath + ".h\"");
    }

    /**
     * Write out the type parameters in the specified list, surrounded by "<" and ">".
     *
     * @param typeParameters list of TypeParameter objects
     * @param includeClassKeyword if true, each parameter is prefixed with "class "
     * @param context context to output to
     */
    public static void writeTypeParameters(List<TypeParameter> typeParameters, boolean includeClassKeyword, Context context) {
        // If we're writing the implementation of a generic method, include the "template<...>" prefix

        boolean first = true;

        context.write("<");
        for (TypeParameter typeParameter : typeParameters) {
            if (! first)
                context.write(", ");

            if (includeClassKeyword)
                context.write("class ");
            context.write(typeParameter.getName().getIdentifier());

            first = false;
        }

        context.write(">");
    }
}

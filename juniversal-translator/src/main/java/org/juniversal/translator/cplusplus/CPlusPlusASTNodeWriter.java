/*
 * Copyright (c) 2012-2015, Microsoft Mobile
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

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.ASTNodeWriter;
import org.xuniversal.translator.cplusplus.CPlusPlusProfile;
import org.xuniversal.translator.cplusplus.ReferenceKind;

import java.util.List;

import static org.juniversal.translator.core.ASTUtil.forEach;


public abstract class CPlusPlusASTNodeWriter<T extends ASTNode> extends ASTNodeWriter<T> {
    private CPlusPlusFileTranslator cPlusPlusSourceFileWriter;

    protected CPlusPlusASTNodeWriter(CPlusPlusFileTranslator cPlusPlusSourceFileWriter) {
        this.cPlusPlusSourceFileWriter = cPlusPlusSourceFileWriter;
    }

    public CPlusPlusProfile getCPPProfile() {
        return cPlusPlusSourceFileWriter.getTranslator().getTargetProfile();
    }

    public CPlusPlusContext getContext() {
        return getFileTranslator().getContext();
    }

    @Override
    protected CPlusPlusFileTranslator getFileTranslator() {
        return cPlusPlusSourceFileWriter;
    }

    /**
     * Write out a type, when it's used (as opposed to defined).
     *
     * @param type type to write
     */
    public void writeType(Type type, ReferenceKind referenceKind) {
        @Nullable ITypeBinding typeBinding = type.resolveBinding();
        boolean isTypeVariable = typeBinding != null && (typeBinding.isTypeVariable() || typeBinding.isWildcardType());

        if (type.isPrimitiveType() || isTypeVariable)
            writeNode(type);
        else {
            switch (referenceKind) {
                case SharedPtr:
                    writeNode(type);
                    write("::ptr");
                    break;

                case WeakPtr:
                    writeNode(type);
                    write("::weak_ptr");
                    break;

                case ConstReference:
                    write("const ");
                    writeNode(type);
                    write("&");
                    break;

                case RawPointer:
                    writeNode(type);
                    write("*");
                    break;

                case Value:
                    writeNode(type);
                    break;
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

    public void writeIncludeForTypeName(Name typeName) {
        String typeNameString = typeName.getFullyQualifiedName();
        String includePath = typeNameString.replace('.', '/');

        writeln("#include \"" + includePath + ".h\"");
    }

    /**
     * Write out the type parameters in the specified list, surrounded by "<" and ">".
     *
     * @param typeParameters         list of TypeParameter objects
     * @param includeTypenameKeyword if true, each parameter is prefixed with "typename "
     */
    public void writeTypeParameters(List typeParameters, boolean includeTypenameKeyword) {
        // If we're writing the implementation of a generic method, include the "template<...>" prefix

        write("<");

        forEach(typeParameters, (TypeParameter typeParameter, boolean first) -> {
            if (!first)
                write(", ");

            if (includeTypenameKeyword)
                write("typename ");
            write(typeParameter.getName().getIdentifier());
        });

        write(">");
    }

/*
    public void writeIncludeForTypeName(Name typeName) {
        String typeNameString = typeName.getFullyQualifiedName();
        String includePath = typeNameString.replace('.', '/');

        writeln("#include \"" + includePath + ".h\"");
    }
*/
}

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
import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.ASTNodeWriter;
import org.xuniversal.translator.core.Flag;
import org.xuniversal.translator.core.TypeName;
import org.xuniversal.translator.cplusplus.CPlusPlusTargetWriter;
import org.xuniversal.translator.cplusplus.ReferenceKind;

import java.util.ArrayList;
import java.util.List;

import static org.juniversal.translator.core.ASTUtil.forEach;
import static org.juniversal.translator.core.ASTUtil.typeReferenceContainsTypeVariable;


public abstract class CPlusPlusASTNodeWriter<T extends ASTNode> extends ASTNodeWriter<T> {
    private CPlusPlusTranslator cPlusPlusTranslator;

    protected CPlusPlusASTNodeWriter(CPlusPlusTranslator cPlusPlusTranslator) {
        this.cPlusPlusTranslator = cPlusPlusTranslator;
    }

    public CPlusPlusContext getContext() {
        return getTranslator().getContext();
    }

    @Override protected CPlusPlusTranslator getTranslator() {
        return cPlusPlusTranslator;
    }

    @Override public CPlusPlusTargetWriter getTargetWriter() {
        return getContext().getTargetWriter();
    }

/*
    public ReferencedTypes getReferencedTypes() {
        return getContext().getReferencedTypes();
    }
*/

    /**
     * Write out a type, when it's used (as opposed to defined).
     *
     * @param type type to write
     */
    public void writeTypeReference(Type type, ReferenceKind referenceKind) {
        @Nullable ITypeBinding typeBinding = type.resolveBinding();
        boolean isTypeVariable = typeBinding != null && (typeBinding.isTypeVariable() || typeBinding.isWildcardType());

        if (type.isPrimitiveType() || isTypeVariable)
            writeNode(type);
        else {
/*
            getReferencedTypes().add(type,
                    referenceKind == ReferenceKind.Value || getContext().isWritingMethodImplementation());
*/

            switch (referenceKind) {
                case SharedPtr:
                    //addNameNeedingImport("std", "shared_ptr");
/*
                    write("std::shared_ptr<");
                    writeNode(type);
                    write(">");
*/
                    //if (typeReferenceContainsTypeVariable(type))
                    write("std::shared_ptr< ");
                    writeNode(type);
                    write(" >");
                    break;

                case WeakPtr:
                    //addNameNeedingImport("std", "weak_ptr");
/*
                    write("std::weak_ptr<");
                    writeNode(type);
                    write(">");
*/
                    //if (typeReferenceContainsTypeVariable(type))
                    write("std::weak_ptr < ");
                    writeNode(type);
                    write(" >");
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
        else return packageName.replace(".", "::");
    }

    protected void writeTypeParameters(List typeParameters, @Nullable String prefix, @Nullable String suffix) {
        write("<");

        Flag outputTypeParameter = new Flag();
        forEach(typeParameters, (TypeParameter typeParameter) -> {
            if (outputTypeParameter.isSet())
                write(", ");

            if (prefix != null)
                write(prefix);
            write(typeParameter.getName().getIdentifier());
            if (suffix != null)
                write(suffix);

            outputTypeParameter.set();
        });

        write(">");
    }

    protected void writeTypeParameters(List typeParameters, ArrayList<WildcardType> wildcardTypes,
                                       @Nullable String prefix, @Nullable String suffix) {
        write("<");

        Flag outputTypeParameter = new Flag();
        forEach(typeParameters, (TypeParameter typeParameter) -> {
            if (outputTypeParameter.isSet())
                write(", ");

            if (prefix != null)
                write(prefix);
            write(typeParameter.getName().getIdentifier());
            if (suffix != null)
                write(suffix);

            outputTypeParameter.set();
        });

        for (WildcardType wildcardType : wildcardTypes) {
            if (outputTypeParameter.isSet())
                write(", ");

            if (prefix != null)
                write(prefix);
            writeWildcardTypeSyntheticName(wildcardTypes, wildcardType);
            if (suffix != null)
                write(suffix);

            outputTypeParameter.set();
        }

        write(">");
    }

    public void writeTypeDeclarationType(AbstractTypeDeclaration abstractTypeDeclaration,
                                         @Nullable String typeParameterSuffix) {
        write(abstractTypeDeclaration.getName().getIdentifier());

        if (abstractTypeDeclaration instanceof TypeDeclaration) {
            TypeDeclaration typeDeclaration = (TypeDeclaration) abstractTypeDeclaration;

            List typeParameters = typeDeclaration.typeParameters();
            if (!typeParameters.isEmpty())
                writeTypeParameters(typeParameters, null, typeParameterSuffix);
        }
    }

/*
    public void writeIncludeForTypeName(Name typeName) {
        String typeNameString = typeName.getFullyQualifiedName();
        String includePath = typeNameString.replace('.', '/');

        writeln("#include \"" + includePath + ".h\"");
    }
*/

    public void writeTypeForwardDeclaration(ITypeBinding typeBinding) {
        if (typeBinding.isGenericType()) {
            write("template ");
            write("<");

            ITypeBinding[] typeParameters = typeBinding.getTypeParameters();
            boolean first = true;
            for (ITypeBinding typeParameter : typeParameters) {
                if (!first)
                    write(", ");
                write("typename ");
                write(typeParameter.getName());
                first = false;
            }

            write("> ");
        }

        writeln("class " + typeBinding.getName() + ";");
    }
}

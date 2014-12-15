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

import java.util.HashMap;


public class SimpleTypeWriter extends CSharpASTNodeWriter<SimpleType> {
    private HashMap<String, String> primitiveWrapperClassMapping;

    public SimpleTypeWriter(CSharpSourceFileWriter cSharpASTWriters) {
        super(cSharpASTWriters);

        primitiveWrapperClassMapping = new HashMap<>();
        primitiveWrapperClassMapping.put("java.lang.Byte", "Byte?");
        primitiveWrapperClassMapping.put("java.lang.Short", "Short?");
        primitiveWrapperClassMapping.put("java.lang.Integer", "Integer?");
        primitiveWrapperClassMapping.put("java.lang.Long", "Long?");
        primitiveWrapperClassMapping.put("java.lang.Float", "Single?");
        primitiveWrapperClassMapping.put("java.lang.Double", "Double?");
        primitiveWrapperClassMapping.put("java.lang.Character", "Char?");
        primitiveWrapperClassMapping.put("java.lang.Boolean", "Bool?");
    }

    @Override
    public void write(SimpleType simpleType) {
        Name name = simpleType.getName();

        String fullyQualifiedTypeName = "";
        @Nullable ITypeBinding typeBinding = simpleType.resolveBinding();
        if (typeBinding != null)
            fullyQualifiedTypeName = typeBinding.getQualifiedName();

        @Nullable String mappedPrimitiveWrapper = primitiveWrapperClassMapping.get(fullyQualifiedTypeName);

        if (fullyQualifiedTypeName.equals("java.lang.Object")) {
            matchNodeAndWrite(name, "object");
        } else if (fullyQualifiedTypeName.equals("java.lang.String")) {
            matchNodeAndWrite(name, "string");
        } else if (fullyQualifiedTypeName.equals("java.lang.Cloneable")) {
            matchNodeAndWrite(name, "ICloneable");
        } else if (mappedPrimitiveWrapper != null) {
            matchNodeAndWrite(name, mappedPrimitiveWrapper);
        } else if (fullyQualifiedTypeName.equals("java.lang.StringBuilder")) {
            getContext().addExtraUsing("System.Text");
            matchNodeAndWrite(name, "StringBuilder");
        } else if (fullyQualifiedTypeName.equals("java.lang.Throwable")) {
            getContext().addExtraUsing("System");
            matchNodeAndWrite(name, "Exception");
        } else if (name instanceof QualifiedName) {
            QualifiedName qualifiedName = (QualifiedName) name;

            Name qualifier = qualifiedName.getQualifier();
            matchNodeAndWrite(qualifier, qualifier.getFullyQualifiedName());

            copySpaceAndComments();
            matchAndWrite(".");
            matchAndWrite(qualifiedName.getName().getIdentifier());
        } else {
            SimpleName simpleName = (SimpleName) name;
            matchAndWrite(simpleName.getIdentifier());
        }
    }
}

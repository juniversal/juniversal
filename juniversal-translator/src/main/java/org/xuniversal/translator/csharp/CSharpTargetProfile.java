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

package org.xuniversal.translator.csharp;

import org.eclipse.jdt.core.dom.PrimitiveType;
import org.juniversal.translator.cplusplus.HierarchicalName;
import org.xuniversal.translator.core.TargetProfile;

/**
 * The CPPProfile class describes how the C++ should be generated.  Primarily this class gives
 * attributes of the target C++ compiler, for example saying what types are used to represent
 * different sizes of unsigned integers.
 *
 * @author bretjohn
 */
public class CSharpTargetProfile extends TargetProfile {
    private static HierarchicalName objectType = new HierarchicalName("System", "Object");
    private static HierarchicalName stringType = new HierarchicalName("System", "String");
    private static HierarchicalName stringBuilderType = new HierarchicalName("System", "Text", "StringBuilder");
    private static HierarchicalName arrayType = new HierarchicalName("System", "Array");

    @Override public String getInt8Type() {
        return "sbyte";
    }

    @Override public String getInt16Type() {
        return "short";
    }

    @Override public String getInt32Type() {
        return "int";
    }

    @Override public String getInt64Type() {
        return "long";
    }

    @Override public String getFloat32Type() {
        return "float";
    }

    @Override public String getFloat64Type() {
        return "double";
    }

    @Override public String getCharType() {
        return "char";
    }

    @Override public String getBooleanType() {
        return "bool";
    }

    @Override public String getVoidType() {
        return "void";
    }

    @Override public HierarchicalName getObjectType() {
        return objectType;
    }

    @Override public HierarchicalName getStringType() {
        return stringType;
    }

    @Override public HierarchicalName getStringBuilderType() {
        return stringBuilderType;
    }

    @Override public HierarchicalName getArrayType() {
        return arrayType;
    }
}

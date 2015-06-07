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

package org.xuniversal.translator.cplusplus;

import org.xuniversal.translator.core.TypeName;
import org.xuniversal.translator.core.TargetProfile;
import org.xuniversal.translator.core.TypeName;

/**
 * The CPPProfile class describes how the C++ should be generated.  Primarily this class gives
 * attributes of the target C++ compiler, for example saying what types are used to represent
 * different sizes of unsigned integers.
 *
 * @author bretjohn
 */
public class CPlusPlusTargetProfile extends TargetProfile {
    private static TypeName objectType = new TypeName("xuniv", "Object");
    private static TypeName stringType = new TypeName("xuniv", "String");
    private static TypeName stringBuilderType = new TypeName("xuniv", "StringBuilder");
    private static TypeName arrayType = new TypeName("xuniv", "Array");
    private static TypeName boxType = new TypeName("xuniv", "Box");
    private static TypeName sharedPtrType = new TypeName("std", "shared_ptr");
    private static TypeName weakPtrType = new TypeName("std", "weak_ptr");

    @Override public boolean isCPlusPlus() {
        return true;
    }

    @Override public String getInt8Type() {
        return "char";
    }

    @Override public String getInt16Type() {
        return "short";
    }

    @Override public String getInt32Type() {
        return "int";
    }

    @Override public String getInt64Type() {
        return "int64_t";
    }

    @Override public String getFloat32Type() {
        return "float";
    }

    @Override public String getFloat64Type() {
        return "double";
    }

    @Override public String getCharType() {
        return "char16_t";
    }

    @Override public String getBooleanType() {
        return "bool";
    }

    @Override public String getVoidType() {
        return "void";
    }
    @Override public TypeName getObjectType() {
        return objectType;
    }

    @Override public TypeName getStringType() {
        return stringType;
    }

    @Override public TypeName getStringBuilderType() {
        return stringBuilderType;
    }

    @Override public TypeName getArrayType() {
        return arrayType;
    }

    public TypeName getBoxType() {
        return boxType;
    }

    public TypeName getSharedPtrType() {
        return sharedPtrType;
    }

    public TypeName getWeakPtrType() {
        return weakPtrType;
    }
}

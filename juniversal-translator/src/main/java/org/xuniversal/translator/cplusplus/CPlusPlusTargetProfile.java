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

import org.juniversal.translator.cplusplus.HierarchicalName;
import org.xuniversal.translator.core.TargetProfile;

/**
 * The CPPProfile class describes how the C++ should be generated.  Primarily this class gives
 * attributes of the target C++ compiler, for example saying what types are used to represent
 * different sizes of unsigned integers.
 *
 * @author bretjohn
 */
public class CPlusPlusTargetProfile extends TargetProfile {
    private static HierarchicalName objectType = new HierarchicalName("xu", "Object");
    private static HierarchicalName stringType = new HierarchicalName("xu", "String");
    private static HierarchicalName stringBuilderType = new HierarchicalName("xu", "StringBuilder");
    private static HierarchicalName arrayType = new HierarchicalName("xu", "Array");
    private static HierarchicalName boxType = new HierarchicalName("xu", "Box");
    private static HierarchicalName makeSharedFunction = new HierarchicalName("std", "make_shared");
    private static HierarchicalName sharedPtrType = new HierarchicalName("std", "shared_ptr");
    private static HierarchicalName weakPtrType = new HierarchicalName("std", "weak_ptr");

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

    public HierarchicalName getBoxType() {
        return boxType;
    }

    public HierarchicalName getMakeSharedFunction() {
        return makeSharedFunction;
    }

    public HierarchicalName getSharedPtrType() {
        return sharedPtrType;
    }

    public HierarchicalName getWeakPtrType() {
        return weakPtrType;
    }
}

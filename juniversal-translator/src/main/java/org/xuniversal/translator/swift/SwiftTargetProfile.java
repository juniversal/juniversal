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

package org.xuniversal.translator.swift;

import org.juniversal.translator.cplusplus.HierarchicalName;
import org.xuniversal.translator.core.TargetProfile;

/**
 * The CPPProfile class describes how the C++ should be generated.  Primarily this class gives
 * attributes of the target C++ compiler, for example saying what types are used to represent
 * different sizes of unsigned integers.
 *
 * @author bretjohn
 */
public class SwiftTargetProfile extends TargetProfile {
    // TODO: Fix this up
    private static HierarchicalName objectType = new HierarchicalName("System", "Object");
    private static HierarchicalName stringType = new HierarchicalName("System", "String");
    private static HierarchicalName stringBuilderType = new HierarchicalName("System", "Text", "StringBuilder");
    private static HierarchicalName arrayType = new HierarchicalName("System", "Array");

    @Override public boolean isSwift() {
        return true;
    }

    @Override public String getInt8Type() {
        return "Int8";
    }

    @Override public String getInt16Type() {
        return "Int16";
    }

    @Override public String getInt32Type() {
        // TODO: For now, map 32 bit Java int to Int type in Swift, which can be 32 or 64 bits.
        // Later probably add @PreserveJavaIntSemantics annotation to force 32 bit + no overflow checking
        return "Int";
    }

    @Override public String getInt64Type() {
        return "Int64";
    }

    @Override public String getFloat32Type() {
        return "Float";
    }

    @Override public String getFloat64Type() {
        return "Double";
    }

    @Override public String getCharType() {
        return "Character";
    }

    @Override public String getBooleanType() {
        return "Bool";
    }

    @Override public String getVoidType() {
        throw new RuntimeException("Should detect void return types before it gets here");
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

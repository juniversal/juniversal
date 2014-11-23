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

package org.juniversal.translator.cplusplus;

import org.juniversal.translator.core.TargetProfile;

/**
 * The CPPProfile class describes how the C++ should be generated.  Primarily this class gives
 * attributes of the target C++ compiler, for example saying what types are used to represent
 * different sizes of unsigned integers.
 *
 * @author bretjohn
 */
public class CPPProfile extends TargetProfile {
    private String int8Type = "char";
    private String int16Type = "short";
    private String int32Type = "int";
    private String int64Type = "long64";
    private String float32Type = "float";
    private String float64Type = "double";
    private String unsignedInt32UnicodeCharType = "unsigned short";


    public String getInt8Type() {
        return int8Type;
    }

    public String getInt16Type() {
        return int16Type;
    }

    public String getInt32Type() {
        return int32Type;
    }

    public String getInt64Type() {
        return int64Type;
    }

    public String getFloat32Type() {
        return float32Type;
    }

    public String getFloat64Type() {
        return float64Type;
    }

    public String getUnsignedInt32UnicodeCharType() {
        return unsignedInt32UnicodeCharType;
    }
}

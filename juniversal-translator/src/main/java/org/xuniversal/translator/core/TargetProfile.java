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

package org.xuniversal.translator.core;

import org.juniversal.translator.cplusplus.HierarchicalName;

/**
 * @author Bret Johnson
 * @since 7/20/2014 6:45 PM
 */
public abstract class TargetProfile {
    private int tabStop = -1;
    private boolean classBraceOnSameLine = true;
    private int preferredIndent = 4;    // TODO: Set

    /**
     * Returns how many spaces a tab should correspond to in the C++ output (e.g. 4 or 2). If tabs
     * shouldn't shouldn't be used in the output, returns -1.
     *
     * @return number of spaces to use for tabs in output or -1 if shouldn't use tabs
     */
    public int getTabStop() {
        return tabStop;
    }

    /**
     * Sets how many spaces a tab should correspond to in the C++ output (e.g. 4 or 2).  If tabs
     * shouldn't be used in the output, set it to -1.
     *
     * @param tabStop number of spaces to use for tabs in output or -1 if shouldn't use tabs
     */
    public void setTabStop(int tabStop) {
        this.tabStop = tabStop;
    }

    /**
     * Returns whether for a class declaration the left brace, {, should be on the same line as the class keyword or
     * on the next line.
     *
     * @return true if brace should be on same line
     */
    public boolean getClassBraceOnSameLine() {
        return classBraceOnSameLine;
    }

    /**
     * Sets whether for a class declaration the left brace, {, should be on the same line as the
     * class keyword or on the next line.
     *
     * @param value true if brace should be on same line, false for next line
     */
    public void setClassBraceOnSameLine(boolean value) {
        classBraceOnSameLine = value;
    }

    public int getPreferredIndent() {
        return preferredIndent;
    }

    public abstract String getInt8Type();

    public abstract String getInt16Type();

    public abstract String getInt32Type();

    public abstract String getInt64Type();

    public abstract String getFloat32Type();

    public abstract String getFloat64Type();

    public abstract String getCharType();

    public abstract String getBooleanType();

    public abstract String getVoidType();

    public abstract HierarchicalName getObjectType();

    public abstract HierarchicalName getStringType();

    public abstract HierarchicalName getStringBuilderType();

    public abstract HierarchicalName getArrayType();

    public boolean isCSharp() { return false; }

    public boolean isCPlusPlus() { return false; }

    public boolean isSwift() { return false; }
}

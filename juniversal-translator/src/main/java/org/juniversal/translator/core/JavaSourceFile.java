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

package org.juniversal.translator.core;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.xuniversal.translator.core.SourceFile;

import java.io.File;

public class JavaSourceFile extends SourceFile {
    private final CompilationUnit compilationUnit;

    public JavaSourceFile(CompilationUnit compilationUnit, File sourceFile, int sourceTabStop) {
        super(sourceFile, sourceTabStop);
        this.compilationUnit = compilationUnit;
    }

    public JavaSourceFile(CompilationUnit compilationUnit, String source, int sourceTabStop) {
        super(source, sourceTabStop);
        this.compilationUnit = compilationUnit;
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    @Override public int getPhysicalColumn(int position) {
        int physicalColumn = compilationUnit.getColumnNumber(position);
        if (physicalColumn < 0)
            throw new RuntimeException("Position is invalid: " + position);

        return physicalColumn;
    }

    @Override public int getLineNumber(int position) {
        int lineNumber = compilationUnit.getLineNumber(position);
        if (lineNumber < 0) {
            if (position == getSource().length())
                throw new RuntimeException("Position " + position + " is at end of source file; can't get line number");
            else throw new RuntimeException("Position " + position + " isn't valid");
        } else return lineNumber;
    }

    @Override public int getPosition(int lineNumber, int physicalColumn) {
        return compilationUnit.getPosition(lineNumber, physicalColumn);
    }
}

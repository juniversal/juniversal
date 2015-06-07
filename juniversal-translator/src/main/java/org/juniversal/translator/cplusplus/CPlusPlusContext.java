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

import org.juniversal.translator.core.JavaSourceContext;
import org.juniversal.translator.core.JavaSourceFile;
import org.juniversal.translator.core.TypeNames;
import org.xuniversal.translator.core.TypeName;
import org.xuniversal.translator.cplusplus.CPlusPlusTargetWriter;

public class CPlusPlusContext extends JavaSourceContext {
    private CPlusPlusTargetWriter targetWriter;
    private OutputType outputType;
    private boolean writingVariableDeclarationNeedingStar;
    private TypeNames referencedOutermostTypes = new TypeNames();

    public CPlusPlusContext(JavaSourceFile sourceFile, CPlusPlusTargetWriter targetWriter, OutputType outputType) {
        super(sourceFile, targetWriter);
        this.targetWriter = targetWriter;
        this.outputType = outputType;
    }

    @Override public CPlusPlusTargetWriter getTargetWriter() {
        return targetWriter;
    }

    public boolean isWritingVariableDeclarationNeedingStar() {
        return writingVariableDeclarationNeedingStar;
    }

    public void setWritingVariableDeclarationNeedingStar(boolean writingVariableDeclarationNeedingStar) {
        this.writingVariableDeclarationNeedingStar = writingVariableDeclarationNeedingStar;
    }

    public OutputType getOutputType() {
        return outputType;
    }

    public TypeNames getReferencedOutermostTypes() {
        return referencedOutermostTypes;
    }

    public void addReferencedTargetType(TypeName typeName) {
        referencedOutermostTypes.add(typeName.getOutermostType());
    }
}

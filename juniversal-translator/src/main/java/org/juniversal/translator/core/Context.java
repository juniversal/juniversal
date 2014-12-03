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

package org.juniversal.translator.core;

import org.eclipse.jdt.core.dom.*;
import org.juniversal.translator.cplusplus.OutputType;

import java.util.ArrayList;
import java.util.List;

import static org.juniversal.translator.core.ASTUtil.isAnnotation;
import static org.juniversal.translator.core.ASTUtil.isFinal;

public class Context {
    private TypeDeclaration typeDeclaration;
    private ArrayList<WildcardType> methodWildcardTypes = null;
    private boolean writingVariableDeclarationNeedingStar;
    private boolean writingMethodImplementation;


    public TypeDeclaration getTypeDeclaration() {
        return typeDeclaration;
    }

    public void setTypeDeclaration(TypeDeclaration typeDeclaration) {
        this.typeDeclaration = typeDeclaration;
    }

    public ArrayList<WildcardType> getMethodWildcardTypes() {
        return methodWildcardTypes;
    }

    public void setMethodWildcardTypes(ArrayList<WildcardType> methodWildcardTypes) {
        this.methodWildcardTypes = methodWildcardTypes;
    }

    public boolean isWritingVariableDeclarationNeedingStar() {
        return writingVariableDeclarationNeedingStar;
    }

    public void setWritingVariableDeclarationNeedingStar(boolean writingVariableDeclarationNeedingStar) {
        this.writingVariableDeclarationNeedingStar = writingVariableDeclarationNeedingStar;
    }

    public boolean isWritingMethodImplementation() {
        return writingMethodImplementation;
    }

    public void setWritingMethodImplementation(boolean writingMethodImplementation) {
        this.writingMethodImplementation = writingMethodImplementation;
    }
}

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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Context {
    private TypeDeclaration typeDeclaration;
    private @Nullable Set<String> typeMethodNames = null;
    private ArrayList<WildcardType> methodWildcardTypes = null;
    private boolean writingMethodImplementation;


    public TypeDeclaration getTypeDeclaration() {
        return typeDeclaration;
    }

    public Set<String> getTypeMethodNames() {
        if (typeMethodNames != null)
            return typeMethodNames;

        typeMethodNames = new HashSet<>();

        ITypeBinding typeBinding = typeDeclaration.resolveBinding();
        while (typeBinding != null) {
            for (IMethodBinding methodBinding : typeBinding.getDeclaredMethods()) {
                typeMethodNames.add(methodBinding.getName());
            }

            typeBinding = typeBinding.getSuperclass();
        }

        return typeMethodNames;
    }

    public void setTypeDeclaration(TypeDeclaration typeDeclaration) {
        if (typeDeclaration != this.typeDeclaration) {
            this.typeDeclaration = typeDeclaration;
            this.typeMethodNames = null;
        }
    }

    public ArrayList<WildcardType> getMethodWildcardTypes() {
        return methodWildcardTypes;
    }

    public void setMethodWildcardTypes(ArrayList<WildcardType> methodWildcardTypes) {
        this.methodWildcardTypes = methodWildcardTypes;
    }

    public boolean isWritingMethodImplementation() {
        return writingMethodImplementation;
    }

    public void setWritingMethodImplementation(boolean writingMethodImplementation) {
        this.writingMethodImplementation = writingMethodImplementation;
    }
}

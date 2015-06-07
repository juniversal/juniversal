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

import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.Nullable;
import org.xuniversal.translator.core.HierarchicalName;
import org.xuniversal.translator.core.Context;
import org.xuniversal.translator.core.TargetWriter;
import org.xuniversal.translator.core.TypeName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.juniversal.translator.core.ASTUtil.getFirstTypeDeclaration;
import static org.juniversal.translator.core.ASTUtil.toHierarchicalName;

public abstract class JavaSourceContext extends Context {
    private JavaSourceFile sourceFile;
    private TypeName outermostTypeName;
    private AbstractTypeDeclaration typeDeclaration;
    private @Nullable Set<String> typeMethodNames = null;
    private ArrayList<WildcardType> methodWildcardTypes = null;

    public JavaSourceContext(JavaSourceFile sourceFile, TargetWriter targetWriter) {
        super(sourceFile, targetWriter);
        this.sourceFile = sourceFile;

        CompilationUnit compilationUnit = sourceFile.getCompilationUnit();

        HierarchicalName packageName = toHierarchicalName(compilationUnit.getPackage().getName());
        @Nullable AbstractTypeDeclaration typeDeclaration = getFirstTypeDeclaration(compilationUnit);
        outermostTypeName = typeDeclaration != null ?
                new TypeName(packageName, typeDeclaration.getName().getIdentifier()) :
                new TypeName(packageName, new HierarchicalName());
    }

    @Override public JavaSourceFile getSourceFile() {
        return sourceFile;
    }

    public AbstractTypeDeclaration getTypeDeclaration() {
        return typeDeclaration;
    }

    public TypeName getOutermostTypeName() {
        return outermostTypeName;
    }

    /**
     * Get all the method names that are defined for the current type (including its superclasses and super interfaces),
     * as returned by getTypeDeclaration().   This returned Set is used to check that variables don't have the same
     * names as methods, which is disallowed in some target languages.
     *
     * @return set of method names for the current type
     */
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

    public void setTypeDeclaration(AbstractTypeDeclaration typeDeclaration) {
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
}

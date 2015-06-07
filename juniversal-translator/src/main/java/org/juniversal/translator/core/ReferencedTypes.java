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

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.jetbrains.annotations.Nullable;
import org.xuniversal.translator.core.HierarchicalName;

import java.util.HashSet;

import static org.juniversal.translator.core.ASTUtil.typeBindingToHierarchicalName;

/**
 * @author Bret Johnson
 * @since 5/25/2015
 */
public class ReferencedTypes {
    private HashSet<ITypeBinding> typesNeedingDefinition = new HashSet<>();
    private HashSet<ITypeBinding> typesJustNeedingDeclaration = new HashSet<>();

    public void add(Type type, boolean needDefinition) {
        @Nullable ITypeBinding typeBinding = type.resolveBinding();

        // If there's no type binding, there much be Java compile errors; just drive on in this case, to output what we
        // can
        if (typeBinding == null)
            return;

        // We don't care about references to primitive types--ignore those
        if (typeBinding.isPrimitive())
            return;

        typeBinding = typeBinding.getTypeDeclaration();

        if (needDefinition) {
            if (typesJustNeedingDeclaration.contains(typeBinding))
                typesJustNeedingDeclaration.remove(typeBinding);
            typesNeedingDefinition.add(typeBinding);
        } else {
            if (!typesNeedingDefinition.contains(typeBinding))
                typesJustNeedingDeclaration.add(typeBinding);
        }
    }

    public HashSet<ITypeBinding> getTypesNeedingDefinition() {
        return typesNeedingDefinition;
    }

    public HashSet<ITypeBinding> getTypesJustNeedingDeclaration() {
        return typesJustNeedingDeclaration;
    }

    public HashSet<HierarchicalName> getTypesNeedDefinitionNames() {
        HashSet<HierarchicalName> names = new HashSet<>();

        for (ITypeBinding typeBinding : typesNeedingDefinition) {
            names.add(typeBindingToHierarchicalName(typeBinding));
        }

        return names;
    }
}

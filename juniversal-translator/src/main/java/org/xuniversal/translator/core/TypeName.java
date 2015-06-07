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

/**
 * @author Bret Johnson
 * @since 5/26/2015
 */
public class TypeName implements Comparable<TypeName> {
    private HierarchicalName pakkage;
    private HierarchicalName type;

    public TypeName(HierarchicalName pakkage, HierarchicalName type) {
        this.pakkage = pakkage;
        this.type = type;
    }

    public TypeName(String pakkage, String type) {
        this.pakkage = new HierarchicalName(pakkage);
        this.type = new HierarchicalName(type);
    }

    public TypeName(HierarchicalName pakkage, String type) {
        this.pakkage = pakkage;
        this.type = new HierarchicalName(type);
    }

    /**
     * Construct a TypeName with empty package name.
     *
     * @param type type name
     */
    public TypeName(String type) {
        this.pakkage = new HierarchicalName();
        this.type = new HierarchicalName(type);
    }

    public HierarchicalName getPackage() {
        return pakkage;
    }

    public HierarchicalName getType() {
        return type;
    }

    public String toString(String delimiter) {
        String packageQualifiedName = pakkage.toString(delimiter);
        String typeQualifiedName = type.toString(delimiter);

        if (packageQualifiedName.isEmpty() || typeQualifiedName.isEmpty())
            return packageQualifiedName + typeQualifiedName;
        else return packageQualifiedName + delimiter + typeQualifiedName;
    }

    @Override public int compareTo(TypeName other) {
        int packageComparison = pakkage.compareTo(other.pakkage);
        if (packageComparison != 0)
            return packageComparison;

        return type.compareTo(other.type);
    }

    @Override public String toString() {
        return toString(".");
    }

    @Override public int hashCode() {
        return pakkage.hashCode() ^ type.hashCode();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeName))
            return false;

        TypeName typeName = (TypeName) o;
        return pakkage.equals(typeName.pakkage) && type.equals(typeName.type);
    }

    public boolean inSamePackageAs(TypeName other) {
        return pakkage.equals(other.getPackage());
    }

    public boolean packageIs(String packageName) {
        return pakkage.length() == 1 && pakkage.getLastComponent().equals(packageName);
    }

    /**
     * Get the outermost type for this type.   That is, if the type name refers to an inner type X inside another inner
     * type Y instead of outermost type Z, then return type Z, in the same package.   If this type is just one level
     * deep (as is typically the case), then this method returns "this".
     *
     * @return the outermost type associated with this type, or this type itself if this type is already outermost (true
     * in most cases)
     */
    public TypeName getOutermostType() {
        if (type.length() == 1)
            return this;
        else return new TypeName(pakkage, type.getFirstComponent());
    }
}

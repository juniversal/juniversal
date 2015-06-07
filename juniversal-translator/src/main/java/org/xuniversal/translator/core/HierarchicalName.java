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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Bret Johnson
 * @since 5/15/2015
 */
public class HierarchicalName implements Comparable<HierarchicalName> {
    private ArrayList<String> components = new ArrayList<>();

    public HierarchicalName(HierarchicalName parent, String name) {
        for (String component : parent.components) {
            components.add(component);
        }
        components.add(name);
    }

    public HierarchicalName(String... nameComponents) {
        for (String component : nameComponents) {
            components.add(component);
        }
    }

    public HierarchicalName(List<String> nameComponents) {
        components.addAll(nameComponents);
    }

    public HierarchicalName(String[] nameQualifierComponents, String name) {
        for (String component : nameQualifierComponents) {
            components.add(component);
        }
        components.add(name);
    }

    public HierarchicalName() {
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof HierarchicalName))
            return false;
        HierarchicalName that = (HierarchicalName) o;
        return components.equals(that.components);
    }

    @Override public int hashCode() {
        return components.hashCode();
    }

    @Override public int compareTo(HierarchicalName other) {
        for (int i = 0; true; ++i) {
            if (i == components.size()) {
                // If we're at the end of this name's components, then either they are equal, if also at the end of the
                // other name's components, or the other name has more components so this one is a prefix of it and is
                // thus sorted before it
                if (i == other.components.size())
                    return 0;
                else
                    return -1;
            } else if (i == other.components.size()) {
                // If we're at the end of the other name's components, then it must be a prefix of this name and thus
                // sorted before it
                return 1;
            }

            String thisComponent = components.get(i);
            String otherComponent = other.components.get(i);

            int comparison = thisComponent.compareTo(otherComponent);

            if (comparison != 0)
                return comparison;
        }
    }

    public String getComponent(int index) {
        return components.get(index);
    }

    public String getFirstComponent() {
        int componentsSize = components.size();
        if (componentsSize == 0)
            throw new RuntimeException("HierarchicalName is empty; it doesn't have a first component");
        else return getComponent(0);
    }

    public String getLastComponent() {
        int componentsSize = components.size();
        if (componentsSize == 0)
            throw new RuntimeException("HierarchicalName is empty; it doesn't have a last component");
        else return getComponent(componentsSize - 1);
    }

    public int length() {
        return components.size();
    }

    public boolean isEmpty() {
        return components.isEmpty();
    }

    /**
     * Get the qualifier for this name--the qualifier contains all name components except for the last one.
     *
     * @return qualifier HierarchicalName, formed by taking the current name and chopping off the last component
     */
    public HierarchicalName getQualifier() {
        if (components.size() == 0)
            throw new RuntimeException("HierarchicalName is empty; it doesn't have a qualifier");

        int qualifierSize = components.size() - 1;
        ArrayList<String> qualifierComponents = new ArrayList<String>(qualifierSize);
        for (int i = 0; i < qualifierSize; ++i)
            qualifierComponents.add(components.get(i));

        return new HierarchicalName(qualifierComponents);
    }

    /**
     * See if the qualifier, which is defined as all name components except for the last one, matches
     * qualifierComponents.   So this method compares all components of the name except for the last one, for example
     * comparing Java package names but not the identifier name at the name.
     *
     * @param qualifierComponents qualifier to compare against
     * @return true if the qualifier matches, false other
     */
    public boolean qualifierIs(String... qualifierComponents) {
        if (qualifierComponents.length != components.size() - 1)
            return false;

        for (int i = 0; i < qualifierComponents.length; i++) {
            if (!qualifierComponents[i].equals(components.get(i)))
                return false;
        }

        return true;
    }

    public boolean forEach(Consumer<String> consumer) {
        boolean hasItem = false;
        for (String component : components) {
            consumer.accept(component);
            hasItem = true;
        }
        return hasItem;
    }

    public String toString(String delimiter) {
        StringBuilder buffer = new StringBuilder();
        boolean first = true;
        for (String component : components) {
            if (!first)
                buffer.append(delimiter);
            buffer.append(component);
            first = false;
        }
        return buffer.toString();
    }

    @Override public String toString() {
        return toString(".");
    }
}

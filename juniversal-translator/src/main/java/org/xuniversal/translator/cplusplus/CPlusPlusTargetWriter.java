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

package org.xuniversal.translator.cplusplus;

import org.jetbrains.annotations.Nullable;
import org.juniversal.translator.core.TypeNames;
import org.xuniversal.translator.core.HierarchicalName;
import org.xuniversal.translator.core.TargetWriter;
import org.xuniversal.translator.core.TypeName;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * @author Bret Johnson
 * @since 5/17/2015
 */
public class CPlusPlusTargetWriter extends TargetWriter {
    private CPlusPlusTargetProfile targetProfile;

    public CPlusPlusTargetWriter(Writer writer, CPlusPlusTargetProfile targetProfile) {
        super(writer, targetProfile);
        this.targetProfile = targetProfile;
    }

    public CPlusPlusTargetProfile getTargetProfile() {
        return targetProfile;
    }

    public void writeHeaderFileStart(TypeName typeName) {
        String multiIncludeDefine = getMultiIncludeDefine(typeName);

        writeln("#ifndef " + multiIncludeDefine);
        writeln("#define " + multiIncludeDefine);
        writeln();
    }

    public void writeHeaderFileEnd(TypeName typeName) {
        writeln("#endif // " + getMultiIncludeDefine(typeName));
    }

    private String getMultiIncludeDefine(TypeName typeName) {
        return typeName.toString("_").toUpperCase() + "_H";
    }

    public void writeIncludesForHeaderFile(TypeNames names) {
        writeln("#include \"xuniversal/xuniversal.h\"");
        writeIncludes(null, names);
    }

    public void writeIncludesForSourceFile(TypeName typeName, TypeNames names) {
        writeln("#include \"first.h\"");
        writeIncludes(typeName, names);
    }

    public void writeIncludes(@Nullable TypeName typeNameForSourceFile, TypeNames typeNames) {
        // For a source file, the first include should be for the type/class defined in that file
        if (typeNameForSourceFile != null)
            writeInclude(typeNameForSourceFile);

        for (TypeName name : typeNames.sort()) {
            // "std" namespace names don't need to be #included
            if (name.packageIs("std"))
                continue;

            // The xuniversal Object, String, and Array types are predefined in xuniversal.h and don't need to be
            // included separately
            if (name.equals(targetProfile.getObjectType()) || name.equals(targetProfile.getStringType()) ||
                name.equals(targetProfile.getStringBuilderType()) || name.equals(targetProfile.getArrayType()) ||
                name.equals(targetProfile.getBoxType()))
                continue;
            
            // For source files, we already wrote out the type name associated with the source file above, so don't
            // write it again
            if (typeNameForSourceFile != null && name.equals(typeNameForSourceFile))
                continue;

            writeInclude(name);
        }
    }

    public void writeUsings(Set<HierarchicalName> names) {
        for (HierarchicalName name : sortNames(names)) {
            writeUsing(name);
        }
    }

    private ArrayList<HierarchicalName> sortNames(Set<HierarchicalName> names) {
        ArrayList<HierarchicalName> sortedNames = new ArrayList<>();
        for (HierarchicalName name : names) {
            sortedNames.add(name);
        }

        Collections.sort(sortedNames);
        return sortedNames;
    }

    public void writeInclude(TypeName typeName) {
        writeln("#include \"" + typeName.toString("/") + ".h\"");
    }

    public void writeUsing(HierarchicalName name) {
        writeln("using " + name.toString("::") + ";");
    }

    public void writeNamespaceStart(HierarchicalName namespaceName) {
        namespaceName.forEach((String nameComponent) -> writeln("namespace " + nameComponent + " {"));
    }

    public void writeNamespaceEnd(HierarchicalName namespaceName) {
        // Close namespace definitions
        namespaceName.forEach((String nameComponent) -> writeln("}"));
    }
}

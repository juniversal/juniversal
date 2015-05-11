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

package org.juniversal.translator.csharp;

import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;

import java.util.List;

public class EnumDeclarationWriter extends CSharpASTNodeWriter<EnumDeclaration> {
    public EnumDeclarationWriter(CSharpFileTranslator cSharpASTWriters) {
        super(cSharpASTWriters);
    }

    @Override public void write(EnumDeclaration enumDeclaration) {
        List modifiers = enumDeclaration.modifiers();

        writeAccessModifier(modifiers);

        // Skip the modifiers
        skipModifiers(modifiers);

        matchAndWrite("enum");

        copySpaceAndComments();
        writeNode(enumDeclaration.getName());

        copySpaceAndComments();
        matchAndWrite("{");

        // TODO: Support trailing comma
        writeCommaDelimitedNodes(enumDeclaration.enumConstants(), (EnumConstantDeclaration enumConstantDeclaration) -> {
            copySpaceAndComments();
            writeNode(enumConstantDeclaration.getName());

            if (enumConstantDeclaration.arguments().size() > 0)
                throw sourceNotSupported("Enum constants with arguments aren't currently supported; change the source to use a normal class, with public final members for the enum constants, instead");
        });

        if (enumDeclaration.bodyDeclarations().size() > 0)
            throw sourceNotSupported("Enums with methods/data members aren't currently supported; change the source to use a normal class instead, with public final members for the enum constants, instead");

        copySpaceAndComments();
        matchAndWrite("}");
    }
}

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

import org.eclipse.jdt.core.dom.*;
import org.juniversal.translator.core.ASTUtil;
import org.xuniversal.translator.core.SourceFile;

import static org.juniversal.translator.core.ASTUtil.forEach;

public class SourceTypeDeclarationWriter extends CPlusPlusASTNodeWriter<TypeDeclaration> {
    private boolean outputSomething;

    public SourceTypeDeclarationWriter(CPlusPlusTranslator translator) {
        super(translator);
    }

    @Override
    public void write(TypeDeclaration typeDeclaration) {
        // Write the static fields, if any
        forEach(typeDeclaration.bodyDeclarations(), (BodyDeclaration bodyDeclaration) -> {
            if (bodyDeclaration instanceof FieldDeclaration) {
                FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclaration;

                if (ASTUtil.containsStatic(fieldDeclaration.modifiers())) {
                    // Skip any Javadoc comments for the field; field comments are just output in the header
                    setPositionToStartOfNode(fieldDeclaration);

                    writeNode(fieldDeclaration);
                    writeln();
                    outputSomething = true;
                }
            }
        });

        forEach(typeDeclaration.bodyDeclarations(), (BodyDeclaration bodyDeclaration) -> {
            if (bodyDeclaration instanceof MethodDeclaration) {
                MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
                if (methodDeclaration.getBody() != null)
                    writeMethod(methodDeclaration);
            } else if (bodyDeclaration instanceof TypeDeclaration) {
                TypeDeclaration nestedTypeDeclaration = (TypeDeclaration) bodyDeclaration;
                writeNestedType(nestedTypeDeclaration);
            }
        });

        setPosition(ASTUtil.getEndPosition(typeDeclaration));
    }

    private void writeNestedType(TypeDeclaration nestedTypeDeclaration) {
        if (outputSomething) {
            getContext().writeln();
            getContext().writeln();
        }

        writeln("/**");
        writeln(" *    " + nestedTypeDeclaration.getName());
        writeln(" */");
        writeln();

        setPositionToStartOfNode(nestedTypeDeclaration);
        writeNode(nestedTypeDeclaration);
        outputSomething = true;
    }

    private void writeMethod(MethodDeclaration methodDeclaration) {
        // We assume that the first non-whitespace text on the first line of the method
        // isn't indented at all--there's nothing in the method left of it. Unindent the
        // whole method by that amount, since methods aren't indented in the C++ source.

        SourceFile sourceFile = getContext().getSourceFile();
        int methodLine = sourceFile.getLineNumber(methodDeclaration.getStartPosition());
        int methodLineStartPosition = sourceFile.getPosition(methodLine, 0);
        setPosition(methodLineStartPosition);
        skipSpacesAndTabs();
        int additionalIndent = getSourceLogicalColumn();

        int previousIndent = getTargetWriter().setAdditionalIndentation(-1 * additionalIndent);

        getContext().setWritingMethodImplementation(true);

        // Skip back to the beginning of the comments, ignoring any comments associated with
        // the previous node
        setPositionToStartOfNodeSpaceAndComments(methodDeclaration);

        // If we haven't output anything yet, don't include the separator blank lines
        if (!outputSomething)
            skipBlankLines();

        copySpaceAndComments();
        writeNode(methodDeclaration);

        getContext().setWritingMethodImplementation(false);
        getTargetWriter().setAdditionalIndentation(previousIndent);

        copySpaceAndCommentsUntilEOL();
        writeln();

        outputSomething = true;
    }
}

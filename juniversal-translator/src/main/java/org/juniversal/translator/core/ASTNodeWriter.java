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
import org.xuniversal.translator.core.BufferTargetWriter;
import org.xuniversal.translator.core.SourceNotSupportedException;
import org.xuniversal.translator.core.TargetWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.juniversal.translator.core.ASTUtil.forEach;


public abstract class ASTNodeWriter<T extends ASTNode> {
    abstract public void write(T node);

    public boolean canProcessTrailingWhitespaceOrComments() {
        return false;
    }

    protected abstract FileTranslator getFileTranslator();

    protected void writeNode(ASTNode node) {
        getFileTranslator().writeNode(node);
    }

    protected void writeNodeFromOtherPosition(ASTNode node) {
        int savedPosition = getPosition();

        setPositionToStartOfNode(node);
        getFileTranslator().writeNode(node);

        setPosition(savedPosition);
    }

    protected void writeNodeAtDifferentPosition(ASTNode node) {
        getFileTranslator().writeNodeAtDifferentPosition(node);
    }

    public <TElmt> void writeCommaDelimitedNodes(List list, Consumer<TElmt> processList) {
        boolean first = true;
        for (Object elmtObject : list) {
            if (!first) {
                copySpaceAndComments();
                matchAndWrite(",");
            }

            TElmt elmt = (TElmt) elmtObject;
            processList.accept(elmt);

            first = false;
        }
    }

    public void writeCommaDelimitedNodes(List list) {
        forEach(list, (ASTNode astNode, boolean first) -> {
            if (!first) {
                copySpaceAndComments();
                matchAndWrite(",");
            }

            copySpaceAndComments();
            writeNode(astNode);
        });
    }

    public void writeNodes(List list) {
        forEach(list, (ASTNode astNode) -> {
            copySpaceAndComments();
            writeNode(astNode);
        });
    }

    public <TElmt> void writeNodes(List list, Consumer<TElmt> consumer) {
        boolean first = true;
        for (Object elmtObject : list) {
            TElmt elmt = (TElmt) elmtObject;
            consumer.accept(elmt);
        }
    }

    public void writeWildcardTypeSyntheticName(ArrayList<WildcardType> wildcardTypes, WildcardType wildcardType) {
        int index = wildcardTypes.indexOf(wildcardType);
        if (index == -1)
            throw new JUniversalException("Wildcard type not found in list");

        if (wildcardTypes.size() == 1)
            write("TWildcard");
        else write("TWildcard" + (index + 1));
    }

    public SourceNotSupportedException sourceNotSupported(String baseMessage) {
        return getFileTranslator().sourceNotSupported(baseMessage);
    }

    public JUniversalException invalidAST(String baseMessage) {
        return getFileTranslator().invalidAST(baseMessage);
    }

    public int getTargetColumn() {
        return getFileTranslator().getTargetColumn();
    }

    public TargetWriter getTargetWriter() {
        return getFileTranslator().getTargetWriter();
    }

    public void copySpaceAndComments() {
        getFileTranslator().copySpaceAndComments();
    }

    public void copySpaceAndCommentsTranslatingJavadoc(@Nullable Javadoc javadoc) {
        if (javadoc != null) {
            copySpaceAndCommentsUntilPosition(javadoc.getStartPosition());
            writeNode(javadoc);
            copySpaceAndComments();
        } else copySpaceAndComments();
    }

    public void copySpaceAndCommentsUntilPosition(int justUntilPosition) {
        getFileTranslator().copySpaceAndCommentsUntilPosition(justUntilPosition);
    }

    public void copySpaceAndCommentsEnsuringDelimiter() {
        getFileTranslator().copySpaceAndCommentsEnsuringDelimiter();
    }

    public void copySpaceAndCommentsUntilEOL() {
        getFileTranslator().copySpaceAndCommentsUntilEOL();
    }

    public void skipSpaceAndComments() {
        getFileTranslator().skipSpaceAndComments();
    }

    public void skipSpaceAndCommentsBackward() {
        getFileTranslator().skipSpaceAndCommentsBackward();
    }

    public void skipSpaceAndCommentsUntilEOL() {
        getFileTranslator().skipSpaceAndCommentsUntilEOL();
    }

    public void skipSpacesAndTabs() {
        getFileTranslator().skipSpacesAndTabs();
    }

    public void skipSpacesAndTabsBackward() {
        getFileTranslator().skipSpacesAndTabsBackward();
    }

    public void skipNewline() {
        getFileTranslator().skipNewline();
    }

    public void skipBlankLines() {
        getFileTranslator().skipBlankLines();
    }

    public void setKnowinglyProcessedTrailingSpaceAndComments(boolean knowinglyProcessedTrailingSpaceAndComments) {
        getFileTranslator().setKnowinglyProcessedTrailingSpaceAndComments(knowinglyProcessedTrailingSpaceAndComments);
    }

    public void matchAndWrite(String matchAndWrite) {
        getFileTranslator().matchAndWrite(matchAndWrite);
    }

    public void matchAndWrite(String match, String write) {
        getFileTranslator().matchAndWrite(match, write);
    }

    public void matchNodeAndWrite(ASTNode node, String string) {
        setPositionToEndOfNode(node);
        write(string);
    }

    public void match(String match) {
        getFileTranslator().match(match);
    }

    public void write(String string) {
        getFileTranslator().write(string);
    }

    public void write(BufferTargetWriter bufferTargetWriter) {
        getFileTranslator().write(bufferTargetWriter);
    }

    public void writeSpaces(int count) {
        getFileTranslator().writeSpaces(count);
    }

    /**
     * Indent as required so the next output written will be at the specified target column.   If the target writer is
     * already past that column, then nothing is output.
     *
     * @param column target column to indent to
     */
    public void indentToColumn(int column) {
        int currentColumn = getTargetColumn();
        if (currentColumn < column)
            writeSpaces(column - currentColumn);
    }

    public void writeSpacesUntilColumn(int column) {
        getFileTranslator().writeSpacesUntilColumn(column);
    }

    public void writeln(String string) {
        getFileTranslator().writeln(string);
    }

    public void writeln() {
        getFileTranslator().writeln();
    }

    public void setPosition(int position) {
        getFileTranslator().setPosition(position);
    }

    public int getPosition() {
        return getFileTranslator().getPosition();
    }

    public int getSourceLogicalColumn() {
        return getFileTranslator().getSourceLogicalColumn();
    }

    public void setPositionToStartOfNode(ASTNode node) {
        getFileTranslator().setPositionToStartOfNode(node);
    }

    public void setPositionToStartOfNodeSpaceAndComments(ASTNode node) {
        getFileTranslator().setPositionToStartOfNodeSpaceAndComments(node);
    }

    public void setPositionToEndOfNode(ASTNode node) {
        getFileTranslator().setPositionToEndOfNode(node);
    }

    public void setPositionToEndOfNodeSpaceAndComments(ASTNode node) {
        getFileTranslator().setPositionToEndOfNodeSpaceAndComments(node);
    }

    public void ensureModifiersJustFinalOrAnnotations(List<?> modifiers) {
        getFileTranslator().ensureModifiersJustFinalOrAnnotations(modifiers);
    }

    public void skipModifiers(List extendedModifiers) {
        getFileTranslator().skipModifiers(extendedModifiers);
    }

    public int getPreferredIndent() {
        return getFileTranslator().getPreferredIndent();
    }

    public MethodDeclaration getFunctionalInterfaceMethod(TypeDeclaration typeDeclaration) {
        if (!typeDeclaration.isInterface())
            throw sourceNotSupported("Type has @FunctionalInterface annotation but isn't an interface");

        MethodDeclaration[] methodDeclarations = typeDeclaration.getMethods();
        int methodCount = methodDeclarations.length;
        if (methodCount != 1)
            throw sourceNotSupported("Interface has " + methodCount + " methods, when it should have exactly 1 method, to be a functional interface");

        return methodDeclarations[0];
    }
}

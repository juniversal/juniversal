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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Javadoc;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public abstract class ASTNodeWriter<T extends ASTNode> {
    abstract public void write(T node);

    public boolean canProcessTrailingWhitespaceOrComments() {
        return false;
    }
    
    protected abstract SourceFileWriter getSourceFileWriter();

    protected void writeNode(ASTNode node) {
        getSourceFileWriter().writeNode(node);
    }

    protected void writeNodeFromOtherPosition(ASTNode node) {
        int savedPosition = getPosition();

        setPositionToStartOfNode(node);
        getSourceFileWriter().writeNode(node);

        setPosition(savedPosition);
    }

    protected void writeNodeAtDifferentPosition(ASTNode node) {
        getSourceFileWriter().writeNodeAtDifferentPosition(node);
    }

    public <TElmt> void writeCommaDelimitedNodes(List list, ASTUtil.IProcessListElmt<TElmt> processList) {
        boolean first = true;
        for (Object elmtObject : list) {
            if (!first) {
                copySpaceAndComments();
                matchAndWrite(",");
            }

            TElmt elmt = (TElmt) elmtObject;
            processList.process(elmt);

            first = false;
        }
    }

    public void writeCommaDelimitedNodes(List list) {
        boolean first = true;
        for (Object astNodeObject : list) {
            if (!first) {
                copySpaceAndComments();
                matchAndWrite(",");
            }

            ASTNode astNode = (ASTNode) astNodeObject;

            copySpaceAndComments();
            writeNode(astNode);

            first = false;
        }
    }

    public void writeNodes(List list) {
        for (Object astNodeObject : list) {
            ASTNode astNode = (ASTNode) astNodeObject;

            copySpaceAndComments();
            writeNode(astNode);
        }
    }

    public SourceNotSupportedException sourceNotSupported(String baseMessage) {
        return getSourceFileWriter().sourceNotSupported(baseMessage);
    }

    public JUniversalException invalidAST(String baseMessage) {
        return getSourceFileWriter().invalidAST(baseMessage);
    }

    public int getTargetColumn() {
        return getSourceFileWriter().getTargetColumn();
    }

    public TargetWriter getTargetWriter() {
        return getSourceFileWriter().getTargetWriter();
    }

    public void copySpaceAndComments() {
        getSourceFileWriter().copySpaceAndComments();
    }

    public void copySpaceAndCommentsTranslatingJavadoc(@Nullable Javadoc javadoc) {
        if (javadoc != null) {
            copySpaceAndCommentsUntilPosition(javadoc.getStartPosition());
            writeNode(javadoc);
            copySpaceAndComments();
        }
        else copySpaceAndComments();
    }

    public void copySpaceAndCommentsUntilPosition(int justUntilPosition) {
        getSourceFileWriter().copySpaceAndCommentsUntilPosition(justUntilPosition);
    }

    public void copySpaceAndCommentsEnsuringDelimiter() {
        getSourceFileWriter().copySpaceAndCommentsEnsuringDelimiter();
    }

    public void copySpaceAndCommentsUntilEOL() {
        getSourceFileWriter().copySpaceAndCommentsUntilEOL();
    }

    public void skipSpaceAndComments() {
        getSourceFileWriter().skipSpaceAndComments();
    }

    public void skipSpaceAndCommentsBackward() {
        getSourceFileWriter().skipSpaceAndCommentsBackward();
    }

    public void skipSpaceAndCommentsUntilEOL() {
        getSourceFileWriter().skipSpaceAndCommentsUntilEOL();
    }

    public void skipSpacesAndTabs() {
        getSourceFileWriter().skipSpacesAndTabs();
    }

    public void skipSpacesAndTabsBackward() {
        getSourceFileWriter().skipSpacesAndTabsBackward();
    }

    public void skipNewline() {
        getSourceFileWriter().skipNewline();
    }

    public void skipBlankLines() {
        getSourceFileWriter().skipBlankLines();
    }

    public void setKnowinglyProcessedTrailingSpaceAndComments(boolean knowinglyProcessedTrailingSpaceAndComments) {
        getSourceFileWriter().setKnowinglyProcessedTrailingSpaceAndComments(knowinglyProcessedTrailingSpaceAndComments);
    }

    public void matchAndWrite(String matchAndWrite) {
        getSourceFileWriter().matchAndWrite(matchAndWrite);
    }

    public void matchAndWrite(String match, String write) {
        getSourceFileWriter().matchAndWrite(match, write);
    }

    public void matchNodeAndWrite(ASTNode node, String string) {
        setPositionToEndOfNode(node);
        write(string);
    }

    public void match(String match) {
        getSourceFileWriter().match(match);
    }

    public void write(String string) {
        getSourceFileWriter().write(string);
    }

    public void write(BufferTargetWriter bufferTargetWriter) {
        getSourceFileWriter().write(bufferTargetWriter);
    }

    public void writeSpaces(int count) {
        getSourceFileWriter().writeSpaces(count);
    }

    public void writeSpacesUntilColumn(int column) {
        getSourceFileWriter().writeSpacesUntilColumn(column);
    }

    public void writeln(String string) {
        getSourceFileWriter().writeln(string);
    }

    public void writeln() {
        getSourceFileWriter().writeln();
    }

    public void setPosition(int position) {
        getSourceFileWriter().setPosition(position);
    }

    public int getPosition() {
        return getSourceFileWriter().getPosition();
    }

    public int getSourceLogicalColumn() {
        return getSourceFileWriter().getSourceLogicalColumn();
    }

    public void setPositionToStartOfNode(ASTNode node) {
        getSourceFileWriter().setPositionToStartOfNode(node);
    }

    public void setPositionToStartOfNodeSpaceAndComments(ASTNode node) {
        getSourceFileWriter().setPositionToStartOfNodeSpaceAndComments(node);
    }

    public void setPositionToEndOfNode(ASTNode node) {
        getSourceFileWriter().setPositionToEndOfNode(node);
    }

    public void setPositionToEndOfNodeSpaceAndComments(ASTNode node) {
        getSourceFileWriter().setPositionToEndOfNodeSpaceAndComments(node);
    }

    public void ensureModifiersJustFinalOrAnnotations(List<?> modifiers) {
        getSourceFileWriter().ensureModifiersJustFinalOrAnnotations(modifiers);
    }

    public void skipModifiers(List extendedModifiers) {
        getSourceFileWriter().skipModifiers(extendedModifiers);
    }

    public int getPreferredIndent() {
        return getSourceFileWriter().getPreferredIndent();
    }
}

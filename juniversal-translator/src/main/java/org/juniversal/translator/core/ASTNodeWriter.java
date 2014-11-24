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
        getSourceFileWriter().writeNodeAtDifferentPosition(node, getContext());
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

    public Context getContext() { return getSourceFileWriter().getContext(); }

    public SourceNotSupportedException sourceNotSupported(String baseMessage) {
        return getContext().sourceNotSupported(baseMessage);
    }

    public JUniversalException invalidAST(String baseMessage) {
        return getContext().invalidAST(baseMessage);
    }

    public int getTargetColumn() {
        return getContext().getTargetColumn();
    }

    public TargetWriter getTargetWriter() {
        return getContext().getTargetWriter();
    }

    public void copySpaceAndComments() {
        getContext().copySpaceAndComments();
    }

    public void copySpaceAndCommentsEnsuringDelimiter() {
        getContext().copySpaceAndCommentsEnsuringDelimiter();
    }

    public void copySpaceAndCommentsUntilEOL() {
        getContext().copySpaceAndCommentsUntilEOL();
    }

    public void skipSpaceAndComments() {
        getContext().skipSpaceAndComments();
    }

    public void skipSpacesAndTabs() {
        getContext().skipSpacesAndTabs();
    }

    public void setKnowinglyProcessedTrailingSpaceAndComments(boolean knowinglyProcessedTrailingSpaceAndComments) {
        getContext().setKnowinglyProcessedTrailingSpaceAndComments(knowinglyProcessedTrailingSpaceAndComments);
    }

    public void matchAndWrite(String matchAndWrite) {
        getContext().matchAndWrite(matchAndWrite);
    }

    public void matchAndWrite(String match, String write) {
        getContext().matchAndWrite(match, write);
    }

    public void match(String match) {
        getContext().match(match);
    }

    public void write(String string) {
        getContext().write(string);
    }

    public void writeSpaces(int count) {
        getContext().writeSpaces(count);
    }

    public void writeSpacesUntilColumn(int column) {
        getContext().writeSpacesUntilColumn(column);
    }

    public void writeln(String string) {
        getContext().writeln(string);
    }

    public void writeln() {
        getContext().writeln();
    }

    public void setPosition(int position) {
        getContext().setPosition(position);
    }

    public int getPosition() {
        return getContext().getPosition();
    }

    public void setPositionToStartOfNode(ASTNode node) {
        getContext().setPositionToStartOfNode(node);
    }

    public void setPositionToEndOfNode(ASTNode node) {
        getContext().setPositionToEndOfNode(node);
    }

    public void setPositionToEndOfNodeSpaceAndComments(ASTNode node) {
        getContext().setPositionToEndOfNodeSpaceAndComments(node);
    }

    public void ensureModifiersJustFinalOrAnnotations(List<?> modifiers) {
        getContext().ensureModifiersJustFinalOrAnnotations(modifiers);
    }

    public void skipModifiers(List extendedModifiers) {
        getContext().skipModifiers(extendedModifiers);
    }

    public int getPreferredIndent() {
        return getContext().getPreferredIndent();
    }
}

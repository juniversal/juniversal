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
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.jetbrains.annotations.Nullable;
import org.xuniversal.translator.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.juniversal.translator.core.ASTUtil.*;


public abstract class ASTNodeWriter<T extends ASTNode> {
    public abstract void write(T node);

    public boolean canProcessTrailingWhitespaceOrComments() {
        return false;
    }

    protected abstract Translator getTranslator();

    protected abstract JavaSourceContext getContext();

    protected void writeNode(ASTNode node) {
        getTranslator().writeNode(node);
    }

    public abstract TargetWriter getTargetWriter();

    protected void writeNodeFromOtherPosition(ASTNode node) {
        int savedPosition = getPosition();

        setPositionToStartOfNode(node);
        getTranslator().writeNode(node);

        setPosition(savedPosition);
    }

    /**
     * Write a node that's not at the current context position. The context position is unchanged by this method--the
     * position is restored to the original position when done. No comments before/after the node are written. This
     * method can be used to write a node multiple times.
     *
     * @param node node to write
     */
    protected void writeNodeAtDifferentPosition(ASTNode node) {
        JavaSourceContext context = getContext();

        int originalPosition = context.getPosition();
        context.setPosition(node.getStartPosition());
        getTranslator().writeNode(node);
        context.setPosition(originalPosition);
    }

    public <TElmt> void writeCommaDelimitedNodes(List list, Consumer<TElmt> processList) {
        forEach(list, (TElmt elmt, boolean first) -> {
            if (!first) {
                copySpaceAndComments();
                matchAndWrite(",");
            }

            processList.accept(elmt);
            first = false;
        });
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
        return getContext().sourceNotSupported(baseMessage);
    }

    public JUniversalException invalidAST(String baseMessage) {
        return new JUniversalException(baseMessage + "\n" + getContext().getCurrentPositionDescription());
    }

    public int getTargetColumn() {
        return getContext().getTargetColumn();
    }

    public void copySpaceAndComments() {
        getContext().copySpaceAndComments();
    }

    public void copySpaceAndCommentsTranslatingJavadoc(@Nullable Javadoc javadoc) {
        if (javadoc != null) {
            copySpaceAndCommentsUntilPosition(javadoc.getStartPosition());
            writeNode(javadoc);
            copySpaceAndComments();
        } else copySpaceAndComments();
    }

    public void copySpaceAndCommentsUntilPosition(int justUntilPosition) {
        getContext().copySpaceAndCommentsUntilPosition(justUntilPosition);
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

    public void skipSpaceAndCommentsBackward() {
        getContext().skipSpaceAndCommentsBackward();
    }

    public void skipSpaceAndCommentsUntilEOL() {
        getContext().skipSpaceAndCommentsUntilEOL();
    }

    public void skipSpacesAndTabs() {
        getContext().skipSpacesAndTabs();
    }

    public void skipSpacesAndTabsBackward() {
        getContext().skipSpacesAndTabsBackward();
    }

    public void skipNewline() {
        getContext().skipNewline();
    }

    public void skipBlankLines() {
        getContext().skipBlankLines();
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

    public void matchNodeAndWrite(ASTNode node, String string) {
        setPositionToEndOfNode(node);
        write(string);
    }

    public TypeName getTargetType(ITypeBinding typeBinding) {
        assert ! typeBinding.isParameterizedType() && ! typeBinding.isTypeVariable();

        // Get the ITypeBinding corresponding to the type declaration, not the type reference.   For one thing,
        // this gets rid of any generic type parameter values for generic type references
        typeBinding = typeBinding.getTypeDeclaration();

/*
        // If the TypeBinding is null then there must be a Java compile error, so just fall back to using the simple
        // type name.   And for type variables, we don't change them in any way, just returning that
        if (typeBinding == null || typeBinding.isTypeVariable()) {
            return new TypeName(typeBinding.getName());
        }
*/

        TargetProfile targetProfile = getTranslator().getTargetProfile();

        String qualifiedName = typeBinding.getQualifiedName();
        if (qualifiedName.equals("java.lang.Object"))
            return targetProfile.getObjectType();
        else if (qualifiedName.equals("java.lang.String"))
            return targetProfile.getStringType();
        else if (qualifiedName.equals("java.lang.StringBuilder"))
            return targetProfile.getStringBuilderType();
        else if (typeBinding.isArray())
            return targetProfile.getArrayType();

        // If the type is an inner class, get its fully nested name
        ArrayList<String> nestedTypeName = new ArrayList<>();
        ITypeBinding currType = typeBinding;
        while (currType != null) {
            nestedTypeName.add(0, currType.getName());
            currType = currType.getDeclaringClass();
        }

        boolean found = false;
        if (nestedTypeName.size() > 1)
            found = true;

        return new TypeName(new HierarchicalName(typeBinding.getPackage().getNameComponents()),
                new HierarchicalName(nestedTypeName));
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

    public int getSourceLogicalColumn() {
        return getContext().getSourceLogicalColumn();
    }

    /**
     * Sets the position to the beginning of the node's code. The AST parser includes Javadoc before a node with the
     * node; we skip past that as we treat spaces/comments separately. Use setPositionToStartOfNodeSpaceAndComments if
     * you want to include the space/comments that our heuristics pair to a node.
     *
     * @param node
     */
    public void setPositionToStartOfNode(ASTNode node) {
        JavaSourceContext context = getTranslator().getContext();
        context.setPosition(node.getStartPosition());
        context.skipSpaceAndComments();
    }

    /**
     * Set the position to the beginning of the whitespace/comments for a node, ignoring any comments associated with
     * the previous node. The heuristic used here is that whitespace/comments that come before a node are for that node,
     * unless they are on the end of a line containing the previous node.
     * <p/>
     * One consequence of these rules is that if the previous node (or its trailing comment) & current node are on the
     * same line, all comments/space between them are assumed to be for the previous node. Otherwise, the position will
     * be set to the beginning of the line following the previous node / previous node's line ending comment.
     *
     * @param node node in question
     */
    public void setPositionToStartOfNodeSpaceAndComments(ASTNode node) {
        JavaSourceContext context = getContext();

        context.setPosition(node.getStartPosition());
        context.skipSpaceAndCommentsBackward();      // Now at the end of the previous node
        context.skipSpaceAndCommentsUntilEOL();      // Skip any comments on previous node's line
        context.skipNewline();                       // Skip the newline character if present
    }

    public void setPositionToEndOfNode(ASTNode node) {
        setPosition(ASTUtil.getEndPosition(node));
    }

    /**
     * Sets the position to the end of the node & any trailing spaces/comments for the node. When called on a statement
     * node or other node that's the last thing on a line, normally the context will now be positioned at the newline
     * character.
     *
     * @param node node in question
     */
    public void setPositionToEndOfNodeSpaceAndComments(ASTNode node) {
        setPositionToEndOfNode(node);
        getContext().skipSpaceAndCommentsUntilEOL();
    }

    /**
     * See if the specified node starts on the same line as we're on currently.   This is used, for instance, when
     * formatting if statements when adding required braces.
     *
     * @param node ASTNode in question
     * @return true if node starts on the current line, false if it starts on a different (presumably later) line
     */
    public boolean startsOnSameLine(ASTNode node) {
        return getContext().getSourceLineNumber() == getContext().getSourceLineNumber(node.getStartPosition());
    }

    public void ensureModifiersJustFinalOrAnnotations(List modifiers) {
        forEach(modifiers, (IExtendedModifier extendedModifier) -> {
            if (!(isFinal(extendedModifier) || isAnnotation(extendedModifier)))
                throw getTranslator().getContext().sourceNotSupported("Modifier isn't supported supported here: " + extendedModifier.toString());
        });
    }

    /**
     * Set the context's current position to just after the end of the modifiers, including (if there are modifiers) any
     * trailing spaces/comments after the last one. If there are no modifiers in the list, the position remains
     * unchanged.
     *
     * @param extendedModifiers modifiers
     */
    public void skipModifiers(List extendedModifiers) {
        int size = extendedModifiers.size();
        if (size == 0)
            return;
        IExtendedModifier lastExtendedModifier = (IExtendedModifier) extendedModifiers.get(size - 1);
        setPositionToEndOfNodeSpaceAndComments((ASTNode) lastExtendedModifier);
    }

    public int getPreferredIndent() {
        return getTranslator().getPreferredIndent();
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

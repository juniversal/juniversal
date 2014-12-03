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

import org.eclipse.jdt.core.dom.*;
import org.juniversal.translator.cplusplus.OutputType;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;

import static org.juniversal.translator.core.ASTUtil.isAnnotation;
import static org.juniversal.translator.core.ASTUtil.isFinal;

public abstract class SourceFileWriter {
    private HashMap<Class<? extends ASTNode>, ASTNodeWriter> visitors = new HashMap<>();
    private SourceFile sourceFile;
    private String source;
    private TargetWriter targetWriter;
    private SourceCopier sourceCopier;
    private int position;
    private OutputType outputType;
    private int preferredIndent = 4;    // TODO: Set
    private boolean knowinglyProcessedTrailingSpaceAndComments = false;


    protected SourceFileWriter(Translator translator, SourceFile sourceFile, Writer writer) {
        this.sourceFile = sourceFile;
        this.source = sourceFile.getSource();

        this.targetWriter = new TargetWriter(writer, translator.getDestTabStop());
        this.position = sourceFile.getCompilationUnit().getStartPosition();

        sourceCopier = new SourceCopier(this.sourceFile, source, targetWriter);
    }

    public SourceFile getSourceFile() {
        return sourceFile;
    }

    public abstract Translator getTranslator();

    public abstract Context getContext();

    protected void addWriter(Class<? extends ASTNode> clazz, ASTNodeWriter visitor) {
        if (visitors.get(clazz) != null)
            throw new JUniversalException("Writer for class " + clazz + " already added to ASTWriters");
        visitors.put(clazz, visitor);
    }

    public ASTNodeWriter getVisitor(Class clazz) {
        ASTNodeWriter visitor = visitors.get(clazz);
        if (visitor == null)
            throw new JUniversalException("No visitor found for class " + clazz.getName());
        return visitor;
    }

    private static final boolean VALIDATE_CONTEXT_POSITION = true;

    public void writeNode(ASTNode node) {
        Context context = getContext();

        int nodeStartPosition;
        if (VALIDATE_CONTEXT_POSITION) {
            nodeStartPosition = node.getStartPosition();

            // If the node starts with Javadoc (true for method declarations with Javadoc before
            // them), then skip past it; the caller is always expected to handle any comments,
            // Javadoc or not, coming before the start of code proper for the node. The exception to
            // that rule is the CompilationUnit; as outermost node it handles any comments at the
            // beginning itself.
            if (sourceCopier.getSourceCharAt(nodeStartPosition) == '/'
                    && sourceCopier.getSourceCharAt(nodeStartPosition + 1) == '*'
                    && sourceCopier.getSourceCharAt(nodeStartPosition + 2) == '*'
                    && !(node instanceof CompilationUnit))
                assertPositionIs(sourceCopier.skipSpaceAndComments(nodeStartPosition, false));
            else assertPositionIs(nodeStartPosition);
        }

        getVisitor(node.getClass()).write(node);

        if (VALIDATE_CONTEXT_POSITION) {
            if (knowinglyProcessedTrailingSpaceAndComments)
                assertPositionIsAtLeast(nodeStartPosition + node.getLength());
            else assertPositionIs(nodeStartPosition + node.getLength());
        }

        this.knowinglyProcessedTrailingSpaceAndComments = false;
    }

    public void writeRootNode(ASTNode node) {
        try {
            setPosition(node.getStartPosition());
            writeNode(node);
        } catch (UserViewableException e) {
            throw e;
        } catch (RuntimeException e) {
            if (e instanceof ContextPositionMismatchException)
                throw e;
            else
                throw new JUniversalException(e.getMessage() + "\nError occurred with context at position\n"
                        + getPositionDescription(getPosition()), e);
        }
    }

    /**
     * Write a node that's not at the current context position. The context position is unchanged by this method--the
     * position is restored to the original position when done. No comments before/after the node are written. This
     * method can be used to write a node multiple times.
     *
     * @param node node to write
     */
    public void writeNodeAtDifferentPosition(ASTNode node) {
        int originalPosition = getPosition();
        setPosition(node.getStartPosition());
        writeNode(node);
        setPosition(originalPosition);
    }

    public CompilationUnit getCompilationUnit() {
        return sourceFile.getCompilationUnit();
    }

    /**
     * Get the current position for the context.
     *
     * @return current position for context
     */
    public int getPosition() {
        return position;
    }

    /**
     * Change the context position to the specified new position. Only needed when position changes explicitly, other
     * than as a result of calling methods of this class to match and skip.
     *
     * @param position new position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * This flag indicates that the node writer purposely processed some of the whitespace/comments that come after a
     * node, so the validation check shouldn't flag that as unexpected.   Most AST nodes don't need to do this, but a
     * few do, in exceptional cases, in order to bind the space/comments with the node before it & perhaps insert new
     * symbols after that in the target language.
     *
     * @param knowinglyProcessedTrailingSpaceAndComments flag value
     */
    public void setKnowinglyProcessedTrailingSpaceAndComments(boolean knowinglyProcessedTrailingSpaceAndComments) {
        this.knowinglyProcessedTrailingSpaceAndComments = knowinglyProcessedTrailingSpaceAndComments;
    }

    /**
     * Set the position to the beginning of the whitespace/comments for a node, ignoring any comments associated with
     * the previous node. The heuristic used here is that whitespace/comments that come before a node are for that node,
     * unless they are on the end of a line containing the previous node.
     * <p>
     * One consequence of these rules is that if the previous node (or its trailing comment) & current node are on the
     * same line, all comments/space between them are assumed to be for the previous node. Otherwise, the position will
     * be set to the beginning of the line following the previous node / previous node's line ending comment.
     *
     * @param node node in question
     */
    public void setPositionToStartOfNodeSpaceAndComments(ASTNode node) {
        setPosition(node.getStartPosition());
        skipSpaceAndCommentsBackward();      // Now at the end of the previous node
        skipSpaceAndCommentsUntilEOL();      // Skip any comments on previous node's line
        skipNewline();                       // Skip the newline character if present
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
        skipSpaceAndCommentsUntilEOL();
    }

    /**
     * Sets the position to the beginning of the node's code. The AST parser includes Javadoc before a node with the
     * node; we skip past that as we treat spaces/comments separately. Use setPositionToStartOfNodeSpaceAndComments if
     * you want to include the space/comments that our heuristics pair to a node.
     *
     * @param node
     */
    public void setPositionToStartOfNode(ASTNode node) {
        setPosition(node.getStartPosition());
        skipSpaceAndComments();
    }

    public void setPositionToEndOfNode(ASTNode node) {
        setPosition(ASTUtil.getEndPosition(node));
    }

    public int getPreferredIndent() {
        return preferredIndent;
    }

    /**
     * Assert that the current position for the context is expectedPosition. Throw an exception if it isn't.
     *
     * @param expectedPosition position where expect that context is at
     */
    public void assertPositionIs(int expectedPosition) {
        if (position != expectedPosition)
            throw new ContextPositionMismatchException("Context is positioned at:\n" + getPositionDescription(position)
                    + "\n  when expected it to be positioned at:\n" + getPositionDescription(expectedPosition));
    }

    /**
     * Assert that the current position for the context is at least expectedPositionMin. Throw an exception if it
     * isn't.
     *
     * @param expectedPositionMin position where expect that context is at
     */
    public void assertPositionIsAtLeast(int expectedPositionMin) {
        if (position < expectedPositionMin)
            throw new ContextPositionMismatchException("Context is positioned at:\n" + getPositionDescription(position)
                    + "\n  when expected it to be positioned here or after:\n" + getPositionDescription(expectedPositionMin));
    }

    public SourceCopier getSourceCopier() {
        return sourceCopier;
    }

    public String getPositionDescription(int position) {
        return sourceFile.getPositionDescription(position);
    }

    public int getSourceLogicalColumn(int position) {
        return sourceFile.getSourceLogicalColumn(position);
    }

    public int getSourceLogicalColumn() {
        return sourceFile.getSourceLogicalColumn(getPosition());
    }

    public int getSourceLineNumber(int position) {
        return sourceFile.getSourceLineNumber(position);
    }

    public int getSourceLineNumber() {
        return sourceFile.getSourceLineNumber(getPosition());
    }

    /**
     * See if the specified node starts on the same line as we're on currently.   This is used, for instance, when
     * formatting if statements when adding required braces.
     *
     * @param node ASTNode in question
     * @return true if node starts on the current line, false if it starts on a different (presumably later) line
     */
    public boolean startsOnSameLine(ASTNode node) {
        return getSourceLineNumber() == getSourceLineNumber(node.getStartPosition());
    }

    public int getTargetColumn() {
        return targetWriter.getCurrColumn();
    }

    /**
     * Copy whitespace and/or comments, from the Java source to the target language.
     *
     * @return true if something was copied, false if there was no whitespace/comments
     */
    public boolean copySpaceAndComments() {
        int startingPosition = position;
        position = sourceCopier.copySpaceAndComments(position, false);
        return position != startingPosition;
    }

    /**
     * Copy whitespace and/or comments, from the Java source to the target language.   If there's nothing at all in the
     * Java, then a single space is added to ensure there's a whitespace delimiter in the target language.
     */
    public void copySpaceAndCommentsEnsuringDelimiter() {
        if (!copySpaceAndComments())
            write(" ");
    }

    public void copySpaceAndCommentsUntilEOL() {
        position = sourceCopier.copySpaceAndComments(position, true);
    }

    public void skipSpaceAndComments() {
        position = sourceCopier.skipSpaceAndComments(position, false);
    }

    public void skipSpaceAndCommentsUntilEOL() {
        position = sourceCopier.skipSpaceAndComments(position, true);
    }

    public void skipSpacesAndTabs() {
        position = sourceCopier.skipSpacesAndTabs(position);
    }

    public void skipBlankLines() {
        position = sourceCopier.skipBlankLines(position);
    }

    public void skipNewline() {
        position = sourceCopier.skipNewline(position);
    }

    public void skipSpaceAndCommentsBackward() {
        position = sourceCopier.skipSpaceAndCommentsBackward(position);
    }

    public void skipSpacesAndTabsBackward() {
        position = sourceCopier.skipSpacesAndTabsBackward(position);
    }

    /**
     * Ensure that the Java source contains the specified match string at it's current position & advance past
     *
     * @param match string to ensure occurs in source
     */
    public void match(String match) {
        position = sourceCopier.match(position, match);
    }

    /**
     * Ensure that the Java source contains the specified match string at it's current position & advance past it. Also
     * write "write" to output.
     *
     * @param match string to ensure occurs in source
     * @param write string to write to C++ output
     */
    public void matchAndWrite(String match, String write) {
        position = sourceCopier.match(position, match);
        write(write);
    }

    /**
     * Match the specified string in the Java source and write it to the output. Equivalent to
     * matchAndWrite(matchAndWrite, matchAndWrite)
     *
     * @param matchAndWrite string to both match and write to output
     */
    public void matchAndWrite(String matchAndWrite) {
        position = sourceCopier.match(position, matchAndWrite);
        write(matchAndWrite);
    }

    /**
     * Write the specified string to the C++ output.
     *
     * @param string string to write to C++ output.
     */
    public void write(String string) {
        targetWriter.write(string);
    }

    public void write(BufferTargetWriter bufferTargetWriter) {
        targetWriter.write(bufferTargetWriter);
    }

    /**
     * /** Write the specified number of spaces to the output.
     *
     * @param count number of spaces to write
     */
    public void writeSpaces(int count) {
        targetWriter.writeSpaces(count);
    }

    /**
     * Space over so that the next character written will be at the specified column. If the current column is already
     * past the specified column, nothing is output.
     *
     * @param column desired column
     */
    public void writeSpacesUntilColumn(int column) {
        targetWriter.writeSpacesUntilColumn(column);
    }

    /**
     * Write the specified string to the C++ output, followed by a newline.
     *
     * @param string string to write to C++ output
     */
    public void writeln(String string) {
        targetWriter.write(string);
        targetWriter.write("\n");
    }

    /**
     * Write a newline to the C++ output.
     */
    public void writeln() {
        targetWriter.write("\n");
    }

    /**
     * Write the specified character to the C++ output. Equivalent to write(Character.toString(character))
     *
     * @param character character to write to output
     */
    public void write(char character) {
        targetWriter.write(character);
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

    public void throwSourceNotSupported(String baseMessage) {
        throw new SourceNotSupportedException(baseMessage, getPositionDescription(position));
    }

    public SourceNotSupportedException sourceNotSupported(String baseMessage) {
        return new SourceNotSupportedException(baseMessage, getPositionDescription(position));
    }

    public ITypeBinding resolveTypeBinding(Type type) {
        ITypeBinding typeBinding = type.resolveBinding();
        if (typeBinding == null)
            throw sourceNotSupported("Type binding could not be resolved; perhaps this Java code doesn't compile or there's a missing JAR dependency?");
        return typeBinding;
    }

    public void ensureModifiersJustFinalOrAnnotations(List<?> modifiers) {
        for (Object modifierObject : modifiers) {
            IExtendedModifier extendedModifier = (IExtendedModifier) modifierObject;

            if (!(isFinal(extendedModifier) || isAnnotation(extendedModifier)))
                throwSourceNotSupported("Modifier isn't supported supported here: " + extendedModifier.toString());
        }
    }

    public JUniversalException invalidAST(String baseMessage) {
        return new JUniversalException(baseMessage + "\n" + getPositionDescription(position));
    }

    public OutputType getOutputType() {
        return outputType;
    }

    public TargetWriter getTargetWriter() {
        return targetWriter;
    }

    public RestoreTargetWriter setTargetWriter(TargetWriter targetWriter) {
        TargetWriter originalTargetWriter = this.targetWriter;
        this.targetWriter = targetWriter;
        sourceCopier = new SourceCopier(sourceFile, source, this.targetWriter);
        return new RestoreTargetWriter(originalTargetWriter);
    }

    public class RestoreTargetWriter implements AutoCloseable {
        private TargetWriter originalTargetWriter;

        public RestoreTargetWriter(TargetWriter originalTargetWriter) {
            this.originalTargetWriter = originalTargetWriter;
        }

        @Override
        public void close() {
            targetWriter = originalTargetWriter;
            sourceCopier = new SourceCopier(sourceFile, source, targetWriter);
        }
    }
}

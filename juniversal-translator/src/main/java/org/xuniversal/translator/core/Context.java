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

public abstract class Context {
    private String source;
    private SourceCopier sourceCopier;
    private int position;
    private boolean knowinglyProcessedTrailingSpaceAndComments = false;
    private boolean writingMethodImplementation;

    public Context(SourceFile sourceFile, TargetWriter targetWriter) {
        this.source = sourceFile.getSource();
        this.position = 0;
        sourceCopier = new SourceCopier(sourceFile, source, targetWriter);
    }

    public abstract SourceFile getSourceFile();

    public abstract TargetWriter getTargetWriter();

    public String getSource() {
        return source;
    }

    public SourceCopier getSourceCopier() {
        return sourceCopier;
    }

    public int getPosition() {
        return position;
    }

    public int getTargetColumn() {
        return getTargetWriter().getCurrColumn();
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean getKnowinglyProcessedTrailingSpaceAndComments() {
        return knowinglyProcessedTrailingSpaceAndComments;
    }

    public void setKnowinglyProcessedTrailingSpaceAndComments(boolean knowinglyProcessedTrailingSpaceAndComments) {
        this.knowinglyProcessedTrailingSpaceAndComments = knowinglyProcessedTrailingSpaceAndComments;
    }


    /**
     * Copy whitespace and/or comments, from the Java source to the target language.
     *
     * @return true if something was copied, false if there was no whitespace/comments
     */
    public boolean copySpaceAndComments() {
        int startingPosition = position;
        position = sourceCopier.copySpaceAndComments(position, false, -1);
        return position != startingPosition;
    }

    /**
     * Copy whitespace and/or comments, from the Java source to the target language.   Copying stops at the end of the
     * space/comments or when reaching the specified position, whatever comes first.   This method is typically used to
     * copy regular comments but stop at Javadoc comments that will be translated separately.
     *
     * @return true if something was copied, false if there was no whitespace/comments
     */
    public boolean copySpaceAndCommentsUntilPosition(int justUntilPosition) {
        int startingPosition = position;
        position = sourceCopier.copySpaceAndComments(position, false, justUntilPosition);
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
        position = sourceCopier.copySpaceAndComments(position, true, -1);
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
        getTargetWriter().write(string);
    }

    /**
     * /** Write the specified number of spaces to the output.
     *
     * @param count number of spaces to write
     */
    public void writeSpaces(int count) {
        getTargetWriter().writeSpaces(count);
    }

    /**
     * Space over so that the next character written will be at the specified column. If the current column is already
     * past the specified column, nothing is output.
     *
     * @param column desired column
     */
    public void writeSpacesUntilColumn(int column) {
        getTargetWriter().writeSpacesUntilColumn(column);
    }

    /**
     * Write the specified string to the C++ output, followed by a newline.
     *
     * @param string string to write to C++ output
     */
    public void writeln(String string) {
        getTargetWriter().writeln(string);
    }

    /**
     * Write a newline to the C++ output.
     */
    public void writeln() {
        getTargetWriter().writeln();
    }

    /**
     * Write the specified character to the C++ output. Equivalent to write(Character.toString(character))
     *
     * @param character character to write to output
     */
    public void write(char character) {
        getTargetWriter().write(character);
    }


    public String getCurrentPositionDescription() {
        return getSourceFile().getPositionDescription(getPosition());
    }

    public String getPositionDescription(int position) {
        return getSourceFile().getPositionDescription(position);
    }

    public int getSourceLogicalColumn(int position) {
        return getSourceFile().getSourceLogicalColumn(position);
    }

    public int getSourceLogicalColumn() {
        return getSourceFile().getSourceLogicalColumn(getPosition());
    }

    public int getSourceLineNumber(int position) {
        return getSourceFile().getLineNumber(position);
    }

    public int getSourceLineNumber() {
        return getSourceFile().getLineNumber(getPosition());
    }

    /**
     * Assert that the current position for the context is expectedPosition. Throw an exception if it isn't.
     *
     * @param expectedPosition position where expect that context is at
     */
    public void assertPositionIs(int expectedPosition) {
        if (getPosition() != expectedPosition)
            throw new ContextPositionMismatchException("Context is positioned at:\n" + getPositionDescription(getPosition())
                                                       + "\n  when expected it to be positioned at:\n" + getPositionDescription(expectedPosition));
    }

    /**
     * Assert that the current position for the context is at least expectedPositionMin. Throw an exception if it
     * isn't.
     *
     * @param expectedPositionMin position where expect that context is at
     */
    public void assertPositionIsAtLeast(int expectedPositionMin) {
        if (getPosition() < expectedPositionMin)
            throw new ContextPositionMismatchException("Context is positioned at:\n" + getPositionDescription(getPosition())
                                                       + "\n  when expected it to be positioned here or after:\n" + getPositionDescription(expectedPositionMin));
    }

    public SourceNotSupportedException sourceNotSupported(String baseMessage) {
        return new SourceNotSupportedException(baseMessage, getCurrentPositionDescription());
    }

    public boolean isWritingMethodImplementation() {
        return writingMethodImplementation;
    }

    public void setWritingMethodImplementation(boolean writingMethodImplementation) {
        this.writingMethodImplementation = writingMethodImplementation;
    }
}

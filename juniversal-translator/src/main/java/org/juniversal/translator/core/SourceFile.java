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

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class SourceFile {
	private final CompilationUnit compilationUnit;
	private @Nullable File sourceFile;    // Null if there is no file
	private final String source;
    private int sourceTabStop;

	public SourceFile(CompilationUnit compilationUnit, File sourceFile, int sourceTabStop) {
		this.compilationUnit = compilationUnit;
		this.sourceFile = sourceFile;
		this.source = Util.readFile(sourceFile);
        this.sourceTabStop = sourceTabStop;
	}

	public SourceFile(CompilationUnit compilationUnit, String source, int sourceTabStop) {
		this.compilationUnit = compilationUnit;
		this.sourceFile = null;
		this.source = source;
        this.sourceTabStop = sourceTabStop;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public @Nullable File getSourceFile() {
		return sourceFile;
	}

	public boolean isDiskFile() {
		return sourceFile != null;
	}

	public String getSource() {
		return source;
	}

    public int getSourceTabStop() {
        return sourceTabStop;
    }

    /**
     * Returns a description of the specified position, including line number, column number, and the contents of the
     * entire line with the position marked. Normally this description is put in error messages. The description should
     * normally be output starting on a new line; the description doesn't include a line break at the beginning though
     * it does include a line break in the middle. If more text will be output following the description, the caller
     * should normally add a line break before appending additional text.
     *
     * @param position position in source file
     * @return description string for position, with one line break in the middle
     */
    public String getPositionDescription(int position) {
        String prefix = isDiskFile() ? "    File " + sourceFile + "\n" : "";

        CompilationUnit compilationUnit = getCompilationUnit();

        int lineNumber = compilationUnit.getLineNumber(position);
        if (lineNumber < 0) {
            if (position == source.length())
                return prefix + "End of file";
            else return prefix + "Position " + position + " isn't valid";
        }

        int columnNumber = getSourceLogicalColumn(position);

        int startPosition = compilationUnit.getPosition(lineNumber, 0);
        int startOfNextLine = compilationUnit.getPosition(lineNumber + 1, 0);
        // If on last line, set endPosition to end of source else set it to end of line
        int endPosition = (startOfNextLine == -1) ? source.length() - 1 : startOfNextLine - 1;

        // Chop off newlines / carriage returns on the end of the line
        while (endPosition >= 0 && (source.charAt(endPosition) == '\n' || source.charAt(endPosition) == '\r'))
            --endPosition;
        if (endPosition < position)
            endPosition = position;

        String linePrefix = "    " + lineNumber + ":" + columnNumber + ":  ";

        String line = source.substring(startPosition, endPosition + 1);

        StringBuilder description = new StringBuilder(prefix);
        description.append(linePrefix).append(line).append("\n");

        // Append spaces or tabs to position the caret at the right spot on the line
        String textBeforePositionMarker = linePrefix + source.substring(startPosition, position);
        int length = textBeforePositionMarker.length();
        for (int i = 0; i < length; ++i)
            if (textBeforePositionMarker.charAt(i) == '\t')
                description.append('\t');
            else description.append(' ');
        description.append("^");

        return description.toString();
    }

    /**
     * Gets the logical column in the source corresponding to the specified position. "Logical" means with tabs expanded
     * according to the specified source tab stop.
     *
     * @param position source position in question
     * @return logical column
     */
    public int getSourceLogicalColumn(int position) {
        int physicalColumn = compilationUnit.getColumnNumber(position);
        if (physicalColumn < 0)
            throw new JUniversalException("Position is invalid: " + position);

        int logicalColumn = 0;
        for (int i = position - physicalColumn; i < position; ++i) {
            char currChar = source.charAt(i);
            if (currChar == '\t')
                logicalColumn += sourceTabStop - (logicalColumn % sourceTabStop);
            else
                ++logicalColumn;
        }

        return logicalColumn;
    }

    public int getSourceLineNumber(int position) {
        int lineNumber = compilationUnit.getLineNumber(position);
        if (lineNumber < 0) {
            if (position == source.length())
                throw new JUniversalException("Position " + position + " is at end of source file; can't get line number");
            else throw new JUniversalException("Position " + position + " isn't valid");
        } else return lineNumber;
    }
}

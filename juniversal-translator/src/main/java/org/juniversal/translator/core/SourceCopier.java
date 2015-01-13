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

public class SourceCopier {
    private SourceFile sourceFile;
    private String source;
    private TargetWriter targetWriter;

    public SourceCopier(SourceFile sourceFile, String source, TargetWriter targetWriter) {
        this.sourceFile = sourceFile;
        this.source = source;
        this.targetWriter = targetWriter;
    }

    public int match(int startPosition, String match) {
        if (!source.startsWith(match, startPosition))
            throw new JUniversalException("Expected source to contain '" + match + "' at position " + startPosition
                    + ", but it doesn't");
        return startPosition + match.length();
    }

    /**
     * Copies to output all whitespace, newlines, and comments from the source starting at startPosition and continuing
     * until the end of the whitespace/newlines/comments. Any tabs in the source are expanded; tabs are reintroduced as
     * appropriate at output time by the CPPWriter class.
     *
     * @param startPosition starting position in source
     * @param justUntilEOL  if true, then the copying stops just short of the end of line (not including \r or \n
     *                      characters); if there's a multiline comment, it's copied in its entirety and the copying
     *                      stops at the first EOL or code character after that
     * @return ending position--position of character following space and comments
     */
    public int copySpaceAndComments(int startPosition, boolean justUntilEOL, int justUntilPosition) {
        int position = startPosition;
        while (true) {
            int currChar = getSourceCharAt(position);

            if (currChar == -1 || (justUntilPosition != -1 && position >= justUntilPosition))
                return position;
            else if (currChar == ' ' || currChar == '\t')
                position = copySpacesAndTabs(position);
            else if (currChar == '\r' || currChar == '\n') {
                if (justUntilEOL)
                    return position;

                targetWriter.write((char) currChar);
                ++position;
            } else if (currChar == '/' && getSourceCharAt(position + 1) == '/')
                position = copySingleLineComment(position);
            else if (currChar == '/' && getSourceCharAt(position + 1) == '*')
                position = copyMultilineComment(position);
            else
                return position;
        }
    }

    /**
     * Skips all whitespace, newlines, and comments from the source starting at startPosition and continuing until the
     * end of the whitespace/newlines/comments.
     *
     * @param startPosition starting position in source
     * @param justUntilEOL  if true, then the skipping stops just short of the end of line (not including \r or \n
     *                      characters); if there's a multiline comment, it's skipped in its entirety and the copying
     *                      stops at the first EOL or code character after that
     * @return ending position--position of character following space and comments
     */
    public int skipSpaceAndComments(int startPosition, boolean justUntilEOL) {
        int position = startPosition;
        while (true) {
            int currChar = getSourceCharAt(position);

            if (currChar == -1)
                return position;
            else if (currChar == ' ' || currChar == '\t')
                position = skipSpacesAndTabs(position);
            else if (currChar == '\r' || currChar == '\n') {
                if (justUntilEOL)
                    return position;

                ++position;
            } else if (currChar == '/' && getSourceCharAt(position + 1) == '/') {
                position += 2;

                while (true) {
                    currChar = getSourceCharAt(position);

                    if (currChar == -1 || currChar == '\r' || currChar == '\n')
                        break;
                    else if (currChar == ' ' || currChar == '\t')
                        position = skipSpacesAndTabs(position);
                    else
                        ++position;
                }
            } else if (currChar == '/' && getSourceCharAt(position + 1) == '*') {
                position += 2;

                while (true) {
                    currChar = getSourceCharAt(position);

                    if (currChar == -1)
                        break;
                    else if (currChar == '*' && getSourceCharAt(position + 1) == '/') {
                        position += 2;
                        break;
                    } else if (currChar == ' ' || currChar == '\t')
                        position = skipSpacesAndTabs(position);
                    else
                        ++position;
                }
            } else
                return position;
        }
    }

    /**
     * Skips all whitespace, newlines, and comments from the source starting at startPosition and continuing until the
     * end of the whitespace/newlines/comments.
     *
     * @param startPosition starting position in source
     * @param justUntilEOL  if true, then the skipping stops just short of the end of line (not including \r or \n
     *                      characters); if there's a multiline comment, it's skipped in its entirety and the copying
     *                      stops at the first EOL or code character after that
     * @return ending position--position of character following space and comments
     */
    public int skipBlankLines(int startPosition) {
        int position = startPosition;

        while (isRestOfLineBlank(position) && !isEOF(position)) {
            position = skipSpacesAndTabs(position);
            position = skipNewline(position);
        }

        return position;
    }

    /**
     * Determines if the rest of the line just contains whitespace.
     *
     * @param startPosition position in question
     * @return true if rest of line is blank
     */
    public boolean isRestOfLineBlank(int startPosition) {
        int position = skipSpacesAndTabs(startPosition);

        int currChar = getSourceCharAt(position);
        return currChar == -1 || currChar == '\r' || currChar == '\n';
    }

    /**
     * Skips all whitespace, newlines, and comments from the source starting at startPosition and continuing backwards
     * until the beginning of the whitespace/newlines/comments. The returned position points to the beginning of the
     * whitespace/newlines/comments or the original position if there wasn't any whitespace/newlines/comments.
     *
     * @param startPosition starting position in source
     * @return position of first character in sequence of space and comments
     */
    public int skipSpaceAndCommentsBackward(int startPosition) {
        int position = startPosition - 1;

        while (true) {
            int currChar = getSourceCharAtForBackward(position);

            if (currChar == -1)
                return position + 1;
            else if (currChar == ' ' || currChar == '\t' || currChar == '\r')
                --position;
            else if (currChar == '\n') {
                CompilationUnit compilationUnit = sourceFile.getCompilationUnit();
                int lineStartPosition = compilationUnit.getPosition(compilationUnit.getLineNumber(position), 0);
                int lineCommentStartPosition = getLineCommentStartPosition(lineStartPosition);

                if (lineCommentStartPosition != -1)
                    position = lineCommentStartPosition - 1;
                else
                    --position;
            } else if (currChar == '/' && getSourceCharAtForBackward(position - 1) == '*') {
                position -= 2;

                while (true) {
                    currChar = getSourceCharAtForBackward(position);

                    if (currChar == -1)
                        break;
                    else if (currChar == '*' && getSourceCharAtForBackward(position - 1) == '/') {
                        position -= 2;
                        break;
                    } else --position;
                }
            } else return position + 1;
        }
    }

    /**
     * Given the start position for a line, return the start position of any line ("//") comment on the line, or -1 if
     * there is no line comment. If a position is returned, it's the position of the first / character for the line
     * comment.
     *
     * @param lineStartPosition
     * @return
     */
    private int getLineCommentStartPosition(int lineStartPosition) {
        int position = lineStartPosition;
        while (true) {
            int currChar = getSourceCharAt(position);

            if (currChar == '\r' || currChar == '\n' || currChar == -1)
                return -1;
            else if (currChar == '/' && getSourceCharAt(position + 1) == '/')
                return position;
            else if (currChar == '/' && getSourceCharAt(position + 1) == '*') {
                position += 2;

                while (true) {
                    currChar = getSourceCharAt(position);

                    if (currChar == '\r' || currChar == '\n' || currChar == -1)
                        return -1;
                    else if (currChar == '*' && getSourceCharAt(position + 1) == '/') {
                        position += 2;
                        break;
                    } else
                        ++position;
                }
            } else
                ++position;
        }
    }

    private int copySingleLineComment(int position) {
        int commentStartSourceColumn = sourceFile.getSourceLogicalColumn(position);
        int commentStartOutputColumn = targetWriter.getCurrColumn();

        while (true) {
            targetWriter.write("//");
            position += 2;

            while (true) {
                int currChar = getSourceCharAt(position);

                if (currChar == -1 || currChar == '\r' || currChar == '\n')
                    break;
                else if (currChar == ' ' || currChar == '\t')
                    position = copySpacesAndTabs(position);
                else {
                    targetWriter.write((char) currChar);
                    ++position;
                }
            }

            // See if there is a / / comment on the next line indented exactly the same amount as
            // the first line. If so assume that it's a continuation of the previous comment & copy
            // it as well, taking care that it's indented in the output so all lines of the comment
            // line up together
            boolean commentContinues = false;
            int peekPosition = position;
            int peekChar = getSourceCharAt(peekPosition);
            if (peekChar == '\r') {
                ++peekPosition;
                peekChar = getSourceCharAt(peekPosition);
            }
            if (peekChar == '\n') {
                ++peekPosition;

                peekPosition = skipSpacesAndTabs(peekPosition);
                peekChar = getSourceCharAt(peekPosition);

                if (peekChar == '/' && getSourceCharAt(peekPosition + 1) == '/' &&
                        sourceFile.getSourceLogicalColumn(peekPosition) == commentStartSourceColumn) {
                    position = peekPosition;
                    commentContinues = true;
                }
            }

            if (!commentContinues)
                return position;

            targetWriter.write("\n");

            int currSourceColumn = sourceFile.getSourceLogicalColumn(position);

            // Ensure that subsequent lines of the comment are indented by the same amount, relative
            // to the first of the comment, as they are in the source
            targetWriter.writeSpacesUntilColumn(commentStartOutputColumn + (currSourceColumn - commentStartSourceColumn));
        }
    }

    private int copyMultilineComment(int position) {
        int currChar;
        int commentStartSourceColumn = sourceFile.getSourceLogicalColumn(position);
        int commentStartOutputColumn = targetWriter.getCurrColumn();

        targetWriter.write("/*");
        position += 2;

        while (true) {
            currChar = getSourceCharAt(position);

            if (currChar == -1)
                break;
            else if (currChar == '*' && getSourceCharAt(position + 1) == '/') {
                targetWriter.write("*/");
                position += 2;
                break;
            } else if (currChar == ' ' || currChar == '\t')
                position = copySpacesAndTabs(position);
            else if (currChar == '\n') {
                targetWriter.write((char) currChar);
                ++position;

                position = skipSpacesAndTabs(position);
                int currSourceColumn = sourceFile.getSourceLogicalColumn(position);

                // Ensure that subsequent lines of the multiline comment are indented by the
                // same amount, relative to the first of the comment, as they are in the
                // source
                targetWriter.writeSpacesUntilColumn(commentStartOutputColumn
                        + (currSourceColumn - commentStartSourceColumn));
            } else {
                targetWriter.write((char) currChar);
                ++position;
            }
        }
        return position;
    }

    /**
     * Copies to output contiguous spaces and tabs from the source starting at startPosition and continuing until the
     * end of the spaces/tabs. Any tabs in the source are expanded; tabs are reintroduced as appropriate at output time
     * by the CPPWriter class.
     *
     * @param startPosition starting position in source
     */
    public int copySpacesAndTabs(int startPosition) {
        int sourceTabStop = sourceFile.getSourceTabStop();

        int logicalColumn = -1; // -1 means don't know logical (untabified) column--yet
        int logicalColumnOffset = 0; // Logical (untabified) column offset

        int position;
        for (position = startPosition; true; ++position) {
            int currChar = getSourceCharAt(position);

            if (currChar == ' ') {
                ++logicalColumnOffset;
                if (logicalColumn != -1)
                    ++logicalColumn;
            } else if (currChar == '\t') {
                // If we haven't yet computed the logical column, compute it now
                if (logicalColumn == -1)
                    logicalColumn = sourceFile.getSourceLogicalColumn(position);

                int spacesForTab = sourceTabStop - (logicalColumn % sourceTabStop);

                logicalColumn += spacesForTab;
                logicalColumnOffset += spacesForTab;
            } else if (currChar == '\r')
                ;
                // If the space is at the end of the line (trailing space), don't copy it
            else if (currChar == '\n')
                return position;
            else
                break;
        }

        for (int i = 0; i < logicalColumnOffset; ++i)
            targetWriter.write(' ');
        return position;
    }

    /**
     * Skips contiguous spaces and tabs from the source starting at startPosition and continuing until the end of the
     * spaces/tabs.
     *
     * @param startPosition starting position in source
     */
    public int skipSpacesAndTabs(int startPosition) {
        int position;
        for (position = startPosition; true; ++position) {
            int currChar = getSourceCharAt(position);
            if (!(currChar == ' ' || currChar == '\t' || currChar == '\r'))
                break;
        }

        return position;
    }

    /**
     * Skips all spaces and tabs from the source starting at startPosition and continuing backwards until the beginning
     * of the spaces/tabs. The returned position points to the beginning of the whitespace or the original position if
     * there wasn't any whitespace before the current position.
     *
     * @param startPosition starting position in source
     * @return position of first character in sequence of spaces/tabs
     */
    public int skipSpacesAndTabsBackward(int startPosition) {
        int position = startPosition - 1;

        while (true) {
            int currChar = getSourceCharAtForBackward(position);

            if (currChar == -1)
                return position + 1;
            else if (currChar == ' ' || currChar == '\t')
                --position;
            else return position + 1;
        }
    }

    /**
     * If position is at the end of the line, the first position on the following line is returned. Else, the current
     * position is returned.
     *
     * @param startPosition starting position in source
     * @return position just past the end of line, if there is one at that position
     */
    public int skipNewline(int position) {
        // Skip carriage returns & only pay attention to newline characters. That way both \n
        // and \r\n line endings are supported.

        if (getSourceCharAt(position) == '\r')
            ++position;
        if (getSourceCharAt(position) == '\n')
            ++position;

        return position;
    }

    /**
     * Skips contiguous spaces, tabs, and newlines from the source starting at startPosition and continuing until the
     * end of the spaces/tabs.
     *
     * @param startPosition starting position in source
     */
    private int getFirstNonspacePositionOnNextLine(int startPosition) {
        int position;
        for (position = startPosition; true; ++position) {
            int currChar = getSourceCharAt(position);
            if (!(currChar == ' ' || currChar == '\t' || currChar == '\r' || currChar == '\n'))
                break;
        }

        return position;
    }

    /**
     * Get the character from the source at the specified position or -1 if past the end of the source.
     *
     * @param position source position in question
     * @return character at that position or -1 if past end
     */
    public int getSourceCharAt(int position) {
        if (position >= source.length())
            return -1;
        return source.charAt(position);
    }

    /**
     * Returns true if the specifed position is at or past the end of the source.
     *
     * @param position source position in question
     * @return true if position is at EOF
     */
    public boolean isEOF(int position) {
        return position >= source.length();
    }

    /**
     * Get the character from the source at the specified position or -1 if before the beginning of the source.
     *
     * @param position source position in question
     * @return character at that position or -1 if past beginning
     */
    public int getSourceCharAtForBackward(int position) {
        if (position < 0)
            return -1;
        return source.charAt(position);
    }
}

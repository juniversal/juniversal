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

import java.io.*;


public class TargetWriter {
    private int currColumn;                           // Current column on line (0 based)
    private boolean accumulatingSpacesAtBeginningOfLine;
    private int spacesAtBeginningOfLine;
    private Writer writer;
    // Additional amount to indent or (if negative) outdent
    private int additionalIndentation = 0;
    private int destTabStop = -1;


    public TargetWriter(Writer writer, int destTabStop) {
        this.writer = writer;
        this.destTabStop = destTabStop;

        currColumn = 0;
        accumulatingSpacesAtBeginningOfLine = true;
        spacesAtBeginningOfLine = 0;
    }

    /**
     * Return the current column (0 based), where the next output character will go.
     *
     * @return current column
     */
    public int getCurrColumn() {
        return currColumn;
    }

    public int getDestTabStop() {
        return destTabStop;
    }

    public Writer getWriter() {
        return writer;
    }

    public void write(String string) {
        try {
            int length = string.length();
            for (int i = 0; i < length; ++i)
                writeCharInternal(string.charAt(i));
        } catch (IOException e) {
            throw new JUniversalException(e);
        }
    }

    public void write(BufferTargetWriter bufferTargetWriter) {
        try {
            writer.write(bufferTargetWriter.getBufferContents());
        } catch (IOException e) {
            throw new JUniversalException(e);
        }
    }

    public void write(char character) {
        try {
            writeCharInternal(character);
        } catch (IOException e) {
            throw new JUniversalException(e);
        }
    }

    /**
     * Write the specified number of spaces to the output.
     *
     * @param count number of spaces to write
     */
    public void writeSpaces(int count) {
        try {
            for (int i = 0; i < count; ++i)
                writeCharInternal(' ');
        } catch (IOException e) {
            throw new JUniversalException(e);
        }
    }

    /**
     * Space over so that the next character written will be at the specified column. If the current column is already
     * past the specified column, nothing is output.
     *
     * @param column desired column
     */
    public void writeSpacesUntilColumn(int column) {
        while (getCurrColumn() < column)
            write(" ");
    }

    private void writeCharInternal(char character) throws IOException {
        if (character == '\r')
            ;
        else if (character == '\n') {
            // If only whitespace on a line, don't write out indentation

            writer.write("\r\n");

            accumulatingSpacesAtBeginningOfLine = true;
            spacesAtBeginningOfLine = additionalIndentation;
            currColumn = additionalIndentation;
        } else if (character == ' ') {
            if (accumulatingSpacesAtBeginningOfLine)
                ++spacesAtBeginningOfLine;
            else writer.write(' ');

            ++currColumn;
        } else if (character == '\t')
            throw new JUniversalException("Can't directly write tabs to a CPPWriter");
        else {
            // Write any accumulated indentation plus any defined additional indentation
            if (accumulatingSpacesAtBeginningOfLine) {
                writeAccumulatedSpacesAtBeginningOfLine();
                accumulatingSpacesAtBeginningOfLine = false;
            }

            writer.write(character);
            ++currColumn;
        }
    }

    private void writeAccumulatedSpacesAtBeginningOfLine() throws IOException {
        int spaces = spacesAtBeginningOfLine;
        if (spaces <= 0)
            return;

        // If tabs are enabled for the output, tabify what we can at the beginning of a line
        if (destTabStop != -1) {
            int tabs = spaces / destTabStop;
            for (int i = 0; i < tabs; ++i)
                writer.write('\t');

            // What can't be tabified should be a space
            spaces = spaces % destTabStop;
        }

        for (int i = 0; i < spaces; ++i)
            writer.write(' ');
    }

    public int setAdditionalIndentation(int value) {
        int oldValue = this.additionalIndentation;
        this.additionalIndentation = value;

        // If we're at the beginning of the line, then make the additional indentation take effect immediately
        if (currColumn == oldValue) {
            int delta = value - oldValue;
            currColumn += delta;
            spacesAtBeginningOfLine += delta;
        }

        return oldValue;
    }

    public int incrementAdditionalIndentation(int increment) {
        return setAdditionalIndentation(additionalIndentation + increment);
    }
}

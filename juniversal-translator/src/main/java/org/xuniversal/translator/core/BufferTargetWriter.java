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

import org.xuniversal.translator.core.TargetWriter;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by Bret on 11/25/2014.
 */
public class BufferTargetWriter extends TargetWriter {
    public BufferTargetWriter(TargetWriter originalTargetWriter) {
        super(new MyStringWriter(), originalTargetWriter.getDestTabStop());
    }

    public String getBufferContents() {
        return getWriter().toString();
    }

    /**
     * The standard Java StringWriter class uses a StringBuffer, which is synchronized, so we have our own version that
     * uses a StringBuilder internally, which is unsynchrnoized and should perform better.   I'm not sure if this
     * makes any difference in practice, but nearly all translated output goes through this class & it couldn't hurt.
     */
    private static class MyStringWriter extends Writer {
        private StringBuilder stringBuilder = new StringBuilder();

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            stringBuilder.append(cbuf, off, len);
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            stringBuilder.append(str, off, off + len);
        }

        @Override
        public Writer append(CharSequence csq) throws IOException {
            stringBuilder.append(csq);
            return this;
        }

        @Override
        public Writer append(CharSequence csq, int start, int end) throws IOException {
            stringBuilder.append(csq, start, end);
            return this;
        }

        @Override
        public Writer append(char c) throws IOException {
            stringBuilder.append(c);
            return this;
        }

        @Override
        public void flush() throws IOException {

        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public String toString() {
            return stringBuilder.toString();
        }
    }
}

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

import org.jetbrains.annotations.Nullable;

/**
 * A Var is a wrapper around some object, which can change.   It's main use currently is to allow a local variable
 * outside of a lambda to be modified by the lambda.   That is, the Var itself can be final (as required for locals
 * referenced from a lambda / anonymous inner class method) but the contents inside it can change as required to
 * captured updated state.   It can also be used as a "var" parameter to methods, to return extra info outside the
 * return value.
 * <p>
 * Created by Bret on 12/21/2014.
 */
public class Var<T> {
    private @Nullable T value;

    public Var(T value) {
        this.value = value;
    }

    public Var() {
        this.value = null;
    }

    public void set(T value) {
        this.value = value;
    }

    public boolean isSet() {
        return value != null;
    }

    public void clear() {
        value = null;
    }

    public T value() {
        return value;
    }
}

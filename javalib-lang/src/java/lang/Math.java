/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.lang;

/**
 * Class Math provides basic math constants and operations such as trigonometric
 * functions, hyperbolic functions, exponential, logarithms, etc.
 * 
 * JUniversal REMOVED:
 * 
 * All floating point methods
 * 
 * random method
 * 
 * shiftIntBits and shiftLongBits
 * 
 */
public final class Math {
	
    /**
     * The double value closest to e, the base of the natural logarithm.
     */
    public static final double E = 2.718281828459045;

    /**
     * The double value closest to pi, the ratio of a circle's circumference to
     * its diameter.
     */
    public static final double PI = 3.141592653589793;

    /**
     * Prevents this class from being instantiated.
     */
    private Math() {
    }

    /**
     * Returns the absolute value of the argument.
     * <p>
     * If the argument is {@code Integer.MIN_VALUE}, {@code Integer.MIN_VALUE}
     * is returned.
     * 
     * @param i
     *            the value whose absolute value has to be computed.
     * @return the argument if it is positive, otherwise the negation of the
     *         argument.
     */
    public static int abs(int i) {
        return i >= 0 ? i : -i;
    }

    /**
     * Returns the absolute value of the argument. If the argument is {@code
     * Long.MIN_VALUE}, {@code Long.MIN_VALUE} is returned.
     * 
     * @param l
     *            the value whose absolute value has to be computed.
     * @return the argument if it is positive, otherwise the negation of the
     *         argument.
     */
    public static long abs(long l) {
        return l >= 0 ? l : -l;
    }

    /**
     * Returns the most positive (closest to positive infinity) of the two
     * arguments.
     * 
     * @param i1
     *            the first argument.
     * @param i2
     *            the second argument.
     * @return the larger of {@code i1} and {@code i2}.
     */
    public static int max(int i1, int i2) {
        return i1 > i2 ? i1 : i2;
    }

    /**
     * Returns the most positive (closest to positive infinity) of the two
     * arguments.
     * 
     * @param l1
     *            the first argument.
     * @param l2
     *            the second argument.
     * @return the larger of {@code l1} and {@code l2}.
     */
    public static long max(long l1, long l2) {
        return l1 > l2 ? l1 : l2;
    }

    /**
     * Returns the most negative (closest to negative infinity) of the two
     * arguments.
     * 
     * @param i1
     *            the first argument.
     * @param i2
     *            the second argument.
     * @return the smaller of {@code i1} and {@code i2}.
     */
    public static int min(int i1, int i2) {
        return i1 < i2 ? i1 : i2;
    }

    /**
     * Returns the most negative (closest to negative infinity) of the two
     * arguments.
     * 
     * @param l1
     *            the first argument.
     * @param l2
     *            the second argument.
     * @return the smaller of {@code l1} and {@code l2}.
     */
    public static long min(long l1, long l2) {
        return l1 < l2 ? l1 : l2;
    }
}

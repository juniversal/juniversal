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

package main;

public class TestBoxing {
    public void testPrimitiveTypes() {
        byte byteValue = 1;
        short shortValue = 1;
        int intValue = 1;
        long longValue = 1;
        float floatValue = (float) 1.0;
        double doubleValue = 1.0;
        boolean booleanValue = true;
        char charValue = 'a';

        actOnObject(byteValue);
        actOnObject(shortValue);
        actOnObject(intValue);
        actOnObject(longValue);
        actOnObject(floatValue);
        actOnObject(doubleValue);
        actOnObject(booleanValue);
        actOnObject(charValue);
    }

    public void testNullBoxing() {
        boolean flag = true;

        Object value = flag ? null : 1;
    }

    public void testExpressionBoxing() {
        int intValue1 = 1;
        int intValue2 = 2;

        long longValue1 = 1L;

        actOnObject(intValue1 + intValue2);
        actOnObject(intValue1 + longValue1);
        actOnObject((int) (intValue1 + longValue1));
    }

    public void testNestedBoxing() {
        objectToInt(objectToInt(1));

        TestClass inst = new TestClass(3);
        actOnObject(inst.getPrimitiveField());
        actOnObject(inst.primitiveField);
    }

    private void actOnObject(Object o) {
        o.toString();
        o.hashCode();
    }

    private void actOnInt(int value) {
    }

    private int objectToInt(Object o) {
        return o.hashCode();
    }

    private Object intToObject(int value) {
        return value;
    }

    private class TestClass {
        public int primitiveField;

        public TestClass(int primitiveField) {
            this.primitiveField = primitiveField;
        }

        public int getPrimitiveField() {
            return primitiveField;
        }

    }
}

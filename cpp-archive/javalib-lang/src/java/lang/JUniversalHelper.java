package java.lang;

public class JUniversalHelper {
    /**
     * Copies the contents of <code>A1</code> starting at offset
     * <code>offset1</code> into <code>A2</code> starting at offset
     * <code>offset2</code> for <code>length</code> elements.
     * 
     * @param A1 the array to copy out of
     * @param offset1 the starting index in array1
     * @param A2 the array to copy into
     * @param offset2 the starting index in array2
     * @param length the number of elements in the array to copy
     */
    static void arraycopy(char[] A1, int offset1, char[] A2, int offset2, int length) {
        if (offset1 >= 0 && offset2 >= 0 && length >= 0 && length <= A1.length - offset1
                && length <= A2.length - offset2) {
            // Check if this is a forward or backwards arraycopy
            if (A1 != A2 || offset1 > offset2 || offset1 + length <= offset2) {
                for (int i = 0; i < length; ++i) {
                    A2[offset2 + i] = A1[offset1 + i];
                }
            } else {
                for (int i = length - 1; i >= 0; --i) {
                    A2[offset2 + i] = A1[offset1 + i];
                }
            }
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Copies the contents of <code>A1</code> into <code>A2</code> for <code>length</code> elements.
     * 
     * @param A1 the array to copy out of
     * @param A2 the array to copy into
     * @param length the number of elements in the array to copy
     */
	static void arraycopy(char[] A1, char[] A2, int length) {
		if (length >= 0 && length <= A1.length && length <= A2.length) {
			for (int i = 0; i < length; ++i) {
				A2[i] = A1[i];
			}
		} else {
			throw new ArrayIndexOutOfBoundsException();
		}
	}
}

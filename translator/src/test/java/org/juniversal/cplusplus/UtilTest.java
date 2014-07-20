package org.juniversal.cplusplus;

import static org.junit.Assert.*;
import org.juniversal.core.Util;

import org.junit.*;

public class UtilTest {

	@Test public void testCountChars() {
		assertTrue(Util.countChars("abcxdefxghix", 'x') == 3);
		assertTrue(Util.countChars("", 'x') == 0);
		assertTrue(Util.countChars("abc\ndef", '\n') == 1);
	}
}

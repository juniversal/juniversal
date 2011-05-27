package cj;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AdHocTest {
	@Test public void test() {
		char a = 'a';
		short b = (short) -10000;
		
		if (b < 0)
			System.out.println("negative");
		else System.out.println("positive");
		
		System.out.println((int) b);
	}
}

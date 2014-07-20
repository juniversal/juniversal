package juniversal;

import org.junit.Test;

public @SuppressWarnings("unused") class AdHocTest {
	@Test public void test() {
		char a = 'a';
		short b = (short) -10000;
		
		if (b < 0)
			System.out.println("negative");
		else System.out.println("positive");
		
		System.out.println((int) b);
	}
}

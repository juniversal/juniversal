import java.util.HashMap;
import java.util.Iterator;

public class Test<T> extends HashMap implements Iterable<T> {

	/**
	 * Here is the Inner class comment.
	 */
	public static class Inner {
		public String foowho() {
			return /* "a" + "b" + "c" */ "";
		}
		
		public void foo() {
			;
		}
	}
		

	/**
	 * HEre's the first method.
	 * @param abc
	 * @return
	 */
	public int helloWorld(int abc) {
		return abc + 1;
	}

	public static void foo() { }

	public T giveMeBack(T me) { return me; }

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	} 
}

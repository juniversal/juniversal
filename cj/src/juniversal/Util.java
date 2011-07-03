package juniversal;

public class Util {
	
	/**
	 * Count number of times specified character appears in string.
	 * 
	 * @param string string in question
	 * @param character character to count
	 * @return count of occurrences of character in string
	 */
	public static int countChars(String string, int character)	{
		int count = 0;
		int currIndex = 0;
		for (;;) {
			currIndex = string.indexOf(character, currIndex);
			if (currIndex == -1)
				break;
			++currIndex; // Start next search from next character
			++count;
		}
		return count;
	}
}

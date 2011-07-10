public class Test {
	public String stripFormattedPhoneNumber(String phoneNumber, String p2) {
		String s;
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < phoneNumber.length(); i++) {
			char ch = phoneNumber.charAt(i);
			if (Character.isDigit(ch))
				result.append(ch);
		}

		return result.toString();
	}
}

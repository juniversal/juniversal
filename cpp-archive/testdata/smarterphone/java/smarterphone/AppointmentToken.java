package smarterphone;

public final class AppointmentToken {
	private String m_data;
	private int m_currPosition;
	private int m_type;            // Token type
	private String m_text;         // Current token text

	AppointmentToken(String string) {
		this.m_data = string;
		this.m_currPosition = 0;

		advance();
	}

	public void advance() {
		char currChar = m_data.charAt(m_currPosition);
		if (isDigit(currChar))
			matchDigitSequence();
	}

	public int type() {
		return m_type;
	}

	public String text() {
		return m_text;
	}

	public boolean hasLengthInRange(int min, int max) {
		int length = m_text.length();
		return length >= min && length <= max;
	}

	public void matchDigitSequence() {
		StringBuilder digitSequence = new StringBuilder();

		while (true) {
			char currChar = currChar();
			if (! isDigit(currChar))
				break;

			digitSequence.append(currChar);
			advanceChar();
		}

		m_type = DIGIT_SEQUENCE;
		m_text = digitSequence.toString();
	}

	private static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	public char currChar() {
		if (m_currPosition >= m_data.length())
			return  '\u0000';

		return m_data.charAt(m_currPosition);
	}

	public void advanceChar() {
		++m_currPosition;
	}

	// Constants
	public final static int DIGIT_SEQUENCE = 1;
	public final static int PHONE_NUMBER = 2;
	public final static int ADDRESS = 3;
	public final static int EOF = 4;
}

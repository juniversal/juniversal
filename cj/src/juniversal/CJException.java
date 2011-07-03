package juniversal;

@SuppressWarnings("serial")
public class CJException extends RuntimeException {

	public CJException(Throwable cause) {
		super(cause);
	}

	public CJException(String message) {
		super(message);
	}

	public CJException(String message, Throwable cause) {
		super(message, cause);
	}
}

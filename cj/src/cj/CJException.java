package cj;

@SuppressWarnings("serial")
public class CJException extends RuntimeException {

	public CJException(Throwable t) {
		super(t);
	}

	public CJException(String message) {
		super(message);
	}
}

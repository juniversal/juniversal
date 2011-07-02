package cj.writecpp;

import cj.CJException;

@SuppressWarnings("serial")
public class ContextPositionMismatchException extends CJException {
	public ContextPositionMismatchException(String message) {
		super(message);
	}
}

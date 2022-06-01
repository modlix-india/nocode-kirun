package com.fincity.nocode.kirun.engine.exception;

public class KIRuntimeException extends RuntimeException {
	
	private static final long serialVersionUID = -7249682248397320278L;

	public KIRuntimeException(String message) {
		super(message);
	}

	public KIRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}

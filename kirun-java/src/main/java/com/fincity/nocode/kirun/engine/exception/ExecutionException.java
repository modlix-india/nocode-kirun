package com.fincity.nocode.kirun.engine.exception;

public class ExecutionException extends RuntimeException {

	private static final long serialVersionUID = -1225932858015182730L;

	public ExecutionException(String message) {
		super(message);
	}
	
	public ExecutionException(String message, Throwable cause) {
		super(message, cause);
	}
}

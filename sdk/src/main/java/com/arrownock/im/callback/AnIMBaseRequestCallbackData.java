package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

public abstract class AnIMBaseRequestCallbackData {
	private boolean error = false;
	private ArrownockException exception = null;
	
	public AnIMBaseRequestCallbackData(boolean error,
			ArrownockException exception) {
		super();
		this.error = error;
		this.exception = exception;
	}
	public boolean isError() {
		return error;
	}
	public ArrownockException getException() {
		return exception;
	}
}

package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.AnIMStatus;

public class AnIMStatusUpdateCallbackData {
	private AnIMStatus status = AnIMStatus.OFFLINE;
	private ArrownockException exception = null;
	
	public AnIMStatusUpdateCallbackData(AnIMStatus status, ArrownockException exception) {
		super();
		this.status = status;
		this.exception = exception;
	}
	public AnIMStatus getStatus() {
		return status;
	}
	public ArrownockException getException() {
		return exception;
	}
}

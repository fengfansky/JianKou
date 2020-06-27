package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

public class AnIMMessageSentCallbackData {
	private boolean error = true;
	private String msgId = null;
	private long timestamp = -1;
	private ArrownockException exception = null;
	
	public AnIMMessageSentCallbackData(boolean error, ArrownockException exception, String msgId, long timestamp) {
		super();
		this.error = error;
		this.msgId = msgId;
		this.timestamp = timestamp;
		this.exception = exception;
	}
	public boolean isError() {
		return error;
	}
	public String getMsgId() {
		return msgId;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public ArrownockException getException() {
		return exception;
	}
}

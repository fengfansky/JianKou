package com.arrownock.im.callback;

public abstract class AnIMBaseMessageCallbackData {
	private String msgId = null;
	private String from = null;
	
	public AnIMBaseMessageCallbackData(String msgId, String from) {
		super();
		this.msgId = msgId;
		this.from = from;
	}
	public String getMsgId() {
		return msgId;
	}
	public String getFrom() {
		return from;
	}
}

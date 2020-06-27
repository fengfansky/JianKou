package com.arrownock.im.callback;

import java.util.Map;
import java.util.Set;


public class AnIMMessageCallbackData extends AnIMBaseMessageCallbackData {
	private Set<String> parties = null;
	private String message = null;
	private Map<String, String> customData = null;
	private long timestamp = -1;
	
	public AnIMMessageCallbackData(String msgId, String from, Set<String> parties, String message, Map<String, String> customData, long timestamp) {
		super(msgId, from);
		this.parties = parties;
		this.message = message;
		this.customData = customData;
		this.timestamp = timestamp;
	}
	
	public Set<String> getParties() {
		return parties;
	}
	public String getMessage() {
		return message;
	}
	public Map<String, String> getCustomData() {
		return customData;
	}
	public long getTimestamp() {
		return timestamp;
	}
}

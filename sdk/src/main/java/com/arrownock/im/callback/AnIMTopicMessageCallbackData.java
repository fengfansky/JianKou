package com.arrownock.im.callback;

import java.util.Map;


public class AnIMTopicMessageCallbackData extends AnIMBaseMessageCallbackData {
	private String topic = null;
	private String message = null;
	private Map<String, String> customData = null;
	private long timestamp = -1;
	
	public AnIMTopicMessageCallbackData(String msgId, String from,
			String topic, String message, Map<String, String> customData,
			long timestamp) {
		super(msgId, from);
		this.topic = topic;
		this.message = message;
		this.customData = customData;
		this.timestamp = timestamp;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public String getTopic() {
		return topic;
	}
	public String getMessage() {
		return message;
	}
	public Map<String, String> getCustomData() {
		return customData;
	}
	
}

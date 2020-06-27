package com.arrownock.im.callback;

import java.util.Map;


public class AnIMTopicBinaryCallbackData extends AnIMBaseMessageCallbackData {
	private String topic = null;
	private byte[] content = null;
	private String fileType = null;
	private Map<String, String> customData = null;
	private long timestamp = -1;
	
	public AnIMTopicBinaryCallbackData(String msgId, String from, String topic,
			byte[] content, String fileType, Map<String, String> customData,
			long timestamp) {
		super(msgId, from);
		this.topic = topic;
		this.content = content;
		this.fileType = fileType;
		this.customData = customData;
		this.timestamp = timestamp;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public String getTopic() {
		return topic;
	}
	public byte[] getContent() {
		return content;
	}
	public String getFileType() {
		return fileType;
	}
	public Map<String, String> getCustomData() {
		return customData;
	}
}

package com.arrownock.im;

import java.util.Map;

public class AnIMMessage {
	private AnIMMessageType type;
	private String msgId;
	private String topicId;
	private String message;
	private byte[] content;
	private String fileType;
	private String from;
	private String to;
	private long timestamp;
	private Map<String, String> customData;
	
	public AnIMMessage(AnIMMessageType type, String msgId, String topicId, String message,
			byte[] content, String fileType, String from, String to, long timestamp,
			Map<String, String> customData) {
		super();
		this.type = type;
		this.msgId = msgId;
		this.topicId = topicId;
		this.message = message;
		this.content = content;
		this.fileType = fileType;
		this.from = from;
		this.timestamp = timestamp;
		this.customData = customData;
		this.to = to;
	}
	
	public AnIMMessageType getType() {
		return type;
	}
	public String getMsgId() {
		return msgId;
	}
	public String getTopicId() {
		return topicId;
	}
	public String getMessage() {
		return message;
	}
	public byte[] getContent() {
		return content;
	}
	public String getFileType() {
		return fileType;
	}
	public String getFrom() {
		return from;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public Map<String, String> getCustomData() {
		return customData;
	}
	public String getTo() {
		return to;
	}
}

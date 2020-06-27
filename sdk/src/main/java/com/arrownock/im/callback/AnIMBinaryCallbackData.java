package com.arrownock.im.callback;

import java.util.Map;
import java.util.Set;


public class AnIMBinaryCallbackData extends AnIMBaseMessageCallbackData {
	private Set<String> parties = null;
	private byte[] content = null;
	private String fileType = null;
	private Map<String, String> customData = null;
	private long timestamp = -1;
	
	public AnIMBinaryCallbackData(String msgId, String from,
			Set<String> parties, byte[] content, String fileType,
			Map<String, String> customData, long timestamp) {
		super(msgId, from);
		this.parties = parties;
		this.content = content;
		this.fileType = fileType;
		this.customData = customData;
		this.timestamp = timestamp;
	}
	public Set<String> getParties() {
		return parties;
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
	public long getTimestamp() {
		return timestamp;
	}
}

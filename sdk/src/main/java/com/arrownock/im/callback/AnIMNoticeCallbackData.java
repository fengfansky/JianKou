package com.arrownock.im.callback;

import java.util.Map;

public class AnIMNoticeCallbackData extends AnIMBaseMessageCallbackData {
	private String topic = null;
	private String notice = null;
	private Map<String, String> customData = null;
	private long timestamp = -1;
	
	public AnIMNoticeCallbackData(String msgId, String from, String notice,
			Map<String, String> customData, long timestamp) {
		this(msgId, from, notice, customData, timestamp, null);
	}
	
	public AnIMNoticeCallbackData(String msgId, String from, String notice,
			Map<String, String> customData, long timestamp, String topic) {
		super(msgId, from);
		this.notice = notice;
		this.customData = customData;
		this.timestamp = timestamp;
		this.topic = topic;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public String getNotice() {
		return notice;
	}
	public Map<String, String> getCustomData() {
		return customData;
	}
	public String getTopic() {
		return topic;
	}
}

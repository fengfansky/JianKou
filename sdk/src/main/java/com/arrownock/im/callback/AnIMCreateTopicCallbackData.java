package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

@Deprecated
public class AnIMCreateTopicCallbackData extends AnIMBaseRequestCallbackData {
    @Deprecated
	private String topic = null;
	@Deprecated
	public AnIMCreateTopicCallbackData(boolean error, ArrownockException exception, String topic) {
		super(error, exception);
		this.topic = topic;
	}
	@Deprecated
	public String getTopic() {
		return topic;
	}
}

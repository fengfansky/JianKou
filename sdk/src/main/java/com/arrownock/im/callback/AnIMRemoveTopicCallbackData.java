package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

@Deprecated
public class AnIMRemoveTopicCallbackData extends AnIMBaseRequestCallbackData {
    @Deprecated
	private String topic = null;
	@Deprecated
	public AnIMRemoveTopicCallbackData(boolean error, ArrownockException exception, String topic) {
		super(error, exception);
		this.topic = topic;
	}
	@Deprecated
	public String getTopic() {
		return topic;
	}
}

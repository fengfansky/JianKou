package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

@Deprecated
public class AnIMUpdateTopicCallbackData extends AnIMBaseRequestCallbackData {
    @Deprecated
	private String topic = null;
	@Deprecated
	public AnIMUpdateTopicCallbackData(boolean error, ArrownockException exception, String topic) {
		super(error, exception);
		this.topic = topic;
	}
	@Deprecated
	public String getTopic() {
		return topic;
	}
}

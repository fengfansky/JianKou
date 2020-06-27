package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

import org.json.JSONObject;

import java.util.List;

public class AnIMGetTopicListCallbackData extends AnIMBaseRequestCallbackData {
	List<JSONObject> topicList = null;
	String clientId = null;

	public AnIMGetTopicListCallbackData(boolean error, ArrownockException exception, List<JSONObject> topicList, String clientId) {
		super(error, exception);
		this.topicList = topicList;
		this.clientId = clientId;
	}

	public String getClientId() {
		return clientId;
	}

	public List<JSONObject> getTopicList() {
		return topicList;
	}
	
}

package com.arrownock.im.callback;

import java.util.List;

import org.json.JSONObject;

import com.arrownock.exception.ArrownockException;

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

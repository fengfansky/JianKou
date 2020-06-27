package com.arrownock.im.callback;

import java.util.Date;
import java.util.Set;

import com.arrownock.exception.ArrownockException;

public class AnIMGetSessionInfoCallbackData extends AnIMBaseRequestCallbackData {
	private String sessionId = null;
	private Set<String> parties = null;
	private Date createdAt = null;
	
	public AnIMGetSessionInfoCallbackData(boolean error,
			ArrownockException exception, String sessionId,
			Set<String> parties, Date createdAt) {
		super(error, exception);
		this.sessionId = sessionId;
		this.parties = parties;
		this.createdAt = createdAt;
	}
	public String getSessionId() {
		return sessionId;
	}
	public Set<String> getParties() {
		return parties;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	
}

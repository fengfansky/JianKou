package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

public class AnIMGetClientIdCallbackData extends AnIMBaseRequestCallbackData {
	private String clientId = null;

	public AnIMGetClientIdCallbackData(boolean error, ArrownockException exception, String clientId) {
		super(error, exception);
		this.clientId = clientId;
	}

	public String getClientId() {
		return clientId;
	}
}

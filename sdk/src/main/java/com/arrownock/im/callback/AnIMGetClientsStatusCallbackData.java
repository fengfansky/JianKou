package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

import java.util.Map;

public class AnIMGetClientsStatusCallbackData extends AnIMBaseRequestCallbackData {
	Map<String, Boolean> clientsStatus = null;

	public AnIMGetClientsStatusCallbackData(boolean error, ArrownockException exception, Map<String, Boolean> clientsStatus) {
		super(error, exception);
		this.clientsStatus = clientsStatus;
	}

	public Map<String, Boolean> getClientsStatus() {
		return clientsStatus;
	}
	
}

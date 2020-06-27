package com.arrownock.appo.desk;

import com.arrownock.exception.ArrownockException;

public interface IAnDeskCreateSessionCallback {
	void onSuccess(String sessionId, String accountId, String accountName);
	void onFailure(ArrownockException e);
}

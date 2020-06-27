package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

public interface IAnIMGetClientIdCallback {
	void onSuccess(String clientId);
	void onError(ArrownockException e);
}

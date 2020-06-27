package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

public interface IAnIMGetClientsStatusCallback {
	void onSuccess(AnIMGetClientsStatusCallbackData data);
	void onError(ArrownockException e);
}

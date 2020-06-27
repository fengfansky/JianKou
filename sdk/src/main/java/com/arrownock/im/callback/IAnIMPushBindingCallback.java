package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

public interface IAnIMPushBindingCallback {
	void onSuccess();
	void onError(ArrownockException e);
}

package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

public interface IAnIMBlacklistCallback {
	void onSuccess();
	void onError(ArrownockException e);
}

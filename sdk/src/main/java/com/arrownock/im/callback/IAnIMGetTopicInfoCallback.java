package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

public interface IAnIMGetTopicInfoCallback {
	void onSuccess(AnIMGetTopicInfoCallbackData data);
	void onError(ArrownockException e);
}

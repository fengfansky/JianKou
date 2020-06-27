package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

public interface IAnIMGetTopicListCallback {
	void onSuccess(AnIMGetTopicListCallbackData data);
	void onError(ArrownockException e);
}

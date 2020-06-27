package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

public interface IAnIMTopicCallback {
	void onSuccess(String topicId, long createdTimestamp, long updatedTimestamp);
	void onError(ArrownockException e);
}

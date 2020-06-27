package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.AnIMMessage;

import java.util.List;

public interface IAnIMHistoryCallback {
	void onSuccess(List<AnIMMessage> messages, int count);
	void onError(ArrownockException e);
}

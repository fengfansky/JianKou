package com.arrownock.im.callback;

import java.util.List;

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.AnIMMessage;

public interface IAnIMHistoryCallback {
	void onSuccess(List<AnIMMessage> messages, int count);
	void onError(ArrownockException e);
}

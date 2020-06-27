package com.arrownock.appo.desk;

import com.arrownock.exception.ArrownockException;

import java.util.List;

public interface IAnDeskHistoryCallback {
	void onSuccess(List<AnDeskMessage> messages, int count);
	void onError(ArrownockException e);
}

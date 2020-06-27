package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

@Deprecated
public class AnIMAddClientsCallbackData extends AnIMBaseRequestCallbackData {
    @Deprecated
	public AnIMAddClientsCallbackData(boolean error, ArrownockException exception) {
		super(error, exception);
	}
}

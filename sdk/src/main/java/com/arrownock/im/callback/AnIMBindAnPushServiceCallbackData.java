package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

@Deprecated
public class AnIMBindAnPushServiceCallbackData extends AnIMBaseRequestCallbackData {
    @Deprecated
	public AnIMBindAnPushServiceCallbackData(boolean error, ArrownockException exception) {
		super(error, exception);
	}
}

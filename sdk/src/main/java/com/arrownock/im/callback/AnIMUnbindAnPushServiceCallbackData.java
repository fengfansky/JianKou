package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

@Deprecated
public class AnIMUnbindAnPushServiceCallbackData extends AnIMBaseRequestCallbackData {
    @Deprecated
	public AnIMUnbindAnPushServiceCallbackData(boolean error, ArrownockException exception) {
		super(error, exception);
	}

}

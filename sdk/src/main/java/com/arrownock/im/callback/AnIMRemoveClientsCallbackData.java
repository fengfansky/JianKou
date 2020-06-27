package com.arrownock.im.callback;

import com.arrownock.exception.ArrownockException;

@Deprecated
public class AnIMRemoveClientsCallbackData extends AnIMBaseRequestCallbackData {
    @Deprecated
	public AnIMRemoveClientsCallbackData(boolean error,
			ArrownockException exception) {
		super(error, exception);
	}

}

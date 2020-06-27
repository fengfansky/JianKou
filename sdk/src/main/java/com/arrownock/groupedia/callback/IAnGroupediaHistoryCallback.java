package com.arrownock.groupedia.callback;

import com.arrownock.exception.ArrownockException;
import com.arrownock.groupedia.model.AnGroupediaMessage;

import java.util.List;

public interface IAnGroupediaHistoryCallback {
    void onSuccess(List<AnGroupediaMessage> messages, int count);

    void onError(ArrownockException e);
}

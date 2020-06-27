package com.arrownock.groupedia.callback;

import java.util.List;

import com.arrownock.exception.ArrownockException;
import com.arrownock.groupedia.model.AnGroupediaMessage;

public interface IAnGroupediaHistoryCallback {
    void onSuccess(List<AnGroupediaMessage> messages, int count);

    void onError(ArrownockException e);
}

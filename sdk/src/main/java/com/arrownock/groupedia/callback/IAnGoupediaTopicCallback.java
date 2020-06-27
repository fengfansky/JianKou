package com.arrownock.groupedia.callback;

import com.arrownock.exception.ArrownockException;
import com.arrownock.groupedia.model.AnGroupediaMessage;
import com.arrownock.groupedia.model.Topic;

import java.util.List;

public interface IAnGoupediaTopicCallback {
    void onSuccess(Topic topic, boolean isJoined, List<AnGroupediaMessage> messages);

    void onFailure(ArrownockException e);
}
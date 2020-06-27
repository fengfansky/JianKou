package com.arrownock.groupedia.callback;

import com.arrownock.groupedia.model.User;

public interface IAnGroupediaCallback {
    void receivedMessage(String topicId, String messageId, String message, long timestamp, User user);

    void receivedImage(String topicId, String messageId, byte[] data, long timestamp, User user);

    void receivedAudio(String topicId, String messageId, byte[] data, long timestamp, User user);
}

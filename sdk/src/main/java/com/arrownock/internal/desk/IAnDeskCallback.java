package com.arrownock.internal.desk;

import com.arrownock.exception.ArrownockException;

import java.util.Map;

public interface IAnDeskCallback {
	void topicClosed(String topicId, long timestamp, Map<String, String> customData);
	void messageSent(String messageId, long timestamp, ArrownockException e);
	void receivedMessage(String messageId, String message, long timestamp, String from, String topicId, Map<String, String> customData);
	void receivedBinary(String messageId, byte[] content, String fileType, long timestamp, String from, String topicId, Map<String, String> customData);
	void accountAddedToSession(String topicId, long timestamp, Map<String, String> customData);
}

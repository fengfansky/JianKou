package com.arrownock.internal.groupedia;

import java.util.Map;

public interface IAnGroupediaCallback {
	void receivedMessage(String messageId, String message, long timestamp, String from, String topicId, Map<String, String> customData);
	void receivedBinary(String messageId, byte[] content, String fileType, long timestamp, String from, String topicId, Map<String, String> customData);
}

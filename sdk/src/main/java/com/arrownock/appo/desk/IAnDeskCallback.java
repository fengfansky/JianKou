package com.arrownock.appo.desk;

public interface IAnDeskCallback {
	void sessionClosed(String groupId, String sessionId, long timestamp);
	void messageSent(String messageId, long timestamp, Exception e);
	void receivedMessage(String groupId, String messageId, String message, long timestamp, String accountId, String accountName);
	void receivedImage(String groupId, String messageId, byte[] data, long timestamp, String accountId, String accountName);
	void accountAddedToSession(String groupId, String sessionId, String accountId, String accountName, long timestamp);
}

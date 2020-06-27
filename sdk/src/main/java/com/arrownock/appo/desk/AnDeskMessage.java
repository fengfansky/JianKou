package com.arrownock.appo.desk;

import java.util.Map;

public class AnDeskMessage {
	private AnDeskMessageType type;
	private String msgId;
	private String groupId;
	private String accountId;
	private String accountName;
	private String message;
	private byte[] data;
	private long timestamp;
	
	public AnDeskMessage(AnDeskMessageType type, String msgId, String groupId,
			String accountId, String accountName, String message, byte[] data,
			long timestamp) {
		super();
		this.type = type;
		this.msgId = msgId;
		this.groupId = groupId;
		this.accountId = accountId;
		this.accountName = accountName;
		this.message = message;
		this.data = data;
		this.timestamp = timestamp;
	}
	public AnDeskMessageType getType() {
		return type;
	}
	public String getMsgId() {
		return msgId;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public String getGroupId() {
		return groupId;
	}
	public String getAccountId() {
		return accountId;
	}
	public String getAccountName() {
		return accountName;
	}
	public String getMessage() {
		return message;
	}
	public byte[] getData() {
		return data;
	}
}

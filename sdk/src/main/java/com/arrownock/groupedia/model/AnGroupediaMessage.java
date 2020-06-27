package com.arrownock.groupedia.model;

import java.io.Serializable;


public class AnGroupediaMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private AnGroupediaMessageType type;
    private String msgId;
    private String topicId;
    private User user;
    private String message;
    private byte[] data;
    private long timestamp;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AnGroupediaMessageType getType() {
        return type;
    }

    public void setType(AnGroupediaMessageType type) {
        this.type = type;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public AnGroupediaMessage() {
        super();
    }

    public AnGroupediaMessage(AnGroupediaMessageType type, String msgId, String topicId, User user, String message,
            byte[] data, long timestamp) {
        super();
        this.type = type;
        this.msgId = msgId;
        this.topicId = topicId;
        this.user = user;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

}

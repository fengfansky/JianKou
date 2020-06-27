package com.arrownock.im.callback;

import java.util.Map;


public class AnIMUpdateTopicEventData {
    private String eventId = null;
    private String from = null;
    private String topicId = null;
    private String topicName = null;
    private long timestamp = -1;
    private String owner = null;
    private Map<String, String> customData = null;

    public AnIMUpdateTopicEventData(String eventId, String from, String topicId, String topicName, String owner, long timestamp, Map<String, String> customData) {
        this.eventId = eventId;
        this.from = from;
        this.topicName = topicName;
        this.topicId = topicId;
        this.timestamp = timestamp;
        this.owner = owner;
        this.customData = customData;
    }

    public String getEventId() {
        return eventId;
    }

    public String getFrom() {
        return from;
    }

    public String getTopicId() {
        return topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getOwner() {
        return owner;
    }
    
    public Map<String, String> getCustomData() {
        return customData;
    }
}

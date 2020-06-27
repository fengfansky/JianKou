package com.arrownock.im.callback;

import java.util.Map;
import java.util.Set;

public class AnIMCreateTopicEventData {
    private String eventId = null;
    private String from = null;
    private String topicId = null;
    private String topicName = null;
    private Set<String> parties = null;
    private long timestamp = -1;
    private String owner = null;
    private Map<String, String> customData = null;

    public AnIMCreateTopicEventData(String eventId, String from, String topicId, String topicName, String owner,
            Set<String> parties, long timestamp, Map<String, String> customData) {
        this.eventId = eventId;
        this.from = from;
        this.topicName = topicName;
        this.topicId = topicId;
        this.parties = parties;
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

    public Set<String> getParties() {
        return parties;
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

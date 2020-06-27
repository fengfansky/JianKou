package com.arrownock.im.callback;

import java.util.Map;
import java.util.Set;

public class AnIMRemoveClientsEventData {
    private String eventId = null;
    private String from = null;
    private String topicId = null;
    private Set<String> parties = null;
    private long timestamp = -1;
    private Map<String, String> customData = null;

    public AnIMRemoveClientsEventData(String eventId, String from, String topicId, Set<String> parties, long timestamp, Map<String, String> customData) {
        this.eventId = eventId;
        this.from = from;
        this.topicId = topicId;
        this.parties = parties;
        this.timestamp = timestamp;
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

    public Set<String> getParties() {
        return parties;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getCustomData() {
        return customData;
    }
}

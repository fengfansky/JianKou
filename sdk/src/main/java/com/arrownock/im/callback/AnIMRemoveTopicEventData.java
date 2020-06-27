package com.arrownock.im.callback;

import java.util.Map;


public class AnIMRemoveTopicEventData {
    private String eventId = null;
    private String from = null;
    private String topicId = null;
    private long timestamp = -1;
    private Map<String, String> customData = null;

    public AnIMRemoveTopicEventData(String eventId, String from, String topicId, long timestamp, Map<String, String> customData) {
        this.eventId = eventId;
        this.from = from;
        this.topicId = topicId;
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

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getCustomData() {
        return customData;
    }
}

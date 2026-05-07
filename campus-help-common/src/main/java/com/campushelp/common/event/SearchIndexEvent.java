package com.campushelp.common.event;

import java.time.Instant;
import java.util.Map;

public class SearchIndexEvent {
    private String eventId;
    private String entityType;
    private String entityId;
    private String op;
    private long version;
    private Instant opTs;
    private Map<String, Object> payload;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Instant getOpTs() {
        return opTs;
    }

    public void setOpTs(Instant opTs) {
        this.opTs = opTs;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}

package com.campushelp.life.feed.dto;

import java.util.List;

public class FeedTimelineResponse {
    private List<FeedItemDTO> items;
    private Long nextCursorScore;
    private String nextCursorId;

    public FeedTimelineResponse(List<FeedItemDTO> items, Long nextCursorScore, String nextCursorId) {
        this.items = items;
        this.nextCursorScore = nextCursorScore;
        this.nextCursorId = nextCursorId;
    }

    public List<FeedItemDTO> getItems() {
        return items;
    }

    public Long getNextCursorScore() {
        return nextCursorScore;
    }

    public String getNextCursorId() {
        return nextCursorId;
    }
}

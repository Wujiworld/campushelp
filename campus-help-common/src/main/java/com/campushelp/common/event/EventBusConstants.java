package com.campushelp.common.event;

public final class EventBusConstants {
    private EventBusConstants() {}

    public static final String EVENT_EXCHANGE = "campus.event.exchange";
    public static final String EVENT_ROUTING_KEY = "campus.event.notify";

    public static final String NOTIFY_QUEUE = "campus.event.notify.q";
    public static final String DLX_EXCHANGE = "campus.event.dlx";
    public static final String DLQ = "campus.event.notify.dlq";
    public static final String DLX_ROUTING_KEY = "campus.event.notify.dlx";

    public static final String FEED_QUEUE = "campus.event.feed.q";
    public static final String FEED_DLQ = "campus.event.feed.dlq";
    public static final String FEED_DLX_ROUTING_KEY = "campus.event.feed.dlx";

    public static final String SEARCH_INDEX_QUEUE = "campus.search.index.q";
    public static final String SEARCH_INDEX_DLQ = "campus.search.index.dlq";
    public static final String SEARCH_INDEX_ROUTING_KEY = "campus.search.index";
    public static final String SEARCH_INDEX_DLX_ROUTING_KEY = "campus.search.index.dlx";
}


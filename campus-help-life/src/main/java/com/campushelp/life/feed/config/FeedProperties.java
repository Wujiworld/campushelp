package com.campushelp.life.feed.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "campus.feed")
public class FeedProperties {
    private String timelinePrefix = "feed:timeline:";
    private String authorPostsPrefix = "feed:author:posts:";
    private String authorProfilePrefix = "feed:author:profile:";
    private int timelineMaxSize = 1000;
    private int pushFollowerThreshold = 2000;
    private int hotShardCount = 8;
    private int pageSizeMax = 50;

    public String getTimelinePrefix() {
        return timelinePrefix;
    }

    public void setTimelinePrefix(String timelinePrefix) {
        this.timelinePrefix = timelinePrefix;
    }

    public String getAuthorPostsPrefix() {
        return authorPostsPrefix;
    }

    public void setAuthorPostsPrefix(String authorPostsPrefix) {
        this.authorPostsPrefix = authorPostsPrefix;
    }

    public String getAuthorProfilePrefix() {
        return authorProfilePrefix;
    }

    public void setAuthorProfilePrefix(String authorProfilePrefix) {
        this.authorProfilePrefix = authorProfilePrefix;
    }

    public int getTimelineMaxSize() {
        return timelineMaxSize;
    }

    public void setTimelineMaxSize(int timelineMaxSize) {
        this.timelineMaxSize = timelineMaxSize;
    }

    public int getPushFollowerThreshold() {
        return pushFollowerThreshold;
    }

    public void setPushFollowerThreshold(int pushFollowerThreshold) {
        this.pushFollowerThreshold = pushFollowerThreshold;
    }

    public int getHotShardCount() {
        return hotShardCount;
    }

    public void setHotShardCount(int hotShardCount) {
        this.hotShardCount = hotShardCount;
    }

    public int getPageSizeMax() {
        return pageSizeMax;
    }

    public void setPageSizeMax(int pageSizeMax) {
        this.pageSizeMax = pageSizeMax;
    }
}

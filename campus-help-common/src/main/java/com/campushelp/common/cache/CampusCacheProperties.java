package com.campushelp.common.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "campus.cache")
public class CampusCacheProperties {
    private long defaultTtlSeconds = 300;
    private long nullTtlSeconds = 45;
    private long jitterSeconds = 30;
    private long localMaximumSize = 10_000;
    private long localExpireSeconds = 120;
    private String lockPrefix = "cache:lock:";

    public long getDefaultTtlSeconds() {
        return defaultTtlSeconds;
    }

    public void setDefaultTtlSeconds(long defaultTtlSeconds) {
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    public long getNullTtlSeconds() {
        return nullTtlSeconds;
    }

    public void setNullTtlSeconds(long nullTtlSeconds) {
        this.nullTtlSeconds = nullTtlSeconds;
    }

    public long getJitterSeconds() {
        return jitterSeconds;
    }

    public void setJitterSeconds(long jitterSeconds) {
        this.jitterSeconds = jitterSeconds;
    }

    public long getLocalMaximumSize() {
        return localMaximumSize;
    }

    public void setLocalMaximumSize(long localMaximumSize) {
        this.localMaximumSize = localMaximumSize;
    }

    public long getLocalExpireSeconds() {
        return localExpireSeconds;
    }

    public void setLocalExpireSeconds(long localExpireSeconds) {
        this.localExpireSeconds = localExpireSeconds;
    }

    public String getLockPrefix() {
        return lockPrefix;
    }

    public void setLockPrefix(String lockPrefix) {
        this.lockPrefix = lockPrefix;
    }
}

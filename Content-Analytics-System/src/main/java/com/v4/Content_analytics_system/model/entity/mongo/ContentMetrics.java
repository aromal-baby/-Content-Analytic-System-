package com.v4.Content_analytics_system.model.entity.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "content_metrics")
public class ContentMetrics {

    @Id
    private String id;

    private Long userId;
    private String platform;
    private LocalDateTime retrievalTimestamp;

    // Engagement Metrics
    private Long views;
    private Long likes;
    private Long comments;
    private Long shares; // Optional, depending on platform

    // To identify which content(in specific platform) this metrics belongs to
    private String platformContentId;

    // Calculated metrics
    private Double engagementRate;

    // Platform-specific metrics
    private Map<String, Object> platformSpecMetrics= new HashMap<>();



    // GETTERS & SETTERS


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public LocalDateTime getRetrievalTimestamp() {
        return retrievalTimestamp;
    }

    public void setRetrievalTimestamp(LocalDateTime retrievalTimestamp) {
        this.retrievalTimestamp = retrievalTimestamp;
    }

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public Long getComments() {
        return comments;
    }

    public void setComments(Long comments) {
        this.comments = comments;
    }

    public Long getShares() {
        return shares;
    }

    public void setShares(Long shares) {
        this.shares = shares;
    }

    public String getPlatformContentId() {
        return platformContentId;
    }

    public void setPlatformContentId(String platformContentId) {
        this.platformContentId = platformContentId;
    }

    public Double getEngagementRate() {
        return engagementRate;
    }

    public void setEngagementRate(Double engagementRate) {
        this.engagementRate = engagementRate;
    }

    public Map<String, Object> getPlatformSpecMetrics() {
        return platformSpecMetrics;
    }

    public void setPlatformSpecMetrics(Map<String, Object> platformSpecMetrics) {
        this.platformSpecMetrics = platformSpecMetrics;
    }
}

package com.v4.Content_analytics_system.service;

import com.v4.Content_analytics_system.model.entity.mongo.ContentMetrics;
import com.v4.Content_analytics_system.model.entity.sql.Content;
import com.v4.Content_analytics_system.model.entity.sql.Platform;
import com.v4.Content_analytics_system.repository.mongo.IContentMetricsRepository;
import com.v4.Content_analytics_system.repository.sql.IContentRepository;
import com.v4.Content_analytics_system.repository.sql.IPlatformRepository;
import com.v4.Content_analytics_system.repository.sql.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlatformService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final IContentMetricsRepository metricsRepository;
    private final IUserRepository userRepository;
    private final IPlatformRepository platformRepository;
    private final IContentRepository contentRepository;
    private final MetricsService metricsService;

    @Autowired
    public PlatformService(IContentMetricsRepository metricsRepository,
                           IUserRepository userRepository,
                           IPlatformRepository platformRepository,
                           IContentRepository contentRepository,
                           MetricsService metricsService) {


        this.metricsRepository = metricsRepository;
        this.userRepository = userRepository;
        this.platformRepository = platformRepository;
        this.contentRepository = contentRepository;
        this.metricsService = metricsService;
    }

    // Get all platforms for a user
    public List<Platform> getUserPlatforms(Long userId) {
        // Check if user exists
        if(!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        // Return platforms all platforms for this user
        return (List<Platform>) platformRepository.findByUserId(userId);
    }

    // Get platform by ID
    public Optional<Platform> getPlatformById(Long id) {
        return platformRepository.findById(id);
    }

    // To save/update platform
    public Platform savePlatform(Platform platform) {
        return platformRepository.save(platform);
    }

    // To delete
    public void deletePlatform(Long id) {
        platformRepository.deleteById(id);
    }

    // To update last sync time
    public void updateLastSyncTime(Long platformId) {
        platformRepository.findById(platformId).ifPresent(p -> {
            p.setLastSyncTime(LocalDateTime.now());
            platformRepository.save(p);
        });
    }


    // Getting all content for a platform with their metrics
    public List<Map<String, Object>> getPlatformContentWithMetrics(Long platformId, Long userId) {
        List<Content> contents = contentRepository.findByPlatformIdAndUserId(platformId, userId);

        return contents.stream().map(content -> {
            Map<String, Object> contentData = new HashMap<>();
            contentData.put("id", content.getId());
            contentData.put("title", content.getTitle());
            contentData.put("contentType", content.getContentType().toString());
            contentData.put("platformContentId", content.getPlatformContentId());
            contentData.put("publishedDate", content.getPublishedDate());

            // Getting the latest metrics for this content
            try {
                Optional<ContentMetrics> metricsOpt = metricsRepository.findTopByPlatformContentIdOrderByRetrievalTimestampDesc(
                        content.getPlatformContentId());

                if (metricsOpt.isPresent()) {
                    ContentMetrics metrics = metricsOpt.get();
                    contentData.put("views", metrics.getViews());
                    contentData.put("likes", metrics.getLikes());
                    contentData.put("comments", metrics.getComments());
                    contentData.put("shares", metrics.getShares());
                    contentData.put("engagementRate", metrics.getEngagementRate());
                } else {
                    // Setting defaults if no metrics found
                    contentData.put("views", 0);
                    contentData.put("likes", 0);
                    contentData.put("comments", 0);
                    contentData.put("shares", 0);
                    contentData.put("engagementRate", 0.0);
                }
            } catch (Exception e) {
                log.error("Error fetching metrics for content {}: {}", content.getId(), e.getMessage());
                // Setting defaults if error
                contentData.put("views", 0);
                contentData.put("likes", 0);
                contentData.put("comments", 0);
                contentData.put("shares", 0);
                contentData.put("engagementRate", 0.0);
            }

            return contentData;
        }).collect(Collectors.toList());
    }


    // To fetch contents metrics
    public void fetchContentMetrics(Long contentId) {

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        try {
            ContentMetrics metrics = metricsService.fetchMetricsForContent(content);

            // Last sync time
            Platform p = content.getPlatform();
            p.setLastSyncTime(LocalDateTime.now());
            metricsRepository.save(metrics);

            log.info("Updated metrics for content ID: {} on platform: {}",
                    contentId, content.getPlatform().getPlatformName());
        } catch (Exception e) {
            log.error("Failed to fetch metrics for content ID: {}: {}", contentId, e.getMessage());
            throw new RuntimeException("Failed to fetch metrics: " + e.getMessage());
        }

    }


    public Map<String, Map<String, Object>> getPlatformStatistics() {

        Map<String, Map<String, Object>> result = new HashMap<>();
        // Get all distinct platform types
        List<String> platformTypes = platformRepository.findDistinctPlatformName();

        for (String platformType : platformTypes) {
            Map<String, Object> stats = new HashMap<>();

            // Count content items by platform type
            long contentCount = contentRepository.countByPlatform_PlatformName(platformType);
            stats.put("contentCount", contentCount);

            // Calculate total views (requires integrating with your metrics system)
            long totalViews = metricsRepository.sumViewsByPlatform(platformType);
            stats.put("totalViews", totalViews);

            result.put(platformType, stats);
        }

        return result;
    }
}

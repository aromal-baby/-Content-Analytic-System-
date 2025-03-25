package com.v4.Content_analytics_system.service;

import com.v4.Content_analytics_system.model.entity.sql.Content;
import com.v4.Content_analytics_system.model.entity.sql.Platform;
import com.v4.Content_analytics_system.repository.mongo.IContentMetricsRepository;
import com.v4.Content_analytics_system.repository.sql.IContentRepository;
import com.v4.Content_analytics_system.repository.sql.IPlatformRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final IPlatformRepository platformRepository;
    private final IContentRepository contentRepository;
    private final IContentMetricsRepository metricsRepository;

    public DashboardService(IPlatformRepository platformRepository, IContentRepository contentRepository, IContentMetricsRepository metricsRepository) {

        this.platformRepository = platformRepository;
        this.contentRepository = contentRepository;
        this.metricsRepository = metricsRepository;

    }

    // Get USer's platform summary(for each)
    public Map<String, Object> getUserPlatformSummary(Long userId) {
        Map<String, Object> summ = new HashMap<>();

        // Get user's platforms
        List<Platform> platforms = platformRepository.findByUserId(userId);
        summ.put("totalPlatforms", platforms.size());

        // Platform distribution
        Map<String, Integer> platformDistribution = new HashMap<>();
        for (Platform p : platforms) {
            platformDistribution.put(
                    p.getPlatformName(),
                    platformDistribution.getOrDefault(p.getPlatformName(), 0) + 1
            );
        }
        summ.put("platformDistribution", platformDistribution);

        return summ;
    }

    // for user's content summary
    public Map<String, Object> getUserContentSummary(Long userId) {
        Map<String, Object> summ = new HashMap<>();

        // Getting user's content
        List<Content> contents = contentRepository.findByUserId(userId);
        System.out.println("Found " + contents.size() + " total content items for user ID: " + userId);
        contents.forEach(c -> System.out.println("- Content: " + c.getTitle() + ", Platform: " + c.getPlatform().getPlatformName()));

        summ.put("totalContents", contents.size());

        // Content type distribution
        Map<Content.ContentType, Integer> cTypDstrbtn = new HashMap<>();
        for (Content c : contents) {
            cTypDstrbtn.put(
                    c.getContentType(),
                    cTypDstrbtn.getOrDefault(c.getContentType(), 0) + 1
            );
        }
        summ.put("contentTypeDistribution", cTypDstrbtn);

        // For content status dist
        Map<Content.ContentStatus, Integer> cStatusDstrbtn = new HashMap<>();
        for (Content c : contents) {
            cStatusDstrbtn.put(
                    c.getStatus(),
                    cStatusDstrbtn.getOrDefault(c.getStatus(), 0) + 1
            );
        }
        summ.put("contentStatusDistribution", cStatusDstrbtn);

        // Recent contents
        PageRequest pageRequest = PageRequest.of(0, 5);  // First page (0), with 5 items
        List<Content> recentContent = contentRepository.findRecentContentByUserId(userId, pageRequest);
        System.out.println("Found " + recentContent.size() + " recent content items for user ID: " + userId);
        recentContent.forEach(c -> System.out.println("- Recent Content: " + c.getTitle()));

        summ.put("recentContent", recentContent);

        return summ;
    }

    // For user's metric summary
    public Map<String, Object> getUserMetricsSummary(Long userId) {

        // This would need implementation to aggregate metrics from MongoDB
        Map<String, Object> summ = new HashMap<>();

        // Placeholders
        summ.put("totalViews", 0L);
        summ.put("totalLikes", 0L);
        summ.put("totalComments", 0L);
        summ.put("totalShares", 0L);
        summ.put("averageEngagementRate", 0.0);

        return summ;


    }


}

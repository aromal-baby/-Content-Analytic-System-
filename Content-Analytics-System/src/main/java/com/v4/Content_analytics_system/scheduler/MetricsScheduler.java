package com.v4.Content_analytics_system.scheduler;

import com.v4.Content_analytics_system.model.entity.mongo.ContentMetrics;
import com.v4.Content_analytics_system.model.entity.sql.Content;
import com.v4.Content_analytics_system.repository.sql.IContentRepository;
import com.v4.Content_analytics_system.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class MetricsScheduler {

    private static final Logger log = LoggerFactory.getLogger(MetricsScheduler.class);

    private final MetricsService metricsService;
    private final IContentRepository contentRepository;

    @Autowired
    public MetricsScheduler(MetricsService metricsService, IContentRepository contentRepository) {
        this.metricsService = metricsService;
        this.contentRepository = contentRepository;
    }

    // Run every 5 minutes for near real-time updates
    @Scheduled(cron = "0 */5 * * * *")
    public void updateAllMetrics() {
        log.info("Starting scheduled metrics update at {}", LocalDateTime.now());

        List<Content> contents = contentRepository.findAll();
        log.info("Found {} content items to update", contents.size());

        int successCount = 0;
        int errorCount = 0;

        for (Content content : contents) {
            try {
                metricsService.fetchMetricsForContent(content);
                successCount++;
                log.debug("Updated metrics for content ID: {}", content.getId());
            } catch (Exception e) {
                errorCount++;
                log.error("Error updating metrics for content ID: {}: {}", content.getId(), e.getMessage());
            }
        }

        log.info("Completed metrics update: {} successful, {} failed", successCount, errorCount);
    }

    // Optional: Add an immediate trigger method that can be called via API
    public void triggerImmediateUpdate() {
        log.info("Manual trigger of metrics update");
        updateAllMetrics();
    }
}
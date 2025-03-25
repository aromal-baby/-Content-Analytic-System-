package com.v4.Content_analytics_system.controller;

import com.v4.Content_analytics_system.exception.ResourceNotFoundException;
import com.v4.Content_analytics_system.model.entity.mongo.ContentMetrics;
import com.v4.Content_analytics_system.model.entity.sql.User;
import com.v4.Content_analytics_system.repository.mongo.IContentMetricsRepository;
import com.v4.Content_analytics_system.service.DashboardService;
import com.v4.Content_analytics_system.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;
    private final UserService userService;
    private final IContentMetricsRepository metricsRepository;

    public DashboardController(DashboardService dashboardService,
                               UserService userService,
                               IContentMetricsRepository metricsRepository) {
        this.dashboardService = dashboardService;
        this.userService = userService;
        this.metricsRepository = metricsRepository;
    }


    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary(Authentication authentication) {

        Long userId = getUserId(authentication);

        try {
            // Creating response for the dashboard
            Map<String, Object> dashboardData = new HashMap<>();

            // Getting platform data
            Map<String, Object> platformsData = dashboardService.getUserPlatformSummary(userId);
            log.info("Platform data for user {}: {}", userId, platformsData);
            dashboardData.put("platforms", platformsData);

            // Getting Content data
            Map<String, Object> contentData = dashboardService.getUserContentSummary(userId);
            log.info("Content data for user {}: {}", userId, contentData);
            dashboardData.put("content", contentData);

            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            log.error("Error generating metrics summary: {}", e.getMessage(), e);
            Map<String, Object> fallback = new HashMap<>();

            Map<String, Object> platforms = new HashMap<>();
            platforms.put("totalPlatforms", 0);
            platforms.put("platformDistribution", new HashMap<>());

            Map<String, Object> content = new HashMap<>();
            content.put("totalContents", 0);
            content.put("contentTypeDistribution", new HashMap<>());
            content.put("recentContent", new ArrayList<>());

            fallback.put("platforms", platforms);
            fallback.put("content", content);

            return ResponseEntity.ok(fallback);
        }
    }


    // HELPER for getting the user id from the authentication
    private Long getUserId(Authentication authentication) {

        if (authentication != null) {
            try {
                String username = authentication.getName();
                User user = userService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                return user.getId();
            } catch (Exception e) {
                throw new UsernameNotFoundException(e.getMessage());
            }
        }
        return null;
    }

}

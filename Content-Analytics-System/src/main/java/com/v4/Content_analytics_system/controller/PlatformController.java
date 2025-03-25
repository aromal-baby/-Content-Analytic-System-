package com.v4.Content_analytics_system.controller;

import com.v4.Content_analytics_system.exception.ResourceNotFoundException;
import com.v4.Content_analytics_system.model.entity.sql.Platform;
import com.v4.Content_analytics_system.model.entity.sql.User;
import com.v4.Content_analytics_system.repository.mongo.IContentMetricsRepository;
import com.v4.Content_analytics_system.repository.sql.IContentRepository;
import com.v4.Content_analytics_system.repository.sql.IPlatformRepository;
import com.v4.Content_analytics_system.service.ContentService;
import com.v4.Content_analytics_system.service.PlatformService;
import com.v4.Content_analytics_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/platforms")
public class PlatformController {

    private final PlatformService platformService;
    private final UserService userService;
    private final IContentRepository contentRepository;
    private final IPlatformRepository platformRepository;
    private final IContentMetricsRepository metricsRepository;

    @Autowired
    public PlatformController(PlatformService platformService,
                              UserService userService,
                              IContentRepository contentRepository,
                              IPlatformRepository platformRepository,
                              IContentMetricsRepository metricsRepository) {
        this.platformService = platformService;
        this.userService = userService;
        this.contentRepository = contentRepository;
        this.platformRepository = platformRepository;
        this.metricsRepository = metricsRepository;
    }

    @GetMapping
    public ResponseEntity<List<Platform>> getAllPlatforms(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Long userId = user.getId();

            return ResponseEntity.ok(platformService.getUserPlatforms(userId));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping
    public ResponseEntity<Platform> createPlatform(@RequestBody Map<String, String> platformData,
                                                   Authentication authentication) {

        try {
            System.out.println("Creating platform with data: " + platformData);

            // Extract data from request
            String platformName = platformData.get("platformName");
            String url = platformData.get("url");

            if (platformName == null || platformName.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Getting current user from authentication context
            User user;
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                user = userService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                System.out.println("Creating platform for authenticated user: " + username + " (ID: " + user.getId() + ")");
            } else {
                // Fallback for development only
                user = userService.getUserById(1L)
                        .orElseThrow(() -> new RuntimeException("Default user not found"));
                System.out.println("Warning: Using default user ID for platform creation: " + user.getId());
            }

            // Creating new platform with user association
            Platform platform = new Platform();
            platform.setPlatformName(platformName);
            platform.setUser(user);
            platform.setConnected(true);
            platform.setConnectionStatus("Connected");
            platform.setLastSyncTime(LocalDateTime.now());

            // Extract username from URL for display purposes
            String username = extractUsername(url, platformName);
            platform.setPlatformUsername(username);

            Platform savedPlatform = platformService.savePlatform(platform);
            System.out.println("Platform created with ID: " + savedPlatform.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(savedPlatform);
        } catch (Exception e) {
            System.err.println("Error creating platform: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // To find platform by name
    @GetMapping("/byName/{platformName}")
    public ResponseEntity<Platform> getPlatformByName(
            @PathVariable String platformName,
            Authentication authentication) {

        // Get authenticated user
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new
                        ResourceNotFoundException("User not found"));

        // Find platform for this user
        List<Platform> userPlatforms = platformService.getUserPlatforms(user.getId());

        Optional<Platform> platform = userPlatforms.stream()
                .filter(p -> p.getPlatformName().equalsIgnoreCase(platformName))
                .findFirst();

        return platform.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // To get the platform's stats in brief for the platformsPage (frontend)
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Map<String, Object>>> getPlatformStats(Authentication authentication) {
        Long userId;

        if (authentication != null) {
            try {
                String username = authentication.getName();
                User user = userService.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                userId = user.getId();
            } catch (Exception e) {
                userId = 1L; // Fallback
            }
        } else {
            userId = 1L; // Fallback
        }

        Map<String, Map<String, Object>> stats = new HashMap<>();

        // Get all platforms for this user
        List<Platform> platforms = platformService.getUserPlatforms(userId);

        // Extract unique platform names
        Set<String> platformNames = platforms.stream()
                .map(Platform::getPlatformName)
                .collect(Collectors.toSet());

        for (String platformName : platformNames) {
            Map<String, Object> platformStats = new HashMap<>();

            // Count content for this platform and user
            long contentCount = contentRepository.countByPlatform_PlatformNameAndUser_Id(platformName, userId);
            platformStats.put("contentCount", contentCount);

            // Get views (placeholder since we don't have metrics fully implemented)
            platformStats.put("totalViews", 0L);

            stats.put(platformName, platformStats);
        }

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Platform> getPlatformById(@PathVariable Long id) {
        return platformService.getPlatformById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlatform(@PathVariable Long id) {
        return platformService.getPlatformById(id)
                .map(platform -> {
                    platformService.deletePlatform(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Helper method to extract username from URL
    private String extractUsername(String url, String platformName) {
        if (url == null) {
            return platformName + " User";
        }

        try {
            if (url.contains("youtube.com") || url.contains("youtu.be")) {
                // For YouTube, we'll use a placeholder since channel info isn't in video URLs
                return "YouTube Channel";
            } else if (url.contains("instagram.com")) {
                // For Instagram posts like instagram.com/username/post/...
                String[] parts = url.split("instagram.com/");
                if (parts.length > 1) {
                    String afterDomain = parts[1];
                    if (afterDomain.contains("/")) {
                        return afterDomain.split("/")[0]; // Gets username
                    }
                }
                return "Instagram User";
            } else if (url.contains("tiktok.com")) {
                // For TikTok URLs like tiktok.com/@username/...
                if (url.contains("@")) {
                    String[] parts = url.split("@");
                    if (parts.length > 1 && parts[1].contains("/")) {
                        return "@" + parts[1].split("/")[0];
                    }
                }
                return "TikTok User";
            }

            // Default case
            return platformName + " User";
        } catch (Exception e) {
            return platformName + " User";
        }
    }
}
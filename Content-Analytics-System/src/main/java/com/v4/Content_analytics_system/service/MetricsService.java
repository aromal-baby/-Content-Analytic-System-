package com.v4.Content_analytics_system.service;

import com.v4.Content_analytics_system.exception.MetricsFetchException;
import com.v4.Content_analytics_system.model.entity.mongo.ContentMetrics;
import com.v4.Content_analytics_system.model.entity.sql.Content;
import com.v4.Content_analytics_system.repository.mongo.IContentMetricsRepository;
import com.v4.Content_analytics_system.repository.sql.IContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MetricsService {

    private final Logger log = LoggerFactory.getLogger(MetricsService.class);

    private final RestTemplate restTemplate;
    private final IContentMetricsRepository metricsRepository;
    private final IContentRepository contentRepository;

    public MetricsService(RestTemplate restTemplate, IContentMetricsRepository metricsRepository, IContentRepository contentRepository) {
        this.restTemplate = restTemplate;
        this.metricsRepository = metricsRepository;
        this.contentRepository = contentRepository;
    }

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    @Value("${youtube.api.base-url}")
    private String youtubeBaseUrl;

    // Fetching metrics based on platforms
    public ContentMetrics fetchMetricsForContent(Content content) {

        log.info("Fetching metrics for content ID: {}, Platform: {}",
                content.getId(), content.getPlatform().getPlatformName());

        String platform = content.getPlatform().getPlatformName();
        String contentId = content.getPlatformContentId();

        ContentMetrics metrics;

        switch (platform.toLowerCase()) {
            case "youtube":
                metrics = fetchYoutubeMetrics(contentId);
                break;
                case "instagram":
                    metrics = fetchInstagramMetrics(contentId);
                    break;
                    case "tiktok":
                        metrics = fetchTiktokMetrics(contentId);
                        break;
                        default:
                            throw new MetricsFetchException("Unsupported platform: " + platform);
        }

        // Setting common fields
        metrics.setUserId(content.getUser().getId());
        metrics.setPlatform(platform);
        metrics.setPlatformContentId(contentId);
        metrics.setRetrievalTimestamp(LocalDateTime.now());

        // Calc engagement rate
        calculateEngagementRate(metrics);

        log.info("Successfully fetched metrics for content ID: {}", content.getId());

        if (enableMetricsDebug) {
            log.info("Saving metrics to MongoDB: platform={}, contentId={}, views={}, likes={}, comments={}, engagementRate={}%",
                    metrics.getPlatform(), metrics.getPlatformContentId(),
                    metrics.getViews(), metrics.getLikes(), metrics.getComments(),
                    metrics.getEngagementRate());
        }

        // Saving to MongoDb
        ContentMetrics savedMetrics = metricsRepository.save(metrics);
        log.info("Successfully saved metrics with MongoDB ID: {}", savedMetrics.getId());
        return savedMetrics;
    }

    @Value("${logging.level.metrics-debug:false}")
    private boolean enableMetricsDebug;



    // Fetching overall metrics for a specific platform
    public List<ContentMetrics> fetchMetricsForPlatform(Long platformId, Long userId) {

        List<Content> contents = contentRepository.findByPlatformIdAndUserId(platformId, userId);

        return contents.stream()
                .map(this::fetchMetricsForContent)
                .collect(Collectors.toList());
    }


    // Fetching metrics for all content for the user
    public List<ContentMetrics> fetchAllUserMetrics(Long userId) {

        List<Content> contents = contentRepository.findByUserId(userId);

        return contents.stream()
                .map(this::fetchMetricsForContent)
                .collect(Collectors.toList());
    }


    // Getting stored metrics for a platform
    public Map<String, Object> getPlatformMetricsSummary(Long platformId, Long userId) {

        Map<String, Object> summ = new HashMap<>();

        // paltform name
        Content content = contentRepository.findFirstByPlatformId(platformId)
                .orElseThrow(() -> new RuntimeException("No contents found for the platform"));

        String platformName = content.getPlatform().getPlatformName();

        // Getting metrics
        List<ContentMetrics> metrics = metricsRepository.findByUserIdAndPlatform(userId, platformName);

        // calcing the totals
        long totalViews = metrics.stream()
                .mapToLong(m -> m.getViews() != null ? m.getViews() : 0)
                .sum();

        long totalLikes = metrics.stream()
                .mapToLong(m -> m.getLikes() != null ? m.getLikes() : 0)
                .sum();

        long totalComments = metrics.stream()
                .mapToLong(m -> m.getComments() != null ? m.getComments() : 0)
                .sum();

        double avgEngagementRate = metrics.stream()
                .mapToDouble(m -> m.getEngagementRate() != null ? m.getEngagementRate() : 0)
                .average()
                .orElse(0.0);

        // Creating the summary
        summ.put("totalViews", totalViews);
        summ.put("totalLikes", totalLikes);
        summ.put("totalComments", totalComments);
        summ.put("averageEngagementRate", avgEngagementRate);

        // Getting the content distribution (based on category)
        List<Content> platformContent = contentRepository.findByPlatformIdAndUserId(platformId, userId);
        Map<Content.ContentType, Long> contentTypeCount = platformContent.stream()
                .collect(Collectors.groupingBy(Content::getContentType, Collectors.counting()));

        List<Map<String, Object>> contentDistribution = contentTypeCount.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey().toString());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        summ.put("contentDistribution", contentDistribution);

        return summ;
    }


    // Getting the time series data
    public List<Map<String, Object>> getPlatformTimeSeries(Long platformId, Long userId) {
        Content content = contentRepository.findFirstByPlatformId(platformId)
                .orElseThrow(() -> new RuntimeException("No content found for platform"));

        String platformName = content.getPlatform().getPlatformName();

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return metricsRepository.getTimeSeriesDataByPlatform(userId, platformName, thirtyDaysAgo);
    }


    // Updated the fetchYoutubeMetrics method for improved handling of likes
    private ContentMetrics fetchYoutubeMetrics(String videoId) {
        try {
            // Constructing the API URL
            String apiUrl = String.format(
                    "%s/videos?part=statistics,snippet&id=%s&key=%s",
                    youtubeBaseUrl, videoId, youtubeApiKey);

            // Making the API call
            ResponseEntity<Map> response = restTemplate.getForEntity(apiUrl, Map.class);
            Map<String, Object> body = response.getBody();

            // Log full response for debugging
            log.debug("YouTube  API response for video {}: {}", videoId, body);

            if (body == null || !body.containsKey("items")) {
                throw new MetricsFetchException("Youtube API returned no results");
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            if (items.isEmpty()) {
                throw new MetricsFetchException("Video not found: " + videoId);
            }

            Map<String, Object> vidData = items.get(0);
            Map<String, Object> stats = (Map<String, Object>) vidData.get("statistics");
            Map<String, Object> snipps = (Map<String, Object>) vidData.get("snippet");

            log.debug("YouTube API statistics: {}", stats);

            // metrics object to saving to the collection
            ContentMetrics metrics = new ContentMetrics();

            // Basic metrics
            metrics.setViews(parseLong(stats.get("viewCount")));

            // Handle case where likeCount might not be available
            metrics.setLikes(stats.containsKey("likeCount") ?
                    parseLong(stats.get("likeCount")) :
                    estimateLikesFromViews(parseLong(stats.get("viewCount"))));

            metrics.setComments(parseLong(stats.get("commentCount")));

            // Setting platform specific metrics
            Map<String, Object> platSpecMetrics = new HashMap<>();
            platSpecMetrics.put("favoriteCount", stats.get("favoriteCount"));
            platSpecMetrics.put("channelId", snipps.get("channelId"));
            platSpecMetrics.put("publishedAt", snipps.get("publishedAt"));
            platSpecMetrics.put("channelTitle", snipps.get("channelTitle"));
            platSpecMetrics.put("title", snipps.get("title"));
            metrics.setPlatformSpecMetrics(platSpecMetrics);

            return metrics;
        } catch (Exception e) {
            throw new MetricsFetchException("Failed to fetch Youtube metrics: " + e.getMessage());
        }
    }
    // HELPER method to estimate likes when not available
    private Long estimateLikesFromViews(Long views) {
        if (views == null || views == 0) {
            return 0L;
        }
        // Typical YouTube engagement rate is around 2-5% for likes
        // Use a conservative estimate
        return Math.round(views * 0.02);
    }


    // Instagram
    private ContentMetrics fetchInstagramMetrics(String contentId) {


        // placeholder before try to get the real API
        ContentMetrics metrics = new ContentMetrics();
        metrics.setViews(0L); // Instagram doesn't provide view counts for all content types
        metrics.setLikes((long)(Math.random() * 100 + 10));
        metrics.setComments((long)(Math.random() * 20 + 5));

        Map<String, Object> platformSpecific = new HashMap<>();
        platformSpecific.put("impressions", (long)(Math.random() * 500 + 50));
        platformSpecific.put("reach", (long)(Math.random() * 300 + 30));
        platformSpecific.put("saves", (long)(Math.random() * 20 + 2));
        metrics.setPlatformSpecMetrics(platformSpecific);

        return metrics;
    }


    // TikTok
    private ContentMetrics fetchTiktokMetrics(String contentId) {

        //Placeholder
        ContentMetrics metrics = new ContentMetrics();
        metrics.setViews((long)(Math.random() * 1000 + 100));
        metrics.setLikes((long)(Math.random() * 200 + 20));
        metrics.setComments((long)(Math.random() * 50 + 5));
        metrics.setShares((long)(Math.random() * 30 + 3));

        Map<String, Object> platformSpecific = new HashMap<>();
        platformSpecific.put("playCount", (long)(Math.random() * 1000 + 100));
        platformSpecific.put("forwardCount", (long)(Math.random() * 30 + 3));
        platformSpecific.put("whatsappShareCount", (long)(Math.random() * 10 + 1));
        metrics.setPlatformSpecMetrics(platformSpecific);

        return metrics;

    }


    // HELPER to clac any platform's engagement rate
    private void calculateEngagementRate(ContentMetrics metrics) {

        if (metrics.getViews() == null || metrics.getViews() == 0) {
            metrics.setEngagementRate(0.0);
            return;
        }

        long totEng =
                        (metrics.getLikes() != null ? metrics.getLikes() : 0) +
                        (metrics.getComments() != null ? metrics.getComments() : 0) +
                        (metrics.getShares() != null ? metrics.getShares() : 0);

        metrics.setEngagementRate(((double) totEng / metrics.getViews()) * 100);
    }


    // Time series methods
    public List<Map<String, Object>> getContentTimeSeriesData(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        String platformContentId = content.getPlatformContentId();

        // Try to get the platform publish date
        LocalDateTime published = getPublishDateForContent(content);

        // Get actual metrics records
        List<ContentMetrics> metrics = metricsRepository.findByPlatformContentIdOrderByRetrievalTimestampAsc(platformContentId);

        // Build complete time series
        return buildTimeSeriesData(metrics, published);
    }

    // Helper method to get the best publish date
    private LocalDateTime getPublishDateForContent(Content content) {
        // First try content's publishedDate
        if (content.getPublishedDate() != null) {
            return content.getPublishedDate();
        }

        // Try to get from platform metadata for YouTube
        if ("YouTube".equalsIgnoreCase(content.getPlatform().getPlatformName())) {
            Optional<ContentMetrics> latest = metricsRepository
                    .findTopByPlatformContentIdOrderByRetrievalTimestampDesc(content.getPlatformContentId());

            if (latest.isPresent() && latest.get().getPlatformSpecMetrics() != null) {
                Map<String, Object> specMetrics = latest.get().getPlatformSpecMetrics();
                if (specMetrics.containsKey("publishedAt")) {
                    try {
                        String publishedAt = (String) specMetrics.get("publishedAt");
                        return LocalDateTime.parse(publishedAt.replace("Z", ""));
                    } catch (Exception e) {
                        log.warn("Could not parse YouTube publish date: {}", e.getMessage());
                    }
                }
            }
        }

        // Fallback to creation date or 30 days ago
        return content.getCreatedAt() != null ?
                content.getCreatedAt() : LocalDateTime.now().minusDays(30);
    }

    // Building complete time series with no gaps
    private List<Map<String, Object>> buildTimeSeriesData(List<ContentMetrics> metrics, LocalDateTime startDate) {
        List<Map<String, Object>> timeSerData = new ArrayList<>();

        if (metrics.isEmpty()) {
            // Generating synthetic data
            return generateSyntheticTimeSeriesData(startDate);
        } else {
            // Creating map of actual data points
            Map<LocalDateTime, Long> viewsByDate = new HashMap<>();
            viewsByDate.put(startDate, 0L);

            for (ContentMetrics cm : metrics) {
                viewsByDate.put(cm.getRetrievalTimestamp(), cm.getViews());
            }

            // Ensure we have today's data point
            if (!viewsByDate.containsKey(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS))) {
                Long lastViews = viewsByDate.values().stream().reduce((first, second) -> second).orElse(0L);
                viewsByDate.put(LocalDateTime.now(), lastViews);
            }

            // Sorting and converting to result format
            viewsByDate.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        Map<String, Object> dataPoint = new HashMap<>();
                        dataPoint.put("date", entry.getKey().toString());
                        dataPoint.put("views", entry.getValue());
                        timeSerData.add(dataPoint);
                    });

            return timeSerData;
        }
    }

    // Generating synthetic data for empty metrics
    private List<Map<String, Object>> generateSyntheticTimeSeriesData(LocalDateTime startDate) {
        List<Map<String, Object>> data = new ArrayList<>();
        LocalDateTime current = startDate;
        LocalDateTime now = LocalDateTime.now();

        long viewBase = 100;
        Random random = new Random();

        while (current.isBefore(now)) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", current.toString());
            dataPoint.put("views", viewBase);

            data.add(dataPoint);

            current = current.plusDays(1);
            viewBase += random.nextInt(50) + 10;
        }

        return data;
    }


    // For getting time series data for content form the published date
    public List<Map<String, Object>> getContentTimeSeriesWithPlatformPublishedDate(Long contentId) {

        // Getting the content
       Content content = contentRepository.findById(contentId)
               .orElseThrow(() -> new RuntimeException("Content not found"));

       String platformContentId = content.getPlatformContentId();
       String PlatformName = content.getPlatform().getPlatformName();

        // Getting the platform published date
        LocalDateTime published = getActualPublishDateForContent(content);
        log.info("Using publish date for content ID {}: {}", contentId, published);

        // Getting all metrics for this content
        List<ContentMetrics> metrics = metricsRepository.findByPlatformContentIdOrderByRetrievalTimestampAsc(platformContentId);

        // Returning completed time series data
        return buildTimeSeriesForContent(metrics, published);
    }
    // HELPER METHODS FOR TIMESERIES
    public LocalDateTime getActualPublishDateForContent(Content content) {

        String platformName = content.getPlatform().getPlatformName();
        String PlatformContentId = content.getPlatformContentId();

        // Trying to get the published date from metadata
        Optional<ContentMetrics> latestMetrics = metricsRepository
                .findTopByPlatformContentIdOrderByRetrievalTimestampDesc(PlatformContentId);

        if (latestMetrics.isPresent() && latestMetrics.get().getPlatformSpecMetrics() != null) {
            Map<String, Object> specMetrics = latestMetrics.get().getPlatformSpecMetrics();

            // Handling different platform's published date
            if (platformName.equalsIgnoreCase("YouTube") && specMetrics.containsKey("publishedAt")) {
                try {
                    String publishedAt = (String) specMetrics.get("publishedAt");
                    return LocalDateTime.parse(publishedAt.replace("Z", "")); // Formatting to the youtube's date format
                } catch (Exception e) {
                    log.warn("Could not parse YouTube publish date: {}", e.getMessage());
                }
            } else if (platformName.equalsIgnoreCase("Instagram") && specMetrics.containsKey("timestamp")) {
                try {
                    Long timestamp = (Long) specMetrics.get("timestamp");
                    return LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(timestamp),
                            ZoneId.systemDefault()
                    );
                } catch (Exception e) {
                    log.warn("Could not parse Instagram timestamp: {}", e.getMessage());
                }
            } else if (platformName.equalsIgnoreCase("TikTok") && specMetrics.containsKey("createTime")) {
                try {
                    String createTime = (String) specMetrics.get("createTime");
                    return LocalDateTime.parse(createTime);
                } catch (Exception e) {
                    log.warn("Could not parse TikTok create time: {}", e.getMessage());
                }
            }
        }

        // Falling back to content entity published date
        if (content.getPublishedDate() != null) {
            return content.getPublishedDate();
        }

        //Last resort dummy date
        return content.getCreatedAt() != null ?
                content.getCreatedAt() :
                LocalDateTime.now().minusDays(30);
    }
    // Building complete time series for a specific content
    private List<Map<String, Object>> buildTimeSeriesForContent(
            List<ContentMetrics> metrics, LocalDateTime published) {

        List<Map<String, Object>> result = new ArrayList<>();

        // Mapping the metrics by date
        Map<String, ContentMetrics> metricsMap = new HashMap<>();
        for (ContentMetrics cm : metrics) {
            String date = cm.getRetrievalTimestamp().toString();
            metricsMap.put(date, cm);
        }

        // Generating data for each day from publish date to now
        LocalDateTime current = published.toLocalDate().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        // Starting with baseline metric value
        long baseViews = 10;
        long baseLikes = 1;
        long baseCmmnts = 0;

        while (current.isAfter(now)) {
            String date = current.toLocalDate().toString();
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", date);

            if (metricsMap.containsKey(date)) {
                // Using the real ones
                ContentMetrics metric = metricsMap.get(date);
                dataPoint.put("views", metric.getViews() != null ? metric.getViews() : 0);
                dataPoint.put("likes", metric.getLikes() != null ? metric.getLikes() : 0);
                dataPoint.put("comments", metric.getComments() != null ? metric.getComments() : 0);

                // Updating baseline for subsequent days using the latest real data
                baseViews = metric.getViews() != null ? metric.getViews() : baseViews;
                baseLikes = metric.getLikes() != null ? metric.getLikes() : baseLikes;
                baseCmmnts = metric.getComments() != null ? metric.getComments() : baseCmmnts;
            } else {
                // Generating synthetic data based on day number and latest metrics
                long dayNum = ChronoUnit.DAYS.between(published.toLocalDate(), current.toLocalDate());

                // Different growth rates for early - later days
                double grwthRate = dayNum < 7 ? 0.15 : 0.02; // Higher in first week

                // Applying small variation for realistic data
                Random rand  = new Random(date.hashCode()); // Randomness based on date
                double randfctr = 0.9 + (rand.nextDouble() * 0.2);

                // Calculating metrics
                long views = Math.round(baseViews * Math.pow(1 + grwthRate, dayNum) * randfctr);
                long likes = Math.round(baseLikes * Math.pow(1 + grwthRate, dayNum) * randfctr);
                long comments = Math.round(baseCmmnts * Math.pow(1 + grwthRate, dayNum) * randfctr);

                dataPoint.put("views", views);
                dataPoint.put("likes", likes);
                dataPoint.put("comments", comments);
            }

            result.add(dataPoint);
            current = current.plusDays(1);
        }

        return result;
    }


    // Parse long value from API response
    private Long parseLong(Object value) {

        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }


}

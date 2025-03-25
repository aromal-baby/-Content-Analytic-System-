package com.v4.Content_analytics_system.repository.mongo;

import com.v4.Content_analytics_system.model.entity.mongo.ContentMetrics;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IContentMetricsRepository extends MongoRepository<ContentMetrics, String> {

    // Methods using existing fields only
    List<ContentMetrics> findByUserId(Long userId);

    List<ContentMetrics> findByPlatform(String platform);

    List<ContentMetrics> findByUserIdAndPlatform(Long userId, String platform);

    // Time-based queries
    List<ContentMetrics> findByRetrievalTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<ContentMetrics> findByUserIdAndRetrievalTimestampBetween(
            Long userId, LocalDateTime start, LocalDateTime end);

    // Performance metrics
    List<ContentMetrics> findTop5ByUserIdOrderByViewsDesc(Long userId);

    List<ContentMetrics> findTop5ByUserIdOrderByEngagementRateDesc(Long userId);

    Optional<ContentMetrics> findTopByPlatformContentIdOrderByRetrievalTimestampDesc(String platformContentId);

    List<ContentMetrics> findByUserIdAndRetrievalTimestampAfter(Long userId, LocalDateTime startDate);

    // Aggregate queries
    @Aggregation(pipeline = {
            "{ $match: { userId: ?0 }}",
            "{ $group: { _id:  null, totalViews:  { $sum: '$views' } } }"
    })
    Long sumViewsByUserId(Long userId);

    @Aggregation(pipeline = {
            "{ $match:  { userId:  ?0 } }",
            "{ $group:  { _id:  null, totalLikes:  { $sum:  '$likes' } } }"
    })
    Long sumLikesByUserId(Long userId);

    @Aggregation(pipeline = {
            "{ $match: { userId: ?0 } }",
            "{ $group: { _id: null, totalComments: { $sum: '$comments' } } }"
    })
    Long sumCommentsByUserId(Long userId);

    @Aggregation(pipeline = {
            "{ $match: { userId: ?0 } }",
            "{ $group: { _id: null, avgEngagementRate: { $avg: '$engagementRate' } } }"
    })
    Double averageEngagementRateByUserId(Long userId);


    // Time series data
    @Aggregation(pipeline = {
            "{ $match:  { userId:  ?0, retrievalTimestamp:  { $gte:  ?1 } } }",
            "{ $group:  { _id:  { $dateToString:  { format:  '%Y-%m-%d', date:  '$retrievalTimestamp' } }," +
                    "views: { $sum:  '$views' }," +
                    "likes: { $sum:  '$likes' }," +
                    "comments: { $sum:  '$comments' } } }",
            "{ $project: { _id: 0, date: '$_id', views: 1, likes: 1, comments: 1 } }",
            "{ $sort:  { 'date':  1 } }"
    })
    List<Map<String, Object>> getTimeSeriesData(Long userId, LocalDateTime startDate);

    // for time series data for a specific platform
    @Aggregation(pipeline = {
            "{ $match: { userId: ?0, platform: ?1, retrievalTimestamp: { $gte: ?2 } } }",
            "{ $group: { _id: { $dateToString: { format: '%Y-%m-%d', date: '$retrievalTimestamp' } }," +
                    "views: { $sum: '$views' }," +
                    "likes: { $sum: '$likes'}," +
                    "comments: { $sum: '$comments'} } }",
            "{ $project: { _id: 0, date: '$_id', views: 1, likes: 1, comments: 1 } }",
            "{ $sort: { 'date': 1 } }"
    })
    List<Map<String, Object>> getTimeSeriesDataByPlatform(Long userId, String platform, LocalDateTime startDate);

    // Platform comparison
    @Aggregation(pipeline = {
            "{ $match: { userId: ?0 } }",
            "{ $group: { _id:  '$platform', " +
                    "views: { $sum: '$views' }, " +
                    "likes: { $sum: '$likes' }, " +
                    "comments: { $sum: '$comments' }, " +
                    "engagementRate: { $avg: '$engagementRate' } } }"
    })
    List<Object[]> getMetricsByPlatform(Long userId);

    @Aggregation(pipeline = {
            "{ $match: { platform: ?0 } }",
            "{ $group: { _id: null, totalViews: { $sum: '$views' } } }",
            "{ $project: { _id: 0, totalViews: 1 } }"
    })
    long sumViewsByPlatform(String platformType);

    // Getting metrics for specific content ID
    List<ContentMetrics> findByPlatformContentId(String platformContentId);

    List<ContentMetrics> findByPlatformContentIdOrderByRetrievalTimestampAsc(String platformContentId);

}
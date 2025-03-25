package com.v4.Content_analytics_system.repository.sql;

import com.v4.Content_analytics_system.model.entity.sql.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByUserId(Long userId);
    List<Content> findByPlatformId(Long platformId);

    // To find content by both platform ID and user ID
    List<Content> findByPlatformIdAndUserId(Long platformId, Long userId);

    // To find first content by platform ID (for getting platform name)
    Optional<Content> findFirstByPlatformId(Long platformId);

    // Find by content type
    List <Content> findByContentType(Content.ContentType contentType);

    // For pagination
    Page<Content> findByUserIdOrderByPublishedDateDesc(Long userId, Pageable pageable);

    // Find published content within a date range
    List<Content> findByPublishedDateBetween(LocalDateTime start, LocalDateTime end);

    // Find content by platform type and user
    List<Content> findByPlatform_PlatformNameAndUserId(String platformName, Long userId);

    // Custom query for limited results
    @Query("SELECT c FROM Content c WHERE c.user.id = :userId ORDER BY c.publishedDate DESC")
    List<Content> findRecentContentByUserId(@Param("userId") Long userId, Pageable pageable);

    Optional<Content> findByPlatformContentId(String platformContentId);

    // Count content items by platform name
    long countByPlatform_PlatformName(String platformName);

    // Count content by platform name AND user ID
    long countByPlatform_PlatformNameAndUser_Id(String platformName, Long userId);

    // Find distinct platform names (optional if you need the list of unique platforms)
    @Query("SELECT DISTINCT p.platformName FROM Platform p")
    List<String> findDistinctPlatformNames();

    // Finding the oldest published content's date
    @Query("SELECT MIN(c.publishedDate) FROM Content c WHERE c.user.id = :userId AND c.publishedDate IS NOT NULL")
    Optional<LocalDateTime> findEarliestPublishedDateByUserId(@Param("userId") Long userId);

}

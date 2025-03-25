package com.v4.Content_analytics_system.repository.sql;

import com.v4.Content_analytics_system.model.entity.sql.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IPlatformRepository extends JpaRepository<Platform, Long> {

    List<Platform> findByUserId(Long userId);

    List<Platform> findByPlatformName(String platformName);

    List<String> findDistinctPlatformNameByUserId(Long userId);

    // Find platforms that need to be refreshed (token expiring soon)
    List<Platform> findByTokenExpiryBefore(java.time.LocalDateTime expiryTime);

    @Query("SELECT DISTINCT p.platformName FROM Platform p")
    List<String> findDistinctPlatformName();

}

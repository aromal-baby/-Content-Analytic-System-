package com.v4.Content_analytics_system.service;

import com.v4.Content_analytics_system.model.entity.sql.Content;
import com.v4.Content_analytics_system.model.entity.sql.Platform;
import com.v4.Content_analytics_system.model.entity.sql.User;
import com.v4.Content_analytics_system.repository.sql.IContentRepository;
import com.v4.Content_analytics_system.repository.sql.IPlatformRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ContentService {

    private final IContentRepository contentRepository;
    private final IPlatformRepository platformRepository;

    public ContentService(IContentRepository contentRepository, IPlatformRepository platformRepository) {
        this.contentRepository = contentRepository;
        this.platformRepository = platformRepository;
    }

    // Get all content for a user
    public List<Content> getUserContents(Long userId) {
        return contentRepository.findByUserId(userId);
    }


    // Get content for specific platform
    public List<Content> getPlatformContents(Long platformId) {
        return contentRepository.findByPlatformId(platformId);
    }


    // To add content synced with user
    public Content addContentWithUser(Long platformId, String platformContentId, String contentUrl,
                                      String contentTypeStr, String title, User user) {
        Platform platform = platformRepository.findById(platformId)
                .orElseThrow(() -> new RuntimeException("Platform not found"));

        System.out.println("Adding content for user ID: " + user.getId());

        // Check if content already exists
        Optional<Content> existingContent = contentRepository.findByPlatformContentId(platformContentId);
        if (existingContent.isPresent()) {
            return existingContent.get();
        }

        // Create new content
        Content content = new Content();
        content.setPlatform(platform);
        content.setUser(user); // Use the provided user object
        content.setPlatformContentId(platformContentId);
        content.setContentUrl(contentUrl);

        // Set the title - THIS IS THE CRITICAL FIX
        if (title != null && !title.isEmpty()) {
            content.setTitle(title);
            System.out.println("Using provided title: " + title);
        } else {
            String defaultTitle = generateDefaultTitle(contentUrl, platform.getPlatformName());
            content.setTitle(defaultTitle);
            System.out.println("Generated default title: " + defaultTitle);
        }

        // Set content type
        try {
            content.setContentType(Content.ContentType.valueOf(contentTypeStr));
        } catch (IllegalArgumentException e) {
            // Default to VIDEO if the type doesn't match enum
            content.setContentType(Content.ContentType.VIDEO);
        }

        content.setStatus(Content.ContentStatus.PUBLISHED);
        content.setPublishedDate(LocalDateTime.now());

        return contentRepository.save(content);
    }

    // for generating a default title for the user's content
    private String generateDefaultTitle(String url, String platformName) {
        return platformName + " Content - " + LocalDateTime.now();
    }
}

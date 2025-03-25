package com.v4.Content_analytics_system.controller;

import com.v4.Content_analytics_system.exception.ResourceNotFoundException;
import com.v4.Content_analytics_system.model.DTO.ContentDTO;
import com.v4.Content_analytics_system.model.entity.sql.Content;
import com.v4.Content_analytics_system.model.entity.sql.User;
import com.v4.Content_analytics_system.service.ContentService;
import com.v4.Content_analytics_system.service.PlatformService;
import com.v4.Content_analytics_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final ContentService contentService;
    private final PlatformService platformService;
    private final UserService userService;

    @Autowired
    public ContentController(ContentService contentService,
                             PlatformService platformService,
                             UserService userService) {
        this.contentService = contentService;
        this.platformService = platformService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Content> createContent(@RequestBody ContentDTO contentDTO,
                                                 Authentication authentication) {
        try {
            System.out.println("Received content data: " + contentDTO);

            // Validate required fields
            if (contentDTO.getPlatformId() == null) {
                System.err.println("Missing platformId");
                return ResponseEntity.badRequest().build();
            }

            if (contentDTO.getPlatformContentId() == null || contentDTO.getPlatformContentId().isEmpty()) {
                System.err.println("Missing platformContentId");
                return ResponseEntity.badRequest().build();
            }

            // Get authenticated user
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            System.out.println("Adding content for user: " + username + " (ID: " + user.getId() + ")");

            // Use default contentType if missing
            String contentType = contentDTO.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "VIDEO";
            }

            // Handle the title - don't pass "null" as a string
            String title = contentDTO.getTitle();
            // If title is "null" (the string) or empty, set it to null to trigger default title generation
            if (title != null && (title.equals("null") || title.isEmpty())) {
                title = null;
            }

            System.out.println("Title being passed to service: " + (title != null ? title : "null - will use default"));

            // Use the addContentWithUser method that directly accepts a User object
            Content content = contentService.addContentWithUser(
                    contentDTO.getPlatformId(),
                    contentDTO.getPlatformContentId(),
                    contentDTO.getUrl(),
                    contentType,
                    title,  // Pass fixed title
                    user    // Pass the user object directly
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(content);
        } catch (Exception e) {
            System.err.println("Error creating content: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Content>> getUserContent(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ResponseEntity.ok(contentService.getUserContents(user.getId()));
    }

    @GetMapping("/platform/{platformId}")
    public ResponseEntity<List<Content>> getPlatformContent(@PathVariable Long platformId) {
        return ResponseEntity.ok(contentService.getPlatformContents(platformId));
    }
}
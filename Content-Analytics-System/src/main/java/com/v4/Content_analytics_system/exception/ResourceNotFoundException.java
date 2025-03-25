package com.v4.Content_analytics_system.exception;

// Custom exception classes
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

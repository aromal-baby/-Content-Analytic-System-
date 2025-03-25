package com.v4.Content_analytics_system.model.DTO;

public class ContentDTO {

    private String url;
    private String platformContentId;
    private String contentType;
    private Long platformId;
    private String title;
    private String description;


    // GETTERS & SETTERS
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPlatformContentId() {
        return platformContentId;
    }

    public void setPlatformContentId(String platformContentId) {
        this.platformContentId = platformContentId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getPlatformId() {
        return platformId;
    }

    public void setPlatformId(Long platformId) {
        this.platformId = platformId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ContentDTO{" +
                "platformId=" + platformId +
                ", platformContentId='" + platformContentId + '\'' +
                ", contentType='" + contentType + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}

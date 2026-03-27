package com.group.nasaspaceexplorer.model;

import java.io.Serializable;

public class ApodData implements Serializable {
    private String title;
    private String date;
    private String explanation;
    private String url;
    private String hdUrl;
    private String mediaType;
    private String copyright;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getHdUrl() { return hdUrl; }
    public void setHdUrl(String hdUrl) { this.hdUrl = hdUrl; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getCopyright() { return copyright; }
    public void setCopyright(String copyright) { this.copyright = copyright; }

    public boolean isImage() {
        return "image".equals(mediaType);
    }
}

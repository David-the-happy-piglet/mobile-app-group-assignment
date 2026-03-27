package com.group.nasaspaceexplorer.model;

import java.io.Serializable;

public class EpicPhoto implements Serializable {
    private String identifier;
    private String caption;
    private String image;
    private String date;       // "2026-03-25 00:13:03"
    private double lat;
    private double lon;

    // 图片 URL 需要从 date 字段解析出 year/month/day 来拼接
    // 格式：https://epic.gsfc.nasa.gov/archive/natural/YYYY/MM/DD/jpg/{image}.jpg
    public String getImageUrl() {
        if (date == null || date.length() < 10) return "";
        String[] parts = date.substring(0, 10).split("-");
        if (parts.length < 3) return "";
        return "https://epic.gsfc.nasa.gov/archive/natural/"
                + parts[0] + "/" + parts[1] + "/" + parts[2]
                + "/jpg/" + image + ".jpg";
    }

    public String getDateOnly() {
        if (date == null || date.length() < 10) return "";
        return date.substring(0, 10);
    }

    public String getTimeOnly() {
        if (date == null || date.length() < 19) return "";
        return date.substring(11, 19) + " UTC";
    }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }
}
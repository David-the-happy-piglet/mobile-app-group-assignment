package com.group.nasaspaceexplorer.model;

import java.io.Serializable;

public class RoverPhoto implements Serializable {
    private int id;
    private int sol;
    private String imgSrc;
    private String earthDate;
    private String cameraName;
    private String cameraFullName;
    private String roverName;
    private String roverStatus;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSol() { return sol; }
    public void setSol(int sol) { this.sol = sol; }

    public String getImgSrc() { return imgSrc; }
    public void setImgSrc(String imgSrc) { this.imgSrc = imgSrc; }

    public String getEarthDate() { return earthDate; }
    public void setEarthDate(String earthDate) { this.earthDate = earthDate; }

    public String getCameraName() { return cameraName; }
    public void setCameraName(String cameraName) { this.cameraName = cameraName; }

    public String getCameraFullName() { return cameraFullName; }
    public void setCameraFullName(String cameraFullName) { this.cameraFullName = cameraFullName; }

    public String getRoverName() { return roverName; }
    public void setRoverName(String roverName) { this.roverName = roverName; }

    public String getRoverStatus() { return roverStatus; }
    public void setRoverStatus(String roverStatus) { this.roverStatus = roverStatus; }
}

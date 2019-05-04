package com.masirg.orientation.Domain;

import android.annotation.SuppressLint;

public class Track {
    private long trackId;
    private long creationTime;
    private String description;
    private double totalDistance;
    private long totalTime;

    public Track(long creationTime) {
        this.trackId = 0;
        this.creationTime = creationTime;
        this.description = null;
        this.totalDistance = -1;
        this.totalTime = -1;
    }

    public Track(long trackId, long creationTime) {
        this.trackId = trackId;
        this.creationTime = creationTime;
        this.description = null;
        this.totalDistance = -1;
        this.totalTime = -1;
    }

    public Track(long trackId, long creationTime, String description, double totalDistance, long totalTime) {
        this.trackId = trackId;
        this.creationTime = creationTime;
        this.description = description;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
    }

    //======Getters and setters======


    public long getTrackId() {
        return trackId;
    }

    public Track setTrackId(long trackId) {
        this.trackId = trackId;
        return this;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public Track setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Track setDescription(String description) {
        this.description = description;
        return this;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public Track setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
        return this;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public Track setTotalTime(long totalTime) {
        this.totalTime = totalTime;
        return this;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("Track - Id: %d, CreatedTime: %d, Distance: %f", this.trackId, this.creationTime, this.totalDistance);
    }
}

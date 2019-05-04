package com.masirg.orientation.Domain;

public class TrackCheckpoint {
    private long trackCheckpointId;
    private long trackId;
    private double latitude;
    private double longitude;
    private double altitude;
    private long time;

    public TrackCheckpoint(long trackId, double latitude, double longitude, double altitude, long time) {
        this.trackCheckpointId = 0;
        this.trackId = trackId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.time = time;
    }

    public TrackCheckpoint(long trackCheckpointId, long trackId, double latitude, double longitude, double altitude, long time) {
        this.trackCheckpointId = trackCheckpointId;
        this.trackId = trackId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.time = time;
    }

    // ==================Getters and setters==================

    public long getTrackCheckpointId() {
        return trackCheckpointId;
    }

    public TrackCheckpoint setTrackCheckpointId(long trackCheckpointId) {
        this.trackCheckpointId = trackCheckpointId;
        return this;
    }

    public long getTrackId() {
        return trackId;
    }

    public TrackCheckpoint setTrackId(long trackId) {
        this.trackId = trackId;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public TrackCheckpoint setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public TrackCheckpoint setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getAltitude() {
        return altitude;
    }

    public TrackCheckpoint setAltitude(double altitude) {
        this.altitude = altitude;
        return this;
    }

    public long getTime() {
        return time;
    }

    public TrackCheckpoint setTime(long time) {
        this.time = time;
        return this;
    }
}

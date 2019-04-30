package com.masirg.orientation.Domain;

public class TrackPoint {
    private long trackPointId;
    private long trackId;
    private double latitude;
    private double longitude;
    private double altitude;
    private long time;

    public TrackPoint(long trackId, double latitude, double longitude, double altitude, long time) {
        this.trackPointId = 0;
        this.trackId = trackId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.time = time;
    }

    public TrackPoint(long trackPointId, long trackId, double latitude, double longitude, double altitude, long time) {
        this.trackPointId = trackPointId;
        this.trackId = trackId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.time = time;
    }

    // ==================Getters and setters==================

    public long getTrackPointId() {
        return trackPointId;
    }

    public TrackPoint setTrackPointId(long trackPointId) {
        this.trackPointId = trackPointId;
        return this;
    }

    public long getTrackId() {
        return trackId;
    }

    public TrackPoint setTrackId(long trackId) {
        this.trackId = trackId;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public TrackPoint setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public TrackPoint setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getAltitude() {
        return altitude;
    }

    public TrackPoint setAltitude(double altitude) {
        this.altitude = altitude;
        return this;
    }

    public long getTime() {
        return time;
    }

    public TrackPoint setTime(long time) {
        this.time = time;
        return this;
    }
}

package com.itcollege.radio2019.Domain;

public class Artist {
    private int artistId;
    private String artistName;
    private int stationId;

    public Artist(String artistName, int stationId) {
        this(0,artistName, stationId);
    }

    public Artist(int artistId, String artistName, int stationId) {
        this.artistId = artistId;
        this.artistName = artistName;
        this.stationId = stationId;
    }

    //-----------------------Getters and setters-----------------------
    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }
}

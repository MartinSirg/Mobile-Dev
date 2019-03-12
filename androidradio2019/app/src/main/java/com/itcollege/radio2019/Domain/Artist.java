package com.itcollege.radio2019.Domain;

import java.util.Hashtable;

public class Artist {
    private int artistId;
    private String artistName;
    private int stationId;
    public Hashtable<String, Integer> uniqueSongs;

    public Artist(String artistName, int stationId) {
        this(0,artistName, stationId);
    }

    public Artist(int artistId, String artistName, int stationId) {
        this.artistId = artistId;
        this.artistName = artistName;
        this.stationId = stationId;
    }

    public int getTimesPlayed() {
        int counter = 0;
        if (uniqueSongs != null) {
            for (Integer value : uniqueSongs.values()) counter += value;
        }
        return counter;
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

    public Hashtable<String, Integer> getUniqueSongs() {
        return uniqueSongs;
    }

    public void setUniqueSongs(Hashtable<String, Integer> uniqueSongs) {
        this.uniqueSongs = uniqueSongs;
    }
}

package com.itcollege.radio2019.Domain;


import java.io.Serializable;

public class Station implements Serializable
{
    private int stationId;
    private String name;
    private String streamUrl;
    private String songNameApiUrl;

    public Station(String name, String streamUrl, String songNameApiUrl) {
        this(0, name, streamUrl, songNameApiUrl);
    }

    public Station(int id, String name, String streamUrl, String songNameApiUrl) {
        this.stationId = id;
        this.name = name;
        this.streamUrl = streamUrl;
        this.songNameApiUrl = songNameApiUrl;
    }


    @Override
    public String toString() {
        return name;
    }

    //-----------------------Getters and setters-----------------------

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this.streamUrl = streamUrl;
    }

    public String getSongNameApiUrl() {
        return songNameApiUrl;
    }

    public void setSongNameApiUrl(String songNameApiUrl) {
        this.songNameApiUrl = songNameApiUrl;
    }
}

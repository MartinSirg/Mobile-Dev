package com.itcollege.radio2019.Domain;

import java.text.DateFormat;
import java.util.Calendar;

public class SongPlayed {
    private int songPlayedId;
    private String songTitle;
    private int artistId;
    private int timePlayedAt;

    public SongPlayed(String songTitle, int artistId, int timePlayedAt) {
        this(0, songTitle, artistId, timePlayedAt);
    }

    public SongPlayed(int songPlayedId, String songTitle, int artistId, int timePlayedAt) {
        this.songPlayedId = songPlayedId;
        this.songTitle = songTitle;
        this.artistId = artistId;
        this.timePlayedAt = timePlayedAt;
    }

    //-----------------------Getters and setters-----------------------

    public int getSongPlayedId() {
        return songPlayedId;
    }

    public void setSongPlayedId(int songPlayedId) {
        this.songPlayedId = songPlayedId;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public int getTimePlayedAt() {
        return timePlayedAt;
    }

    public void setTimePlayedAt(int timePlayedAt) {
        this.timePlayedAt = timePlayedAt;
    }

    @Override
    public String toString() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis( (long) timePlayedAt * 1000);
        return String.format("%s - %s by %d", DateFormat.getDateTimeInstance().format(c.getTime()), songTitle, artistId );
    }
}

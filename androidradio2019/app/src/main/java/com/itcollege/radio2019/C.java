package com.itcollege.radio2019;

public final class C {
    public static final String SERVICE_STATION_ID_KEY = "serviceStationId";
    public static final String SERVICE_STATION_NAME_KEY = "serviceStationName";
    public static final String SERVICE_STATION_STREAM_URL_KEY = "serviceStationStreamUrl";
    public static final String SERVICE_STATION_SONGS_API_URL_KEY = "serviceStationSongsApiUrl";
    public static final String SERIALIZABLE_STATIONS = "stations";

    static private final String prefix = "com.itcollege.radio2019.";
    static public final String SERVICE_MEDIASOURCE_KEY = prefix + "SERVICE_MEDIASOURCE_KEY";

    // Mediaplayer statuses
    static public final int MUSICSERVICE_STOPPED = 0;
    static public final int MUSICSERVICE_BUFFERING = 1;
    static public final int MUSICSERVICE_PLAYING = 2;

    // Mediaplayer to Activity broadcast intent messages
    static public final String MUSICSERVICE_INTENT_PLAYING = prefix + "MUSICSERVICE_INTENT_PLAYING";
    static public final String MUSICSERVICE_INTENT_BUFFERING = prefix + "MUSICSERVICE_INTENT_BUFFERING";
    static public final String MUSICSERVICE_INTENT_STOPPED = prefix + "MUSICSERVICE_INTENT_STOPED";

    static public final String MUSICSERVICE_INTENT_SONGINFO = prefix + "MUSICSERVICE_INTENT_SONGINFO";

    static public final String MUSICSERVICE_ARTIST = prefix + "MUSICSERVICE_ARTIST";
    static public final String MUSICSERVICE_TRACKTITLE = prefix + "MUSICSERVICE_TRACKTITLE";

    // Activity to Mediaplayer broadcast intent messages
    static public final String ACTIVITY_INTENT_STARTMUSIC = prefix + "ACTIVITY_INTENT_STARTMUSIC";
    static public final String ACTIVITY_INTENT_STOPPMUSIC = prefix + "ACTIVITY_INTENT_STOPPMUSIC";

    // Main activity PLAY button labels
    static public final String BUTTONCONTROLMUSIC_LABEL_PLAYING = "STOP";
    static public final String BUTTONCONTROLMUSIC_LABEL_BUFFERING = "BUFFERING";
    static public final String BUTTONCONTROLMUSIC_LABEL_STOPPED = "PLAY";

    //
    static public final String MUSICSERVICE_VOLLEYTAG= prefix + "MUSICSERVICE_VOLLEYTAG";

}


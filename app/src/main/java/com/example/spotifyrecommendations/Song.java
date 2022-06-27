package com.example.spotifyrecommendations;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

@ParseClassName("Song")
public class Song extends ParseObject {

    public static final String KEY_ARTIST = "Artist";
    //public static final String KEY_LENGTH = "Length";
    //public static final String KEY_LIKE = "Like";
    public static final String KEY_SPOTIFYID = "spotifyId";
    public static final String KEY_NAME = "Name";




    public String getSpotifyid(){
        return getString(KEY_SPOTIFYID);
    }

    public void setSpotifyid(String id){
        put(KEY_SPOTIFYID, id);
    }

    public String getArtist(){
        return getString(KEY_ARTIST);
    }

    public void setArtist(String artist){
        put(KEY_ARTIST, artist);
    }

    public String getName(){
        return getString(KEY_NAME);
    }

    public void setName(String name){
        put(KEY_NAME, name);
    }



}

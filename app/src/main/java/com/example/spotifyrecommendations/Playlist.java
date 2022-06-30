package com.example.spotifyrecommendations;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;


import java.io.Serializable;
import java.util.List;

@ParseClassName("Playlist")
public class Playlist extends ParseObject implements Serializable {

    public static final String KEY_SONGS = "Songs";
    public static final String KEY_AUTHOR = "Author";
    public static final String KEY_LENGTH = "Length";
    public static final String KEY_LIKE = "Like";
    public static final String KEY_SPOTIFYID = "SpotifyId";
    public static final String KEY_NAME = "Name";
    public static final String KEY_URI = "URI";
    public static final String KEY_OBJECT_ID = "ObjectId";



    public List<String> getSongs() {
        return getList(KEY_SONGS);
    }

    public String getPlaylistId(){
        return getObjectId();
    }

    public void setSongs(List<String> songs){
        put(KEY_SONGS, songs);
    }

    public ParseUser getUser(){
        return getParseUser(KEY_AUTHOR);
    }

    public void setUser(ParseUser user){
        put(KEY_AUTHOR, user);
    }

    public int getLength(){
        return getInt(KEY_LENGTH);
    }

    public void setLength(int length){
        put(KEY_AUTHOR, length);
    }

    public Boolean getLike(){
        return getBoolean(KEY_LIKE);
    }

    public void setLike(Boolean like){
        put(KEY_LIKE, like);
    }


    public String getSpotifyid(){
        return getString(KEY_SPOTIFYID);
    }

    public void setSpotifyid(String id){
        put(KEY_SPOTIFYID, id);
    }

    public String getName(){
        return getString(KEY_NAME);
    }

    public void setName(String name){
        put(KEY_NAME, name);
    }

    public String getURI(){
        return getString(KEY_URI);
    }

    public void setURI(String uri){
        put(KEY_URI, uri);
    }



}

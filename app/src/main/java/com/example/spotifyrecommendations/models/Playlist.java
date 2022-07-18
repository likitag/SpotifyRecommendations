package com.example.spotifyrecommendations.models;

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
    public static final String KEY_OBJECT_ID = "objectId";
    public static final String KEY_VALENCE = "Valence";
    public static final String KEY_TEMPO = "Tempo";
    public static final String KEY_GENRE = "Genre";
    public static final String KEY_ARTISTID = "ArtistID";
    public static final String KEY_TRACKID = "TrackID";
    public static final String KEY_RATED = "Rated";



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

    public String getGenre(){
        return getString(KEY_GENRE);
    }

    public void setGenre(String genre){
        put(KEY_GENRE, genre);
    }

    public String getValence(){
        return getString(KEY_VALENCE);
    }

    public void setValence(String valence){
        put(KEY_VALENCE, valence);
    }

    public String getTempo(){
        return getString(KEY_TEMPO);
    }

    public void setTempo(String tempo){
        put(KEY_TEMPO, tempo);
    }

    public String getArtistID(){
        return getString(KEY_ARTISTID);
    }

    public void setArtistID(String id){
        put(KEY_ARTISTID, id);
    }

    public String getTrackID(){
        return getString(KEY_TRACKID);
    }

    public void setTrackID(String id){
        put(KEY_TRACKID, id);
    }

    public Boolean getRated(){
        return getBoolean(KEY_RATED);
    }

    public void setRated(Boolean bool){
        put(KEY_RATED, bool);
    }



}

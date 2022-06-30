package com.example.spotifyrecommendations;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_DESCRIPTION = "Description";
    public static final String KEY_AUTHOR = "Author";
    public static final String KEY_IMAGE = "Image";
    public static final String KEY_LIKES = "Likes";
    public static final String KEY_SAVES = "Saves";
    public static final String KEY_PLAYLIST = "Playlist";




    public ParseUser getUser(){
        return getParseUser(KEY_AUTHOR);
    }

    public ParseFile getImage(){
        return getParseFile(KEY_IMAGE);

    }

    public void setImage(ParseFile image){
        put(KEY_IMAGE, image);
    }

    public void setUser(ParseUser user){
        put(KEY_AUTHOR, user);
    }

    public String getDescription(){
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description){
        put(KEY_DESCRIPTION, description);
    }






    public Integer getLikes(){
        return getInt(KEY_LIKES);
    }

    public void setLikes(int likes){
        put(KEY_LIKES, likes);
    }

    public Integer getSaves(){
        return getInt(KEY_SAVES);
    }

    public void setSaves(int saves){
        put(KEY_SAVES, saves);
    }

    public ParseObject getPlaylist(){
        return getParseObject(KEY_PLAYLIST);
    }

    public void setPlaylist(Playlist playlist){
        put(KEY_PLAYLIST, playlist);
    }









}

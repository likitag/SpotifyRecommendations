package com.example.spotifyrecommendations.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_DESCRIPTION = "Description";
    public static final String KEY_AUTHOR = "Author";
    public static final String KEY_IMAGE = "Image";
    public static final String KEY_LIKES = "Likes";
    public static final String KEY_SAVES = "Saves";
    public static final String KEY_PLAYLIST = "Playlist";
    public static final String KEY_PLAYLISTURI = "PlaylistURI";
    public static final String KEY_PLAYLISTID = "PlaylistID";
    public static final String KEY_COVER = "Cover";




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

    public String getPlaylistURI(){
        return getString(KEY_PLAYLISTURI);
    }

    public void setPlaylistURI(String uri){
        put(KEY_PLAYLISTURI, uri);
    }

    public String getPlaylistID(){
        return getString(KEY_PLAYLISTID);
    }

    public void setPlaylistID(String id){
        put(KEY_PLAYLISTID, id);
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

    public String getCover(){
        return getString(KEY_COVER);
    }

    public void setCover(String url){
        put(KEY_COVER, url);
    }









}

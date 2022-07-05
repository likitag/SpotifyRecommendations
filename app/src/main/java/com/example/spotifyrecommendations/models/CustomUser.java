package com.example.spotifyrecommendations.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;


import org.json.JSONArray;

import java.io.Serializable;
import java.util.List;

import spotify.models.users.User;


public class CustomUser implements Serializable {

    public static final String KEY_USER = "User";
    public static final String KEY_FAVORITES = "favorites";

    ParseUser user;



    public JSONArray getFavorites(){
        return user.getJSONArray(KEY_FAVORITES);
    }


    public void setFavorites(JSONArray favorites){
        user.put(KEY_FAVORITES, favorites);
    }



}

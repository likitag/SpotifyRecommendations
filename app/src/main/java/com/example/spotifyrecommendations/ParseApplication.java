package com.example.spotifyrecommendations;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Playlist.class);
        ParseObject.registerSubclass(Song.class);
        ParseObject.registerSubclass(Post.class);

         Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("sTfGkOZi0O5umnTeELENYSoABkpsROsDBWfit6nr")
                .clientKey("2hFIFOfcuFSKT0BzYpJcyJFS2lI9pCs8mjVJemQH")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}

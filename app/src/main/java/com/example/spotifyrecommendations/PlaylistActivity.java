package com.example.spotifyrecommendations;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.spotifyrecommendations.adapters.PlaylistAdapter;
import com.example.spotifyrecommendations.models.Song;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {
    private static final String TAG = "PlaylistActivity";
    RecyclerView rvSongs;
    protected PlaylistAdapter adapter;
    protected List<Song> allSongs;
    String spot_id;
    //BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        rvSongs = findViewById(R.id.rvSongs);
        allSongs = new ArrayList<>();
        adapter = new PlaylistAdapter(this, allSongs);

        rvSongs.setAdapter(adapter);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));


        Intent i = getIntent();
        Bundle b = i.getExtras();

        if(b!=null)
        {
            Log.i(TAG, "bundle not null");
            spot_id = (String) b.get("spot id");
            Log.i(TAG, spot_id);
        }

        querySongs();

    }
    private void querySongs() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Song> query = ParseQuery.getQuery(Song.class);
        query.whereEqualTo(Song.KEY_PLAYLISTOBJ, spot_id);
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Song>() {
            @Override
            public void done(List<Song> songs, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e("TAG", "Issue with getting posts", e);
                    return;
                }

                // for debugging purposes let's print every post description to logcat

                // save received posts to list and notify adapter of new data
                allSongs.addAll(songs);
                adapter.notifyDataSetChanged();
            }
        });
    }



}
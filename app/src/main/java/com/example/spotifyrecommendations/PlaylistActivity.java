package com.example.spotifyrecommendations;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.spotifyrecommendations.fragments.ComposeFragment;
import com.example.spotifyrecommendations.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {
    RecyclerView rvSongs;
    protected PlaylistAdapter adapter;
    protected List<Song> allSongs;
    //BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        rvSongs = findViewById(R.id.rvSongs);
        allSongs = new ArrayList<>();
        adapter = new PlaylistAdapter(this, allSongs);
        //bottomNavigationView = findViewById(R.id.bottomNavigation);

        rvSongs.setAdapter(adapter);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        queryPosts();

    }
    private void queryPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Song> query = ParseQuery.getQuery(Song.class);


        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Song>() {
            @Override
            public void done(List<Song> posts, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e("TAG", "Issue with getting posts", e);
                    return;
                }

                // for debugging purposes let's print every post description to logcat


                // save received posts to list and notify adapter of new data
                allSongs.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }



}
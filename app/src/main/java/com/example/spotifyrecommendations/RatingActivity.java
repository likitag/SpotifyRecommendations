package com.example.spotifyrecommendations;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifyrecommendations.models.Playlist;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.Map;

public class RatingActivity extends AppCompatActivity {
    private static final String TAG = "Rating activity";
    TextView tvLike;
    TextView tvDislike;
    ImageButton ibLike;
    ImageButton ibDislike;
    String playlist_id;
    String token;
    Map<String, String> options = new HashMap<>();
    Playlist playlist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rating);
        Intent i = getIntent();
        Bundle b = i.getExtras();


        if(b!=null)
        {
            Log.i(TAG, "bundle not null");
            playlist = (Playlist) getIntent().getSerializableExtra("new playlist");
            playlist_id = (String) b.get("new playlist id");
            Log.i(TAG, playlist.getName());
            Log.i(TAG, playlist_id);
            token =(String) b.get("token");


        }
        tvLike = findViewById(R.id.tvLike);
        tvDislike = findViewById(R.id.tvDislike);
        ibLike = findViewById(R.id.ibLike);
        ibDislike = findViewById(R.id.ibDislike);

        ibLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RatingActivity.this, "User likes playlist!", Toast.LENGTH_SHORT).show();
//                playlist.setLike(true);
//                playlist.saveInBackground();
                updatePlaylist(true);
                finish();

                //TODO: create functionality for if user liked the playlist
            }
        });



        ibDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RatingActivity.this, "User dislikes playlist", Toast.LENGTH_SHORT).show();
                updatePlaylist(false);
                finish();
                //TODO: create functionality for if user disliked the playlist
            }
        });

    }

    private void updatePlaylist(Boolean like)  {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Playlist");
        Log.i(TAG, query.getClassName());

        Log.i(TAG, "playlist id " + playlist_id);

        query.getInBackground(playlist_id, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    Log.i(TAG, "here!");
                    object.put("Like", like);
                    object.saveInBackground();
                } else {
                    Log.i(TAG, "sad");
                    Log.e(TAG, "something went wrong...", e);
                }

            }
        });



        Log.i(TAG, "done updating");

        //TODO: update like/dislike field in parse db


    }

}
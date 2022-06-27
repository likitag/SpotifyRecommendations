package com.example.spotifyrecommendations;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class RatingActivity extends AppCompatActivity {
    TextView tvLike;
    TextView tvDislike;
    ImageButton ibLike;
    ImageButton ibDislike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        tvLike = findViewById(R.id.tvLike);
        tvDislike = findViewById(R.id.tvDislike);
        ibLike = findViewById(R.id.ibLike);
        ibDislike = findViewById(R.id.ibDislike);

        ibLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RatingActivity.this, "User likes playlist!", Toast.LENGTH_SHORT).show();
                //TODO: create functionality for if user liked the playlist
            }
        });

        ibDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RatingActivity.this, "User dislikes playlist", Toast.LENGTH_SHORT).show();
                //TODO: create functionality for if user disliked the playlist
            }
        });

    }
}
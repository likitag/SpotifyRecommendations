package com.example.spotifyrecommendations.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.spotifyrecommendations.R;


public class ComposeFragment extends Fragment {

    EditText etLength;
    EditText etArtists;
    EditText etGenres;
    EditText etTracks;
    Button btnSubmit;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etLength = view.findViewById(R.id.etLength);
        etArtists = view.findViewById(R.id.etArtists);
        etGenres = view.findViewById(R.id.etGenres);
        etTracks= view.findViewById(R.id.etTracks);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int playlist_length = Integer.parseInt(etLength.getText().toString());
                String artist_seeds = etArtists.getText().toString();
                String genre_seeds = etGenres.getText().toString();
                String track_seeds = etGenres.getText().toString();
            }
        });




    }
}
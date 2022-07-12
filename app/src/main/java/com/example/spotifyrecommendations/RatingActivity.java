package com.example.spotifyrecommendations;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.spotifyrecommendations.models.Playlist;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spotify.api.spotify.SpotifyApi;
import spotify.models.audio.AudioFeatures;
import spotify.models.generic.AbstractPlayableObject;
import spotify.models.playlists.PlaylistItem;
import spotify.models.playlists.PlaylistTrack;
import spotify.models.playlists.requests.DeleteItemsPlaylistRequestBody;
import spotify.models.recommendations.RecommendationCollection;

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

    String spotify_playlist_id;
    LottieAnimationView music_load;


    TextView tvMood;
    TextView tvTempo;
    SeekBar seekBarMood;
    SeekBar seekBarTempo;

    TextView tvTooSlow;
    TextView tvTooFast;
    TextView tvTooSad;
    TextView tvTooHappy;

    TextView tvTitle;

    Button btnUpdate;
    Button btnKeep;

    int seekMood;
    int seekTempo;

    List<String> listArtistId = new ArrayList<>();
    List<String> listTrackId = new ArrayList<>();
    List<String> listGenres = new ArrayList<>();


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
            spotify_playlist_id = (String) b.get("spotify playlist id");
            Log.i(TAG, playlist.getName());
            Log.i(TAG, playlist_id);
            token =(String) b.get("token");

            listArtistId = getIntent().getStringArrayListExtra("listArtists");
            listTrackId = getIntent().getStringArrayListExtra("listTracks");
            listGenres = getIntent().getStringArrayListExtra("listGenres");


        }
//        tvLike = findViewById(R.id.tvLike);
//        tvDislike = findViewById(R.id.tvDislike);
//        ibLike = findViewById(R.id.ibLike);
//        ibDislike = findViewById(R.id.ibDislike);



        tvMood = findViewById(R.id.tvMood);
        tvTempo = findViewById(R.id.tvTempo);
        seekBarMood = findViewById(R.id.seekBarMood);
        seekBarTempo = findViewById(R.id.seekBarTempo);

        tvTooHappy = findViewById(R.id.tvTooHappy);
        tvTooSad = findViewById(R.id.tvTooSad);
        tvTooSlow = findViewById(R.id.tvTooSlow);
        tvTooFast = findViewById(R.id.tvTooFast);

        tvTitle = findViewById(R.id.tvTitle);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnKeep = findViewById(R.id.btnKeep);
        music_load = findViewById(R.id.lottie_music_load);
        music_load.setVisibility(View.GONE);

        btnKeep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(RatingActivity.this, "added", Toast.LENGTH_SHORT).show();
                RatingActivity.this.finish();
            }
        });

        music_load.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                music_load.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {
                music_load.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                seekMood = seekBarMood.getProgress();
                Log.i(TAG, "onClick: " + seekMood);
                seekTempo = seekBarTempo.getProgress();

                tvMood.setVisibility(View.GONE);
                tvTempo.setVisibility(View.GONE);
                seekBarMood.setVisibility(View.GONE);
                seekBarTempo.setVisibility(View.GONE);
                tvTitle.setVisibility(View.GONE);

                tvTooHappy.setVisibility(View.GONE);
                tvTooSad.setVisibility(View.GONE);
                tvTooSlow.setVisibility(View.GONE);
                tvTooFast.setVisibility(View.GONE);

                btnUpdate.setVisibility(View.GONE);
                btnKeep.setVisibility(View.GONE);

                music_load.setVisibility(View.VISIBLE);
                music_load.playAnimation();
                new updatePlaylistItems().execute();

            }
        });

//        ibLike.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(RatingActivity.this, "User likes playlist!", Toast.LENGTH_SHORT).show();
////                playlist.setLike(true);
////                playlist.saveInBackground();
//                updatePlaylist(true);
//                finish();
//
//                //TODO: create functionality for if user liked the playlist
//            }
//        });
//
//
//
//        ibDislike.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(RatingActivity.this, "User dislikes playlist", Toast.LENGTH_SHORT).show();
//                updatePlaylist(false);
//                finish();
//                //TODO: create functionality for if user disliked the playlist
//            }
//        });
//
//    }


//    private void updatePlaylist(Boolean like)  {
//        ParseQuery<ParseObject> query = ParseQuery.getQuery("Playlist");
//        Log.i(TAG, query.getClassName());
//
//        Log.i(TAG, "playlist id " + playlist_id);
//
//        query.getInBackground(playlist_id, new GetCallback<ParseObject>() {
//            @Override
//            public void done(ParseObject object, ParseException e) {
//                if (e == null) {
//                    Log.i(TAG, "here!");
//                    object.put("Like", like);
//                    object.saveInBackground();
//                } else {
//                    Log.i(TAG, "sad");
//                    Log.e(TAG, "something went wrong...", e);
//                }
//
//            }
//        });
//
//
//
//        Log.i(TAG, "done updating");
//
//
//
    }

    private class updatePlaylistItems extends AsyncTask<URL, Integer, Long> {

        int num_deleted;
        List<PlaylistItem> items_to_delete = new ArrayList<>();

        @Override
        protected Long doInBackground(URL... urls) {
            SpotifyApi spotifyApi = new SpotifyApi(token);

            Log.i(TAG, "initial num songs: " + spotifyApi.getPlaylist(spotify_playlist_id, options).getTracks().getItems().size());
            List<PlaylistTrack> tracks = spotifyApi.getPlaylist(spotify_playlist_id, options).getTracks().getItems();

            //TODO: get the current average tempo of the playlist

            //TODO: get current average tempo

            float sum_tempo = 0;
            for (int i = 0; i < tracks.size(); i++){
                AbstractPlayableObject track = tracks.get(i).getTrack();
                String trackId = track.getId();
                AudioFeatures audioFeatures= spotifyApi.getTrackAudioFeatures(trackId);
                sum_tempo = sum_tempo + audioFeatures.getTempo();
            }

            float avg_tempo = sum_tempo / tracks.size();
            Log.i(TAG, "initial playlist avg tempo: " + avg_tempo);

            for (int i = 0; i < tracks.size(); i++){

                AbstractPlayableObject track = tracks.get(i).getTrack();
                int[] intArray = new int[1];
                intArray[0]= i;
                PlaylistItem item = new PlaylistItem(track.getUri(), intArray);
                Log.i(TAG, "item: " + item.getUri());

                String trackId = track.getId();
                AudioFeatures audioFeatures= spotifyApi.getTrackAudioFeatures(trackId);

                //indicates the instrumentalness of a track (0.5 and above is intrumental)
                float tempo = audioFeatures.getTempo();
                Log.i(TAG, "tempo: " + tempo);

                //TODO: remove all songs that are less than / greater than average tempo

                if ((seekTempo == 0 && tempo < avg_tempo) || (seekTempo == 2 && tempo > avg_tempo)){

                    //deletes a song from playlist if it doesn't satisfy mood requirements
                    items_to_delete.add(item);
                    Log.i(TAG, "wish to delete: " + item.getUri());
                    num_deleted++;
                }

                //indicates the happiness of a track (0 = sad, 1= happy)
                float valence = audioFeatures.getValence();
                Log.i(TAG, "valence: " + valence);

                if ((seekMood == 0 && valence < 0.2) || (seekMood == 2 && valence > 0.8)){

                    //deletes a song from playlist if it doesn't satisfy mood requirements
                    items_to_delete.add(item);
                    Log.i(TAG, "wish to delete: " + item.getUri());
                    num_deleted++;
                }
            }


            Log.i(TAG, "num items to delete: " + items_to_delete.size());

            DeleteItemsPlaylistRequestBody requestBody = new DeleteItemsPlaylistRequestBody(items_to_delete, spotifyApi.getPlaylist(spotify_playlist_id, options).getSnapshotId());
            
            //delete all the songs from the playlist that do not meet the user's mood expectations 
            spotifyApi.deleteItemsFromPlaylist(spotify_playlist_id, requestBody);
            Log.i(TAG, "num songs after deleting: " + spotifyApi.getPlaylist(spotify_playlist_id, options).getTracks().getItems().size());



            Map<String, String> rec_extra = new HashMap<>();
            if (seekMood == 0){
                rec_extra.put("min_valence", "0.3");
            }
            else if (seekMood == 2){
                rec_extra.put("max_valence", "0.7");
            }


            if (seekTempo == 0){
                rec_extra.put("min_tempo", Float.toString(avg_tempo + 10));
            }
            else if (seekTempo == 2){
                rec_extra.put("max_tempo", Float.toString(avg_tempo - 10));
            }



            if (num_deleted !=0) {

                rec_extra.put("limit", Integer.toString(num_deleted));

                Log.i(TAG, "artist:  " + listArtistId.get(0));
                Log.i(TAG, "track:  " + listTrackId.get(0));
                Log.i(TAG, "genre:  " + listGenres.get(0));

                //TODO: get song recommendations that are +10/-10 greater than the average tempo based on if the user thought it was too fast/too slow
                RecommendationCollection recommendations = spotifyApi.getRecommendations(listArtistId, listGenres, listTrackId, rec_extra);

                List<String> uris = new ArrayList<>();
                for (int i = 0; i < recommendations.getTracks().size(); i++) {
                    String uri = spotifyApi.getTrack(recommendations.getTracks().get(i).getId(), options).getUri();
                    uris.add(uri);
                }


                spotifyApi.addItemsToPlaylist(uris, spotify_playlist_id, 0);

                Log.i(TAG, "added new songs for the user");
                Log.i(TAG, "final num songs after adding: " + spotifyApi.getPlaylist(spotify_playlist_id, options).getTracks().getItems().size());
            }
            else {

                Log.i(TAG, "no songs to delete");

            }

            List<PlaylistTrack> tracks2 = spotifyApi.getPlaylist(spotify_playlist_id, options).getTracks().getItems();

            float sum_tempo2 = 0;
            for (int i = 0; i < tracks2.size(); i++){
                AbstractPlayableObject track = tracks2.get(i).getTrack();
                String trackId = track.getId();
                AudioFeatures audioFeatures= spotifyApi.getTrackAudioFeatures(trackId);
                sum_tempo2 = sum_tempo2 + audioFeatures.getTempo();
            }

            float avg_tempo2 = sum_tempo2 / tracks.size();
            Log.i(TAG, "final playlist avg tempo: " + avg_tempo2);
            return null;

        }
        @Override
        protected void onPostExecute(Long aLong) {
           // Log.i(TAG, "done removing songs from playlist" );
            Toast.makeText(RatingActivity.this, "done updating!", Toast.LENGTH_SHORT).show();
            music_load.clearAnimation();
            RatingActivity.this.finish();

 }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);




        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            music_load.playAnimation();

        }
    }

}
package com.example.spotifyrecommendations;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import spotify.api.spotify.SpotifyApi;
import spotify.models.audio.AudioFeatures;
import spotify.models.generic.AbstractPlayableObject;
import spotify.models.playlists.PlaylistItem;
import spotify.models.playlists.PlaylistTrack;
import spotify.models.playlists.requests.DeleteItemsPlaylistRequestBody;
import spotify.models.recommendations.RecommendationCollection;

public class RatingActivity extends AppCompatActivity {
    private static final String TAG = "Rating activity";
    String playlist_id;
    String token;
    Map<String, String> options = new HashMap<>();


    String spotify_playlist_id;
    LottieAnimationView music_load;

    SharedPreferences sharedPreferences;


    TextView tvMood;
    TextView tvTempo;
    SeekBar seekBarMood;
    SeekBar seekBarTempo;

    TextView tvTooSlow;
    TextView tvTooFast;
    TextView tvTooSad;
    TextView tvTooHappy;
    float avg_tempo;
    float avg_valence;

    TextView tvTitle;

    Button btnUpdate;
    Button btnKeep;
    float user_pref_tempo;
    float user_pref_valence;

    int seekMood;
    int seekTempo;

    List<String> listArtistId = new ArrayList<>();
    List<String> listTrackId = new ArrayList<>();
    List<String> listGenres = new ArrayList<>();

    Playlist plst;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rating);
        Intent i = getIntent();
        Bundle b = i.getExtras();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RatingActivity.this);
        token = sharedPreferences.getString("token", "default");



        if(b!=null)
        {
            Log.i(TAG, "bundle not null");
            playlist_id = (String) b.get("new playlist id");
            Log.i(TAG, playlist_id);
        }
        try {
            plst = queryPlaylist();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        spotify_playlist_id = plst.getSpotifyid();
        avg_tempo = Float.parseFloat(plst.getTempo());
        avg_valence = Float.parseFloat(plst.getValence());
        listArtistId.add(plst.getArtistID());
        listTrackId.add(plst.getTrackID());
        listGenres.add(plst.getGenre());


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

        //TODO: get user preferences
        try {
            getUserPrefs();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnKeep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    updatePlaylist(Float.toString(avg_tempo), Float.toString(avg_valence));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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






    }

    private Playlist queryPlaylist() throws ParseException {
        ParseQuery<Playlist> query = ParseQuery.getQuery(Playlist.class);
        query.whereEqualTo(Playlist.KEY_OBJECT_ID, playlist_id);
        return query.find().get(0);
    }


    private void getUserPrefs() throws JSONException {
        //we will examine the users saved posts, and accordingly determine the user's preferred tempos & valences.

        ParseUser user = ParseUser.getCurrentUser();
        JSONArray currSaved = user.getJSONArray("saved");

        List<String> saved_tempos = new ArrayList<>();
        List<String> saved_valences = new ArrayList<>();

        for (int i = 0; i< currSaved.length(); i++){
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Playlist");
            query.getInBackground((String) currSaved.get(i), new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    saved_valences.add((String) object.get(Playlist.KEY_VALENCE));
                    saved_tempos.add((String) object.get(Playlist.KEY_TEMPO));

                }
            });

        }

        //default preferences if the user does not have any saved playlists.
        if(saved_tempos.size()==0){
            user_pref_tempo = 95;
            user_pref_valence = .5F;
        }

        //we will take average of all the users saved playlist tempos & valences to determine the user's preferences
        else {
            float sum_valence=0;
            float sum_tempo = 0;
            for(int i=0; i<=saved_tempos.size(); i++){
                sum_valence+=Float.parseFloat(saved_valences.get(i));
                sum_tempo+=Float.parseFloat(saved_tempos.get(i));
            }

            user_pref_tempo = sum_tempo / saved_tempos.size();
            user_pref_valence = sum_valence / saved_valences.size();
        }


    }



    private void updatePlaylist(String tempo, String valence) throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Playlist");
        ParseObject object = query.get(playlist_id);
        object.put("Like", true);
        object.put("Tempo", tempo);
        object.put("Rated", true);
        object.put("Valence", valence);
        object.save();
    }

    private class updatePlaylistItems extends AsyncTask<URL, Integer, Long> {

        int num_deleted;
        List<PlaylistItem> items_to_delete = new ArrayList<>();

        @Override
        protected Long doInBackground(URL... urls) {
            SpotifyApi spotifyApi = new SpotifyApi(token);



            List<PlaylistTrack> tracks = spotifyApi.getPlaylist(spotify_playlist_id, options).getTracks().getItems();

            float change_factor_mood = 0F;

            //if the user rates the playlist as too sad, but the user pref mood is lower than the playlist valence, change factor will be smaller
            if (seekMood==0 && user_pref_valence<avg_valence){
                change_factor_mood = .1F;

            }

            //if the user rates the playlist as too happy, but the user pref mood is happier than the playlist mood, change factor will be smaller
            else if (seekMood==2 && user_pref_tempo>avg_valence){
                change_factor_mood = (float) -0.1;
            }

            //if the user rates the playlist as too sad, but the user pref mood is happier than the playlist mood, change factor will be larger
            else if (seekMood==0&& user_pref_tempo>avg_valence){
                change_factor_mood = (float) .2;

            }

            //if the user rates the playlist as too happy, and the user pref mood is lower than the playlist mood, change factor will be larger
            else if (seekMood==2 && user_pref_tempo<avg_valence){
                change_factor_mood = (float) -0.2;
            }

            float new_target_valence = avg_valence + change_factor_mood;
            if (new_target_valence < 0){
                new_target_valence = (float) 0.1;
            }
            else if (new_target_valence > 1){
                new_target_valence = (float) 0.9;
            }




            Log.i(TAG, "initial playlist tempo: " + avg_tempo);
            Log.i(TAG, "initial playlist valence: " + avg_valence);


            float change_factor_tempo = 0;

            //change_factor is how much we want to change the playlist tempo by

            //if the user rates the playlist as too slow, but the user pref tempo is lower than the playlist tempo, change factor will be smaller
            if (seekTempo==0 && user_pref_tempo<avg_tempo){
                change_factor_tempo = 5;

            }

            //if the user rates the playlist as too fast, but the user pref tempo is greater than the playlist tempo, change factor will be smaller
            else if (seekTempo==2 && user_pref_tempo>avg_tempo){
                change_factor_tempo = -5;
            }

            //if the user rates the playlist as too slow, but the user pref tempo is higher than the playlist tempo, change factor will be larger
            else if (seekTempo==0&& user_pref_tempo>avg_tempo){
                change_factor_tempo = 20;

            }

            //if the user rates the playlist as too fast, and the user pref tempo is lower than the playlist tempo, change factor will be larger
            else if (seekTempo==2 && user_pref_tempo<avg_tempo){
                change_factor_tempo = -20;

            }
            Log.i(TAG, "change tempo by: " + change_factor_tempo);

            float new_target_tempo = avg_tempo + change_factor_tempo;

            Log.i(TAG, "target tempo: " + new_target_tempo);

            for (int i = 0; i < tracks.size(); i++){

                AbstractPlayableObject track = tracks.get(i).getTrack();
                int[] intArray = new int[1];
                intArray[0]= i;
                PlaylistItem item = new PlaylistItem(track.getUri(), intArray);
                String trackId = track.getId();
                AudioFeatures audioFeatures= spotifyApi.getTrackAudioFeatures(trackId);

                float tempo = audioFeatures.getTempo();
                float valence = audioFeatures.getValence();

                //if the user rates the playlist as too slow, and this track is slower than our target tempo, we will delete the track from the playlist
                if(seekTempo==0 && tempo < new_target_tempo){
                    items_to_delete.add(item);
                    num_deleted++;

                }

                //if the user rates the playlist as too fast, and this track is faster than our target tempo, we will delete the track from the playlist
                else if(seekTempo==2 && tempo > new_target_tempo){
                    items_to_delete.add(item);
                    num_deleted++;

                }

                else if(seekMood==0 && valence < new_target_valence){
                    items_to_delete.add(item);
                    num_deleted++;

                }

                //if the user rates the playlist as too fast, and this track is faster than our target tempo, we will delete the track from the playlist
                else if(seekMood==2 && valence > new_target_valence){
                    items_to_delete.add(item);
                    num_deleted++;

                }

            }


            DeleteItemsPlaylistRequestBody requestBody = new DeleteItemsPlaylistRequestBody(items_to_delete, spotifyApi.getPlaylist(spotify_playlist_id, options).getSnapshotId());
            
            //delete all the songs from the playlist that do not meet the user's mood expectations 
            spotifyApi.deleteItemsFromPlaylist(spotify_playlist_id, requestBody);

            Map<String, String> rec_extra = new HashMap<>();

            if (seekTempo == 0){
                Log.i(TAG, "setting minimum tempo: " + new_target_tempo);
                rec_extra.put("min_tempo", Float.toString(new_target_tempo));
            }
            else if (seekTempo == 2){
                Log.i(TAG, "setting maximum tempo: " + new_target_tempo);
                rec_extra.put("max_tempo", Float.toString(new_target_tempo));
            }

            if (seekMood == 0){
                Log.i(TAG, "setting minimum valence: " + new_target_valence);
                rec_extra.put("min_valence", Float.toString(new_target_valence));
            }
            else if (seekTempo == 2){
                Log.i(TAG, "setting maximum valence: " + new_target_valence);
                rec_extra.put("max_valence", Float.toString(new_target_valence));
            }


            if (num_deleted !=0) {
                rec_extra.put("limit", Integer.toString(num_deleted));
                RecommendationCollection recommendations = spotifyApi.getRecommendations(listArtistId, listGenres, listTrackId, rec_extra);

                List<String> uris = new ArrayList<>();
                for (int i = 0; i < recommendations.getTracks().size(); i++) {
                    String uri = spotifyApi.getTrack(recommendations.getTracks().get(i).getId(), options).getUri();
                    uris.add(uri);
                }
                spotifyApi.addItemsToPlaylist(uris, spotify_playlist_id, 0);
            }
            else {
                Log.i(TAG, "no songs to delete");
            }

            List<PlaylistTrack> tracks2 = spotifyApi.getPlaylist(spotify_playlist_id, options).getTracks().getItems();


            //calculates the overall final tempo of the playlist (for checking purposes, can remove later)
            float sum_tempo2 = 0;
            float sum_valence2=0;
            for (int i = 0; i < tracks2.size(); i++){
                AbstractPlayableObject track = tracks2.get(i).getTrack();
                String trackId = track.getId();
                AudioFeatures audioFeatures= spotifyApi.getTrackAudioFeatures(trackId);
                sum_tempo2 = sum_tempo2 + audioFeatures.getTempo();
                sum_valence2 = sum_valence2 + audioFeatures.getValence();
            }

            float avg_tempo2 = sum_tempo2 / tracks2.size();
            float avg_valence2 = sum_valence2 / tracks2.size();
            Log.i(TAG, "final playlist avg tempo: " + avg_tempo2);
            Log.i(TAG, "final playlist avg valence: " + avg_valence2);

            try {
                updatePlaylist(Float.toString(avg_tempo2), Float.toString(avg_valence2));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return null;

        }
        @Override
        protected void onPostExecute(Long aLong) {
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
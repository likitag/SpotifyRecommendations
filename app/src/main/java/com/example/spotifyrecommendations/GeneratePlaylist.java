package com.example.spotifyrecommendations;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.spotifyrecommendations.models.CustomUser;
import com.example.spotifyrecommendations.models.Playlist;
import com.example.spotifyrecommendations.models.Song;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import spotify.api.spotify.SpotifyApi;
import spotify.models.audio.AudioFeatures;
import spotify.models.generic.AbstractPlayableObject;
import spotify.models.playlists.PlaylistTrack;
import spotify.models.playlists.requests.CreateUpdatePlaylistRequestBody;



public class GeneratePlaylist extends AppCompatActivity {

    private static final String TAG = "Generate Playlist";
    String token;
    Map<String, String> extra = new HashMap<>();
    int time;
    List<String> spotifyRecs;
    Map<String, String> options = new HashMap<>();
    private static String playlistId;
    private static String playlist_uri;
    ProgressBar pb;
    String playlistName;
    //private static Playlist newPlaylist;
    LottieAnimationView music_load;
    String playlist_obj_id;



    List<String> listArtistId = new ArrayList<>();
    List<String> listTrackId = new ArrayList<>();
    List<String> listGenres = new ArrayList<>();
    String playlist_valence;
    String playlist_tempo;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_playlist);
        Intent i2 = getIntent();
        Bundle b = i2.getExtras();
        //pb = findViewById(R.id.progress);
        music_load = findViewById(R.id.lottie_music_load);
        music_load.playAnimation();



        if(b!=null)
        {
            Log.i(TAG, "bundle not null");
            token =(String) b.get("token");
            spotifyRecs = getIntent().getStringArrayListExtra("rec tracks");
            time = (Integer) b.get("time");
            playlistName = (String) b.get("playlist name");

            listArtistId = getIntent().getStringArrayListExtra("listArtists");
            listTrackId = getIntent().getStringArrayListExtra("listTracks");
            listGenres = getIntent().getStringArrayListExtra("listGenres");

        }

        new Task().execute();


    }



    private void savePlaylist(String name, int length, String spotifyId, ParseUser author, String uri, String genre, String valence, String tempo) throws JSONException, ParseException {
        //ParseUser currentUser = ParseUser.getCurrentUser();

        //JSONArray user_playlists = currentUser.getJSONArray("Playlist");

       // Log.i(TAG, "playlist array is null ? " + (user_playlists == null));
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setLength(length);
        playlist.setSpotifyid(spotifyId);
        playlist.setUser(author);
        playlist.setURI(uri);
        playlist.setGenre(genre);
        playlist.setValence(valence);
        playlist.setTempo(tempo);

        playlist.setArtistID(listArtistId.get(0));
        playlist.setTrackID(listTrackId.get(0));
        playlist.save();

        ParseUser user = ParseUser.getCurrentUser();
        Log.i(TAG, "savePlaylist: adding to faves: " + playlist.getObjectId());
        user.add(CustomUser.KEY_FAVORITES, playlist.getObjectId());
        user.save();
        finish();

    }


    private class Task extends AsyncTask<URL, Integer, Long> {

        @Override
        protected Long doInBackground(URL... urls) {
            SpotifyApi spotifyApi = new SpotifyApi(token);
            String userId = spotifyApi.getCurrentUser().getId();
            CreateUpdatePlaylistRequestBody requestBody = new CreateUpdatePlaylistRequestBody(playlistName, "made by Spotfind", false, false);
            spotifyApi.createPlaylist(userId, requestBody);

            List<String > uris = new ArrayList<>();
            for (int i = 0; i<spotifyRecs.size(); i++){
                String uri = spotifyApi.getTrack(spotifyRecs.get(i), extra).getUri();
                uris.add(uri);
            }

            for (int i=0; i< spotifyApi.getUserPlaylists(userId, extra).getItems().size(); i++){
                if (spotifyApi.getUserPlaylists(userId, extra).getItems().get(i).getName().equals(playlistName)) {
                    playlistId = spotifyApi.getUserPlaylists(userId, extra).getItems().get(i).getId();
                    Log.i(TAG, "new playlist id: " +playlistId);
                    break;
                }

            }

            spotifyApi.addItemsToPlaylist(uris, playlistId, 0);
            playlist_uri = spotifyApi.getPlaylist(playlistId, extra).getUri();

            Log.i(TAG, "uri" + playlist_uri);

            List<PlaylistTrack> tracks = spotifyApi.getPlaylist(playlistId, options).getTracks().getItems();

            float sum_valence = 0;
            float sum_tempo = 0;

            for (int i = 0; i < tracks.size(); i++){
                AbstractPlayableObject track = tracks.get(i).getTrack();
                String trackId = track.getId();
                AudioFeatures audioFeatures= spotifyApi.getTrackAudioFeatures(trackId);
                sum_valence = sum_valence + audioFeatures.getValence();
                sum_tempo = sum_tempo + audioFeatures.getTempo();
            }

            playlist_valence = Float.toString(sum_valence /tracks.size());
            playlist_tempo = Float.toString(sum_tempo / tracks.size());
            return null;
        }
        @Override
        protected void onPostExecute(Long aLong) {
            try {
                savePlaylist(playlistName, spotifyRecs.size(), playlistId, ParseUser.getCurrentUser(), playlist_uri, listGenres.get(0), playlist_valence, playlist_tempo);
                //Log.i(TAG, "playlist saved: " + newPlaylist);
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "finished adding items to new playlist ");

            super.onPostExecute(aLong);
            finish();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Toast.makeText(GeneratePlaylist.this, "animating", Toast.LENGTH_SHORT);

        }
    }



}
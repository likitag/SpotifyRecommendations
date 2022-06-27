package com.example.spotifyrecommendations;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.models.Track;
import spotify.api.enums.QueryType;
import spotify.api.spotify.SpotifyApi;
import spotify.models.playlists.requests.CreateUpdatePlaylistRequestBody;
import spotify.models.recommendations.RecommendationCollection;
import spotify.models.tracks.TrackFull;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_playlist);
        Intent i2 = getIntent();
        Bundle b = i2.getExtras();
        pb = findViewById(R.id.progress);

        Log.i(TAG, "onCreate: ");

        if(b!=null)
        {
            token =(String) b.get("token");
            spotifyRecs = getIntent().getStringArrayListExtra("rec tracks");
            time = (Integer) b.get("time");

        }

        Log.i(TAG, spotifyRecs.get(0));
        new Task().execute();

    }



    private void savePlaylist(String name, int length, String spotifyId, ParseUser author, String uri) {
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setLength(length);
        playlist.setSpotifyid(spotifyId);
        playlist.setUser(author);
        playlist.setURI(uri);
        Log.i(TAG, "savePlaylist: " +playlist.getURI());
        playlist.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!=null){
                    Log.e(TAG, "error while saving playlist", e);


                }
                Log.i(TAG, "post was successful");

            }
        });
    }



    private class Task extends AsyncTask<URL, Integer, Long> {

        @Override
        protected Long doInBackground(URL... urls) {


            SpotifyApi spotifyApi = new SpotifyApi(token);
            String userId = spotifyApi.getCurrentUser().getId();
            String playlistName = "test playlist";
            CreateUpdatePlaylistRequestBody requestBody = new CreateUpdatePlaylistRequestBody(playlistName, "made this with api", false, false);
            spotifyApi.createPlaylist(userId, requestBody);

            List<String > uris = new ArrayList<>();
            for (int i = 0; i<spotifyRecs.size(); i++){
                String uri = spotifyApi.getTrack(spotifyRecs.get(i), extra).getUri();
                uris.add(uri);
                //String songName = spotifyApi.getTrack(spotifyRecs.get(i), options).getName();
                //String songArtist = spotifyApi.getTrack(spotifyRecs.get(i), options).getArtists().get(0).getName();
                //saveSong(songName, songArtist);

                //TODO: add each of these tracks to Parse DB

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
            //savePlaylist(playlistName, spotifyRecs.size(), playlistId, ParseUser.getCurrentUser(), spotifyApi.getPlaylist(playlistId, extra).getUri());
            Log.i(TAG, "finsihed adding items to new playlist ");

            return null;
        }
        @Override
        protected void onPostExecute(Long aLong) {
            Log.i(TAG, "done creating songs");

            super.onPostExecute(aLong);
            Intent i = new Intent(GeneratePlaylist.this, PlaylistActivity.class);
            startActivity(i);

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            pb.setVisibility(View.VISIBLE);



        }
    }

    private void saveSong(String songName, String songArtist) {
        Song song = new Song();
        song.setName(songName);
        song.setArtist(songArtist);
        song.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!=null){
                    Log.e(TAG, "issue with saving song", e);
                    return;

                }


            }
        });
    }
}
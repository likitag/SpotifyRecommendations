package com.example.spotifyrecommendations;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.spotifyrecommendations.models.Playlist;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import spotify.api.spotify.SpotifyApi;
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
    private static Playlist newPlaylist;

    String playlist_obj_id;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_playlist);
        Intent i2 = getIntent();
        Bundle b = i2.getExtras();
        pb = findViewById(R.id.progress);


        if(b!=null)
        {
            Log.i(TAG, "bundle not null");
            token =(String) b.get("token");
            spotifyRecs = getIntent().getStringArrayListExtra("rec tracks");
            time = (Integer) b.get("time");
            playlistName = (String) b.get("playlist name");

        }

        new Task().execute();


    }



    private void savePlaylist(String name, int length, String spotifyId, ParseUser author, String uri) throws JSONException {
        //ParseUser currentUser = ParseUser.getCurrentUser();

        //JSONArray user_playlists = currentUser.getJSONArray("Playlist");

       // Log.i(TAG, "playlist array is null ? " + (user_playlists == null));
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setLength(length);
        playlist.setSpotifyid(spotifyId);
        playlist.setUser(author);
        playlist.setURI(uri);


        playlist.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e!=null){
                    Log.e(TAG, "error while saving playlist", e);



                }
                Log.i(TAG, "post was successful");
                Log.i(TAG, "savePlaylist: " + playlist.getPlaylistId());
                playlist_obj_id = playlist.getPlaylistId();

                Intent rating = new Intent(GeneratePlaylist.this, RatingActivity.class);
                Log.i(TAG, "passing: " + playlist_obj_id);

                rating.putExtra("new playlist id", playlist_obj_id);
                rating.putExtra("new playlist", (Serializable) newPlaylist);
                rating.putExtra("token", token);
                rating.putExtra("spotify playlist id", playlistId);

//                Intent spotify_app = new Intent(Intent.ACTION_VIEW);
//                spotify_app.setData(Uri.parse(playlist_uri));
//                startActivity(spotify_app);

                startActivity(rating);
                finish();

            }
        });

        newPlaylist = playlist;


    }



    private class Task extends AsyncTask<URL, Integer, Long> {

        @Override
        protected Long doInBackground(URL... urls) {


            SpotifyApi spotifyApi = new SpotifyApi(token);
            String userId = spotifyApi.getCurrentUser().getId();
            CreateUpdatePlaylistRequestBody requestBody = new CreateUpdatePlaylistRequestBody(playlistName, "made this with api", false, false);
            spotifyApi.createPlaylist(userId, requestBody);

            List<String > uris = new ArrayList<>();
            for (int i = 0; i<spotifyRecs.size(); i++){
                String uri = spotifyApi.getTrack(spotifyRecs.get(i), extra).getUri();
                uris.add(uri);
                //String songName = spotifyApi.getTrack(spotifyRecs.get(i), options).getName();
                //String songArtist = spotifyApi.getTrack(spotifyRecs.get(i), options).getArtists().get(0).getName();
                //saveSong(songName, songArtist);


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


            return null;
        }
        @Override
        protected void onPostExecute(Long aLong) {
            try {
                savePlaylist(playlistName, spotifyRecs.size(), playlistId, ParseUser.getCurrentUser(), playlist_uri);
                Log.i(TAG, "playlist saved: " + newPlaylist);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "finished adding items to new playlist ");
            Log.i(TAG, "done creating songs");

            super.onPostExecute(aLong);
            Intent spotify_app = new Intent(Intent.ACTION_VIEW);
            spotify_app.setData(Uri.parse(playlist_uri));

//            Intent rating = new Intent(GeneratePlaylist.this, RatingActivity.class);
//            Log.i(TAG, "passing: " + playlist_obj_id);
//
//            rating.putExtra("new playlist id", playlist_obj_id);
//            rating.putExtra("new playlist", (Serializable) newPlaylist);
//            rating.putExtra("token", token);
//
//            startActivity(rating);
            finish();

//            TaskStackBuilder.create(GeneratePlaylist.this)
//                    .addNextIntent( rating)
//                    // use this method if you want "intentOnTop" to have it's parent chain of activities added to the stack. Otherwise, more "addNextIntent" calls will do.
//                    .addNextIntentWithParentStack( spotify_app )
//                    .startActivities();


        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            pb.setVisibility(View.VISIBLE);



        }
    }

//    private void saveSong(String songName, String songArtist) {
//        Song song = new Song();
//        song.setName(songName);
//        song.setArtist(songArtist);
//        song.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                if(e!=null){
//                    Log.e(TAG, "issue with saving song", e);
//                    return;
//
//                }
//
//
//            }
//        });
//    }
}
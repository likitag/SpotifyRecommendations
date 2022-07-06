package com.example.spotifyrecommendations;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifyrecommendations.models.Playlist;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;

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
import spotify.models.playlists.requests.CreateUpdatePlaylistRequestBody;
import spotify.models.playlists.requests.DeleteItemsPlaylistRequestBody;
import spotify.models.tracks.TrackFull;

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


    TextView tvMood;
    TextView tvInstrument;
    SeekBar seekBarMood;
    SeekBar seekBarInstrument;

    TextView tvTooLittle;
    TextView tvTooMuch;
    TextView tvTooSad;
    TextView tvTooHappy;

    Button btnUpdate;
    Button btnKeep;

    int seekMood;
    int seekInstrument;


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


        }
        tvLike = findViewById(R.id.tvLike);
        tvDislike = findViewById(R.id.tvDislike);
        ibLike = findViewById(R.id.ibLike);
        ibDislike = findViewById(R.id.ibDislike);

        tvMood = findViewById(R.id.tvMood);
        tvInstrument = findViewById(R.id.tvInstrumentalTitle);
        seekBarMood = findViewById(R.id.seekBarMood);
        seekBarInstrument = findViewById(R.id.seekBarInstrument);

        tvTooHappy = findViewById(R.id.tvTooHappy);
        tvTooSad = findViewById(R.id.tvTooSad);
        tvTooLittle = findViewById(R.id.tvTooLittle);
        tvTooMuch = findViewById(R.id.tvTooMuch);

        btnUpdate = findViewById(R.id.btnUpdate);
        btnKeep = findViewById(R.id.btnKeep);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                seekMood = seekBarMood.getProgress();
                Log.i(TAG, "onClick: " + seekMood);
                seekInstrument = seekBarInstrument.getProgress();
                new updatePlaylistItems().execute();








            }
        });

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



    }

    private class updatePlaylistItems extends AsyncTask<URL, Integer, Long> {

        int num_deleted;
        List<PlaylistItem> items_to_delete = new ArrayList<>();

        @Override
        protected Long doInBackground(URL... urls) {
            SpotifyApi spotifyApi = new SpotifyApi(token);
            //String userId = spotifyApi.getCurrentUser().getId();

            Log.i(TAG, "initial num songs: " + spotifyApi.getPlaylist(spotify_playlist_id, options).getTracks().getItems().size());

           // List<PlaylistItem> items = new ArrayList<>();
            List<PlaylistTrack> tracks = spotifyApi.getPlaylist(spotify_playlist_id, options).getTracks().getItems();

            for (int i = 0; i < tracks.size(); i++){

                AbstractPlayableObject track = tracks.get(i).getTrack();
                int[] intArray = new int[1];
                intArray[0]= i;
                PlaylistItem item = new PlaylistItem(track.getUri(), intArray);
                Log.i(TAG, "item: " + item.getUri());

                String trackId = track.getId();
                AudioFeatures audioFeatures= spotifyApi.getTrackAudioFeatures(trackId);

                //indicates the instrumentalness of a track (0.5 and above is intrumental)
                float instrumentalness = audioFeatures.getInstrumentalness();

                //indicates the happiness of a track (0 = sad, 1= happy)
                float valence = audioFeatures.getValence();

                if ((seekInstrument == 0 && instrumentalness < 0.5) || (seekInstrument == 2 && instrumentalness > 0.5) || (seekMood == 0 && valence < 0.5) || (seekMood == 2 && valence > 0.5)){
                    //TODO: delete song from playlist, not instrumental enough
                    items_to_delete.add(item);
                    Log.i(TAG, "wish to delete: " + item.getUri());
                    num_deleted++;
                }
            }


            Log.i(TAG, "num items to delete: " + items_to_delete.size());

            DeleteItemsPlaylistRequestBody requestBody = new DeleteItemsPlaylistRequestBody(items_to_delete, spotifyApi.getPlaylist(spotify_playlist_id, options).getSnapshotId());

            spotifyApi.deleteItemsFromPlaylist(spotify_playlist_id, requestBody);



            Log.i(TAG, "final num songs: " + spotifyApi.getPlaylist(spotify_playlist_id, options).getTracks().getItems().size());



            return null;
        }
        @Override
        protected void onPostExecute(Long aLong) {
            Log.i(TAG, "done removing songs from playlist" );
            Toast.makeText(RatingActivity.this, "removed songs", Toast.LENGTH_SHORT).show();






        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);



        }
    }

}
package com.example.spotifyrecommendations;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.spotifyrecommendations.fragments.ComposeFragment;
import com.example.spotifyrecommendations.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.net.URL;

import spotify.api.spotify.SpotifyApi;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN";
    BottomNavigationView bottomNavigationView;
    String token;
    String username;
    final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        Intent i2 = getIntent();
        Bundle b = i2.getExtras();

        if(b!=null)
        {
            token =(String) b.get("token");
            //Log.i(TAG, token);

        }


        new Task().execute();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {
                    case R.id.action_home:
                        //Toast.makeText(MainActivity.this, "Home!", Toast.LENGTH_SHORT).show();
                        Bundle bundle = new Bundle();
                        bundle.putString("username", username );
                        fragment = new ProfileFragment();
                        fragment.setArguments(bundle);


                        break;

                    case R.id.action_add:
                    default:
                        //Toast.makeText(MainActivity.this, "Profile!", Toast.LENGTH_SHORT).show();
                        fragment = new ComposeFragment();
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.action_home);

    }

    private class Task extends AsyncTask<URL, Integer, Long> {

        @Override
        protected Long doInBackground(URL... urls) {
            //Log.i(TAG, "doInBackground: ");
            SpotifyApi spotifyApi = new SpotifyApi(token);

            //Log.i(TAG, "get api");



            username = spotifyApi.getCurrentUser().getDisplayName();
//            Map<String, String> extra = new HashMap<>();
//            String albumName = spotifyApi.getAlbum("4aawyAB9vmqN3uQ7FjRGTy", extra).getName();
//
//            List<String> seedArtists = new ArrayList<>();
//            seedArtists.add("4NHQUGzhtTLFvgF5SZesLK");
//
//            List<String> seedGenres = new ArrayList<>();
//            seedGenres.add("classical");
//            seedGenres.add("country");
//
//            List<String> seedTracks = new ArrayList<>();
//            seedTracks.add("0c6xIDDpzE81m2q797ordA");
            //Log.i(TAG, "doInBackground: ");
            //CreateUpdatePlaylistRequestBody requestBody = new CreateUpdatePlaylistRequestBody("my new playlist", "made this with api", false, false);
            //spotifyApi.createPlaylist(spotifyApi.getCurrentUser().getId(), requestBody);
           // Log.i(TAG,  "made a new playlist");



//            String song1 = spotifyApi.getRecommendations(seedArtists, seedGenres, seedTracks, extra).getTracks().get(2).getName();
//            Log.i(TAG, song1);
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }
}
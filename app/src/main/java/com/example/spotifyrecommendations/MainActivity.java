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
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.net.URL;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
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

        }

        kaaes.spotify.webapi.android.SpotifyApi api_kaees = new kaaes.spotify.webapi.android.SpotifyApi();
        api_kaees.setAccessToken(token);
        SpotifyService service =  api_kaees.getService();
        
        service.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                Log.d(TAG, "success: ");
                username = userPrivate.display_name;
                
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "failure: ");

            }
        });
        


        //new Task().execute();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {


                    case R.id.action_add:

                        //Toast.makeText(MainActivity.this, "Profile!", Toast.LENGTH_SHORT).show();
                        Bundle bundle2 = new Bundle();
                        Log.i(TAG, "onNavigationItemSelected: " + token);
                        bundle2.putString("token", token);
                        fragment = new ComposeFragment();
                        fragment.setArguments(bundle2);
                        break;

                    case R.id.action_home:
                        default:
                        //Toast.makeText(MainActivity.this, "Home!", Toast.LENGTH_SHORT).show();
                        Bundle bundle = new Bundle();
                        bundle.putString("username", username );
                        bundle.putString("token", token);
                        fragment = new ProfileFragment();
                        fragment.setArguments(bundle);


                        break;

                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.action_home);

        //queryPlaylists();

    }

    private void queryPlaylists() {
        ParseQuery<Playlist> query = ParseQuery.getQuery(Playlist.class);
        query.include(Playlist.KEY_AUTHOR);
        query.findInBackground(new FindCallback<Playlist>() {
            @Override
            public void done(List<Playlist> playlists, ParseException e) {
                if(e!=null){
                    Log.e(TAG, "issue with getting playlists", e);
                    return;

                }
                for (Playlist playlist : playlists){
                    Log.i(TAG, "Playlist: " + playlist.getName());

                }

            }
        });
    }

//    private class Task extends AsyncTask<URL, Integer, Long> {
//
//        @Override
//        protected Long doInBackground(URL... urls) {
//            SpotifyApi spotifyApi = new SpotifyApi(token);
//            username = spotifyApi.getCurrentUser().getDisplayName();
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Long aLong) {
//            super.onPostExecute(aLong);
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            super.onProgressUpdate(values);
//        }
//    }
}
package com.example.spotifyrecommendations;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.spotifyrecommendations.fragments.ComposeFragment;
import com.example.spotifyrecommendations.fragments.PostFragment;
import com.example.spotifyrecommendations.fragments.ProfileFragment;
import com.example.spotifyrecommendations.fragments.SocialFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.spotify.sdk.android.auth.AuthorizationClient;

import java.net.URL;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN";
    BottomNavigationView bottomNavigationView;
    String token;
    String username;
    MenuItem logout;
    final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            Toast.makeText(this, "logging out", Toast.LENGTH_SHORT).show();
            AuthorizationClient.clearCookies(getApplicationContext());
            ParseUser.logOut();
            ParseUser currentUser = ParseUser.getCurrentUser();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            return true;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logout = findViewById(R.id.action_logout);
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

                    case R.id.action_social:

                        //Toast.makeText(MainActivity.this, "Profile!", Toast.LENGTH_SHORT).show();
                        Bundle bundle3 = new Bundle();
                        Log.i(TAG, "onNavigationItemSelected: " + token);
                        bundle3.putString("token", token);
                        fragment = new SocialFragment();
                        fragment.setArguments(bundle3);
                        break;

                    case R.id.action_post:


                        fragment = new PostFragment();

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
        bottomNavigationView.setSelectedItemId(R.id.action_add);

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
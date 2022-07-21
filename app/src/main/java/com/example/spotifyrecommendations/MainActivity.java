package com.example.spotifyrecommendations;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifyrecommendations.fragments.ComposeFragment;
import com.example.spotifyrecommendations.fragments.GroupsFragment;
import com.example.spotifyrecommendations.fragments.PostFragment;
import com.example.spotifyrecommendations.fragments.ProfileFragment;
import com.example.spotifyrecommendations.fragments.SocialFragment;
import com.example.spotifyrecommendations.models.Playlist;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
//import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.AuthorizationClient;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import spotify.api.spotify.SpotifyApi;
import spotify.models.artists.ArtistFull;
import spotify.models.artists.ArtistSimplified;
import spotify.models.tracks.TrackFull;




public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN";
    BottomNavigationView bottomNavigationView;
    String token;
    String username;
    MenuItem logout;
    final FragmentManager fragmentManager = getSupportFragmentManager();
    FirebaseAuth auth;
    DatabaseReference reference;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    List<ArtistFull> user_top_artists = new ArrayList<>();
    List<ArtistSimplified> all_top_artists = new ArrayList<>();
    List<TrackFull> top_tracks = new ArrayList<>();
    Boolean isComplete = false;
    Boolean permission = false;
    //private SpotifyAppRemote spotifyAppRemote;

//    private Toolbar mToolbar;



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

        if(item.getItemId() == R.id.new_gc){

            RequestNewGroup();

        }

        if(item.getItemId() == R.id.settings){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Spotifind Account Details");




        }

        if(item.getItemId() == R.id.all_gc){
            Fragment newFragment = new GroupsFragment();
            Bundle all_gc_bundle = new Bundle();

            all_gc_bundle.putString("gc list", "all gc");
            newFragment.setArguments(all_gc_bundle);
           // fragmentManager.beginTransaction();
            fragmentManager.beginTransaction().replace(R.id.flContainer, newFragment).commit();

        }

        return true;
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("enter group name");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g. group chat name");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();
                if (TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "please enter a name", Toast.LENGTH_SHORT).show();

                }
                else {
                    createNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();


            }
        });

        builder.show();
    }

    private void createNewGroup(String groupName) {
        reference.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "successfully created gc", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "failure creating gc", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        token = sharedPreferences.getString("token", "default");


        new Task().execute();
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        logout = findViewById(R.id.action_logout);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        Intent i2 = getIntent();
        //spotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");


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
        bottomNavigationView.setSelectedItemId(R.id.action_social);

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

    private class Task extends AsyncTask<URL, Integer, Long> {

        @Override
        protected Long doInBackground(URL... urls) {
            Map<String, String> options = new HashMap<>();
            SpotifyApi spotifyApi = new SpotifyApi(token);
            username = spotifyApi.getCurrentUser().getDisplayName();
            Log.i(TAG, "doInBackground: user" + username);
            Log.i(TAG, "doInBackground: token" + token);

            user_top_artists = spotifyApi.getTopArtists(options).getItems();
            Log.i(TAG, "doInBackground: top" + user_top_artists.size());



            top_tracks = spotifyApi.getTopTracks(options).getItems();

            if (user_top_artists.size()==0){

                //Log.i(TAG, "albums new reelase:  " + spotifyApi.getNewReleases(options).getAlbums().getItems().get(0).getArtists().get(0));

                all_top_artists.addAll(spotifyApi.getNewReleases(options).getAlbums().getItems().get(0).getArtists());
            }



            Map<String, String> opt = new HashMap<>();
            if(top_tracks.size()==0){
                opt.put("market", "ES");

                top_tracks.addAll(spotifyApi.getArtistTopTracks(all_top_artists.get(0).getId(), opt).getTracks());
                Log.i(TAG, "top tracks all: " + spotifyApi.getArtistTopTracks(all_top_artists.get(0).getId(), opt).getTracks().size());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            Set<String> set_a = new HashSet<String>();

            for (ArtistFull a: user_top_artists){
                set_a.add(a.getId());
            }

            if(set_a.size()==0){
                for (ArtistSimplified a: all_top_artists){
                    set_a.add(a.getId());
                }

            }

            Log.i(TAG, "onPostExecute: main" + set_a.size());

            Log.i(TAG, "onPostExecute: " + set_a.size());

            Set<String> set_t = new HashSet<String>();

            for (TrackFull t: top_tracks){
                set_t.add(t.getId());
            }


            editor.putStringSet("top artists", set_a);
            editor.apply();
            editor.putStringSet("top tracks", set_t);
            editor.apply();
            isComplete = true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }
    }
}
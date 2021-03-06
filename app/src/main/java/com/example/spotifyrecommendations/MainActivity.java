package com.example.spotifyrecommendations;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.spotifyrecommendations.adapters.DialogAdapter;
import com.example.spotifyrecommendations.fragments.ComposeFragment;
import com.example.spotifyrecommendations.fragments.GroupsFragment;
import com.example.spotifyrecommendations.fragments.PostFragment;
import com.example.spotifyrecommendations.fragments.ProfileFragment;
import com.example.spotifyrecommendations.fragments.SocialFragment;
import com.example.spotifyrecommendations.models.CustomUser;
import com.example.spotifyrecommendations.models.Playlist;
import com.example.spotifyrecommendations.models.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
//import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.AuthorizationClient;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            builder.setPositiveButton("View Collected Data", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        displayMyData();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });

            builder.setNegativeButton("Delete my data", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                    builder1.setTitle("Type Delete to proceed and wipe all existing data");
                    final EditText deleteCommand = new EditText(MainActivity.this);
                    deleteCommand.setHint("Delete");
                    builder1.setView(deleteCommand);
                    builder1.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String deletion = deleteCommand.getText().toString();
                            if (TextUtils.equals(deletion, "Delete"))
                                try {
                                    deleteMyAccount();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                        }
                    });
                    builder1.show();

                }
            });

            builder.show();

        }

        if(item.getItemId() == R.id.all_gc){
            Fragment newFragment = new GroupsFragment();
            Bundle all_gc_bundle = new Bundle();
            all_gc_bundle.putString("gc list", "all gc");
            newFragment.setArguments(all_gc_bundle);
            fragmentManager.beginTransaction().replace(R.id.flContainer, newFragment).commit();
        }

        return true;
    }

    private void deleteMyAccount() throws ParseException {

        ParseUser user = ParseUser.getCurrentUser();
        //deletes all of user's playlists and posts
        deleteAllUserData();

        //deletes ParseUser
        user.delete();

        //deleted firebase user
        final FirebaseUser user_firebase = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getUsername() + "@gmail.com", sharedPreferences.getString("password", "default"));
        user_firebase.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        user_firebase.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User account deleted.");
                                        }
                                    }
                                });

                    }
                });
        finish();

    }


    private void displayMyData() throws ParseException {
        Toast.makeText(this, "displaying", Toast.LENGTH_SHORT).show();
        List<String> all_user_data = new ArrayList<>();

        ParseUser user = ParseUser.getCurrentUser();

        String jsonStringPersonal = get_personal_data(user);
        all_user_data.add("USER PERSONAL INFO: ");
        all_user_data.add(jsonStringPersonal);

        ParseQuery<ParseObject> query_playlists = ParseQuery.getQuery("Playlist");
        query_playlists.whereEqualTo(Playlist.KEY_AUTHOR, user);
        List<ParseObject> my_playlists = query_playlists.find();

        List<String> user_playlist_data = new ArrayList<>();
        user_playlist_data.add("USER PLAYLIST COLLECTED INFORMATION: ");
        all_user_data.addAll(get_user_playlist_date(my_playlists));


        ParseQuery<ParseObject> query_posts = ParseQuery.getQuery("Post");
        query_posts.whereEqualTo(Playlist.KEY_AUTHOR, user);
        List<ParseObject> my_posts = query_posts.find();
        all_user_data.addAll(get_user_post_data(my_posts));
        show_collected_info_dialog(all_user_data);

    }

    private void show_collected_info_dialog(List<String> all_user_data) {
        Dialog dialog = new Dialog(this, R.style.DialogSlideAnim);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(R.layout.dialog_layout);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();

        RecyclerView rvTest = (RecyclerView) dialog.findViewById(R.id.rvTest);
        rvTest.setHasFixedSize(true);
        rvTest.setLayoutManager(new LinearLayoutManager(this));

        DialogAdapter rvAdapter = new DialogAdapter(this, all_user_data);
        rvTest.setAdapter(rvAdapter);
    }

    private List<String> get_user_post_data(List<ParseObject> my_posts) {
        List<String> user_post_data = new ArrayList<>();
        user_post_data.add("USER POST COLLECTED INFORMATION: ");
        for (ParseObject obj: my_posts){
            Map<String, String> post_info = new HashMap<>();
            post_info.put("image url", (String) obj.get(Post.KEY_COVER));
            post_info.put("description", (String) obj.get(Post.KEY_DESCRIPTION));
            post_info.put("likes", Integer.toString((Integer) obj.get(Post.KEY_LIKES)));
            post_info.put("playlist id", (String) obj.get(Post.KEY_PLAYLISTID));
            post_info.put("createdAt", obj.getCreatedAt().toString());
            Gson gson = new GsonBuilder().create();
            String jsonString = gson.toJson(post_info);
            user_post_data.add(jsonString);
        }
        return user_post_data;
    }

    private List<String> get_user_playlist_date(List<ParseObject> my_playlists) {
        List<String> user_playlist_data = new ArrayList<>();
        user_playlist_data.add("USER PLAYLIST COLLECTED INFORMATION: ");
        for (ParseObject obj: my_playlists){
            Map<String, String> playlist_info = new HashMap<>();
            playlist_info.put("name", (String) obj.get(Playlist.KEY_NAME));
            playlist_info.put("URI", (String) obj.get(Playlist.KEY_URI));
            playlist_info.put("genre", (String) obj.get(Playlist.KEY_GENRE));
            playlist_info.put("tempo", (String) obj.get(Playlist.KEY_TEMPO) );
            playlist_info.put("valence", (String) obj.get(Playlist.KEY_VALENCE) );
            playlist_info.put("artist seed id", (String) obj.get(Playlist.KEY_ARTISTID));
            playlist_info.put("track seed id", (String) obj.get(Playlist.KEY_TRACKID));
            playlist_info.put("rated", Boolean.toString((Boolean) obj.get(Playlist.KEY_RATED)));
            playlist_info.put("spotify id", (String) obj.get(Playlist.KEY_SPOTIFYID) );
            playlist_info.put("createdAt", obj.getCreatedAt().toString());
            Gson gson = new GsonBuilder().create();
            String jsonString = gson.toJson(playlist_info);
            user_playlist_data.add(jsonString);
        }
        return user_playlist_data;
    }

    private String get_personal_data(ParseUser user) {
        Map<String, String> personal_info = new HashMap<>();
        personal_info.put("username", user.getUsername().toString());
        personal_info.put("createdAt", user.getCreatedAt().toString());
        personal_info.put("favorite playlist ids", user.getJSONArray(CustomUser.KEY_FAVORITES).toString());
        personal_info.put("saved playlist ids", user.getJSONArray(CustomUser.KEY_SAVED).toString());
        Gson gsonPersonal = new GsonBuilder().create();
        String jsonStringPersonal = gsonPersonal.toJson(personal_info);
        return  jsonStringPersonal;
    }

    private void deleteAllUserData() throws ParseException {
        ParseUser user = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> query_playlists = ParseQuery.getQuery("Playlist");
        query_playlists.whereEqualTo(Playlist.KEY_AUTHOR, user);
        List<ParseObject> playlist_objs = query_playlists.find();
        Log.i(TAG, "queryed playlist objs");
        for (ParseObject playlistObj: playlist_objs){
            playlistObj.delete();
            Log.i(TAG, "delete playlist");
        }

        ParseQuery<ParseObject> query_posts = ParseQuery.getQuery("Post");
        query_posts.whereEqualTo(Playlist.KEY_AUTHOR, user);
        List<ParseObject> post_objs = query_posts.find();
        for (ParseObject postObj: post_objs){
            postObj.delete();
        }

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

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment;
                switch (menuItem.getItemId()) {
                    case R.id.action_add:
                        Bundle bundle2 = new Bundle();
                        bundle2.putString("token", token);
                        fragment = new ComposeFragment();
                        fragment.setArguments(bundle2);
                        break;

                    case R.id.action_social:
                        Bundle bundle3 = new Bundle();
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
    }

    private class Task extends AsyncTask<URL, Integer, Long> {
        @Override
        protected Long doInBackground(URL... urls) {
            Map<String, String> options = new HashMap<>();
            SpotifyApi spotifyApi = new SpotifyApi(token);
            username = spotifyApi.getCurrentUser().getDisplayName();
            user_top_artists = spotifyApi.getTopArtists(options).getItems();
            top_tracks = spotifyApi.getTopTracks(options).getItems();
            if (user_top_artists.size()==0){
                all_top_artists.addAll(spotifyApi.getNewReleases(options).getAlbums().getItems().get(0).getArtists());
            }

            Map<String, String> opt = new HashMap<>();
            if(top_tracks.size()==0){
                opt.put("market", "ES");
                top_tracks.addAll(spotifyApi.getArtistTopTracks(all_top_artists.get(0).getId(), opt).getTracks());
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
            Set<String> set_t = new HashSet<String>();

            for (TrackFull t: top_tracks){
                set_t.add(t.getId());
            }


            editor.putStringSet("top artists", set_a);
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
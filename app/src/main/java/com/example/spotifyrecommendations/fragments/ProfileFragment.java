package com.example.spotifyrecommendations.fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.spotifyrecommendations.models.CustomUser;
import com.example.spotifyrecommendations.models.Playlist;
import com.example.spotifyrecommendations.adapters.ProfileAdapter;
import com.example.spotifyrecommendations.R;
import com.example.spotifyrecommendations.models.Post;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import spotify.api.spotify.SpotifyApi;
import spotify.models.artists.ArtistFull;
import spotify.models.artists.ArtistFullCollection;
import spotify.models.tracks.TrackFull;

//TODO: jsonArray of favorties is currently hodling lpost object ids rather than playlist object ids. Need to add another field for liked playlists

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFrg";
    private RecyclerView rvPlaylists;
    private RecyclerView rvSaved;
    private TextView tvUsername;
    String username;
    String token;
    protected ProfileAdapter adapter;
    protected ProfileAdapter adapter2;
    protected List<Playlist> MyPlaylists;
    protected List<Playlist> SavedPlaylists;
    ParseUser currentUser;
    SharedPreferences sharedPreferences;
    List<String> fave_artists = new ArrayList<>();
    List<String> fave_tracks=new ArrayList<>();
    private SwipeRefreshLayout swipeContainerSaved;
    private SwipeRefreshLayout swipeContainerMy;
    private TextView tvTop;









    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        username = getArguments().getString("username");
        return inflater.inflate(R.layout.fragment_profile, container, false);


    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvTop = view.findViewById(R.id.tvTop);
        tvUsername.setText(username);
        rvPlaylists = view.findViewById(R.id.rvPlaylists);
        rvPlaylists.setLayoutManager(new LinearLayoutManager(getContext()));
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());



        fave_artists.addAll(sharedPreferences.getStringSet("top artists", null));
        fave_tracks.addAll(sharedPreferences.getStringSet("top tracks", null));






        Log.i(TAG, "onViewCreated: num fave artists " + fave_artists.size());

        rvSaved = view.findViewById(R.id.rvSaved);
        rvSaved.setLayoutManager(new LinearLayoutManager(getContext()));
        MyPlaylists = new ArrayList<>();
        adapter = new ProfileAdapter(getContext(), MyPlaylists);

        SavedPlaylists = new ArrayList<>();
        adapter2 = new ProfileAdapter(getContext(), SavedPlaylists);

        rvPlaylists.setAdapter(adapter);
        rvSaved.setAdapter(adapter2);

        token = sharedPreferences.getString("token", "default");

        new displayFaves().execute();

        queryPlaylists();
        try {
            queryPlaylistsSaved();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        swipeContainerSaved = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainerSaved.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                SavedPlaylists.clear();
                adapter2.clear();
                try {
                    queryPlaylistsSaved();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                swipeContainerSaved.setRefreshing(false);
            }
        });

        swipeContainerMy = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainerMy);
        swipeContainerMy.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MyPlaylists.clear();
                adapter.clear();
                queryPlaylists();
                swipeContainerMy.setRefreshing(false);
            }
        });



        ParseUser user = getArguments().getParcelable("username");

        if (user!=null){
            currentUser = user;
        }
        else{
            currentUser = ParseUser.getCurrentUser();
        }
        new Task().execute();
    }



    private void queryPlaylists() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Playlist> query = ParseQuery.getQuery(Playlist.class);
        query.whereEqualTo(Playlist.KEY_AUTHOR, ParseUser.getCurrentUser());
        query.addDescendingOrder("createdAt");

        ParseUser user = ParseUser.getCurrentUser();
        JSONArray curr_faves = user.getJSONArray(CustomUser.KEY_FAVORITES);


        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Playlist>() {
            @Override
            public void done(List<Playlist> playlists, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e("TAG", "Issue with getting playlists", e);
                    return;
                }
                // save received posts to list and notify adapter of new data
                HashSet<Object> set_faves = new HashSet<>();
                for (int i=0; i<curr_faves.length(); i++){
                    try {
                        set_faves.add(curr_faves.get(i));
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }

                for (Playlist playlist: playlists){
                    if(set_faves.contains(playlist.getObjectId())){
                        MyPlaylists.add(playlist);
                    }
                }

                //MyPlaylists.addAll(playlists);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void queryPlaylistsSaved() throws JSONException {
        ParseUser user = ParseUser.getCurrentUser();
        ParseQuery<Playlist> query = ParseQuery.getQuery(Playlist.class);
        query.addDescendingOrder("createdAt");

        for (int i = 0; i < user.getJSONArray(CustomUser.KEY_SAVED).length(); i++){

            query.whereEqualTo(Playlist.KEY_OBJECT_ID, user.getJSONArray(CustomUser.KEY_SAVED).get(i));


            query.findInBackground(new FindCallback<Playlist>() {
                @Override
                public void done(List<Playlist> playlists, ParseException e) {
                    // check for errors
                    if (e != null) {
                        Log.e("TAG", "Issue with getting posts", e);
                        return;
                    }

                    SavedPlaylists.addAll(playlists);
                    adapter2.notifyDataSetChanged();
                }
            });
        }

    }

    private class displayFaves extends AsyncTask<URL, Integer, Long> {
        String textFaves;

        @Override
        protected Long doInBackground(URL... urls) {
            Map<String, String> options = new HashMap<>();
            SpotifyApi spotifyApi = new SpotifyApi(token);

            List<String> fave_names_a = new ArrayList<>();
            List<String> fave_names_t= new ArrayList<>();

            for(int i=0; i<fave_artists.size(); i++) {
                fave_names_a.add(spotifyApi.getArtist(fave_artists.get(i)).getName());
                if(i >=3){
                    break;
                }
            }


            for(int i=0; i<fave_tracks.size(); i++) {
                if(i >=3){
                    continue;
                }
                fave_names_t.add(spotifyApi.getTrack(fave_tracks.get(i), options).getName());

            }



            textFaves = "Favorite Artists: " + " \n" + String.join(", ", fave_names_a) + " \n\n" + "Favorite Tracks: " + " \n" + String.join(", ", fave_names_t)+ " \n";




            return null;

        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            tvTop.setText(textFaves);

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }
    }

    private class Task extends AsyncTask<URL, Integer, Long> {

        @Override
        protected Long doInBackground(URL... urls) {
            Map<String, String> options = new HashMap<>();
            SpotifyApi spotifyApi = new SpotifyApi(token);

            if (fave_artists.size()==0){
                fave_artists.add(spotifyApi.getNewReleases(options).getAlbums().getItems().get(0).getArtists().get(0).getId());

            }


            Map<String, String> opt = new HashMap<>();
            if(fave_tracks.size()==0){
                opt.put("market", "ES");
                fave_tracks.add(spotifyApi.getArtistTopTracks(fave_artists.get(0), opt).getTracks().get(0).getId());
            }

            List<ArtistFull> lst_a = spotifyApi.getArtists(new ArrayList<>(fave_artists)).getArtists();
            List<TrackFull> lst_t = spotifyApi.getTracks(fave_tracks, options).getTracks();

            for (ArtistFull a: lst_a){
                Log.i(TAG, "favorite: " + a.getName());
            }

            for (TrackFull t: lst_t){
                Log.i(TAG, "favorite: " + t.getName());
            }


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


package com.example.spotifyrecommendations.fragments;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.spotifyrecommendations.GeneratePlaylist;
import com.example.spotifyrecommendations.Playlist;
import com.example.spotifyrecommendations.ProfileAdapter;
import com.example.spotifyrecommendations.R;
import com.example.spotifyrecommendations.RatingActivity;
import com.example.spotifyrecommendations.Song;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import spotify.api.spotify.SpotifyApi;
import spotify.models.playlists.requests.CreateUpdatePlaylistRequestBody;


public class ProfileFragment extends Fragment {
    private RecyclerView rvPlaylists;
    private TextView tvUsername;
    String username;
    String token;
    protected ProfileAdapter adapter;
    protected List<Playlist> allPlaylists;
    private SwipeRefreshLayout swipeContainer;
    ParseUser currentUser;





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
        tvUsername.setText(username);
        rvPlaylists = view.findViewById(R.id.rvPlaylists);
        rvPlaylists.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvPlaylists.getContext(), DividerItemDecoration.HORIZONTAL);
        rvPlaylists.addItemDecoration(dividerItemDecoration);



        allPlaylists = new ArrayList<>();
        adapter = new ProfileAdapter(getContext(), allPlaylists);

        rvPlaylists.setAdapter(adapter);

        queryPlaylists();


        ParseUser user = getArguments().getParcelable("username");

        if (user!=null){
            currentUser = user;
        }
        else{
            currentUser = ParseUser.getCurrentUser();

        }
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                adapter.clear();
                queryPlaylists();
                // ...the data has come back, add new items to your adapter...
                //adapter.addAll(...);
                // Now we call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }
        });

    }

    private void queryPlaylists() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Playlist> query = ParseQuery.getQuery(Playlist.class);
        query.whereEqualTo(Playlist.KEY_AUTHOR, ParseUser.getCurrentUser());
        query.addDescendingOrder("createdAt");


        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Playlist>() {
            @Override
            public void done(List<Playlist> playlists, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e("TAG", "Issue with getting playlists", e);
                    return;
                }

                // for debugging purposes let's print every post description to logcat


                // save received posts to list and notify adapter of new data
                allPlaylists.addAll(playlists);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private class Task extends AsyncTask<URL, Integer, Long> {

        @Override
        protected Long doInBackground(URL... urls) {

            SpotifyApi spotifyApi = new SpotifyApi(token);


            return null;
        }
        @Override
        protected void onPostExecute(Long aLong) {



        }

        @Override
        protected void onProgressUpdate(Integer... values) {




        }





} }
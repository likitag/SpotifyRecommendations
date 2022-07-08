package com.example.spotifyrecommendations.fragments;

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
import android.widget.TextView;

import com.example.spotifyrecommendations.models.CustomUser;
import com.example.spotifyrecommendations.models.Playlist;
import com.example.spotifyrecommendations.adapters.ProfileAdapter;
import com.example.spotifyrecommendations.R;
import com.example.spotifyrecommendations.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import spotify.api.spotify.SpotifyApi;

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

        rvSaved = view.findViewById(R.id.rvSaved);
        rvSaved.setLayoutManager(new LinearLayoutManager(getContext()));
        MyPlaylists = new ArrayList<>();
        adapter = new ProfileAdapter(getContext(), MyPlaylists);

        SavedPlaylists = new ArrayList<>();
        adapter2 = new ProfileAdapter(getContext(), SavedPlaylists);

        rvPlaylists.setAdapter(adapter);
        rvSaved.setAdapter(adapter2);

        queryPlaylists();
        try {
            queryPlaylists3();
        } catch (JSONException e) {
            e.printStackTrace();
        }



        ParseUser user = getArguments().getParcelable("username");

        if (user!=null){
            currentUser = user;
        }
        else{
            currentUser = ParseUser.getCurrentUser();

        }
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
//        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//
//                adapter.clear();
//                adapter2.clear();
//                queryPlaylists();
////                try {
////                    queryPlaylists2();
////                } catch (JSONException e) {
////                    e.printStackTrace();
////                }
//                // ...the data has come back, add new items to your adapter...
//                //adapter.addAll(...);
//                // Now we call setRefreshing(false) to signal refresh has finished
//                swipeContainer.setRefreshing(false);
//            }
//        });

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
                MyPlaylists.addAll(playlists);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void queryPlaylists3() throws JSONException {
        ParseUser user = ParseUser.getCurrentUser();
        ParseQuery<Playlist> query = ParseQuery.getQuery(Playlist.class);

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

    } }

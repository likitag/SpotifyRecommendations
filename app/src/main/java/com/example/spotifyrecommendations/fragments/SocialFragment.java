package com.example.spotifyrecommendations.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.spotifyrecommendations.models.Post;
import com.example.spotifyrecommendations.R;
import com.example.spotifyrecommendations.adapters.SocialAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SocialFragment extends Fragment {
    private static final String TAG = "SocialFragment";
    private RecyclerView rvPosts;
    protected SocialAdapter adapter;
    protected List<Post> allPosts;
    private SwipeRefreshLayout swipeContainer;
    List<String> key_words = new ArrayList<>();

    SearchView searchKey;



    public SocialFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_social, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchKey = view.findViewById(R.id.searchKey);
        rvPosts = view.findViewById(R.id.rvPosts);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        allPosts = new ArrayList<>();
        adapter = new SocialAdapter(getContext(), allPosts);

        rvPosts.setAdapter(adapter);

        queryPosts();

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                adapter.clear();
                if (key_words.size() > 0){
                    queryFilteredPosts();


                }
                else {
                    queryPosts();
                }

                // ...the data has come back, add new items to your adapter...
                //adapter.addAll(...);
                // Now we call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }
        });

        searchKey.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.clear();
                String str[] = query.split(" ");
                key_words = Arrays.asList(str);
                //we reverse key words in order to so that the first entered key word appears higher on feed
                Collections.reverse(key_words);



                for (String s: key_words){
                    Log.i(TAG, "word: " + s);
                }
                
                queryFilteredPosts();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length()==0){
                    adapter.clear();
                    key_words = new ArrayList<>();
                    queryPosts();

                }


                return false;
            }

        });
    }

    //give each post a value
    // match in playlist name = 5pts, description = 4pts, playlist seeds = 3pts
    // hold posts in a hashmap {queried playlist : point value}
    // sort posts from highest point value -> lowest point value.

    private void queryFilteredPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        //this checks matches in the post description
        for (String s: key_words){
            //query.whereContainedIn(Post.KEY_DESCRIPTION, key_words);
            query.whereContains(Post.KEY_DESCRIPTION, s);


            query.findInBackground(new FindCallback<Post>() {
                @Override
                public void done(List<Post> posts, ParseException e) {
                    // check for errors
                    if (e != null) {
                        Log.e("TAG", "Issue with getting posts", e);
                        return;
                    }

                    allPosts.addAll(posts);
                    adapter.notifyDataSetChanged();
                }
            });
        }

    }


    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.addDescendingOrder("createdAt");


        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e("TAG", "Issue with getting posts", e);
                    return;
                }

                // for debugging purposes let's print every post description to logcat


                // save received posts to list and notify adapter of new data
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
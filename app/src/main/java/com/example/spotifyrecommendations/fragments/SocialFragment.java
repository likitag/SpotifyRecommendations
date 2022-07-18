package com.example.spotifyrecommendations.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.spotifyrecommendations.models.CustomUser;
import com.example.spotifyrecommendations.models.Playlist;
import com.example.spotifyrecommendations.models.Post;
import com.example.spotifyrecommendations.R;
import com.example.spotifyrecommendations.adapters.SocialAdapter;
import com.parse.FindCallback;
import com.parse.PLog;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class SocialFragment extends Fragment {
    private static final String TAG = "SocialFragment";
    private RecyclerView rvPosts;
    protected SocialAdapter adapter;
    protected List<Post> allPosts;
    private TextView tvText;
    List<String> key_words = new ArrayList<>();
    Map<String, Integer> scores = new LinkedHashMap<>(); //maps post id with integer score
    Map<String, Map<String, String>> playlist_info = new HashMap<>();

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
        searchKey.setQueryHint("e.g. 'sad', 'taylor swift', 'road trip'");
        rvPosts = view.findViewById(R.id.rvPosts);
        tvText = view.findViewById(R.id.tvText);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        allPosts = new ArrayList<>();
        adapter = new SocialAdapter(getContext(), allPosts);
        rvPosts.setAdapter(adapter);

        //stores all playlist info in a hashmap
        queryPlaylistInfo();

        //queries all posts
        queryPosts();

        searchKey.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onQueryTextSubmit(String query) {
                //everytime the user submits a new search query, we clear the scores hashmap and adapter since we must reassign scores based on new input
                scores.clear();
                adapter.clear();
                String str[] = query.split(" ");
                key_words = Arrays.asList(str);
                //we reverse key words in order to so that the first entered key word appears higher on feed
                Collections.reverse(key_words);

                try {
                    //first we will query all posts that have a match in the post description
                    queryDescription();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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



    private void queryPlaylistInfo(){
        ParseQuery<Playlist> query_playlist = ParseQuery.getQuery(Playlist.class);
        query_playlist.findInBackground(new FindCallback<Playlist>() {
            @Override
            public void done(List<Playlist> playlists, ParseException e) {

                for (Playlist playlist: playlists){
                    Map<String, String> curr_info = new HashMap<>();
                    //update the scores post
                    curr_info.put("tempo", playlist.getTempo());
                    curr_info.put("name", playlist.getName());
                    curr_info.put("valence", playlist.getValence());
                    curr_info.put("genre", playlist.getGenre());
                    playlist_info.put(playlist.getObjectId(), curr_info);
                }
            }
        });

    };

    //give each post a value
    // match in playlist name = 20pts, description = 10pts, playlist genre = 5pts
    // hold posts in a hashmap {queried playlist : point value}
    // sort posts from highest point value -> lowest point value.

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void queryDescription() throws ParseException {
        ParseQuery<Post> query_description = ParseQuery.getQuery(Post.class);
        for (String s: key_words) {
            query_description.whereContains(Post.KEY_DESCRIPTION, s);
            List<Post> posts = query_description.find();
            for (Post post: posts){
                //if a post has a match in the description, we will assign it 10 points.
                scores.put(post.getObjectId(), 10);
            }
        }

        //now we will check if the user search query related to any of the playlist characteristics
        queryPlaylistNameGenre();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void queryPlaylistNameGenre() throws ParseException {
        ParseQuery<Post> query_name_genre = ParseQuery.getQuery(Post.class);
        List<Post> posts = query_name_genre.find();


        HashSet<String> word_match = new HashSet<>();

        for(Post post: posts){

            //first will check genre match
            Map<String, String> post_playlist_info = playlist_info.get(post.getPlaylistID());
            String genre = post_playlist_info.get("genre");
            if (key_words.contains(genre)){
                //add score of 20, if the playlist associated with the post has a genre contained in the key words
                if (scores.get(post.getObjectId()) == null){
                    scores.put(post.getObjectId(), 20);
                }
                else{
                    scores.put(post.getObjectId(), scores.get(post.getObjectId())+20);}
            }

            //now we check playlist name match
            word_match.addAll(key_words);
            int init_size = word_match.size();

            String name = post_playlist_info.get("name");
            String str[] = name.split(" ");
            word_match.addAll(Arrays.asList(str));
            int final_size = word_match.size();

            if (final_size < init_size + str.length){
                //number of points added to score is based on the number of word matches between key words and playlist name string
                if (scores.get(post.getObjectId()) == null){
                    scores.put(post.getObjectId(), (final_size - init_size));


                }
                else {
                    scores.put(post.getObjectId(), scores.get(post.getObjectId())+(final_size - init_size));}
            }
            word_match.clear();
        }

        List<String> ordered_posts = new ArrayList<>();
        LinkedHashMap<String, Integer> ordered_scores= new LinkedHashMap<>();

        scores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> ordered_scores.put(x.getKey(), x.getValue()));


        for (String post_id: ordered_scores.keySet()){
            if (ordered_scores.get(post_id) > 0){
                ordered_posts.add(post_id);

            }


        }

        List<Post> final_posts= new ArrayList<>();
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        for (int i = 0; i < ordered_posts.size(); i++){
            query.whereEqualTo(Post.KEY_OBJECT_ID, ordered_posts.get(i));
            final_posts.addAll(query.find());
        }

        adapter.clear();
        allPosts.removeAll(allPosts);
        allPosts.addAll(final_posts);
        adapter.notifyDataSetChanged();

    }



    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.addDescendingOrder("createdAt");

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
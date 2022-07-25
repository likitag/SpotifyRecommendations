package com.example.spotifyrecommendations.fragments;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifyrecommendations.adapters.ProfileAdapter;
import com.example.spotifyrecommendations.adapters.SocialAdapter;
import com.example.spotifyrecommendations.models.Playlist;
import com.example.spotifyrecommendations.models.Post;
import com.example.spotifyrecommendations.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spotify.api.spotify.SpotifyApi;
import spotify.models.artists.ArtistFull;
import spotify.models.generic.Image;
import spotify.models.tracks.TrackFull;


public class PostFragment extends Fragment {
    private static final String TAG = "Post Fragment";
    AutoCompleteTextView etPlaylist;
    Button btnPost;
    EditText etDescription;
    SharedPreferences sharedPreferences;
    String token;
    String spotify_id;
    String img_url;
    String description_input;
    String playlist_input;
    Playlist playlist_to_save;
    ArrayAdapter<String> adapter;
    TextView tvChoose;

    public static final ArrayList allPlaylists= new ArrayList<>();
    public static final Map<String, Playlist> playlistobjs = new HashMap<String, Playlist>();


    public PostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnPost = view.findViewById(R.id.btnPost);
        etDescription = view.findViewById(R.id.etDescription);
        etPlaylist = view.findViewById(R.id.etPlaylist);
        tvChoose = view.findViewById(R.id.tvTitleChoose);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        token = sharedPreferences.getString("token", "default");

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                description_input = etDescription.getText().toString();
                playlist_input = etPlaylist.getText().toString();
                if(checkValidInputs()) {
                    spotify_id = playlistobjs.get(playlist_input).getSpotifyid();
                    playlist_to_save = playlistobjs.get(playlist_input);
                    new getCoverImage().execute();
                }
            }
        });

        adapter = new ArrayAdapter<String> (getActivity(), android.R.layout.simple_dropdown_item_1line, allPlaylists);
        etPlaylist = view.findViewById(R.id.etPlaylist);
        etPlaylist.setAdapter(adapter);

        etPlaylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String playlist = parent.getItemAtPosition(position).toString();
                etPlaylist.setText(playlist);
            }
        });

        try {
            queryPlaylists();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private Boolean checkValidInputs() {
        if(description_input.isEmpty()) {
            Toast.makeText(getContext(), "Description can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(playlist_input.isEmpty()) {
            Toast.makeText(getContext(), "Description can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;

    }

    private void queryPlaylists() throws ParseException {
        // specify what type of data we want to query - Post.class
        ParseQuery<Playlist> query = ParseQuery.getQuery(Playlist.class);
        query.whereEqualTo(Playlist.KEY_AUTHOR, ParseUser.getCurrentUser());

        List<Playlist> playlists = query.find();
        List<String> to_add_playlists = new ArrayList<>();

        for (int i = 0; i < playlists.size(); i++){
            String name = playlists.get(i).getName();
            to_add_playlists.add(name);
            playlistobjs.put(name, playlists.get(i));
        }
        adapter.clear();
        allPlaylists.removeAll(allPlaylists);
        allPlaylists.addAll(to_add_playlists);
        adapter.notifyDataSetChanged();

    }


    private void savePost(String description, ParseUser currentUser, Playlist Playlist) throws ParseException {
        Post post = new Post();
        post.setCover(img_url);
        post.setDescription(description);
        post.setUser(currentUser);
        post.setPlaylist(Playlist);
        post.setPlaylistURI(Playlist.getURI());
        post.setPlaylistID(Playlist.getObjectId());
        post.save();
        etDescription.setText("");


    }


    private class getCoverImage extends AsyncTask<URL, Integer, Long> {
        @Override
        protected Long doInBackground(URL... urls) {
            SpotifyApi spotifyApi = new SpotifyApi(token);
            Image img = spotifyApi.getPlaylistCoverImages(spotify_id).get(0);
            img_url = img.getUrl();
            return null;

        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            try {
                savePost(description_input, ParseUser.getCurrentUser(), playlist_to_save);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }
    }

}
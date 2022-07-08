package com.example.spotifyrecommendations.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.example.spotifyrecommendations.models.Playlist;
import com.example.spotifyrecommendations.models.Post;
import com.example.spotifyrecommendations.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PostFragment extends Fragment {
    private static final String TAG = "Post Fragment";
    MultiAutoCompleteTextView etPlaylist;
    ParseUser user;
//    Spinner dropdown;
//    String selected, spinner_item;
//    TextView tvSelection;
    Button btnPost;
    EditText etDescription;



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

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString();
                if(description.isEmpty()) {
                    Toast.makeText(getContext(), "Description can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                String playlist = etPlaylist.getText().toString().substring(0, etPlaylist.getText().toString().indexOf(","));
                if(playlist.isEmpty()) {
                    Toast.makeText(getContext(), "Description can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                savePost(description, ParseUser.getCurrentUser(), (Playlist) playlistobjs.get(playlist));

            }
        });


        queryPlaylists();


//        dropdown = view.findViewById(R.id.dropdown);
//        Log.i("postFragment", "just created dropdown ");
//
//
//        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, allPlaylists);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        dropdown.setAdapter(adapter);
//        dropdown.setOnItemSelectedListener(this);
//
//
//        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//            @Override
//            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                selected = dropdown.getSelectedItem().toString();
//                Log.i("on item", "onItemSelected: " + selected);
//                spinner_item = selected;
//                dropdown.setSelection(arg2);
//                //tvSelection = view.findViewById(R.id.tvSelection);
//                //tvSelection.setText(selected);
//
//
//
//            }
//
//
//            @Override
//            public void onNothingSelected(AdapterView<?> arg0) {
//                Log.i("PostFrag" , "onNothingSelected: ");
//
//            }
//        });


        ArrayAdapter<String> adapter = new ArrayAdapter<String> (getActivity(), android.R.layout.simple_dropdown_item_1line, allPlaylists);
        etPlaylist = view.findViewById(R.id.etPlaylist);
        etPlaylist.setAdapter(adapter);
        etPlaylist.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        etPlaylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String playlist = parent.getItemAtPosition(position).toString();
                Toast toast = Toast.makeText(getContext(), playlist, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Log.i("PostFrag", etPlaylist.getText().toString().substring(0, etPlaylist.getText().toString().indexOf(",")));

                Log.i(TAG, playlistobjs.get(etPlaylist.getText().toString().substring(0, etPlaylist.getText().toString().indexOf(","))).toString());
            }
        });


    }




    private void queryPlaylists() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Playlist> query = ParseQuery.getQuery(Playlist.class);

        query.whereEqualTo(Playlist.KEY_AUTHOR, ParseUser.getCurrentUser());


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
                for (int i = 0; i < playlists.size(); i++){

                    String name = playlists.get(i).getName();
                    allPlaylists.add(name);

                    playlistobjs.put(name, playlists.get(i));


                }
               // allPlaylists.addAll(playlists);
            }
        });
    }

    private void savePost(String description, ParseUser currentUser, Playlist Playlist) {
        Post post = new Post();
        post.setDescription(description);
        post.setUser(currentUser);
        post.setPlaylist(Playlist);
        post.setPlaylistURI(Playlist.getURI());
        post.setPlaylistID(Playlist.getObjectId());
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null){
                    Log.e(TAG, "error while saving", e);
                    Toast.makeText(getContext(), "Error while saving!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.i(TAG, "Post save successful!");
                etDescription.setText("");

            }
        });


} }
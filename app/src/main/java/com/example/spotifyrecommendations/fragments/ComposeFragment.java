package com.example.spotifyrecommendations.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spotifyrecommendations.GeneratePlaylist;
import com.example.spotifyrecommendations.R;
import com.example.spotifyrecommendations.adapters.ArtistAdapter;
import com.example.spotifyrecommendations.adapters.TrackAdapter;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spotify.api.enums.QueryType;
import spotify.api.spotify.SpotifyApi;
import spotify.models.artists.ArtistFull;
import spotify.models.recommendations.RecommendationCollection;
import spotify.models.tracks.TrackFull;


public class ComposeFragment extends Fragment {

    private static final String TAG = "Compose";
    private View.OnFocusChangeListener mOnQueryTextFocusChangeListener;
    EditText etLength;
    EditText etArtists;
    EditText etGenres;
    EditText etTracks;
    Button btnSubmit;
    String token;
    TextView textView;
    int time;
    Map<String, String> extra = new HashMap<>();
    List<String> listArtistId = new ArrayList<>();
    List<String> listTrackId = new ArrayList<>();
    List<String> listGenres = new ArrayList<>();
    String artist_id;
    String track_id;
    RecommendationCollection recommendations;
    RecommendationCollection recommendations_backup;
    private static ArrayList <String> rectrackIds;
    private static ArrayList <String> rectrackIds_backup;
    Boolean isTask1Completed=false;
    EditText etName;
    Map<String, String> options = new HashMap<>();

    SharedPreferences sharedPreferences;
    List<String> fave_artists = new ArrayList<>();
    List<String> fave_tracks=new ArrayList<>();









    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etLength = view.findViewById(R.id.etLength);
        etArtists = view.findViewById(R.id.etArtists);
        etGenres = view.findViewById(R.id.etGenres);
        etTracks= view.findViewById(R.id.etTracks);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        etName = view.findViewById(R.id.etName);
        textView = view.findViewById(R.id.textView);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        token = sharedPreferences.getString("token", "default");
        fave_artists.addAll(sharedPreferences.getStringSet("top artists", null));
        fave_tracks.addAll(sharedPreferences.getStringSet("top tracks", null));

        // Locate the ListView in listview_main.xml
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            //once user submits inputs, start network requests.
            public void onClick(View v) {
                new Task().execute();
            }
        });
    }





    private class Task extends AsyncTask<URL, Integer, Long> {

        @Override
        // (1) takes users input for track&artist, uses search endpoint to get respective ids.
        // (2) uses seeds as input for recommendations endpoint
        // (3) create string array list with track ids for each recommended song
        protected Long doInBackground(URL... urls) {

            SpotifyApi spotifyApi = new SpotifyApi(token);
            List<QueryType> types = new ArrayList<>();

            types.add(QueryType.ARTIST);
            types.add(QueryType.TRACK);
            if(TextUtils.isEmpty(etTracks.getText()) && TextUtils.isEmpty(etArtists.getText())){

                int index1 = (int)(Math.random() * fave_tracks.size());
                track_id = fave_tracks.get(index1);

                int index2 = (int)(Math.random() * fave_artists.size());
                artist_id = fave_artists.get(index2);
            }

            else if(!TextUtils.isEmpty(etTracks.getText())) {
                try {
                    track_id = spotifyApi.searchItem(etTracks.getText().toString(), types, extra).getTracks().getItems().get(0).getId();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "please enter a valid track name", Toast.LENGTH_SHORT).show();
                    etTracks.setText("");
                }

                try {
                    artist_id = spotifyApi.getTrack(track_id, options).getArtists().get(0).getId();

                }
                catch (Exception e){
                    Toast.makeText(getContext(), "please enter a valid artist name", Toast.LENGTH_SHORT).show();
                    etArtists.setText("");
                }
            }

            else if(!TextUtils.isEmpty(etArtists.getText())){
                artist_id = spotifyApi.searchItem(etArtists.getText().toString(), types, extra).getArtists().getItems().get(0).getId();
                track_id = spotifyApi.getArtistTopTracks(artist_id, options).getTracks().get(0).getId();
            }



            listArtistId.add(artist_id);
            listTrackId.add(track_id);
            listGenres.add(etGenres.getText().toString());

            Map<String, String> rec_extra = new HashMap<>();

            time = Integer.parseInt(etLength.getText().toString());

            // 197 seconds is the average length of a spotify song.
            int num_tracks = time*60 / 197;

            rec_extra.put("limit", Integer.toString(num_tracks));

            recommendations = spotifyApi.getRecommendations(listArtistId, listGenres, listTrackId, rec_extra);
            recommendations_backup = spotifyApi.getRecommendations(listArtistId, listGenres, listTrackId, rec_extra);

            Log.i(TAG, "added length extra feature");


            rectrackIds = new ArrayList<>();

            for (int i = 0; i < recommendations.getTracks().size(); i++) {
                rectrackIds.add( recommendations.getTracks().get(i).getId());
            }


            return null;
        }
        @Override
        //once async task is finished executing, navigate to generate playlist intent
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            isTask1Completed = true;

            if(isTask1Completed){
                Intent i = new Intent(getActivity(), GeneratePlaylist.class);
                i.putExtra("token", token);
                i.putExtra("time", time);
                i.putStringArrayListExtra("rec tracks", (ArrayList<String>) rectrackIds);
                i.putStringArrayListExtra("listArtists", (ArrayList<String>) listArtistId);
                i.putStringArrayListExtra("listTracks", (ArrayList<String>) listTrackId);
                i.putStringArrayListExtra("listGenres", (ArrayList<String>) listGenres);
                i.putExtra("playlist name", etName.getText().toString());
                startActivity(i);
                ((Activity) getActivity()).overridePendingTransition(0, 0);
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }
}
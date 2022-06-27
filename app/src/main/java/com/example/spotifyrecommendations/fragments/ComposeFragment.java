package com.example.spotifyrecommendations.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.spotifyrecommendations.GeneratePlaylist;
import com.example.spotifyrecommendations.MainActivity;
import com.example.spotifyrecommendations.R;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import spotify.api.enums.QueryType;
import spotify.api.spotify.SpotifyApi;
import spotify.models.recommendations.RecommendationCollection;
import spotify.models.search.SearchQueryResult;


public class ComposeFragment extends Fragment {

    private static final String TAG = "Compose";
    EditText etLength;
    EditText etArtists;
    EditText etGenres;
    EditText etTracks;
    Button btnSubmit;
    String token;
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






    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        token = getArguments().get("token").toString();

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

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            //once user submits inputs, start network requests.
            public void onClick(View v) {
                //findArtist();
                //findTrack();

                new Task().execute();
            }
        });
    }

    private void findArtist() {
        kaaes.spotify.webapi.android.SpotifyApi api_kaees = new kaaes.spotify.webapi.android.SpotifyApi();
        api_kaees.setAccessToken(token);
        SpotifyService service =  api_kaees.getService();
        service.searchArtists(etArtists.getText().toString(), new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                Artist artist = artistsPager.artists.items.get(0);
                Log.i(TAG, "success: " + artist.name);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void findTrack() {

        kaaes.spotify.webapi.android.SpotifyApi api_kaees = new kaaes.spotify.webapi.android.SpotifyApi();
        api_kaees.setAccessToken(token);
        SpotifyService service =  api_kaees.getService();
        service.searchTracks(etTracks.getText().toString(), new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                Track song = tracksPager.tracks.items.get(0);
                Log.i(TAG, "success: " + song.name);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "failure: ");

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

            track_id = spotifyApi.searchItem(etTracks.getText().toString(), types, extra).getTracks().getItems().get(0).getId();
            artist_id = spotifyApi.searchItem(etArtists.getText().toString(), types, extra).getArtists().getItems().get(0).getId();

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
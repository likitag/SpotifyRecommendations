package com.example.spotifyrecommendations.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

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

    ListView list;
    ListView listViewArtists;
    TrackAdapter adapter_track;

    ArtistAdapter adapter_artist;
    List<TrackFull> tracks = new ArrayList<>();
    List<ArtistFull> artists = new ArrayList<>();

    Button btnFindTrack;
    Button btnFindArtist;
//
//    SearchView editsearch;
//    SearchView artist_search;
//    String[] animalNameList;
//    ArrayList<TrackFull> arraylist = new ArrayList<TrackFull>();









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
        etName = view.findViewById(R.id.etName);
//        list = view.findViewById(R.id.listview);
       // listViewArtists = view.findViewById(R.id.listviewArtists);
//        btnFindArtist = view.findViewById(R.id.btnFindArtist);
        //btnFindTrack = view.findViewById(R.id.btnFindTrack);


//        btnFindTrack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                adapter_track = new TrackAdapter(getContext(), tracks);
//                list.setAdapter(adapter_track);
//                list.setVisibility(View.VISIBLE);
//                new DisplayTracks(etTracks.getText().toString()).execute();
//
//            }
//        });

//        btnFindArtist.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                adapter_artist = new ArtistAdapter(getContext(), artists);
//                listViewArtists.setAdapter(adapter_artist);
//                listViewArtists.setVisibility(View.VISIBLE);
//                new DisplayArtists(etArtists.getText().toString()).execute();
//
//            }
//        });

//        editsearch = view.findViewById(R.id.search);
//        artist_search = view.findViewById(R.id.searchArtists);
//\
//
//
//        editsearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if(!hasFocus){
//                    Toast.makeText(getContext(), "switching focus", Toast.LENGTH_SHORT).show();
//
//
//                }
//            }
//        });

//        artist_search.setOnQueryTextListener(new OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                adapter_artist = new ArtistAdapter(getContext(), artists);
//                listViewArtists.setAdapter(adapter_artist);
//                listViewArtists.setVisibility(View.VISIBLE);
//                new DisplayArtists(query).execute();
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });


//        editsearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                adapter_track = new TrackAdapter(getContext(), tracks);
//                list.setAdapter(adapter_track);
//                list.setVisibility(View.VISIBLE);
//                new DisplayTracks(query).execute();
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//
//
//                return false;
//            }
//        });

        // Locate the ListView in listview_main.xml


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
            spotifyApi.searchItem(etTracks.getText().toString(), types, extra).getTracks().getItems();

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
//            etTracks.setText("");
//            etArtists.setText("");
//            etGenres.setText("");
//            etName.setText("");
//            etLength.setText("");
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

    private class DisplayTracks extends AsyncTask<URL, Integer, Long> {

        String query;

        public DisplayTracks(String query) {
            super();
            this.query = query;
            // do stuff
        }


        @Override
        // (1) takes users input for track&artist, uses search endpoint to get respective ids.
        // (2) uses seeds as input for recommendations endpoint
        // (3) create string array list with track ids for each recommended song
        protected Long doInBackground(URL... urls) {

            SpotifyApi spotifyApi = new SpotifyApi(token);
            List<QueryType> types = new ArrayList<>();

            //types.add(QueryType.ARTIST);
            types.add(QueryType.TRACK);

            spotifyApi.searchItem(query, types, extra).getTracks().getItems();

            for (int i =0; i< tracks.size(); i++){
                Log.i(TAG, "track: " + tracks.get(i).getName());
            }




            tracks.addAll(spotifyApi.searchItem(query, types, extra).getTracks().getItems());




            return null;
        }
        @Override
        //once async task is finished executing, navigate to generate playlist intent
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            adapter_track.notifyDataSetChanged();
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TrackFull track = (TrackFull) list.getAdapter().getItem(position);

                    listTrackId.add(track.getId());

                    etTracks.setText(track.getName() + ": " + track.getArtists().get(0).getName());

                    //editsearch.setQuery(track.getName() + ": " + track.getArtists().get(0).getName(), false);
                    list.setAdapter(null);
                    tracks.removeAll(tracks);
                    list.setVisibility(View.GONE);

                    if (etTracks.hasFocus()) {
                        etTracks.clearFocus();
                    }


                }
            });






        }
    }

    private class DisplayArtists extends AsyncTask<URL, Integer, Long> {

        String query;

        public DisplayArtists(String query) {
            super();
            this.query = query;
            // do stuff
        }


        @Override
        protected Long doInBackground(URL... urls) {

            SpotifyApi spotifyApi = new SpotifyApi(token);
            List<QueryType> types = new ArrayList<>();

            types.add(QueryType.ARTIST);
            //types.add(QueryType.TRACK);

            //spotifyApi.searchItem(query, types, extra).getTracks().getItems();

//            for (int i =0; i< tracks.size(); i++){
//                Log.i(TAG, "track: " + tracks.get(i).getName());
//            }




            artists.addAll(spotifyApi.searchItem(query, types, extra).getArtists().getItems());




            return null;
        }
        @Override
        //once async task is finished executing, navigate to generate playlist intent
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            adapter_artist.notifyDataSetChanged();
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ArtistFull artist = (ArtistFull) listViewArtists.getAdapter().getItem(position);

                    listArtistId.add(artist.getId());

                    etArtists.setText(artist.getName());

                    //artist_search.setQuery(artist.getName(), false);
                    listViewArtists.setAdapter(null);
                    artists.removeAll(artists);
                    list.setVisibility(View.GONE);
                    if (etArtists.hasFocus()) {
                        etArtists.clearFocus();
                    }


                }
            });




        }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
}


}
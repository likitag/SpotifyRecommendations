package com.example.spotifyrecommendations.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.text.TextUtils;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
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
    EditText etGenres;
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

    ListView listViewTracks;
    List<TrackFull> tracks_lst = new ArrayList<>();
    TrackAdapter adapter_track;
    SearchView search_tracks;
    TextView tvGenreChoices;



    ListView listViewArtists;
    SearchView search_artists;
    ArtistAdapter adapter_artist;
    List<ArtistFull> artist_lst = new ArrayList<>();










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
        etGenres = view.findViewById(R.id.etGenres);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        etName = view.findViewById(R.id.etName);
        tvGenreChoices = view.findViewById(R.id.tvSeeGenres);
        tvGenreChoices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent see_genres = new Intent(Intent.ACTION_VIEW);
                see_genres.setData(Uri.parse("https://everynoise.com/everynoise1d.cgi?scope=all"));
                startActivity(see_genres);
            }
        });
        textView = view.findViewById(R.id.textView);
        listViewTracks = view.findViewById(R.id.listviewTracks);
        listViewArtists = view.findViewById(R.id.listviewArtists);
        search_artists = view.findViewById(R.id.searchArtists);
        search_artists.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter_artist = new ArtistAdapter(getContext(), artist_lst);
                listViewArtists.setAdapter(adapter_artist);
                //listViewArtists.setVisibility(View.VISIBLE);
                new DisplayArtists(query).execute();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        search_tracks = view.findViewById(R.id.searchTracks);
        search_tracks.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter_track = new TrackAdapter(getContext(), tracks_lst);
                listViewTracks.setAdapter(adapter_track);
                //listViewTracks.setVisibility(View.VISIBLE);
                new DisplayTracks(query).execute();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        token = sharedPreferences.getString("token", "default");
        fave_artists.addAll(sharedPreferences.getStringSet("top artists", null));
        fave_tracks.addAll(sharedPreferences.getStringSet("top tracks", null));

        // Locate the ListView in listview_main.xml
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            //once user submits inputs, start network requests.
            public void onClick(View v) {
                if(TextUtils.isEmpty(etLength.getText())){
                    Toast.makeText(getContext(), "Please enter a time!", Toast.LENGTH_SHORT).show();

                }

                else if(TextUtils.isEmpty(etName.getText())){

                    Toast.makeText(getContext(), "Please enter a name!", Toast.LENGTH_SHORT).show();

                }

                else if(TextUtils.isEmpty(etGenres.getText())){
                    Toast.makeText(getContext(), "Please enter a genre!", Toast.LENGTH_SHORT).show();

                }
                else {
                    try{
                        Integer.parseInt(etLength.getText().toString());
                        if(TextUtils.isEmpty(search_tracks.getQuery())){
                            int index = (int)(Math.random() * fave_tracks.size());
                            track_id = fave_tracks.get(index);
                            listTrackId.add(track_id);
                        }

                        if(TextUtils.isEmpty(search_artists.getQuery())){
                            int index = (int)(Math.random() * fave_artists.size());
                            artist_id = fave_artists.get(index);
                            listArtistId.add(artist_id);
                        }

                        new Task().execute();
                    }
                    catch(Exception e){
                        Toast.makeText(getContext(), "Please enter a valid integer time!", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    private class DisplayTracks extends AsyncTask<URL, Integer, Long> {

        String query;
        public DisplayTracks(String query) {
            super();
            this.query = query;
        }

        @Override
        // (1) takes users input for track&artist, uses search endpoint to get respective ids.
        // (2) uses seeds as input for recommendations endpoint
        // (3) create string array list with track ids for each recommended song
        protected Long doInBackground(URL... urls) {
            SpotifyApi spotifyApi = new SpotifyApi(token);
            List<QueryType> types = new ArrayList<>();
            types.add(QueryType.TRACK);
            spotifyApi.searchItem(query, types, extra).getTracks().getItems();

            tracks_lst.addAll(spotifyApi.searchItem(query, types, extra).getTracks().getItems());
            return null;
        }
        @Override
        //once async task is finished executing, navigate to generate playlist intent
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            adapter_track.notifyDataSetChanged();
            listViewTracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    TrackFull track = (TrackFull) listViewTracks.getAdapter().getItem(position);
                    listTrackId.clear();
                    listTrackId.add(track.getId());
                    //.setText(track.getName() + ": " + track.getArtists().get(0).getName());

                    search_tracks.setQuery(track.getName() + ": " + track.getArtists().get(0).getName(), false);
                    listViewTracks.setAdapter(null);
                    tracks_lst.removeAll(tracks_lst);
                    //listViewTracks.setVisibility(View.GONE);
                }
            });
        }
    }

    private class DisplayArtists extends AsyncTask<URL, Integer, Long> {

        String query;
        public DisplayArtists(String query) {
            super();
            this.query = query;
        }

        @Override
        // (1) takes users input for track&artist, uses search endpoint to get respective ids.
        // (2) uses seeds as input for recommendations endpoint
        // (3) create string array list with track ids for each recommended song
        protected Long doInBackground(URL... urls) {
            SpotifyApi spotifyApi = new SpotifyApi(token);
            List<QueryType> types = new ArrayList<>();
            types.add(QueryType.ARTIST);
            spotifyApi.searchItem(query, types, extra).getArtists().getItems();

            artist_lst.addAll(spotifyApi.searchItem(query, types, extra).getArtists().getItems());
            return null;
        }
        @Override
        //once async task is finished executing, navigate to generate playlist intent
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            adapter_artist.notifyDataSetChanged();
            listViewArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ArtistFull artist = (ArtistFull) listViewArtists.getAdapter().getItem(position);
                    listArtistId.clear();
                    listArtistId.add(artist.getId());
                    //.setText(track.getName() + ": " + track.getArtists().get(0).getName());

                    search_artists.setQuery(artist.getName(), false);
                    listViewArtists.setAdapter(null);
                    artist_lst.removeAll(artist_lst);
                    //listViewArtists.setVisibility(View.GONE);
                }
            });
        }
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
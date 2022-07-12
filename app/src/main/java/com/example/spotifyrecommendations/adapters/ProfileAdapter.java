package com.example.spotifyrecommendations.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifyrecommendations.R;
import com.example.spotifyrecommendations.models.CustomUser;
import com.example.spotifyrecommendations.models.Playlist;
import com.example.spotifyrecommendations.models.Post;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import spotify.api.spotify.SpotifyApi;
import spotify.models.playlists.requests.CreateUpdatePlaylistRequestBody;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
    private static final String TAG = "prof adapter";
    private Context context;
    private List<Playlist> playlists;
    SharedPreferences sharedPreferences;
    String token;
   // String name = prefs.getString("name", "Blank Name"); //"Blank Name" the defaul





    public ProfileAdapter(Context context, List<Playlist> playlists) {
        this.context = context;
        this.playlists = playlists;
    }
    // Clean all elements of the recycler
    public void clear() {
        playlists.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Playlist> list) {
        playlists.addAll(list);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        token = sharedPreferences.getString("token", "default");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        holder.bind(playlist);
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }




    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvPlaylistName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaylistName = itemView.findViewById(R.id.tvPlaylistName);

//            itemView.setOnTouchListener(new View.OnTouchListener() {
//
//                GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
//
//                    @Override
//                    public void onLongPress(MotionEvent e) {
//                        super.onLongPress(e);
//                        int position = getAdapterPosition();
//                        if (position != RecyclerView.NO_POSITION) {
//                            Playlist playlist = playlists.get(position);
//
//
//                        }
//                    }
//
//                    @Override
//                    public boolean onDoubleTap(MotionEvent e) {
//                        Toast.makeText(context, "double tap", Toast.LENGTH_SHORT).show();
//                        return true;
//                    }
//                });
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    gestureDetector.onTouchEvent(event);
//                    return true;
//                }
//            });


            itemView.setOnClickListener(this);
        }

        public void bind(Playlist playlist) {
            // Bind the post data to the view elements

            tvPlaylistName.setText(playlist.getName());

        }







        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {

                // get the movie at the position, this won't work if the class is static
                Playlist playlist = playlists.get(position);
                new getSongs().execute();



                Intent spotify_app = new Intent(Intent.ACTION_VIEW);
                spotify_app.setData(Uri.parse(playlist.getURI()));

//                Intent profile = new Intent(context, MainActivity.class);
////                profile.putExtra("playlist id", playlistId);
////                profile.putExtra("token", token);
//
//                TaskStackBuilder.create(context)
//                        .addNextIntent(profile)
//                        // use this method if you want "intentOnTop" to have it's parent chain of activities added to the stack. Otherwise, more "addNextIntent" calls will do.
//                        .addNextIntentWithParentStack( spotify_app )
//                        .startActivities();

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(playlist.getURI()));
                //context.startActivity(i);


            }


        }
    }
    private class getSongs extends AsyncTask<URL, Integer, Long> {

        @Override
        protected Long doInBackground(URL... urls) {
            SpotifyApi spotifyApi = new SpotifyApi(token);
            Log.i(TAG, "token: " + token);
            Log.i(TAG, "currUser: " + spotifyApi.getCurrentUser());
            //spotifyApi.getCurrentUser();


            return null;
        }
        @Override
        protected void onPostExecute(Long aLong) {




        }

        @Override
        protected void onProgressUpdate(Integer... values) {




        }
    }


}



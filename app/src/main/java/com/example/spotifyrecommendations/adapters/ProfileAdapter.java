package com.example.spotifyrecommendations.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spotifyrecommendations.GeneratePlaylist;
import com.example.spotifyrecommendations.MainActivity;
import com.example.spotifyrecommendations.R;
import com.example.spotifyrecommendations.RatingActivity;
import com.example.spotifyrecommendations.models.CustomUser;
import com.example.spotifyrecommendations.models.Playlist;
import com.example.spotifyrecommendations.models.Post;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
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

            itemView.setOnTouchListener(new View.OnTouchListener() {

                GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){

                    @Override
                    public void onLongPress(MotionEvent e) {
                        super.onLongPress(e);
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            Playlist playlist = playlists.get(position);
                            deletePlaylist(playlist);



                        }
                    }


                });
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    Toast.makeText(context, "touch", Toast.LENGTH_SHORT).show();
                    onClick(v);
                    return true;
                }
            });


            itemView.setOnClickListener(this);
        }

        public void bind(Playlist playlist) {
            // Bind the post data to the view elements

            tvPlaylistName.setText(playlist.getName());

        }

        public Integer check_liked(String id, ParseUser user, String key) throws JSONException {

            for (int i = 0; i< user.getJSONArray(key).length(); i++){
                if (user.getJSONArray(key).get(i).equals(id)){
                    return i;
                }
            }
            return -1;
        }







        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                // get the movie at the position, this won't work if the class is static
                Playlist playlist = playlists.get(position);

                Boolean isInSaved = false;

                try {
                    if (check_liked(playlist.getObjectId(), ParseUser.getCurrentUser(), CustomUser.KEY_SAVED) == -1){
                        isInSaved = false;
                    }
                    else {
                        isInSaved = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "onClick: " + playlist.getRated());



                if (!playlist.getRated() && !isInSaved) {
                //TODO:launch rating intent
                    Intent rating = new Intent(context, RatingActivity.class);
                    rating.putExtra("new playlist id", playlist.getObjectId());
                    context.startActivity(rating);

                }
                else {
                    Intent spotify_app = new Intent(Intent.ACTION_VIEW);
                    spotify_app.setData(Uri.parse(playlist.getURI()));
                    context.startActivity(spotify_app);

                }

                new getSongs().execute();

//                Intent spotify_app = new Intent(Intent.ACTION_VIEW);
//                spotify_app.setData(Uri.parse(playlist.getURI()));

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

    private void deletePlaylist(Playlist playlist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Are you sure you want to remove this playlist? (to delete your own playlist from spotify, you must delete through Spotify app)");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Playlist");
                ParseUser user = ParseUser.getCurrentUser();
                JSONArray currSaved = user.getJSONArray("saved");
                JSONArray currFaves = user.getJSONArray("favorites");
                try {
                    int ind = check_saved(playlist.getObjectId(), user, CustomUser.KEY_SAVED);
                    if(ind!=-1) {
                        currSaved.remove(ind);
                        user.put(CustomUser.KEY_SAVED, currSaved);
                        user.saveInBackground();
                        Toast.makeText(context, "playlist was removed", Toast.LENGTH_SHORT).show();
                    }

                    int ind2 = check_saved(playlist.getObjectId(), user, CustomUser.KEY_FAVORITES);
                    if(ind2!=-1) {
                        currFaves.remove(ind2);
                        user.put(CustomUser.KEY_FAVORITES, currFaves);
                        user.saveInBackground();
                        Toast.makeText(context, "playlist was removed", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }




            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });

        builder.show();
    }

    public Integer check_saved(String id, ParseUser user, String key) throws JSONException {

        for (int i = 0; i< user.getJSONArray(key).length(); i++){
            if (user.getJSONArray(key).get(i).equals(id)){
                return i;
            }
        }
        return -1;
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



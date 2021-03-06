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
import com.example.spotifyrecommendations.PlaylistActivity;
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
    private Context mContext;
    private List<Playlist> playlists;
    SharedPreferences sharedPreferences;
    String token;
    public ProfileAdapter(Context context, List<Playlist> playlists) {
        this.mContext = context;
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_playlist, parent, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
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

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Playlist playlist = playlists.get(position);
                        deletePlaylist(playlist);
                    }

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
                if (!playlist.getRated() && !isInSaved) {
                    Intent rating = new Intent(mContext, RatingActivity.class);
                    rating.putExtra("new playlist id", playlist.getObjectId());
                    mContext.startActivity(rating);
                }
                else {
                    Intent spotify_app = new Intent(Intent.ACTION_VIEW);
                    spotify_app.setData(Uri.parse(playlist.getURI()));
                    mContext.startActivity(spotify_app);
                }
            }
        }
    }

    private void deletePlaylist(Playlist playlist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Are you sure you want to remove this playlist? (to delete your own playlist from spotify, you must delete through Spotify app)");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ParseUser user = ParseUser.getCurrentUser();
                JSONArray currSaved = user.getJSONArray("saved");
                JSONArray currFaves = user.getJSONArray("favorites");
                updateUser(user, currSaved, currFaves, playlist);
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

    private void updateUser(ParseUser user, JSONArray currSaved, JSONArray currFaves, Playlist playlist) {
        try {
            int ind_saved = checkArray(playlist.getObjectId(), user, CustomUser.KEY_SAVED);
            if(ind_saved!=-1) {
                currSaved.remove(ind_saved);
                user.put(CustomUser.KEY_SAVED, currSaved);
                user.saveInBackground();
                Toast.makeText(mContext, "playlist was removed", Toast.LENGTH_SHORT).show();
            }

            int ind_favorite = checkArray(playlist.getObjectId(), user, CustomUser.KEY_FAVORITES);
            if(ind_favorite!=-1) {
                currFaves.remove(ind_favorite);
                user.put(CustomUser.KEY_FAVORITES, currFaves);
                user.saveInBackground();
                Toast.makeText(mContext, "playlist was removed", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Integer checkArray(String id, ParseUser user, String key) throws JSONException {

        for (int i = 0; i< user.getJSONArray(key).length(); i++){
            if (user.getJSONArray(key).get(i).equals(id)){
                return i;
            }
        }
        return -1;
    }



}



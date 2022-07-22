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
import java.util.Map;

import spotify.api.spotify.SpotifyApi;
import spotify.models.playlists.requests.CreateUpdatePlaylistRequestBody;

public class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.ViewHolder> {
    private static final String TAG = "dialog adapter";
    private Context context;
    private List<String> txts;

    public DialogAdapter(Context context, List<String> txts) {
        this.context = context;
        this.txts = txts;
    }
    // Clean all elements of the recycler
    public void clear() {
        txts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<String> list) {
        txts.addAll(list);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_view_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String txt = txts.get(position);
        holder.bind(txt);
    }

    @Override
    public int getItemCount() {
        return txts.size();
    }




    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvInfo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvInfo = itemView.findViewById(R.id.text);


            itemView.setOnClickListener(this);
        }

        public void bind(String txt) {
            // Bind the post data to the view elements

            tvInfo.setText(txt);

        }



        @Override
        public void onClick(View v) {

        }
    }



}



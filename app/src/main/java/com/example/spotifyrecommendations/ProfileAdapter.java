package com.example.spotifyrecommendations;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
    private Context context;
    private List<Playlist> playlists;




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

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(playlist.getURI()));
                context.startActivity(i);


            }


        }
    }


}



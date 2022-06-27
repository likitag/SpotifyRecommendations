package com.example.spotifyrecommendations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private Context context;
    private List<Song> songs;

    public PlaylistAdapter(Context context, List<Song> songs){
        this.context= context;
        this.songs = songs;


    }

    @NonNull
    @Override
    public PlaylistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistAdapter.ViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.bind(song);

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvSong;
        TextView tvArtist;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSong = itemView.findViewById(R.id.tvSongName);
            tvArtist = itemView.findViewById(R.id.tvArtistName);


        }

        @Override
        public void onClick(View v) {

        }

        public void bind(Song song) {
            tvSong.setText(song.getName());
            tvArtist.setText(song.getArtist());
        }
    }


}

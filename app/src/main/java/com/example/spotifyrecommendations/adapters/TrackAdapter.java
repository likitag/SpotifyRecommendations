package com.example.spotifyrecommendations.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.spotifyrecommendations.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import spotify.models.tracks.TrackFull;

public class TrackAdapter extends BaseAdapter {
    Context mContext;
    LayoutInflater inflater;
    private List<TrackFull> trackList;
    private ArrayList<TrackFull> arraylist;

    public TrackAdapter(Context context, List<TrackFull> trackNamesList) {
        mContext = context;
        this.trackList = trackNamesList;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<>();
        this.arraylist.addAll(trackNamesList);

    }

    public class ViewHolder {
        TextView name;
    }

    @Override
    public int getCount() {
        return trackList.size();
    }

    @Override
    public TrackFull getItem(int position) {
        return trackList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.search_item, parent, false);
            holder.name = (TextView) view.findViewById(R.id.name);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.name.setText(trackList.get(position).getName() + ": " + trackList.get(position).getArtists().get(0).getName());
        return view;
    }
}

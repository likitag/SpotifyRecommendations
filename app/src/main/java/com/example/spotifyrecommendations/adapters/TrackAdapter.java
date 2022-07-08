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

    // Declare Variables

    Context mContext;
    LayoutInflater inflater;
    private List<TrackFull> trackList = null;
    private ArrayList<TrackFull> arraylist;



    public TrackAdapter(Context context, List<TrackFull> animalNamesList) {
        mContext = context;
        this.trackList = animalNamesList;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<TrackFull>();
        this.arraylist.addAll(animalNamesList);

    }

    public class ViewHolder {
        TextView name;
        TextView artist;
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
            view = LayoutInflater.from(mContext).inflate(R.layout.list_view_item, parent, false);
            // Locate the TextViews in listview_item.xml
            holder.name = (TextView) view.findViewById(R.id.name);
            holder.artist = (TextView) view.findViewById(R.id.artist);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(trackList.get(position).getName() + ": ");





        holder.artist.setText(trackList.get(position).getArtists().get(0).getName());
        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        trackList.clear();
        if (charText.length() == 0) {
            trackList.addAll(arraylist);
        } else {
            for (TrackFull wp : arraylist) {
                if (wp.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    trackList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

}
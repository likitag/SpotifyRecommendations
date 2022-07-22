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

import spotify.models.artists.ArtistFull;

public class ArtistAdapter extends BaseAdapter {

    // Declare Variables

    Context mContext;
    LayoutInflater inflater;
    private List<ArtistFull> artistList = null;
    private ArrayList<ArtistFull> arraylist;


    public ArtistAdapter(Context context, List<ArtistFull> animalNamesList) {
        mContext = context;
        this.artistList = animalNamesList;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<ArtistFull>();
        this.arraylist.addAll(animalNamesList);

    }

    public class ViewHolder {
        TextView artist;
    }

    @Override
    public int getCount() {
        return artistList.size();
    }

    @Override
    public ArtistFull getItem(int position) {
        return artistList.get(position);
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
            // Locate the TextViews in listview_item.xml
            holder.artist = (TextView) view.findViewById(R.id.artist);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.artist.setText(artistList.get(position).getName());


        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        artistList.clear();
        if (charText.length() == 0) {
            artistList.addAll(arraylist);
        } else {
            for (ArtistFull wp : arraylist) {
                if (wp.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    artistList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }
}

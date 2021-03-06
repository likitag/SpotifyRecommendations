package com.example.spotifyrecommendations.adapters;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.spotifyrecommendations.MainActivity;
import com.example.spotifyrecommendations.fragments.ProfileFragment;
import com.example.spotifyrecommendations.models.CustomUser;
import com.example.spotifyrecommendations.R;
import com.example.spotifyrecommendations.models.Playlist;
import com.example.spotifyrecommendations.models.Post;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;



public class SocialAdapter extends RecyclerView.Adapter<SocialAdapter.ViewHolder> {
    private static final String TAG = "Social Adapter";
    private Context mContext;
    private List<Post> posts;

    public SocialAdapter(Context context, List<Post> posts) {
        this.mContext = context;
        this.posts = posts;
    }
    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvUsername;
        TextView tvDescription;
        ImageView ivPostImage;
        ImageButton ibLikePost;
        LottieAnimationView like;
        LottieAnimationView unlike;
        ImageButton ibSave;

        private GestureDetector gestureDetector;
        public ViewHolder(@NonNull View itemView)  {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUser);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivPostImage = itemView.findViewById(R.id.ivPlaylistImage);
            ibLikePost = itemView.findViewById(R.id.ibLikePost);
            like = itemView.findViewById(R.id.lottie_heart);
            ibSave = itemView.findViewById(R.id.ibSaved);

            like.setVisibility(View.GONE);
            unlike = itemView.findViewById(R.id.lottie_break_heart);
            unlike.setVisibility(View.GONE);

            like.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    like.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    like.setVisibility(View.GONE);

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            unlike.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    unlike.setVisibility(View.VISIBLE);

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    unlike.setVisibility(View.GONE);

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            ParseUser user = ParseUser.getCurrentUser();
            ivPostImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchPlaylist();
                }
            });

            tvUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showUserAccountDetails();
                }
            });

            itemView.setOnTouchListener(new View.OnTouchListener() {
                GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener(){
                    @Override
                    public void onLongPress(MotionEvent e) {
                        saveToProfile(user);
                        super.onLongPress(e);
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        addToFavorites(user);
                        return true;
                    }
                });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });
            itemView.setOnClickListener(this);
        }

        private void showUserAccountDetails() {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(tvUsername.getText().toString() + "'s Spotifind: ");
            final TextView user_info = new TextView(mContext);
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo(CustomUser.KEY_NAME, tvUsername.getText().toString());
            int fave_size = 0;
            int saved_size = 0;
            try {
                ParseUser userOther = query.find().get(0);
                fave_size = userOther.getJSONArray(CustomUser.KEY_FAVORITES).length();
                saved_size = userOther.getJSONArray(CustomUser.KEY_SAVED).length();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            user_info.setText("   " + tvUsername.getText().toString() + " has " + fave_size + " favorited playlists and " + saved_size + " saved playlists");
            builder.setView(user_info);
            builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }

        private void addToFavorites(ParseUser user) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Post post = posts.get(position);
                try {
                    //if not currently liked:
                    ibLikePost.setImageResource(R.drawable.ic_heart_full);
                    int ind = check_in_array(post.getObjectId(), user, CustomUser.KEY_FAVORITES);
                    if (ind == -1){
                        like.setVisibility(View.VISIBLE);
                        like.playAnimation();
                        user.add(CustomUser.KEY_FAVORITES, post.getObjectId());
                        user.saveInBackground();
                    }
                    //is currently liked, need to remove
                    else {
                        unlike.setVisibility(View.VISIBLE);
                        unlike.playAnimation();
                        ibLikePost.setImageResource(R.drawable.ic_heart_empty);
                        JSONArray currFaves = user.getJSONArray("favorites");
                        currFaves.remove(ind);
                        user.put(CustomUser.KEY_FAVORITES, currFaves);
                        user.saveInBackground();
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void saveToProfile(ParseUser user) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Post post = posts.get(position);
                try {
                    ibSave.setImageResource(R.drawable.saved);
                    int ind = check_in_array(post.getPlaylistID(), user, CustomUser.KEY_SAVED);
                    if (ind == -1){
                        Toast.makeText(mContext, "saving", Toast.LENGTH_SHORT).show();
                        user.add(CustomUser.KEY_SAVED, post.getPlaylistID());
                        user.save();
                    }
                    else {
                        ibSave.setImageResource(R.drawable.ic_baseline_save_alt_24);
                        JSONArray currSaved = user.getJSONArray("saved");
                        currSaved.remove(ind);
                        Toast.makeText(mContext, "unsaving", Toast.LENGTH_SHORT).show();
                        user.put(CustomUser.KEY_SAVED, currSaved);
                        user.save();
                    }
                } catch (JSONException | ParseException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public Integer check_in_array(String id, ParseUser user, String key) throws JSONException {
            for (int i = 0; i< user.getJSONArray(key).length(); i++){
                if (user.getJSONArray(key).get(i).equals(id)){
                    return i;
                }
            }
            return -1;
        }

        public void bind(Post post) {
            bindHeart(post);
            bindSave(post);
            bindUsername(post);
            bindImage(post);
            tvDescription.setText(post.getDescription());
        }

        private void bindImage(Post post) {
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(mContext).load(post.getCover()).centerCrop().into(ivPostImage);
            }
        }

        private void bindUsername(Post post) {
            ParseUser author = post.getUser();
            try {
                String username = author.fetchIfNeeded().getString("username");
                tvUsername.setText(username);
            } catch (ParseException e) {
                Log.e("Social adapter", "Something has gone terribly wrong with Parse", e);
            }
        }

        private void bindSave(Post post) {
            try {
                if (check_in_array(post.getObjectId(), ParseUser.getCurrentUser(), CustomUser.KEY_SAVED) == -1){
                    ibSave.setImageResource(R.drawable.ic_baseline_save_alt_24);
                }
                else {
                    ibSave.setImageResource(R.drawable.saved);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void bindHeart(Post post) {
            try {
                if (check_in_array(post.getObjectId(), ParseUser.getCurrentUser(), CustomUser.KEY_FAVORITES) == -1){
                    ibLikePost.setImageResource(R.drawable.ic_heart_empty);
                }
                else {
                    ibLikePost.setImageResource(R.drawable.ic_heart_full);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) { }

        private void launchPlaylist() {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Post post = posts.get(position);
                String uri = post.getPlaylistURI();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(uri));
                mContext.startActivity(i);
            }
        }


}



}



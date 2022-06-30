package com.example.spotifyrecommendations;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.spotifyrecommendations.Post;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.List;

public class SocialAdapter extends RecyclerView.Adapter<SocialAdapter.ViewHolder> {
    private static final String TAG = "Social Adapter";
    private Context context;
    private List<Post> posts;

    public SocialAdapter(Context context, List<Post> posts) {
        this.context = context;
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
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
        TextView tvLikeNum;
        public ViewHolder(@NonNull View itemView)  {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUser);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivPostImage = itemView.findViewById(R.id.ivPlaylistImage);
            ibLikePost = itemView.findViewById(R.id.ibLikePost);
            tvLikeNum = itemView.findViewById(R.id.tvLikeNum);
            ParseUser user = ParseUser.getCurrentUser();

            ibLikePost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {


                        Post post = posts.get(position);
                        int curr_likes = post.getLikes();

                        try {
                            //if not currently liked:
                            ibLikePost.setImageResource(R.drawable.ic_heart_full);
                            int ind = check_liked(post.getObjectId(), user);
                            if (ind == -1){
                                updatePostLikes(curr_likes + 1, post.getObjectId());
                                user.add(CustomUser.KEY_FAVORITES, post.getObjectId());
                                user.saveInBackground();


                            }
                            //is currently liked, need to remove
                            else {
                                ibLikePost.setImageResource(R.drawable.ic_heart_empty);
                                JSONArray currFaves = user.getJSONArray("favorites");
                                currFaves.remove(ind);
                                user.put(CustomUser.KEY_FAVORITES, currFaves);
                                user.saveInBackground();

                                updatePostLikes(curr_likes - 1, post.getObjectId());

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    
                }
            });





            itemView.setOnClickListener(this);
        }

        public void updatePostLikes(int new_likes, String post_id) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
            Log.i(TAG, "post id " + post_id );

            query.getInBackground(post_id, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        Log.i(TAG, "here!");
                        object.put("Likes", new_likes);
                        object.saveInBackground();


                    } else {
                        Log.i(TAG, "sad");
                        Log.e(TAG, "something went wrong...", e);
                    }

                }
            });

        }

        public Integer check_liked(String post_id, ParseUser user) throws JSONException {

            for (int i = 0; i< user.getJSONArray(CustomUser.KEY_FAVORITES).length(); i++){
                if (user.getJSONArray(CustomUser.KEY_FAVORITES).get(i).equals(post_id)){
                    return i;

                }



            }

            return -1;

        }


        public void bind(Post post) {
            // Bind the post data to the view elements

            tvLikeNum.setText(post.getLikes() + " likes");

            try {
                if (check_liked(post.getObjectId(), ParseUser.getCurrentUser()) == -1){
                    ibLikePost.setImageResource(R.drawable.ic_heart_empty);
                }
                else {
                    ibLikePost.setImageResource(R.drawable.ic_heart_full);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ParseUser author = post.getUser();
            try {
               String username = author.fetchIfNeeded().getString("username");
               Log.i("Social adapter", "author: " + username);
               tvUsername.setText(username);
            } catch (ParseException e) {
                Log.e("Social adapter", "Something has gone terribly wrong with Parse", e);
            }

            Log.i("Social adapter", "description: " + post.getDescription());

            tvDescription.setText(post.getDescription());
            ParseFile image = post.getImage();

            if (image != null) {
                Glide.with(context).load(image.getUrl()).centerCrop().into(ivPostImage);
            }

            // TODO: set like to full heart image if the currentUser has liked the post


        }


        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {

                // get the movie at the position, this won't work if the class is static
                Post post = posts.get(position);

//                Intent spotify_app = new Intent(Intent.ACTION_VIEW);
//                try {
//                    spotify_app.setData(Uri.parse(post.getPlaylist().fetch().getString("URI")));
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }

//                Intent profile = new Intent(context, MainActivity.class);
////                profile.putExtra("playlist id", playlistId);
////                profile.putExtra("token", token);
//
//                TaskStackBuilder.create(context)
//                        .addNextIntent(profile)
//                        // use this method if you want "intentOnTop" to have it's parent chain of activities added to the stack. Otherwise, more "addNextIntent" calls will do.
//                        .addNextIntentWithParentStack( spotify_app )
//                        .startActivities();

//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(post.getPlaylist().get("URI").toString()));
//                context.startActivity(i);

        }
    }


} }



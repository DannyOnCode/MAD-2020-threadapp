package com.threadteam.thread.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;
import com.threadteam.thread.models.Post;
import com.threadteam.thread.models.Server;
import com.threadteam.thread.viewholders.PostsItemViewHolder;
import com.threadteam.thread.viewholders.ViewServerViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Handles binding of post object data to PostsItemViewHolder
 *
 * @author Eugene Long
 * @version 2.0
 * @since 2.0
 *
 * @see PostsItemViewHolder
 */

public class PostsItemAdapter extends RecyclerView.Adapter<PostsItemViewHolder> {

    // DATA STORE

    /** Contains all posts to be presented. */
    public List<Post> postList;

    // CONSTRUCTOR

    public PostsItemAdapter(List<Post> posts) { this.postList = posts; }

    @NonNull
    @Override
    public PostsItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        PostsItemViewHolder viewHolder;

        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.activity_partial_posts,
                parent,
                false
        );

        viewHolder = new PostsItemViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostsItemViewHolder holder, int position) {

        String imageLink = postList.get(position).get_imageLink();
        String title = postList.get(position).get_title();
        String message = postList.get(position).get_message();
        String sender = postList.get(position).get_senderUsername();
        Long tsMillis = postList.get(position).getTimestampMillis();

        String timeString = "Loading...";
        if(tsMillis != null) {
            Date date = new Date(tsMillis);
            timeString = new SimpleDateFormat("d/MM/yyyy h:mma", Locale.UK).format(date);
        }

        Picasso.get().setLoggingEnabled(true);
        Picasso.get()
                .load(imageLink)
                .into(holder.PostImageView);


        holder.PostTitleTextView.setText(title);
        holder.PostDescTextView.setText(message);
        holder.PostSenderTextView.setText(sender);
        holder.PostTimestampTextView.setText(timeString);

        LogHandler.staticPrintLog("Binding PostsItemViewHolder with data: " + postList.get(position).toString());
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }
}

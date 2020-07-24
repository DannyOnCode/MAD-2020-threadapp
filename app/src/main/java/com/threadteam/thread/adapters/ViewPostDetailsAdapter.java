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
import com.threadteam.thread.models.PostMessage;
import com.threadteam.thread.viewholders.IncomingChatMessageViewHolder;
import com.threadteam.thread.viewholders.OutgoingChatMessageViewHolder;
import com.threadteam.thread.viewholders.PostsItemViewHolder;
import com.threadteam.thread.viewholders.SystemChatMessageViewHolder;
import com.threadteam.thread.viewholders.ViewCommentDividerViewHolder;
import com.threadteam.thread.viewholders.ViewCommentMessageViewHolder;
import com.threadteam.thread.viewholders.ViewProfileCardViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Handles binding of post details and comments to the appropriate view holders
 *
 * @author Danny Chan Yu Tian
 * @version 2.0
 * @since 2.0
 *
 * @see PostsItemViewHolder
 * @see ViewCommentDividerViewHolder
 * @see ViewCommentMessageViewHolder
 */
public class ViewPostDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    // DATA STORE
    /** Contains post and its details. */
    public Post post;

    /** Contains all Post Comments objects to be displayed. */
    public List<PostMessage> postMessageList;

    public ViewPostDetailsAdapter(Post post, List<PostMessage> commentMessages) {

        this.post = post;
        this.postMessageList = commentMessages;
    }

    //Get View Type for each position
    public int getItemViewType(int position) {
        if(position == 0){
            return position;
        }
        else if(position == 1){
            return position;
        }
        else{
            return 2;
        }
    }
    //Inflate RecyclerView with viewholder
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch(viewType){
            case 0:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_partial_postdetail,parent,false);
                return new PostsItemViewHolder(view);
            case 1:
                View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_partial_comment_divider,parent,false);
                return new ViewCommentDividerViewHolder(view1);
            case 2:
                View view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_partial_comments,parent,false);
                return new ViewCommentMessageViewHolder(view2);
        }
        View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_partial_posts,parent,false);
        return new ViewProfileCardViewHolder(view1);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch(holder.getItemViewType()) {

            case 0:
                //Post Card
                PostsItemViewHolder postHolder = (PostsItemViewHolder) holder;
                String imageLink = post.get_imageLink();
                String title = post.get_title();
                String message = post.get_message();
                String sender = post.get_senderUsername();
                Long tsMillis = post.getTimestampMillis();

                String timeString = "Loading...";
                if(tsMillis != null) {
                    Date date = new Date(tsMillis);
                    timeString = new SimpleDateFormat("d/MM/yyyy h:mma", Locale.UK).format(date);
                }

                if(imageLink != null) {
                    Picasso.get()
                            .load(imageLink)
                            .into(postHolder.PostImageView);
                }

                postHolder.PostTitleTextView.setText(title);
                postHolder.PostDescTextView.setText(message);
                postHolder.PostSenderTextView.setText(sender);
                postHolder.PostTimestampTextView.setText(timeString);

                LogHandler.staticPrintLog("Binding PostsItemViewHolder with data: " + post.toString());

            case 1:
                //Divider
                break;
            case 2:
                //Comment Card
                ViewCommentMessageViewHolder commentMessageViewHolder = (ViewCommentMessageViewHolder) holder;
                String senderName = postMessageList.get(position-2).get_senderUsername();
                String senderTitle = postMessageList.get(position-2).get_title();
                String senderLevel = postMessageList.get(position-2).get_level();
                String senderMessage = postMessageList.get(position-2).get_comment();
                Long timeStamp = postMessageList.get(position-2).getTimestampMillis();
                Integer textColor = postMessageList.get(position-2).get_displayColour();

                String commentTimeString = "Loading...";
                if(timeStamp != null) {
                    Date date = new Date(timeStamp);
                    commentTimeString = new SimpleDateFormat("d/MM/yyyy h:mma", Locale.UK).format(date);
                }

                commentMessageViewHolder.UserTextView.setText(senderName);
                commentMessageViewHolder.TitleTextView.setText(senderTitle);
                commentMessageViewHolder.LevelTextView.setText(senderLevel);
                commentMessageViewHolder.CommentTextView.setText(senderMessage);
                commentMessageViewHolder.TimestampTextView.setText(commentTimeString);
                commentMessageViewHolder.UserTextView.setTextColor(textColor);
                commentMessageViewHolder.TitleTextView.setTextColor(textColor);
                commentMessageViewHolder.LevelTextView.setTextColor(textColor);
                break;
        }
    }

    @Override
    public int getItemCount() {
        // (+ 2) for post card and divider ViewHolder
        if(post == null){
            return 0;
        }

        return 2 + postMessageList.size();
    }
}
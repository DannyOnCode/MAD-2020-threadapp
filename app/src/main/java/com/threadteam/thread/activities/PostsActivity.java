package com.threadteam.thread.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.threadteam.thread.R;
import com.threadteam.thread.RecyclerTouchListener;
import com.threadteam.thread.adapters.PostsItemAdapter;
import com.threadteam.thread.abstracts.ServerBaseActivity;
import com.threadteam.thread.interfaces.RecyclerViewClickListener;
import com.threadteam.thread.models.Post;

import java.util.ArrayList;

/**
 * This activity class handles displaying the server's posts.
 *
 * @author Danny Chan Yu Tian
 * @author Notfications: Thabith
 * @version 2.0
 * @since 2.0
 */

public class PostsActivity extends ServerBaseActivity {

    // DATA STORE

    /** Adapter object for PostsRecyclerView. */
    private PostsItemAdapter adapter;

    /** Flag for scrolling to the latest post upon a new post being sent. */
    private Boolean scrollToLatestPost = false;

    // VIEW OBJECTS

    /**
     * Handles the display of all server posts.
     * Uses PostsItemAdapter as its adapter.
     * @see PostsItemAdapter
     */

    private RecyclerView PostsRecyclerView;

    // INITIALISE LISTENERS

    /**
     *  Handles the loading of posts into the adapter on post added/deleted/changed events.
     *
     *  Database Path:      root/posts/(serverId)
     *  Usage:              ChildEventListener
     */

    private ChildEventListener postListener = new ChildEventListener() {

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            logHandler.printLogWithMessage("Post added/loaded!");

            if(dataSnapshot.getKey() == null) {
                logHandler.printDatabaseResultLog(".getKey()", "Post ID", "postListener", "null");
                return;
            }

            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "Post Values", "postListener", "null");
                return;
            }



            String imageLink = (String) dataSnapshot.child("_imageLink").getValue();
            String title = (String) dataSnapshot.child("_title").getValue();
            String message = (String) dataSnapshot.child("_message").getValue();
            String senderUID = (String) dataSnapshot.child("_senderUID").getValue();
            String sender = (String) dataSnapshot.child("_sender").getValue();
            Long timestampMillis = (Long) dataSnapshot.child("timestamp").getValue();

            Post post;

            if (title == null) {
                logHandler.printDatabaseResultLog(".child(\"_title\").getValue()", "Title", "postListener", "null");
                return;
            } else if (message == null) {
                logHandler.printDatabaseResultLog(".child(\"_message\").getValue()", "Message", "postListener", "null");
                return;
            } else if (senderUID == null) {
                logHandler.printDatabaseResultLog(".child(\"_senderUID\").getValue()", "Sender ID", "postListener", "null");
                return;
            } else if (sender == null) {
                logHandler.printDatabaseResultLog(".child(\"_sender\").getValue()", "Sender Username", "postListener", "null");
                return;
            } else if (timestampMillis == null) {
                logHandler.printDatabaseResultLog(".child(\"timestamp\").getValue()", "Timestamp", "postListener", "null");
                post = new Post(imageLink, title, message, senderUID, sender);
            } else {
                if(imageLink != null){
                    post = new Post(imageLink, title, message, senderUID, sender, timestampMillis);
                }
                else{
                    logHandler.printDatabaseResultLog(".child(\"_imageLink\").getValue()", "Image Link", "postListener", "null");
                    post= new Post(null,title, message, senderUID, sender, timestampMillis);
                }
            }

            post.set_id(dataSnapshot.getKey());


            // CHECK IF SERVER IS ALREADY DISPLAYED
            boolean postIsDisplayed = false;

            for(int i=0; i<adapter.postList.size(); i++) {
                if(adapter.postList.get(i).get_id().equals(post.get_id())) {
                    adapter.postList.set(i, post);
                    postIsDisplayed = true;
                    break;
                }
            }

            logHandler.printDatabaseResultLog("", "Post", "postListener", post.toString());
            if(!postIsDisplayed) {
                adapter.postList.add(post);
            }
            adapter.notifyItemInserted(adapter.postList.size());

            if(scrollToLatestPost) {
                logHandler.printLogWithMessage("scrollToLatestPost = true; scrolling to latest post now!");
                PostsRecyclerView.smoothScrollToPosition(Math.max(0, adapter.postList.size() - 1));
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            logHandler.printLogWithMessage("Post changed! Updating timestamp!");

            // Note: Future features may introduce editing of posts, but for now it's just for updating the timestamp
            //       when Firebase Cloud Functions gives us the server side timestamp.

            if(dataSnapshot.getKey() == null) {
                logHandler.printDatabaseResultLog(".getKey()", "Post ID", "postListener", "null");
                return;
            }
            logHandler.printDatabaseResultLog(".getKey()", "Post ID", "postListener", dataSnapshot.getKey());

            Long timestampMillis = (Long) dataSnapshot.child("timestamp").getValue();

            if(timestampMillis == null) {
                logHandler.printDatabaseResultLog(".child(\"timestamp\").getValue()", "Timestamp", "postListener", "null");
                return;
            }
            logHandler.printDatabaseResultLog(".child(\"timestamp\").getValue()", "Timestamp", "postListener", timestampMillis.toString());

            String postId = dataSnapshot.getKey();

            for(int i=0; i<adapter.postList.size(); i++) {
                if(adapter.postList.get(i).get_id() != null && adapter.postList.get(i).get_id().equals(postId)) {
                    adapter.postList.get(i).setTimestampMillis(timestampMillis);
                    adapter.notifyItemChanged(i);
                    return;
                }
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            logHandler.printLogWithMessage("Post removed! Deleting post from adapter!");

            if(dataSnapshot.getKey() == null) {
                logHandler.printDatabaseResultLog(".getKey()", "Post ID", "postListener", "null");
                return;
            }
            logHandler.printDatabaseResultLog(".getKey()", "Post ID", "postListener", dataSnapshot.getKey());

            for(int i=0; i<adapter.postList.size(); i++) {
                if(adapter.postList.get(i).get_id() != null &&
                        adapter.postList.get(i).get_id().equals(dataSnapshot.getKey())) {
                    adapter.postList.remove(i);
                    adapter.notifyItemRemoved(i);
                    return;
                }
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            logHandler.printDatabaseErrorLog(databaseError);
        }
    };

    // DEFAULT SUPER METHODS

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serverId = getIntent().getStringExtra("SERVER_ID");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {

        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    // ABSTRACT OVERRIDE METHODS

    @Override
    protected int setLayoutIDForContentView() {
        return R.layout.activity_posts;
    }

    @Override
    protected AppCompatActivity setCurrentActivity() {
        return PostsActivity.this;
    }

    @Override
    protected String setTitleForActivity() {
        return "Posts";
    }

    @Override
    protected ImageButton setMainActionButton() {
        View bottomToolbarView = findViewById(R.id.postsBottomToolbarInclude);
        ImageButton mainActionButton = (ImageButton) bottomToolbarView.findViewById(R.id.mainActionFAB);

        Drawable icon = ContextCompat.getDrawable(this, R.drawable.round_add_white_24);
        mainActionButton.setImageDrawable(icon);

        mainActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToAddPost = new Intent(currentActivity, AddPostActivity.class);
                PutExtrasForServerIntent(goToAddPost);
                currentActivity.startActivity(goToAddPost);

                onStop();
            }
        });

        return mainActionButton;
    }

    @Override
    protected Integer setTopNavToolbarIncludeId() {
        return R.id.postsNavBarInclude;
    }

    @Override
    protected Integer setBottomToolbarAMVIncludeId() {
        return R.id.postsBottomToolbarInclude;
    }

    @Override
    protected void BindViewObjects() {
        PostsRecyclerView = (RecyclerView) findViewById(R.id.postsRecyclerView);
    }

    @Override
    protected ConstraintLayout setBaseLayer() {
        return (ConstraintLayout) findViewById(R.id.basePostsConstraintLayout);
    }

    @Override
    protected void SetupViewObjects() {
        adapter = new PostsItemAdapter(new ArrayList<Post>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        PostsRecyclerView.setLayoutManager(layoutManager);
        PostsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        PostsRecyclerView.setAdapter(adapter);

        PostsRecyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(this, PostsRecyclerView, new RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        handleTransitionIntoPost(position);
                    }
                })
        );

        final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();

                if(dy < 0 && scrollToLatestPost) {
                    logHandler.printLogWithMessage("Scrolled up, toggled scrollToLatestPost = false!");
                    scrollToLatestPost = false;

                } else if(llm != null && llm.findLastCompletelyVisibleItemPosition() == adapter.postList.size()-1) {
                    logHandler.printLogWithMessage("Scrolled to bottom of posts, setting scrollToLatestPost = true!");
                    scrollToLatestPost = true;
                }

            }
        };

        PostsRecyclerView.addOnScrollListener(scrollListener);
    }

    @Override
    protected void AttachOnStartListeners() {
        databaseRef.child("posts")
                .child(serverId)
                .addChildEventListener(postListener);
    }

    @Override
    protected void DestroyOnStartListeners() {
        if(postListener != null) {
            databaseRef.removeEventListener(postListener);
        }
    }

    @Override
    protected int setCurrentMenuItemID() {
        return R.id.postsMenuItem;
    }

    // ACTIVITY SPECIFIC METHODS

    /**
     * Sends the user to the ViewPostDetails activity and loads the appropriate data based on which post the user clicked.
     * @param position The index of the post the user clicked on.
     * @see ViewPostDetailsActivity
     */

    private void handleTransitionIntoPost(Integer position) {
        logHandler.printLogWithMessage("User tapped on a server!");
        Intent goToViewPostDetails = new Intent(currentActivity, ViewPostDetailsActivity.class);
        goToViewPostDetails.putExtra("POST_ID", adapter.postList.get(position).get_id());
        PutExtrasForServerIntent(goToViewPostDetails);
        currentActivity.startActivity(goToViewPostDetails);
    }
}

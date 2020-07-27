package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.abstracts.ServerBaseActivity;
import com.threadteam.thread.adapters.ViewMemberAdapter;
import com.threadteam.thread.libraries.Progression;
import com.threadteam.thread.R;
import com.threadteam.thread.adapters.ViewPostDetailsAdapter;
import com.threadteam.thread.models.Post;
import com.threadteam.thread.models.PostMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * This activity class handles the viewing of a post and commenting on the post
 *
 * @author Danny Chan Yu Tian
 * @version 2.0
 * @since 2.0
 */
public class ViewPostDetailsActivity extends ServerBaseActivity {

    // DATA STORE
    /** Adapter object for viewPostDetailsRecyclerView. */
    private ViewPostDetailsAdapter adapter;

    /** Contains the current user username. Used to send commments. */
    private String username;

    /** Contains the comment sender userExp. */
    private int userExp;

    /** Contains the post ID of the current viewed post. */
    private String postID;

    /** Contain the list of titles for the server. */
    private List<String> titleData;

    /** Initial scroll boolean. */
    private Boolean scrollToLatestMessage = false;

    /** Contains the list of user and their Exp*/
    public HashMap<String, Integer> userExpList = new HashMap<>();


    // VIEW OBJECTS
    /**
     * Displays post details and comments of the current post.
     * Uses ViewPostDetailsAdapter as its adapter.
     * @see ViewPostDetailsAdapter
     */
    private RecyclerView viewPostDetailsRecyclerView;

    /** Contains text data to be sent upon user tapping SendCommentButton. */
    private EditText CommentEditText;

    /** Triggers sending of comment data to the postmessage. */
    private ImageButton SendCommentButton;


    // INITIALISE LISTENERS
    /**
     * Retrieves the username of the current user.
     *
     *  Database Path:      root/users/(currentUser.getUid())/_username
     *  Usage:              Single ValueEventListener
     */
    private ValueEventListener getUsername = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "Current Username", "getUsername", "null");
                username = "anonymous";
                return;
            }
            username = (String) dataSnapshot.getValue();
            logHandler.printDatabaseResultLog(".getValue()", "Current Username", "getUsername", username);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            logHandler.printDatabaseErrorLog(databaseError);
        }
    };

    /**
     *  Retrieves all user in server and their exp into HashMap to be sent to Adapter.
     *
     *  Database Path:      root/members/(serverId)
     *  Usage:              ValueEventListener
     */
    ValueEventListener mapUsersToExp = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "Member Data", "mapUsersToExp", "null");
                return;
            }

            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                if (snapshot.getKey() == null) {
                    logHandler.printDatabaseResultLog("snapshot.getKey()", "Member ID", "mapUsersToExp", "null");
                    return;
                }

                String memberId = snapshot.getKey();
                logHandler.printDatabaseResultLog("snapshot.getKey()", "Member ID", "mapUsersToExp", memberId);

                if (snapshot.getValue() == null) {
                    logHandler.printDatabaseResultLog("snapshot.getValue()", "Member EXP", "mapUsersToExp", "null");
                    return;
                }

                int memberExp = ((Long) snapshot.getValue()).intValue();
                logHandler.printDatabaseResultLog("snapshot.getValue()", "Member EXP", "mapUsersToExp", Integer.toString(memberExp));

                userExpList.put(memberId, memberExp);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            logHandler.printDatabaseErrorLog(databaseError);
        }
    };

    /**
     *  Retrieve the selected Post details and assign them to a post object.
     *
     *  Database Path:      root/posts/(serverId)/(postID)
     *  Usage:              ValueEventListener
     */
    private ValueEventListener getPostDetails = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            logHandler.printLogWithMessage("Post added/loaded!");
            if (dataSnapshot.getKey() == null) {
                logHandler.printDatabaseResultLog(".getKey()", "Message ID", "getPostDetails", "null");
                return;
            }

            if (dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "Message Values", "getPostDetails", "null");
                return;
            }

            String postImageURL = (String) dataSnapshot.child("_imageLink").getValue();
            String title = (String) dataSnapshot.child("_title").getValue();
            String message = (String) dataSnapshot.child("_message").getValue();
            String senderUID = (String) dataSnapshot.child("_senderUID").getValue();
            String sender = (String) dataSnapshot.child("_sender").getValue();
            Long timestampMillis = (Long) dataSnapshot.child("timestamp").getValue();

            Post postDetail;

            if (title == null) {
                logHandler.printDatabaseResultLog(".child(\"_title\").getValue()", "Title", "getPostDetails", "null");
                return;
            } else if (message == null) {
                logHandler.printDatabaseResultLog(".child(\"_message\").getValue()", "Message", "getPostDetails", "null");
                return;
            } else if (senderUID == null) {
                logHandler.printDatabaseResultLog(".child(\"_senderUID\").getValue()", "Sender ID", "getPostDetails", "null");
                return;
            } else if (sender == null) {
                logHandler.printDatabaseResultLog(".child(\"_sender\").getValue()", "Sender Username", "getPostDetails", "null");
                return;
            } else if (timestampMillis == null) {
                logHandler.printDatabaseResultLog(".child(\"timestamp\").getValue()", "Timestamp", "getPostDetails", "null");
                postDetail = new Post(postImageURL, title, message, senderUID, sender);
            } else {
                if (postImageURL != null) {
                    postDetail = new Post(postImageURL, title, message, senderUID, sender, timestampMillis);
                } else {
                    logHandler.printDatabaseResultLog(".child(\"_imageLink\").getValue()", "Image Link", "getPostDetails", "null");
                    postDetail = new Post(null, title, message, senderUID, sender, timestampMillis);
                }
            }

            postDetail.set_id(postID);

            logHandler.printDatabaseResultLog("", "Post", "postListener", postDetail.toString());

            adapter.post = postDetail;
            adapter.notifyDataSetChanged();
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            logHandler.printDatabaseErrorLog(databaseError);
        }
    };

    /**
     *  Retrieves all member titles for the current server and loads it into the adapter.
     *
     *  Database Path:      root/titles/(serverId)
     *  Usage:              ValueEventListener
     */
    private ValueEventListener retrieveMemberTitles = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "Member Title Data", "retrieveMemberTitles", "null");
                return;
            }

            titleData = new ArrayList<>(Collections.nCopies(10, ""));

            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                if (snapshot.getKey() == null) {
                    logHandler.printDatabaseResultLog("snapshot.getKey()", "Member Title Index", "retrieveMemberTitles", "null");
                    return;
                }

                int index = Integer.parseInt(snapshot.getKey());
                logHandler.printDatabaseResultLog("snapshot.getValue()", "Member Title", "retrieveMemberTitles", Integer.toString(index));

                String title = (String) snapshot.getValue();
                if (title == null) {
                    logHandler.printDatabaseResultLog("snapshot.getValue()", "Member Title", "retrieveMemberTitles", "null");
                    return;
                }
                logHandler.printDatabaseResultLog("snapshot.getValue()", "Member Title", "retrieveMemberTitles", title);

                titleData.set(index, title);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            logHandler.printDatabaseErrorLog(databaseError);
        }
    };

    /**
     *  Handles the loading of comment messages into the adapter.
     *
     *  Database Path:      root/postmessages/(serverId)/(postID)
     *  Usage:              ChildEventListener
     */
    private ChildEventListener commentMessageListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            logHandler.printLogWithMessage("Chat message added/loaded!");

            if(dataSnapshot.getKey() == null) {
                logHandler.printDatabaseResultLog(".getKey()", "Comment ID", "commentMessageListener", "null");
                return;
            }

            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "Comment Values", "commentMessageListener", "null");
                return;
            }

            String senderUID = (String) dataSnapshot.child("_senderUID").getValue();
            String senderName = (String) dataSnapshot.child("_senderusername").getValue();
            String commentMessage = (String) dataSnapshot.child("_comment").getValue();
            Long timestampMillis = (Long) dataSnapshot.child("timestamp").getValue();


            PostMessage postMessage;

            if (senderUID == null) {
                logHandler.printDatabaseResultLog(".child(\"_senderUID\").getValue()", "Sender ID", "commentMessageListener", "null");
                return;
            } else if (senderName == null) {
                logHandler.printDatabaseResultLog(".child(\"_sender\").getValue()", "Sender Name", "commentMessageListener", "null");
                return;
            } else if (commentMessage == null) {
                logHandler.printDatabaseResultLog(".child(\"_message\").getValue()", "Comment", "commentMessageListener", "null");
                return;
            } else if (timestampMillis == null) {
                logHandler.printDatabaseResultLog(".child(\"timestamp\").getValue()", "Timestamp", "commentMessageListener", "null");
                postMessage = new PostMessage(senderUID, senderName, commentMessage);
            } else {
                postMessage = new PostMessage(senderUID, senderName, commentMessage, timestampMillis);
            }

            userExp = userExpList.get(senderUID);
            int level = Progression.ConvertExpToLevel(userExp);
            int stage = Progression.ConvertLevelToStage(level);
            Integer colorInt = Progression.GetDefaultColorIntForStage(stage);
            String title = "";
            if(titleData != null){
                if(titleData.size() > 0 && !titleData.get(stage).equals("")) {
                    title = titleData.get(stage);
                } else {
                    title = Progression.GetDefaultTitleForStage(stage);
                }
            }
            else{
                title = Progression.GetDefaultTitleForStage(stage);
            }
            postMessage.set_level(String.valueOf(level));
            postMessage.set_title(title);
            postMessage.set_displayColour(colorInt);

            postMessage.set_id(dataSnapshot.getKey());

            logHandler.printDatabaseResultLog("", "Comment Message", "commentMessageListener", postMessage.toString());

            adapter.postMessageList.add(0,postMessage);
            adapter.notifyItemInserted(2);

            if(scrollToLatestMessage) {
                logHandler.printLogWithMessage("scrollToLatestMessage = true; scrolling to latest message now!");
                viewPostDetailsRecyclerView.smoothScrollToPosition(2);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    //DEFAULT SUPER METHODS
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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


    // ABSTRACT OVERRIDE METHODS
    @Override
    protected ConstraintLayout setBaseLayer() {
        return (ConstraintLayout) findViewById(R.id.baseViewPostConstraintLayout);
    }

    //Getting of Post ID
    @Override
    protected void HandleAdditionalIntentExtras(){
        final Intent dataReceiver = getIntent();
        postID = dataReceiver.getStringExtra("POST_ID");

        if (postID == null) {
            logHandler.printGetExtrasResultLog("POST_ID", "null");
        } else {
            logHandler.printGetExtrasResultLog("POST_ID", postID);
        }
    }



    @Override
    protected int setLayoutIDForContentView() {
        return R.layout.activity_view_post;
    }

    @Override
    protected AppCompatActivity setCurrentActivity() {
        return ViewPostDetailsActivity.this;
    }

    @Override
    protected String setTitleForActivity() {
        return "Post";
    }

    @Override
    protected ImageButton setMainActionButton() {
        return null;
    }

    @Override
    protected Integer setTopNavToolbarIncludeId() {
        return R.id.viewPostNavBarInclude;
    }

    @Override
    protected Integer setBottomToolbarAMVIncludeId() {
        return null;
    }

    @Override
    protected void BindViewObjects() {
        viewPostDetailsRecyclerView = findViewById(R.id.viewPostRecyclerView);
        CommentEditText = findViewById(R.id.commentEditText);
        SendCommentButton = findViewById(R.id.sendCommentButton);
    }

    @Override
    protected void SetupViewObjects() {
        //Populate Send Comment button
        SendCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });

        //Setting up Adapter
        adapter = new ViewPostDetailsAdapter(new Post(),new ArrayList<PostMessage>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        viewPostDetailsRecyclerView.setLayoutManager(layoutManager);
        viewPostDetailsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        viewPostDetailsRecyclerView.setAdapter(adapter);

        // Use RecyclerView as scrim to dismiss keyboard
        viewPostDetailsRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    InputMethodManager imm = (InputMethodManager) currentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    View view  = currentActivity.getCurrentFocus();
                    if (view == null) {
                        view = new View(currentActivity);
                    }
                    if (imm != null && imm.isAcceptingText()) {
                        logHandler.printLogWithMessage("RecyclerView detected touch, hiding keyboard (if active)!");
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });

        // Load more messages (if possible) when top is reached.
        final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();

                if(dy < 0 && scrollToLatestMessage) {
                    logHandler.printLogWithMessage("Scrolled up, toggled scrollToLatestMessage = false!");
                    scrollToLatestMessage = false;

                } else if(llm != null && llm.findLastCompletelyVisibleItemPosition() == (adapter.getItemCount() - 1)) {
                    logHandler.printLogWithMessage("Scrolled to bottom of comments, setting scrollToLatestMessage = true!");
                    scrollToLatestMessage = true;
                }

            }
        };
        viewPostDetailsRecyclerView.addOnScrollListener(scrollListener);
    }


    @Override
    protected void AttachOnStartListeners() {
        databaseRef.child("users")
                .child(currentUser.getUid())
                .child("_username")
                .addListenerForSingleValueEvent(getUsername);

        databaseRef.child("members")
                .child(serverId)
                .addValueEventListener(mapUsersToExp);

        databaseRef.child("posts")
                .child(serverId)
                .child(postID)
                .addValueEventListener(getPostDetails);

        databaseRef.child("titles")
                .child(serverId)
                .addValueEventListener(retrieveMemberTitles);

        databaseRef.child("postmessages")
                .child(serverId)
                .child(postID)
                .addChildEventListener(commentMessageListener);
    }

    @Override
    protected void DestroyOnStartListeners() {
        if(getPostDetails != null) {
            databaseRef.removeEventListener(getPostDetails);
        }
        if(retrieveMemberTitles != null) {
            databaseRef.removeEventListener(retrieveMemberTitles);
        }
        if(mapUsersToExp != null) {
            databaseRef.removeEventListener(mapUsersToExp);
        }
        if(commentMessageListener != null) {
            databaseRef.removeEventListener(commentMessageListener);
        }
    }

    @Override
    protected int setCurrentMenuItemID() {
        return NO_MENU_ITEM_FOR_ACTIVITY;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            logHandler.printLogWithMessage("User tapped on Back Button!");
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    // ACTIVITY SPECIFIC METHODS
    private void sendComment(){
        if(username == null) {
            logHandler.printLogWithMessage("Username is null (which it shouldn't be)! Aborting sendComment()!");
            return;
        }

        String comment = CommentEditText.getText().toString();

        // Stop newline spamming by doing some formatting
        String[] messageLines = comment.split("\n");
        StringBuilder formattedComment = new StringBuilder();
        int previousNewlines = 0;
        for(String line: messageLines) {
            String trimmedLine = line.trim();
            if(previousNewlines > 2 && trimmedLine.length() == 0) {
                continue;
            } else if(trimmedLine.length() == 0) {
                previousNewlines += 1;
            } else {
                previousNewlines = 0;
            }
            formattedComment.append(trimmedLine).append("\n");
        }

        // do a final trim
        formattedComment = new StringBuilder(formattedComment.toString().trim());
        logHandler.printLogWithMessage("User submitted comment: " + comment + " and was it was formatted as: " + formattedComment.toString());

        CommentEditText.setText(null);

        if(formattedComment.length() > 0) {
            HashMap<String, Object> commentMessageHashMap = new HashMap<>();
            commentMessageHashMap.put("_senderUID", currentUser.getUid());
            commentMessageHashMap.put("_senderusername", username);
            commentMessageHashMap.put("_comment", formattedComment.toString());
            commentMessageHashMap.put("timestamp", System.currentTimeMillis());
            databaseRef.child("postmessages").child(serverId).child(postID).push().setValue(commentMessageHashMap);

            scrollToLatestMessage = true;
            AddExpForServerMember(currentUser.getUid(), serverId, 1, 60);
        } else {
            logHandler.printLogWithMessage("No comment was pushed because there was no text after formatting!");
        }
    }
}
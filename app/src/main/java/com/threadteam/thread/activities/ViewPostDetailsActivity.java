package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.threadteam.thread.R;
import com.threadteam.thread.Utils;
import com.threadteam.thread.adapters.ChatMessageAdapter;
import com.threadteam.thread.adapters.ViewPostDetailsAdapter;
import com.threadteam.thread.models.ChatMessage;
import com.threadteam.thread.models.Post;
import com.threadteam.thread.models.PostMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ViewPostDetailsActivity extends  _ServerBaseActivity{

    //TODO: when the post is freshly created the items are aligned not to the top parent

    // DATA STORE
    //
    // adapter:                 ADAPTER FOR CHAT MESSAGE RECYCLER VIEW.
    //                          HANDLES STORAGE OF DISPLAYED CHAT MESSAGE DATA AS WELL.
    // username:                CONTAINS CURRENT USER'S USERNAME. USED TO SEND MESSAGES.

    private ViewPostDetailsAdapter adapter;
    private String username;
    private int userExp;
    private int userLevel;
    private String postID;
    private List<String> titleData;
    private Boolean scrollToLatestMessage = false;
    public HashMap<String, Integer> userExpList = new HashMap<>();

    // VIEW OBJECTS
    //
    // viewPostDetailsRecyclerView: DISPLAYS ALL COMMENT MESSAGES AND POST IN THE POST. USES adapter AS ITS ADAPTER.
    // CommentEditText:         CONTAINS TEXT DATA TO BE SENT UPON USER TAPPING SendCommentButton.
    // SendCommentButton:           TRIGGERS SENDING OF TEXT DATA TO THE SERVER

    private RecyclerView viewPostDetailsRecyclerView;
    private EditText CommentEditText;
    private ImageButton SendCommentButton;

    // INITIALISE LISTENERS

    // getUsername:     RETRIEVES CURRENT USER'S USERNAME
    //                  CORRECT INVOCATION CODE: databaseRef.child("users")
    //                                                      .child(currentUser.getUid())
    //                                                      .child("_username")
    //                                                      .addListenerForSingleValueEvent(getUsername)
    //                  SHOULD NOT BE USED INDEPENDENTLY.

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

    //TODO: Not working as well as expected
    private ChildEventListener commentMessageListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            logHandler.printLogWithMessage("Chat message added/loaded!");

            if(dataSnapshot.getKey() == null) {
                logHandler.printDatabaseResultLog(".getKey()", "Message ID", "commentMessageListener", "null");
                return;
            }

            if(dataSnapshot.getValue() == null) {
                logHandler.printDatabaseResultLog(".getValue()", "Message Values", "commentMessageListener", "null");
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
                logHandler.printDatabaseResultLog(".child(\"_message\").getValue()", "Message", "commentMessageListener", "null");
                return;
            } else if (timestampMillis == null) {
                logHandler.printDatabaseResultLog(".child(\"timestamp\").getValue()", "Timestamp", "commentMessageListener", "null");
                postMessage = new PostMessage(senderUID, senderName, commentMessage);
            } else {
                postMessage = new PostMessage(senderUID, senderName, commentMessage, timestampMillis);
            }

            userExp = userExpList.get(senderUID);
            int level = Utils.ConvertExpToLevel(userExp);
            int stage = Utils.ConvertLevelToStage(level);
            Integer colorInt = Utils.GetDefaultColorIntForStage(stage);
            String title;
            if(titleData.size() > 0 && !titleData.get(stage).equals("")) {
                title = titleData.get(stage);
            } else {
                title = Utils.GetDefaultTitleForStage(stage);
            }

            postMessage.set_level(String.valueOf(level));
            postMessage.set_title(title);
            postMessage.set_displayColour(colorInt);

            postMessage.set_id(dataSnapshot.getKey());

            logHandler.printDatabaseResultLog("", "Comment Message", "commentMessageListener", postMessage.toString());

            adapter.postMessageList.add(postMessage);
            adapter.notifyItemInserted(adapter.postMessageList.size()+2);

            if(scrollToLatestMessage) {
                logHandler.printLogWithMessage("scrollToLatestMessage = true; scrolling to latest message now!");
                viewPostDetailsRecyclerView.smoothScrollToPosition(adapter.getItemCount() -1);
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


    @Override
    ConstraintLayout setBaseLayer() {
        return (ConstraintLayout) findViewById(R.id.baseViewPostConstraintLayout);
    }

    @Override
    void HandleAdditionalIntentExtras(){
        final Intent dataReceiver = getIntent();
        postID = dataReceiver.getStringExtra("POST_ID");

        if (postID == null) {
            logHandler.printGetExtrasResultLog("POST_ID", "null");
        } else {
            logHandler.printGetExtrasResultLog("POST_ID", postID);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    int setLayoutIDForContentView() {
        return R.layout.activity_view_post;
    }

    @Override
    AppCompatActivity setCurrentActivity() {
        return ViewPostDetailsActivity.this;
    }

    @Override
    String setTitleForActivity() {
        return "Post";
    }

    @Override
    ImageButton setMainActionButton() {
        return null;
    }

    @Override
    Toolbar setTopNavToolbar() {
        View includeView = findViewById(R.id.viewPostNavBarInclude);
        return (Toolbar) includeView.findViewById(R.id.topNavToolbar);
    }

    @Override
    ActionMenuView setBottomToolbarAMV() {
        return null;
    }

    @Override
    void BindViewObjects() {
        viewPostDetailsRecyclerView = findViewById(R.id.viewPostRecyclerView);
        CommentEditText = findViewById(R.id.commentEditText);
        SendCommentButton = findViewById(R.id.sendCommentButton);
    }

    @Override
    void SetupViewObjects() {
        SendCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });

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
                    logHandler.printLogWithMessage("Scrolled to bottom of chat, setting scrollToLatestMessage = true!");
                    scrollToLatestMessage = true;
                }

            }
        };
        viewPostDetailsRecyclerView.addOnScrollListener(scrollListener);
    }

    @Override
    void AttachListeners() {
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
    void DestroyListeners() {
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
    int setCurrentMenuItemID() {
        return NO_MENU_ITEM_FOR_ACTIVITY;
    }

    private void sendComment(){
        if(username == null) {
            logHandler.printLogWithMessage("Username is null (which it shouldn't be)! Aborting sendMessage()!");
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
        logHandler.printLogWithMessage("User submitted message: " + comment + " and was it was formatted as: " + formattedComment.toString());

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
            logHandler.printLogWithMessage("No message was pushed because there was no text after formatting!");
        }


    }
}
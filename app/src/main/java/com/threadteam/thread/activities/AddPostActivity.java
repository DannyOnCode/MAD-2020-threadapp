package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.threadteam.thread.R;
import com.threadteam.thread.abstracts.ServerBaseActivity;
import com.threadteam.thread.models.Post;

import java.util.HashMap;

/**
 * This activity class handles the addition and creation of post.
 *
 * @author Danny Chan Yu Tian
 * @version 2.0
 * @since 2.0
 */

public class AddPostActivity extends ServerBaseActivity {


    // DATA STORE
    /** Stores immage URI from file image for uploading to firebase. */
    private Uri mImageUri;
    /** Constant Request to Identify Image Request. */
    private static final int PICK_IMAGE_REQUEST = 1;
    /** Store the username of the current user. */
    private String userName;

    // FIREBASE
    /** Firebase Storage reference for the current session. */
    private StorageReference mStorageRef;


    // VIEW OBJECTS
    /** Triggers picking image from device file. */
    private Button mChooseImageButton;
    /** Triggers picking image from device file. */
    private Button mChangeImageButton;
    /** Triggers clearing image from device file. */
    private Button mClearImageButton;
    /** Triggers upload to firebase with all necessary input. */
    private Button mConfirmButton;
    /** Displays post image of post. */
    private ImageView mDisplayImage;
    /** Contains the title of the post to be uploaded to database. */
    private EditText mTitleEdit;
    /** Contains the Message of the post to be uploaded to database. */
    private EditText mMessageEdit;
    /** Displays the upload progress once mConfirmButton has been pressed. */
    private ProgressBar mProgressBar;

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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }



    // ABSTRACT OVERRIDE METHODS
    @Override
    protected ConstraintLayout setBaseLayer() {
        return (ConstraintLayout) findViewById(R.id.baseAddPostItemConstraintLayout);
    }

    @Override
    protected int setLayoutIDForContentView() {
        return R.layout.activity_addpost;
    }

    @Override
    protected AppCompatActivity setCurrentActivity() {
        return AddPostActivity.this;
    }

    @Override
    protected String setTitleForActivity() {
        return "Add Post";
    }

    @Override
    protected ImageButton setMainActionButton() {
        return null;
    }

    @Override
    protected Integer setTopNavToolbarIncludeId() {
        return R.id.addPostNavbarInclude;
    }

    @Override
    protected Integer setBottomToolbarAMVIncludeId() {
        return null;
    }

    @Override
    protected void DoAdditionalSetupForToolbars() {
        TopNavToolbar.setTitle("Posts");
        super.DoAdditionalSetupForToolbars();
    }

    @Override
    protected void BindViewObjects() {
        mChooseImageButton = (Button) findViewById(R.id.addImageButton);
        mChangeImageButton = (Button) findViewById(R.id.changeImageButton);
        mClearImageButton = (Button) findViewById(R.id.clearImageButton);
        mDisplayImage = (ImageView) findViewById(R.id.postImageView);
        mConfirmButton = (Button) findViewById(R.id.confirmPostButton);
        mTitleEdit = (EditText) findViewById(R.id.editPostName);
        mMessageEdit = (EditText) findViewById(R.id.editMessage);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBarAddPost);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            logHandler.printLogWithMessage("User tapped on Back Button!");

            Intent goToPost = new Intent(currentActivity, PostsActivity.class);
            PutExtrasForServerIntent(goToPost);
            currentActivity.startActivity(goToPost);
            logHandler.printActivityIntentLog("Posts");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void SetupViewObjects() {
        // Set Reference for Firebase Storage
        mStorageRef = FirebaseStorage.getInstance().getReference("posts");

        //Set Initial button to gone
        mChangeImageButton.setVisibility(View.GONE);

        //Set Initial Image to be thread Logo
        mDisplayImage.setImageDrawable(getResources().getDrawable(R.drawable.imageemptyplaceholder));

        //Populate Buttons with Listeners
        //Populate Choose Image Button
        mChooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
                mChangeImageButton.setVisibility(View.VISIBLE);
                mChooseImageButton.setVisibility(View.GONE);
            }
        });

        //Populate Change Image Button
        mChangeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        //Populate Clear Image Button
        mClearImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDisplayImage.setImageDrawable(getResources().getDrawable(R.drawable.imageemptyplaceholder));
                mChooseImageButton.setVisibility(View.VISIBLE);
                mChangeImageButton.setVisibility(View.GONE);
            }
        });
        //Populate Confirm Button
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validation for correct username input
                if(TextUtils.isEmpty(mTitleEdit.getText())){
                    mTitleEdit.setError( "Title is required!" );
                    logHandler.printLogWithMessage("Input Title was: " + mTitleEdit.getText());
                }
                else if(mTitleEdit.getText().length() < 2 ){
                    mTitleEdit.setError("Minimum of 3 characters is required");
                    logHandler.printLogWithMessage("Input Title was: " + mTitleEdit.getText());
                }

                else if (mTitleEdit.getText().length() > 32){
                    mTitleEdit.setError("Maximum 32 characters only");
                    logHandler.printLogWithMessage("Input Title was: " + mTitleEdit.getText());
                }
                else{
                    uploadPostData();
                }
            }
        });
    }


    // INITIALISE LISTENERS
    /**
     * Retrieves the username of the current user.
     *
     *  Database Path:      root/users/(currentUser.getUid())/_username
     *  Usage:              Single ValueEventListener
     */
    ValueEventListener userDataListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            userName = (String) dataSnapshot.child("_username").getValue();

            logHandler.printDatabaseResultLog(".getValue()", "Current Username", "userDataListener", userName);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            logHandler.printDatabaseErrorLog(databaseError);
        }
    };

    @Override
    protected void AttachOnStartListeners() {
        databaseRef.child("users")
                .child(currentUser.getUid())
                .addListenerForSingleValueEvent(userDataListener);
    }

    @Override
    protected void DestroyOnStartListeners() {
        if(userDataListener != null) {
            databaseRef.removeEventListener(userDataListener);
        }
    }

    @Override
    protected int setCurrentMenuItemID() {
        return NO_MENU_ITEM_FOR_ACTIVITY;
    }

    // ACTIVITY SPECIFIC METHODS

    /**
     *  Opens Device File Explorer to search for image ONLY
     */
    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    /**
     * Takes in image chosen through openFileChooser and processes selected image
     * @param requestCode
     * @param resultCode
     * @param data Image Data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            mImageUri = data.getData();
            Picasso.get()
                    .load(mImageUri)
                    .fit()
                    .placeholder(R.drawable.backgroundforaddimage)
                    .error(R.drawable.backgroundforaddimage)
                    .centerCrop()
                    .into(mDisplayImage);


        }
    }

    /**
     * Gets the file extension of the image selected
     * @param uri Takes in the Uri of image to find the extension
     * @return a String with the file extension e.g(.png, .jpg)
     */
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    /**
     * Uploads the post data to firebase database
     */
    private void uploadPostData(){
        //Set Progressbar to visible
        mProgressBar.setVisibility(View.VISIBLE);
        if(mImageUri != null){
            final StorageReference fileReference = mStorageRef.child(serverId).child(System.currentTimeMillis()
                    + "."+getFileExtension(mImageUri));

            fileReference.putFile(mImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
            {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>()
            {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if (task.isSuccessful())
                    {
                        Uri downloadUri = task.getResult();
                        String senderID = currentUser.getUid();

                        //Creation and assigning of data into object
                        Post post = new Post();
                        post.set_imageLink(downloadUri.toString());
                        post.set_title(mTitleEdit.getText().toString().trim());
                        String postMessage = mMessageEdit.getText().toString();

                        // Stop newline spamming by doing some formatting
                        String[] messageLines = postMessage.split("\n");
                        StringBuilder formattedDescription = new StringBuilder();
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
                            formattedDescription.append(trimmedLine).append("\n");
                        }

                        // do a final trim
                        formattedDescription = new StringBuilder(formattedDescription.toString().trim());
                        logHandler.printLogWithMessage("User submitted comment: " + postMessage + " and was it was formatted as: " + formattedDescription.toString());

                        if(formattedDescription.length() > 0) {
                            post.set_message(formattedDescription.toString());
                        } else {
                            post.set_message("No Description");
                            logHandler.printLogWithMessage("No description was added because there was no text after formatting!");
                        }

                        post.set_senderID(senderID);
                        post.set_senderUsername(userName);
                        post.setTimestampMillis(System.currentTimeMillis());

                        logHandler.printLogWithMessage("User submitted Title: " + mTitleEdit.getText().toString().trim());
                        logHandler.printLogWithMessage("User submitted Message: " + mMessageEdit.getText().toString().trim());

                        //Uploading to firebase through HashMap
                        HashMap<String, Object> postDescriptions = new HashMap<>();
                        postDescriptions.put("_title", post.get_title());
                        postDescriptions.put("_senderUID", post.get_senderID());
                        postDescriptions.put("_sender", post.get_senderUsername());
                        postDescriptions.put("_message", post.get_message());
                        postDescriptions.put("_imageLink", post.get_imageLink());
                        postDescriptions.put("timestamp", post.getTimestampMillis());

                        databaseRef.child("posts").child(serverId).push().setValue(postDescriptions,new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                Intent goToPost = new Intent(AddPostActivity.this, PostsActivity.class);
                                PutExtrasForServerIntent(goToPost);
                                currentActivity.startActivity(goToPost);
                                logHandler.printLogWithMessage("Upload Post successful");
                                logHandler.printActivityIntentLog("Post Activity");
                                Toast.makeText(AddPostActivity.this,"Updated successfully",Toast.LENGTH_LONG).show();
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });

                        //Notify users of new post
                        sendPostNotification(serverId,currentUser.getUid()," uploaded a new post!");

                        AddExpForServerMember(currentUser.getUid(), serverId, 3, 60);


                    } else
                    {
                        logHandler.printLogWithMessage("Upload failed at uploadPostData: " + task.getException().getMessage());
                        Toast.makeText(AddPostActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            //When no image has been given
        }else{
            String senderID = currentUser.getUid();

            //Creation and assigning of data into object
            Post post = new Post();
            post.set_title(mTitleEdit.getText().toString().trim());
            post.set_message(mMessageEdit.getText().toString().trim());
            post.set_senderID(senderID);
            post.set_senderUsername(userName);
            post.setTimestampMillis(System.currentTimeMillis());

            logHandler.printLogWithMessage("User submitted Title: " + mTitleEdit.getText().toString().trim());
            logHandler.printLogWithMessage("User submitted Message: " + mMessageEdit.getText().toString().trim());

            //Uploading to firebase through HashMap
            HashMap<String, Object> postDescriptions = new HashMap<>();
            postDescriptions.put("_title", post.get_title());
            postDescriptions.put("_senderUID", post.get_senderID());
            postDescriptions.put("_sender", post.get_senderUsername());
            postDescriptions.put("_message", post.get_message());
            postDescriptions.put("timestamp", post.getTimestampMillis());

            databaseRef.child("posts").child(serverId).push().setValue(postDescriptions,new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    Intent goToPost = new Intent(AddPostActivity.this, PostsActivity.class);
                    PutExtrasForServerIntent(goToPost);
                    currentActivity.startActivity(goToPost);
                    logHandler.printLogWithMessage("Upload Post Data successful");
                    logHandler.printActivityIntentLog("Post Activity");
                    Toast.makeText(AddPostActivity.this,"Updated successfully",Toast.LENGTH_LONG).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            });

            //Notify users of new post
            sendPostNotification(serverId,currentUser.getUid()," uploaded a new post!");
        }
    }
}
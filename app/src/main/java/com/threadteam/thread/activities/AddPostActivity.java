package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
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
import com.threadteam.thread.models.Post;

import java.util.HashMap;

// ADD POST ACTIVITY
//
// PROGRAMMER-IN-CHARGE:
// DANNY CHAN, S10196363F
//
// DESCRIPTION
// Allows users to create post one at a time
//
// NAVIGATION
// PARENT: VIEW POST
// CHILDREN: N/A

public class AddPostActivity extends _ServerBaseActivity {


    // DATA STORE
    //
    // mImageUri:               STORE IMAGE URI FROM FILE IMAGE FOR UPLOADING TO FIREBASE.
    // PICK_IMAGE_REQUEST       CONSTANT REQUEST TO IDENTIFY IMAGE REQUEST.

    private Uri mImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private String userName;

    // FIREBASE
    //
    // currentUser:             CURRENT USER FOR THE CURRENT SESSION.
    // firebaseAuth:            FIREBASE AUTH INSTANCE FOR THE CURRENT SESSION.
    // mDatabaseRef:            FIREBASE DATABASE REFERENCE FOR THE CURRENT SESSION.
    // mStorageRef:             FIREBASE STORAGE REFERENCE FOR THE CURRENT SESSION.
    // currentData:             VALUE EVENT LISTENER FOR RETRIEVING CURRENT DATA OF USER.

    private StorageReference mStorageRef;


    // VIEW OBJECTS
    //
    // mButtonChooseImage:      TRIGGERS PICKING IMAGE FROM DEVICE FILE.
    // mCancelButton:           TRIGGERS RETURN TO PROFILE PAGE.
    // mConfirmButton:          TRIGGERS UPLOAD TO FIREBASE WITH ALL NECESSARY INPUT DATA.
    // mDisplayImage:           DISPLAYS PROFILE PICTURE.
    // mUserNameEdit:           CONTAINS USERNAME DATA OF USER TO BE UPLOADED TO DATABASE.
    // mStatusTitle:            CONTAINS Status/Title DATA OF USER TO BE UPLOADED TO DATABASE.
    // mDescription:            CONTAINS ABOUT ME DESCRIPTION DATA OF USER TO BE UPLOADED TO DATABASE.
    // mProgressBar:            DISPLAYS THE UPLOAD PROGRESS ONCE mConfirmButton HAS BEEN CLICKED.

    private ImageView mChooseImage;
    private Button mConfirmButton;
    private ImageView mDisplayImage;
    private EditText mTitleEdit;
    private EditText mMessageEdit;
    private ProgressBar mProgressBar;

    // INITIALISE LISTENERS
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            logHandler.printLogWithMessage("User tapped on Back Button!");

            Intent goToServer = new Intent(currentActivity, PostsActivity.class);
            PutExtrasForServerIntent(goToServer);
            currentActivity.startActivity(goToServer);
            logHandler.printActivityIntentLog("Posts");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ABSTRACT OVERRIDE METHODS
    @Override
    ConstraintLayout setBaseLayer() {
        return (ConstraintLayout) findViewById(R.id.baseAddPostItemConstraintLayout);
    }

    @Override
    int setLayoutIDForContentView() {
        return R.layout.activity_addpost;
    }

    @Override
    AppCompatActivity setCurrentActivity() {
        return AddPostActivity.this;
    }

    @Override
    String setTitleForActivity() {
        return "Add Post";
    }

    @Override
    ImageButton setMainActionButton() {
        return null;
    }

    @Override
    Toolbar setTopNavToolbar() {
        View includeView = findViewById(R.id.addPostNavbarInclude);
        return (Toolbar) includeView.findViewById(R.id.topNavToolbar);
    }

    @Override
    ActionMenuView setBottomToolbarAMV() {
        return null;
    }

    @Override
    void BindViewObjects() {
        mChooseImage = (ImageView) findViewById(R.id.postImageView);
        mDisplayImage = (ImageView) findViewById(R.id.postImageView);
        mConfirmButton = (Button) findViewById(R.id.confirmPostButton);
        mTitleEdit = (EditText) findViewById(R.id.editPostName);
        mMessageEdit = (EditText) findViewById(R.id.editMessage);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBarAddPost);
    }

    @Override
    void SetupViewObjects() {
        // INITIALISE FIREBASE
        mStorageRef = FirebaseStorage.getInstance().getReference("posts");

        // SETUP VIEW OBJECTS
        //Populate Buttons with Listeners
        mChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });


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
                    uploadUserData();
                }
            }
        });
    }

    @Override
    void AttachListeners() {
        databaseRef.child("users")
                .child(currentUser.getUid())
                .addValueEventListener(userDataListener);
    }

    @Override
    void DestroyListeners() {
        if(userDataListener != null) {
            databaseRef.removeEventListener(userDataListener);
        }
    }

    @Override
    int setCurrentMenuItemID() {
        return NO_MENU_ITEM_FOR_ACTIVITY;
    }

    // ACTIVITY SPECIFIC METHODS

    //Open Device File Explorer to search for image only
    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    //Process selected image
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
    //Get File Extensions e.g(.png , .jpg)
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    //Upload UserData to Firebase Storage and Update RealTime Database
    private void uploadUserData(){
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
                        Post post = new Post();
                        post.set_imageLink(downloadUri.toString());
                        post.set_title(mTitleEdit.getText().toString().trim());
                        post.set_message(mMessageEdit.getText().toString().trim());
                        post.set_senderID(senderID);
                        post.set_senderUsername(userName);
                        post.setTimestampMillis(System.currentTimeMillis());

                        logHandler.printLogWithMessage("User submitted username: " + mTitleEdit.getText().toString().trim());
                        logHandler.printLogWithMessage("User submitted status/title: " + mMessageEdit.getText().toString().trim());

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
                                startActivity(new Intent(AddPostActivity.this, ViewServersActivity.class));
                                logHandler.printLogWithMessage("Upload Post successful");
                                logHandler.printActivityIntentLog("View Server Activity");
                                Toast.makeText(AddPostActivity.this,"Updated successfully",Toast.LENGTH_LONG).show();
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });

                    } else
                    {
                        logHandler.printLogWithMessage("Upload failed at uploadUserData: " + task.getException().getMessage());
                        Toast.makeText(AddPostActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            //When no image has been given
        }else{
            String senderID = currentUser.getUid();
            Post post = new Post();
            post.set_title(mTitleEdit.getText().toString().trim());
            post.set_message(mMessageEdit.getText().toString().trim());
            post.set_senderID(senderID);
            post.set_senderUsername(userName);
            post.setTimestampMillis(System.currentTimeMillis());

            logHandler.printLogWithMessage("User submitted username: " + mTitleEdit.getText().toString().trim());
            logHandler.printLogWithMessage("User submitted status/title: " + mMessageEdit.getText().toString().trim());

            HashMap<String, Object> postDescriptions = new HashMap<>();
            postDescriptions.put("_title", post.get_title());
            postDescriptions.put("_senderUID", post.get_senderID());
            postDescriptions.put("_sender", post.get_senderUsername());
            postDescriptions.put("_message", post.get_message());
            postDescriptions.put("timestamp", post.getTimestampMillis());

            databaseRef.child("posts").child(serverId).push().setValue(postDescriptions,new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    startActivity(new Intent(AddPostActivity.this, ViewServersActivity.class));
                    logHandler.printLogWithMessage("Upload Userdata successful");
                    logHandler.printActivityIntentLog("View Server Activity");
                    Toast.makeText(AddPostActivity.this,"Updated successfully",Toast.LENGTH_LONG).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
    }
}
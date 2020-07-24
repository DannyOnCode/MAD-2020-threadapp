package com.threadteam.thread.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;
import com.threadteam.thread.abstracts.MainBaseActivity;
import com.threadteam.thread.models.User;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * This activity class handles the edit of profile.
 *
 * @author Danny Chan Yu Tian
 * @version 2.0
 * @since 1.0
 */

public class EditProfileActivity extends MainBaseActivity {


    // DATA STORE
    /** Stores immage URI from file image for uploading to firebase. */
    private Uri mImageUri;
    /** Constant Request to Identify Image Request. */
    private static final int PICK_IMAGE_REQUEST = 1;

    // FIREBASE
    /** Firebase Storage reference for the current session. */
    private StorageReference mStorageRef;

    // VIEW OBJECTS
    /** Triggers picking image from device file. */
    ImageView mButtonChooseImage;
    /** Triggers return to profile page. */
    Button mCancelButton;
    /** Triggers uploading to firebase with all necessary input data*/
    Button mConfirmButton;
    /** Displays profile picture. */
    CircleImageView mDisplayImage;
    /** Contains username data of user to be uploaded to database. */
    EditText mUserNameEdit;
    /** Contains Status/Title data of user to be uploaded to database.*/
    EditText mStatusTitle;
    /** Contains About Me Description data of user to be uploaded to database.*/
    EditText mDescription;
    /** Displays the upload progress once mConfirmButton has been pressed. */
    ProgressBar mProgressBar;

    // INITIALISE LISTENERS
    /**
     * Retrieves the current profile data of the current user.
     *
     *  Database Path:      root/users/(currentUser.getUid())
     *  Usage:              Single ValueEventListener
     */
    final ValueEventListener currentData = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            String initialProfileImage = (String) dataSnapshot.child("_profileImageURL").getValue();
            String initialUserName = (String) dataSnapshot.child("_username").getValue();
            String initialStatusMessage = (String) dataSnapshot.child("_statusMessage").getValue();
            String initialAboutMeMessage = (String) dataSnapshot.child("_aboutUsMessage").getValue();

            logHandler.printDatabaseResultLog(".getValue()", "Current Profile Image", "currentData", initialProfileImage);
            logHandler.printDatabaseResultLog(".getValue()", "Current Username", "currentData", initialUserName);
            logHandler.printDatabaseResultLog(".getValue()", "Current Status/Title", "currentData", initialStatusMessage);
            logHandler.printDatabaseResultLog(".getValue()", "Current Description", "currentData", initialAboutMeMessage);
            Picasso.get()
                    .load(initialProfileImage)
                    .fit()
                    .placeholder(R.drawable.profilepictureempty)
                    .error(R.drawable.profilepictureempty)
                    .centerCrop()
                    .into(mDisplayImage);
            mUserNameEdit.setText(initialUserName);
            mStatusTitle.setText(initialStatusMessage);
            mDescription.setText(initialAboutMeMessage);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Toast.makeText(EditProfileActivity.this,"No previous profile image",Toast.LENGTH_SHORT).show();
            logHandler.printDatabaseErrorLog(databaseError);
        }
    };

    //DEFAULT SUPER METHODS
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    protected void onStart() { super.onStart(); }

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
    protected int setLayoutIDForContentView() {
        return R.layout.activity_editprofile;
    }

    @Override
    protected AppCompatActivity setCurrentActivity() {
        return EditProfileActivity.this;
    }

    @Override
    protected String setTitleForActivity() {
        return "Edit Profile";
    }

    @Override
    protected ImageButton setMainActionButton() {
        return null;
    }

    @Override
    protected Integer setTopNavToolbarIncludeId() {
        return R.id.editProfileInclude;
    }

    @Override
    protected Integer setBottomToolbarAMVIncludeId() {
        return null;
    }

    @Override
    protected void DoAdditionalSetupForToolbars() {
        if(currentActivity.getSupportActionBar() != null) {
            currentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        super.DoAdditionalSetupForToolbars();
    }

    @Override
    protected void BindViewObjects() {
        mButtonChooseImage = (ImageView) findViewById(R.id.buttonSelectImage);
        mDisplayImage = (CircleImageView) findViewById(R.id.userProfilePictureEdit);
        mConfirmButton = (Button) findViewById(R.id.confirmButton);
        mCancelButton = (Button) findViewById(R.id.cancelButton);
        mUserNameEdit = (EditText) findViewById(R.id.userNameEdit);
        mStatusTitle = (EditText) findViewById(R.id.statusMessageEdit);
        mDescription = (EditText) findViewById(R.id.aboutMeDesciptionEdit);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    @Override
    protected void SetupViewObjects() {
        // SETUP VIEW OBJECTS

        // INITIALISE FIREBASE STORAGE
        mStorageRef = FirebaseStorage.getInstance().getReference("users");

        //Populate Choose Image Buttons with Listener
        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        //Populate Confirm Button with Listener
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validation for correct username input
                if(TextUtils.isEmpty(mUserNameEdit.getText())){
                    mUserNameEdit.setError( "Username is required!" );
                    logHandler.printLogWithMessage("Input Username was: " + mUserNameEdit.getText());
                }
                else if(mUserNameEdit.getText().length() < 2 ){
                    mUserNameEdit.setError("Minimum of 3 characters is required");
                    logHandler.printLogWithMessage("Input Username was: " + mUserNameEdit.getText());
                }

                else if (mUserNameEdit.getText().length() > 16){
                    mUserNameEdit.setError("Maximum 16 characters only");
                    logHandler.printLogWithMessage("Input Username was: " + mUserNameEdit.getText());
                }
                else{
                    uploadUserData();
                }
            }
        });

        //Populate Cancel Button with Listener
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Moving activity
                Intent goToViewProfile = new Intent(EditProfileActivity.this, ViewProfileActivity.class);
                logHandler.printActivityIntentLog("View Profile Activity");
                startActivity(goToViewProfile);

            }
        });
    }

    @Override
    protected void AttachOnStartListeners() {

    }

    @Override
    protected void DestroyOnStartListeners() {

    }

    @Override
    protected void AttachOnCreateListeners() {
        databaseRef.child("users")
                .child(currentUser.getUid())
                .addListenerForSingleValueEvent(currentData);
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
                    .placeholder(R.drawable.profilepictureempty)
                    .error(R.drawable.profilepictureempty)
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
    private void uploadUserData(){
        if(mImageUri != null){
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
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

                        String userID = currentUser.getUid();
                        User user = new User();
                        user.set_profileImageURL(downloadUri.toString());
                        user.set_username(mUserNameEdit.getText().toString().trim());
                        user.set_aboutUsMessage(mDescription.getText().toString().trim());
                        user.set_statusMessage(mStatusTitle.getText().toString().trim());

                        logHandler.printLogWithMessage("User submitted username: " + mUserNameEdit.getText().toString().trim());
                        logHandler.printLogWithMessage("User submitted status/title: " + mStatusTitle.getText().toString().trim());
                        logHandler.printLogWithMessage("User submitted description: " + mDescription.getText().toString().trim());

                        databaseRef.child("users").child(userID).child("_username").setValue(user.get_username());
                        databaseRef.child("users").child(userID).child("_statusMessage").setValue(user.get_statusMessage());
                        databaseRef.child("users").child(userID).child("_aboutUsMessage").setValue(user.get_aboutUsMessage());
                        databaseRef.child("users").child(userID).child("_profileImageURL").setValue(user.get_profileImageURL(),new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setProgress(100);
                                    }
                                },500);
                                returnToViewProfile();
                                logHandler.printActivityIntentLog("View Profile Activity");
                                Toast.makeText(EditProfileActivity.this,"Updated successfully",Toast.LENGTH_LONG).show();
                            }
                        });



                    } else
                    {
                        logHandler.printLogWithMessage("Upload failed at uploadUserData: " + task.getException().getMessage());
                        Toast.makeText(EditProfileActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            //When no profile image has been given
        }else{
            String userID = currentUser.getUid();
            User user = new User();
            user.set_username(mUserNameEdit.getText().toString().trim());
            user.set_aboutUsMessage(mDescription.getText().toString().trim());
            user.set_statusMessage(mStatusTitle.getText().toString().trim());

            logHandler.printLogWithMessage("User submitted username: " + mUserNameEdit.getText().toString().trim());
            logHandler.printLogWithMessage("User submitted status/title: " + mStatusTitle.getText().toString().trim());
            logHandler.printLogWithMessage("User submitted description: " + mDescription.getText().toString().trim());

            databaseRef.child("users").child(userID).child("_username").setValue(user.get_username());
            databaseRef.child("users").child(userID).child("_statusMessage").setValue(user.get_statusMessage());
            databaseRef.child("users").child(userID).child("_aboutUsMessage").setValue(user.get_aboutUsMessage(),new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    try{
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setProgress(100);
                            }
                        }, 500);
                        returnToViewProfile();
                        Toast.makeText(EditProfileActivity.this, "Updated successfully", Toast.LENGTH_LONG).show();
                    }catch (Exception e){
                        logHandler.printDatabaseErrorLog(databaseError);
                    }

                }

            });

        }
    }


    private void returnToViewProfile() {
        Intent goToViewMembers = new Intent(currentActivity, ViewProfileActivity.class);
        currentActivity.startActivity(goToViewMembers);
        logHandler.printActivityIntentLog("View Profile");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            logHandler.printLogWithMessage("User tapped on Back Button!");
            returnToViewProfile();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
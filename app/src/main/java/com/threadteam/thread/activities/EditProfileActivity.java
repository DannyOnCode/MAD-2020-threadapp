package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.threadteam.thread.LogHandler;
import com.threadteam.thread.R;
import com.threadteam.thread.models.User;


import de.hdodenhof.circleimageview.CircleImageView;

// VIEW PROFILE ACTIVITY
//
// PROGRAMMER-IN-CHARGE:
// DANNY CHAN, S10196363F
//
// DESCRIPTION
// Handles editing of profile details
//
// NAVIGATION
// PARENT: VIEW PROFILE
// CHILDREN: NONE
// OTHER: NONE

public class EditProfileActivity extends AppCompatActivity {

    // TODO: DOCUMENTATION TO BE CONTINUED
    //LOGGING
    private LogHandler logHandler = new LogHandler("Edit Profile Activity");

    // DATA STORE
    //
    // mImageUri:               STORE IMAGE URI FROM FILE IMAGE FOR UPLOADING TO FIREBASE.
    // PICK_IMAGE_REQUEST       CONSTANT REQUEST TO IDENTIFY IMAGE REQUEST.
    private Uri mImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    // FIREBASE
    //
    // currentUser:             CURRENT USER FOR THE CURRENT SESSION.
    // firebaseAuth:            FIREBASE AUTH INSTANCE FOR THE CURRENT SESSION.
    // mDatabaseRef:            FIREBASE DATABASE REFERENCE FOR THE CURRENT SESSION.
    // mStorageRef:             FIREBASE STORAGE REFERENCE FOR THE CURRENT SESSION.
    // currentData:             VALUE EVENT LISTENER FOR RETRIEVING CURRENT DATA OF USER.
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    private ValueEventListener currentData;

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
    ImageView mButtonChooseImage;
    Button mCancelButton;
    Button mConfirmButton;
    CircleImageView mDisplayImage;
    EditText mUserNameEdit;
    EditText mStatusTitle;
    EditText mDescription;
    ProgressBar mProgressBar;
    Toolbar TopNavToolbar;


    // ACTIVITY STATE MANAGEMENT METHODS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        // BIND TOOLBARS
        // NOTE:    IT IS IMPORTANT TO GET THE INCLUDE VIEWS BEFORE DOING FIND VIEW BY ID.
        //          THIS ENSURES THAT ANDROID CAN ALWAYS FIND THE CORRECT VIEW OBJECT.

        View includeView = findViewById(R.id.editProfileInclude);
        TopNavToolbar = (Toolbar) includeView.findViewById(R.id.topNavToolbar);

        logHandler.printDefaultLog(LogHandler.TOOLBAR_BOUND);

        // SETUP TOOLBARS
        TopNavToolbar.setTitle("Edit Profile");
        this.setSupportActionBar(TopNavToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        logHandler.printDefaultLog(LogHandler.TOOLBAR_SETUP);

        // BIND VIEW OBJECTS
        mButtonChooseImage = (ImageView) findViewById(R.id.buttonSelectImage);
        mDisplayImage = (CircleImageView) findViewById(R.id.userProfilePictureEdit);
        mConfirmButton = (Button) findViewById(R.id.confirmButton);
        mCancelButton = (Button) findViewById(R.id.cancelButton);
        mUserNameEdit = (EditText) findViewById(R.id.userNameEdit);
        mStatusTitle = (EditText) findViewById(R.id.statusMessageEdit);
        mDescription = (EditText) findViewById(R.id.aboutMeDesciptionEdit);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        //LOG
        logHandler.printDefaultLog(LogHandler.VIEW_OBJECTS_BOUND);

        // INITIALISE FIREBASE
        mStorageRef = FirebaseStorage.getInstance().getReference("users");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users");
        firebaseAuth = FirebaseAuth.getInstance();

        currentUser = firebaseAuth.getCurrentUser();
        //Validation for if there is a user
        if(currentUser == null) {
            logHandler.printDefaultLog(LogHandler.FIREBASE_USER_NOT_FOUND);
            Intent backToLogin = new Intent(EditProfileActivity.this, LoginActivity.class);
            startActivity(backToLogin);
            logHandler.printActivityIntentLog("Login Activity");
            return;
        }
        logHandler.printDefaultLog(LogHandler.FIREBASE_USER_FOUND);
        String userID = currentUser.getUid();


        // SETUP VIEW OBJECTS
        //Populate Buttons with Listeners
        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

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

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Moving activity
                Intent goToViewProfile = new Intent(EditProfileActivity.this, ViewProfileActivity.class);
                logHandler.printActivityIntentLog("View Profile Activity");
                startActivity(goToViewProfile);
            }
        });

        // INITIALISE LISTENERS

        // currentData:     RETRIEVES CURRENT USER'S DATA
        //                  CORRECT INVOCATION CODE: databaseRef.child("users")
        //                                                      .child(currentUser.getUid())
        //                                                      .addListenerForSingleValueEvent(currentData)
        //                  SHOULD NOT BE USED INDEPENDENTLY.
        currentData = new ValueEventListener() {
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


        // Input all current User data
        mDatabaseRef.child(userID).addListenerForSingleValueEvent(currentData);

        logHandler.printDefaultLog(LogHandler.FIREBASE_LISTENERS_INITIALISED);
    }


    // CLASS METHODS
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
                    .placeholder(R.drawable.profilepictureempty)
                    .error(R.drawable.profilepictureempty)
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

                        firebaseAuth = FirebaseAuth.getInstance();
                        currentUser = firebaseAuth.getCurrentUser();
                        String userID = currentUser.getUid();
                        User user = new User();
                        user.set_profileImageURL(downloadUri.toString());
                        user.set_username(mUserNameEdit.getText().toString().trim());
                        user.set_aboutUsMessage(mDescription.getText().toString().trim());
                        user.set_statusMessage(mStatusTitle.getText().toString().trim());

                        logHandler.printLogWithMessage("User submitted username: " + mUserNameEdit.getText().toString().trim());
                        logHandler.printLogWithMessage("User submitted status/title: " + mStatusTitle.getText().toString().trim());
                        logHandler.printLogWithMessage("User submitted description: " + mDescription.getText().toString().trim());

                        mDatabaseRef.child(userID).child("_username").setValue(user.get_username());
                        mDatabaseRef.child(userID).child("_statusMessage").setValue(user.get_statusMessage());
                        mDatabaseRef.child(userID).child("_aboutUsMessage").setValue(user.get_aboutUsMessage());
                        mDatabaseRef.child(userID).child("_profileImageURL").setValue(user.get_profileImageURL(),new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setProgress(100);
                                    }
                                },500);
                                startActivity(new Intent(EditProfileActivity.this, ViewProfileActivity.class));
                                logHandler.printLogWithMessage("Upload Userdata successful");
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
            firebaseAuth = FirebaseAuth.getInstance();
            currentUser = firebaseAuth.getCurrentUser();
            String userID = currentUser.getUid();
            User user = new User();
            user.set_username(mUserNameEdit.getText().toString().trim());
            user.set_aboutUsMessage(mDescription.getText().toString().trim());
            user.set_statusMessage(mStatusTitle.getText().toString().trim());

            logHandler.printLogWithMessage("User submitted username: " + mUserNameEdit.getText().toString().trim());
            logHandler.printLogWithMessage("User submitted status/title: " + mStatusTitle.getText().toString().trim());
            logHandler.printLogWithMessage("User submitted description: " + mDescription.getText().toString().trim());

            mDatabaseRef.child(userID).child("_username").setValue(user.get_username());
            mDatabaseRef.child(userID).child("_statusMessage").setValue(user.get_statusMessage());
            mDatabaseRef.child(userID).child("_aboutUsMessage").setValue(user.get_aboutUsMessage(),new DatabaseReference.CompletionListener() {
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
                        startActivity(new Intent(EditProfileActivity.this, ViewProfileActivity.class));
                        logHandler.printActivityIntentLog("View Profile Activity");
                        Toast.makeText(EditProfileActivity.this, "Updated successfully", Toast.LENGTH_LONG).show();
                    }catch (Exception e){
                        logHandler.printDatabaseErrorLog(databaseError);
                    }

                }

            });

        }
    }

}
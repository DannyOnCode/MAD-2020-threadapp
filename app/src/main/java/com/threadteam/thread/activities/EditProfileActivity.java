package com.threadteam.thread.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.threadteam.thread.R;
import com.threadteam.thread.models.User;

import org.w3c.dom.Text;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    TextView mButtonChooseImage;
    ImageView mDisplayImage;
    EditText mUserNameEdit;
    EditText mStatusTitle;
    EditText mDescription;
    Button mCancelButton;
    Button mConfirmButton;
    ProgressBar mProgressBar;

    private Uri mImageUri;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        mButtonChooseImage = findViewById(R.id.buttonSelectImage);
        mDisplayImage = findViewById(R.id.userProfilePictureEdit);
        mConfirmButton = findViewById(R.id.confirmButton);
        mCancelButton = findViewById(R.id.cancelButton);
        mUserNameEdit = findViewById(R.id.userNameEdit);
        mStatusTitle = findViewById(R.id.statusMessageEdit);
        mDescription = findViewById(R.id.aboutMeDesciptionEdit);
        mProgressBar = findViewById(R.id.progress_bar);

        mStorageRef = FirebaseStorage.getInstance().getReference("users");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        String userID = currentUser.getUid();
        mDatabaseRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String initialProfileImage = (String) dataSnapshot.child("_profileImageURL").getValue();
                String initialUserName = (String) dataSnapshot.child("_username").getValue();
                String initialStatusMessage = (String) dataSnapshot.child("_statusMessage").getValue();
                String initialAboutMeMessage = (String) dataSnapshot.child("_aboutUsMessage").getValue();
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
            }
        });


        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( TextUtils.isEmpty(mUserNameEdit.getText())){
                    mUserNameEdit.setError( "Username is required!" );}
                else{
                    uploadUserData();
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToViewProfile = new Intent(EditProfileActivity.this, ViewProfileActivity.class);
                startActivity(goToViewProfile);
            }
        });

    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
            && data != null && data.getData() != null){
            mImageUri = data.getData();

            Picasso.get().load(mImageUri).into(mDisplayImage);
        }
    }
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

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
                                Toast.makeText(EditProfileActivity.this,"Updated successfully",Toast.LENGTH_LONG).show();
                            }
                        });



                    } else
                    {
                        Toast.makeText(EditProfileActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            firebaseAuth = FirebaseAuth.getInstance();
            currentUser = firebaseAuth.getCurrentUser();
            String userID = currentUser.getUid();
            User user = new User();
            user.set_username(mUserNameEdit.getText().toString().trim());
            user.set_aboutUsMessage(mDescription.getText().toString().trim());
            user.set_statusMessage(mStatusTitle.getText().toString().trim());

            mDatabaseRef.child(userID).child("_username").setValue(user.get_username());
            mDatabaseRef.child(userID).child("_statusMessage").setValue(user.get_statusMessage());
            mDatabaseRef.child(userID).child("_aboutUsMessage").setValue(user.get_aboutUsMessage(),new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setProgress(100);
                        }
                    }, 500);
                    startActivity(new Intent(EditProfileActivity.this, ViewProfileActivity.class));
                    Toast.makeText(EditProfileActivity.this, "Updated successfully", Toast.LENGTH_LONG).show();
                }

            });

        }
    }
}
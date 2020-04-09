package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
  private CircleImageView profileImage;
   private Uri profileImageUri = null;
    private EditText usernameEditText;
    private Button submitBtn;
    private StorageReference reference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String userId;
    private ProgressBar progressBar;
    private boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        getSupportActionBar().setTitle("Account Settings");
        profileImage = findViewById(R.id.profileImage);
        usernameEditText = findViewById(R.id.usernameET);
        progressBar = findViewById(R.id.setupPB);
        submitBtn = findViewById(R.id.submitBtn);
        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.DARKEN);

        progressBar.setVisibility(View.VISIBLE);
        submitBtn.setEnabled(false);

        firestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        String name = task.getResult().getString(getString(R.string.nameKey));
                        String image = task.getResult().getString(getString(R.string.imageKey));
                        usernameEditText.setText(name);
                        profileImageUri = Uri.parse(image);
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(profileImage);
                        Toast.makeText(SetupActivity.this, "Data exists", Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(SetupActivity.this, "Error with retrieve data", Toast.LENGTH_LONG).show();
                }

                progressBar.setVisibility(View.INVISIBLE);
                submitBtn.setEnabled(true);
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {

                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setBorderCornerColor(R.color.colorAccent)
                                .start(SetupActivity.this);

                    }
                }
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = usernameEditText.getText().toString();
                if (!TextUtils.isEmpty(username) && profileImageUri != null) {
                progressBar.setVisibility(View.VISIBLE);


                if(isChanged) {

                        final StorageReference imagePath = reference.child("profile_images").child(userId + ".jpg");
                        UploadTask uploadTask = imagePath.putFile(profileImageUri);

                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SetupActivity.this, "submit failed check your connec", Toast.LENGTH_SHORT).show();
                                    throw task.getException();

                                }
                                return imagePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {

                                    pushToFireStore(task, username);

                                    // Toast.makeText(SetupActivity.this, "Happy to get that uploaded", Toast.LENGTH_LONG).show();
                                } else {

                                    Toast.makeText(SetupActivity.this, "falied to get the download URI ", Toast.LENGTH_LONG).show();
                                }

                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });


                    } else{
                    pushToFireStore(null, username);

                }
                }else {
                    Toast.makeText(SetupActivity.this, "your name or profile image can't be empty!", Toast.LENGTH_LONG).show();

                }
            }
        });

//       
    }

    private void pushToFireStore(Task<Uri> task, String username) {

         Uri downloadUri;
        if(task != null) {
           downloadUri = task.getResult();
        }else{

            downloadUri = profileImageUri;
        }
        Map<String, String> user = new HashMap<>();
        user.put(getString(R.string.nameKey), username);
        user.put(getString(R.string.imageKey) , downloadUri.toString());
        firestore.collection("Users").document(userId).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    Toast.makeText(SetupActivity.this, "user data updated, wahoooo!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SetupActivity.this, MainActivity.class));
                    finish();

                }else{
                    Toast.makeText(SetupActivity.this, "Failed", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                profileImageUri = result.getUri();
                profileImage.setImageURI(profileImageUri);
                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }
}

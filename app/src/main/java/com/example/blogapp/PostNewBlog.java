package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firestore.v1.DocumentTransform;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;


public class PostNewBlog extends AppCompatActivity {
    private ImageView blogImage;
    private EditText imageDesc;
    private Button postBtn;
    private Uri blogImageUri = null;
    private ProgressBar progressBar;
    private StorageReference reference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String currentUserId;
    private boolean isChanged = false;
    private Bitmap bitmap;
    private String postImageDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_new_blog);
        blogImage = findViewById(R.id.blogImage);
        imageDesc = findViewById(R.id.imageDesc);
        postBtn = findViewById(R.id.postBtn);
        progressBar = findViewById(R.id.newPostPB);
        reference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.DARKEN);


//-------------------------------------------------------------------------------------------------------------------------//
        blogImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setBorderCornerColor(R.color.colorAccent)
                        .start(PostNewBlog.this);
            }
        });


//-------------------------------------------------------------------------------------------------------------------------//
        postBtn.setOnClickListener(new View.OnClickListener() {
            String randomImageTitle = UUID.randomUUID().toString();

            @Override
            public void onClick(View v) {

                postImageDesc = imageDesc.getText().toString();
                postBtn.setEnabled(false);

                if (!TextUtils.isEmpty(postImageDesc) && blogImageUri != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    if (isChanged) {

                        final StorageReference imagePath = reference.child("post_images").child(randomImageTitle + ".jpg");
                        upload(imagePath, 512, 512, 50, "image");
                        final StorageReference thumbReference = reference.child("post_images").child("post_images_thumbnails").child(randomImageTitle + ".jpg");
                        upload(thumbReference, 100, 100, 1, "thumb");


                    } else {
                        pushToFireStore(null, postImageDesc, "image");
                        pushToFireStore(null, postImageDesc, "thumb");
                    }
                } else {
                    Toast.makeText(PostNewBlog.this, "Fill the empty fields", Toast.LENGTH_LONG).show();
                }

                progressBar.setVisibility(View.INVISIBLE);
               // postBtn.setEnabled(true);
            }
        });
        postBtn.setEnabled(true);
    }

    private void sendToMain() {
        startActivity(new Intent(PostNewBlog.this, MainActivity.class));
        finish();
    }

    private void pushToFireStore(Task<Uri> task, String postImageDesc, String type) {

        Uri downloadUri;
        String imageType = "";

        if (task != null) {
            downloadUri = task.getResult();
        } else {
            downloadUri = blogImageUri;
        }

        Map<String, Object> postsMap = new HashMap<>();
        if (type.equals("thumb")) {
            imageType = "thumb_url";
        } else if (type.equals("image")) {
            imageType = "image_url";
        }

        postsMap.put(imageType, downloadUri.toString());
        postsMap.put("photo_description", postImageDesc);
        postsMap.put("user_id", currentUserId);
        postsMap.put("timestamp", new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));

        firestore.collection(type).add(postsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    //Toast.makeText(PostNewBlog.this, "heeeeeeeeeeeey", Toast.LENGTH_LONG).show();
                    sendToMain();
                } else {
                    Toast.makeText(PostNewBlog.this, ":(", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void upload(final StorageReference reference, int maxHeight, int maxWidth, int quality, final String type) {
//        compress(maxHeight, maxWidth, quality);
        UploadTask task = reference.putBytes(compress(maxHeight, maxWidth, quality));
        Task<Uri> upTask = task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful())
                    throw task.getException();
                else
                    return reference.getDownloadUrl();

            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    pushToFireStore(task, postImageDesc, type);
                    Toast.makeText(PostNewBlog.this, "we did it", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PostNewBlog.this, "it's failed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                blogImageUri = result.getUri();
                blogImage.setImageURI(blogImageUri);
                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    public byte[] compress(int maxHeight, int maxWidth, int quality) {
        File newImageFile = new File(blogImageUri.getPath());

        try {
            bitmap = new Compressor(PostNewBlog.this)
                    .setMaxHeight(maxHeight)
                    .setMaxWidth(maxWidth)
                    .setQuality(quality)
                    .compressToBitmap(newImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);

        return byteArray.toByteArray();
    }


}

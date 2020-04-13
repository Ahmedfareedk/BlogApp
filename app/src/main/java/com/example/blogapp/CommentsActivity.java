package com.example.blogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {
    private RecyclerView commentsRV;
    private ImageView sendComment;
    private EditText commentET;
    private String blogPostId;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String currentUSerId;
    private String commentMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        commentsRV = findViewById(R.id.comments_RV);
        commentET = findViewById(R.id.comment_ET);
        sendComment = findViewById(R.id.send_comment_btn);
        blogPostId = getIntent().getStringExtra("blog_post_id");
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUSerId = mAuth.getCurrentUser().getUid();

        sendComment.setOnClickListener(commentListener);
    }

    View.OnClickListener commentListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            commentMessage = commentET.getText().toString();
            Map<String, Object> commentValues  = new HashMap<>();
            commentValues.put("user_id", currentUSerId);
            commentValues.put("timestamp", FieldValue.serverTimestamp());
            commentValues.put("comment_message",commentMessage);
            blogPostId = getIntent().getStringExtra("blog_post_id");
            firestore.collection("image/" + blogPostId + "/comments").document(currentUSerId).set(commentValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){
                        Toast.makeText(CommentsActivity.this, "Comment pushed successfully", Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }
    };
}

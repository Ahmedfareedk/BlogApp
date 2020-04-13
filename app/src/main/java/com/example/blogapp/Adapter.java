/*
package com.example.blogapp;

import android.content.Context;
import android.content.Intent;
import android.media.MediaDrm;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.clans.fab.Label;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private List<BlogPostDataBase> blogListView;
    private Context context;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String thumbUrl;
    private String blogPostId;


    public Adapter(List<BlogPostDataBase> blogListView) {
        this.blogListView = blogListView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        String blogPostDescription = blogListView.get(position).getPhoto_description();
        blogPostId = blogListView.get(position).BlogPostId;

        String blogImageUri = blogListView.get(position).getImage_url();
        holder.setBlogImage(blogImageUri);
        String userId = blogListView.get(position).getUser_id();
        String date = blogListView.get(position).getTimestamp();
        final String currentUserId = mAuth.getCurrentUser().getUid();
        holder.setDescText(blogPostDescription, date);


        firestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String username = task.getResult().getString("name");
                    String image = task.getResult().getString("image");
                    holder.setUserData(username, image);
                }
            }
        });


        firestore.collection("image/" + blogPostId + "/likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    holder.likeCheckBox.setChecked(true);
                } else {
                    holder.likeCheckBox.setChecked(false);
                }
            }
        });

        firestore.collection("image/" + blogPostId + "/likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {


                if (!queryDocumentSnapshots.isEmpty()) {

                    holder.setLikesCounter(queryDocumentSnapshots.size());

                } else {
                    holder.likesCounter.setText(0);
                }
            }
        });


        holder.likeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firestore.collection("image/" + blogPostId + "/likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (!task.getResult().exists()) {
                            Map<String, Object> likeValues = new HashMap<>();
                            likeValues.put("timestamp", FieldValue.serverTimestamp());
                            firestore.collection("image/" + blogPostId + "/likes").document(currentUserId).set(likeValues);
                        } else {
                            firestore.collection("image/" + blogPostId + "/likes").document(currentUserId).delete();
                        }

                    }
                });





            }
        });


        firestore.collection("thumb").document().get().addOnCompleteListener(thumbListener);

        holder.commentImage.setOnClickListener(commentListener);


    }

    View.OnClickListener commentListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, CommentsActivity.class);
            intent.putExtra("blog_post_id", blogPostId);
            context.startActivity(intent);
        }
    };
    OnCompleteListener<DocumentSnapshot> thumbListener = new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    thumbUrl = task.getResult().getString("thumb_url");
                }
            }
        }
    };


    @Override
    public int getItemCount() {
        return blogListView.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView blogPostDesc, usernameTV, dateTextView;
        private ImageView blogImage;
        private CircleImageView userProfileImage;
        private CheckBox likeCheckBox;
        private TextView likesCounter;
        private ImageView commentImage;
        View mView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            likeCheckBox = mView.findViewById(R.id.likeBtn);

            commentImage = mView.findViewById(R.id.commentImage);
        }

        private void setDescText(String desc, String date) {
            blogPostDesc = mView.findViewById(R.id.post_desc_home);
            dateTextView = mView.findViewById(R.id.post_date_text_view);
            dateTextView.setText(date);
            blogPostDesc.setText(desc);
        }

        private void setBlogImage(String downloadUri) {
            blogImage = mView.findViewById(R.id.blog_image_home);
            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.image_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderRequest).load(downloadUri)
                    .thumbnail(Glide.with(context).load(thumbUrl))
                    .into(blogImage);
        }

        private void setUserData(String username, String imageUri) {
            usernameTV = mView.findViewById(R.id.username_text_view);
            userProfileImage = mView.findViewById(R.id.profile_image_of_post);
            //  dateTextView = mView.findViewById(R.id.post_date_text_view);
            usernameTV.setText(username);
            RequestOptions placeholder = new RequestOptions();
            placeholder.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholder).load(imageUri).into(userProfileImage);

        }

        private void setLikesCounter(int count)
        {
            likesCounter = mView.findViewById(R.id.likes_counter_TV);
            likesCounter.setText(count + " Likes");
        }

    }
}

//    @Override
//    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return super.getItemId(position);
//    }
//}
*/
package com.example.blogapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.clans.fab.Label;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private List<BlogPostDataBase> blogListView;
    private Context context;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String thumbUrl;



    public Adapter(List<BlogPostDataBase> blogListView)
    {
        this.blogListView = blogListView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        String blogPostDescription = blogListView.get(position).getPhoto_description();
        final String blogPostId = blogListView.get(position).BlogPostId;

        String blogImageUri = blogListView.get(position).getImage_url();
        holder.setBlogImage(blogImageUri);
        String userId = blogListView.get(position).getUser_id();
        String date = blogListView.get(position).getTimestamp();
        final String currentUserId = mAuth.getCurrentUser().getUid();
        holder.setDescText(blogPostDescription, date);


        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CommentsActivity.class);
                intent.putExtra("blog_post_id", blogPostId);
                context.startActivity(intent);
            }
        });

        firestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    String username = task.getResult().getString("name");
                    String image = task.getResult().getString("image");
                    holder.setUserData(username, image);
                }
            }
        });

        firestore.collection("image/" + blogPostId + "/likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty())
                {
                    holder.setLikesCounter(queryDocumentSnapshots.size());
                }else{
                    holder.setLikesCounter(0);
                }
            }
        });

        firestore.collection("image/" + blogPostId + "/likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists())
                {
                    holder.likeCheckBox.setChecked(true);
                }else {
                    holder.likeCheckBox.setChecked(false);
                }
            }
        });

        holder.likeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firestore.collection("image/" + blogPostId + "/likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists()){
                            Map<String, Object> likeValues = new HashMap<>();
                            likeValues.put("timestamp", FieldValue.serverTimestamp());
                            firestore.collection("image/" + blogPostId + "/likes").document(currentUserId).set(likeValues);
                        }else{
                            firestore.collection("image/" + blogPostId + "/likes").document(currentUserId).delete();
                        }
                    }
                });

            }
        });

        firestore.collection("thumb").document().get().addOnCompleteListener(thumbListener);

//        firestore.collection("image/" + blogPostId + "")


    }
    OnCompleteListener <DocumentSnapshot> thumbListener = new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

            if(task.isSuccessful())
            {
                if(task.getResult().exists())
                {
                    thumbUrl = task.getResult().getString("thumb_url");
                }
            }
        }
    };


    @Override
    public int getItemCount() {
        return blogListView.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView blogPostDesc, usernameTV, dateTextView;
        private ImageView blogImage;
        private CircleImageView userProfileImage;
        private CheckBox likeCheckBox;
        private TextView likesCounter;
        private ImageView commentBtn;
        View mView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            likeCheckBox = mView.findViewById(R.id.likeBtn);
            commentBtn = mView.findViewById(R.id.commentImage);
        }

        private void setDescText(String desc, String date)
        {
            blogPostDesc = mView.findViewById(R.id.post_desc_home);
            dateTextView = mView.findViewById(R.id.post_date_text_view);
            dateTextView.setText(date);
            blogPostDesc.setText(desc);
        }
        private void setBlogImage(String downloadUri)
        {
            blogImage = mView.findViewById(R.id.blog_image_home);
            RequestOptions placeholderRequest =  new RequestOptions();
            placeholderRequest.placeholder(R.drawable.image_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholderRequest).load(downloadUri)
                    .thumbnail(Glide.with(context).load(thumbUrl))
                    .into(blogImage);
        }
        private void setUserData(String username, String imageUri)
        {
            usernameTV = mView.findViewById(R.id.username_text_view);
            userProfileImage = mView.findViewById(R.id.profile_image_of_post);
            dateTextView = mView.findViewById(R.id.post_date_text_view);
            usernameTV.setText(username);
            RequestOptions placeholder = new RequestOptions();
            placeholder.placeholder(R.drawable.profile_placeholder);
            Glide.with(context).applyDefaultRequestOptions(placeholder).load(imageUri).into(userProfileImage);
        }

        private void setLikesCounter(int count)
        {
            likesCounter = mView.findViewById(R.id.likes_counter_TV);
            likesCounter.setText(count + " Likes");
        }

    }
}
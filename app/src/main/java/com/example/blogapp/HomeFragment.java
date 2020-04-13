package com.example.blogapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private RecyclerView homeRecyclerView;
    private List<BlogPostDataBase> blogListView;
    private FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    private Adapter blogAdapter;
    private DocumentSnapshot lastLimit;
    private Boolean firstLoadFirstPage = true;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_fragment, container, false);
        homeRecyclerView = view.findViewById(R.id.home_recycler_view);
        blogListView = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        blogAdapter = new Adapter(blogListView);
        homeRecyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        homeRecyclerView.setAdapter(blogAdapter);

        homeRecyclerView.addOnScrollListener(scrollListener);

        if (mAuth.getCurrentUser() != null) {
            firestore = FirebaseFirestore.getInstance();
            Query firestoreQuery = firestore.collection("image").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
            firestoreQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    if (firstLoadFirstPage) {
                        lastLimit = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                    }
                    for (DocumentChange document : queryDocumentSnapshots.getDocumentChanges()) {
                        if (document.getType() == DocumentChange.Type.ADDED) {
                            String blogPostId = document.getDocument().getId();
                            BlogPostDataBase blogPost = document.getDocument().toObject(BlogPostDataBase.class).withId(blogPostId);
                            if (firstLoadFirstPage) {
                                blogListView.add(blogPost);
                            } else {
                                blogListView.add(0, blogPost);
                            }
                            blogAdapter.notifyDataSetChanged();

                        }
                    }

                    firstLoadFirstPage = false;
                }
            }
        });

        }
            return view;

    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if(!recyclerView.canScrollVertically(1)){

               Log.i("lat element reached", "reached!!!!!!!!!");
               loadMoreQueries();
            }
        }
    };

 private void loadMoreQueries()
 {
     Query firestoreQuery = firestore.collection("image")
             .orderBy("timestamp", Query.Direction.DESCENDING)
             .startAfter(lastLimit)
             .limit(3);
     firestoreQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
         @Override
         public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
             if (!queryDocumentSnapshots.isEmpty()) {
                 lastLimit = queryDocumentSnapshots.getDocuments()
                         .get(queryDocumentSnapshots.size() - 1);
                 for (DocumentChange document : queryDocumentSnapshots.getDocumentChanges()) {
                     if (document.getType() == DocumentChange.Type.ADDED) {
                         String blogPostId = document.getDocument().getId();
                         BlogPostDataBase blogPost = document.getDocument().toObject(BlogPostDataBase.class).withId(blogPostId);
                         blogListView.add(blogPost);
                         blogAdapter.notifyDataSetChanged();
                     }
                 }
             }
         }
     });
 }

}

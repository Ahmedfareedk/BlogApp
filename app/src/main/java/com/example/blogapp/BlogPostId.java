package com.example.blogapp;


import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

import javax.annotation.Nullable;

public class BlogPostId {

    @Exclude
    public String BlogPostId;

    public <T extends BlogPostId> T withId(@NonNull final String id)
    {
        this.BlogPostId = id;
        return (T) this;
    }
}



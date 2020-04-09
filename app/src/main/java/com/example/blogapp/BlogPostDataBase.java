package com.example.blogapp;


import java.util.Date;

public class BlogPostDataBase extends BlogPostId {
    private String photo_description;
    private String thumb_url;
    private String image_url;
    private String user_id;
    private String timestamp;



    public BlogPostDataBase() {
    }

    public BlogPostDataBase(String photo_description, String thumb_url, String image_url, String user_id, String timestamp) {
        this.photo_description = photo_description;
        this.thumb_url = thumb_url;
        this.image_url = image_url;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }


    public String getPhoto_description() {
        return photo_description;
    }

    public void setDesphoto_description(String photo_description) {
        this.photo_description = photo_description;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}

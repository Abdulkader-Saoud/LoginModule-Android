package com.example.loginmodule.Post;

import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;
import java.util.Date;

public class Post implements Serializable {
    private static final long serialVersionUID = 1001;
    private String id,title;
    private Date date;
    private int commentsCount= 0;
    private String path;

    public Post(String id, String title, Date date , int commentsCount, String path) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.commentsCount = commentsCount;
        this.path = path;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Date getDate() {
        return date;
    }

    public int getCommentsCount() {
        return commentsCount;
    }
    public String getPath() {
        return path;
    }

}

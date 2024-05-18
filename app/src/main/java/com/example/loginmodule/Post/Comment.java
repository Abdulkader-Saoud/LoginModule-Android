package com.example.loginmodule.Post;

import java.util.ArrayList;

public class Comment {
    private String userID,docPath;
    private String pictureURL,username, commentContent;
    private ArrayList<Comment> replies;
    public Comment(String userID, String username, String commentContent,String docPath) {
        this.userID = userID;
        this.username = username;
        this.commentContent = commentContent;
        this.replies = new ArrayList<>();
        this.docPath = docPath;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public String getUsername() {
        return username;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public ArrayList<Comment> getReplies() {
        return replies;
    }
}

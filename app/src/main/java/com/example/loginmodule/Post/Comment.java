package com.example.loginmodule.Post;

import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

public class Comment {
    private String userID,docPath;
    private String pictureURL,username, commentContent;
    private ArrayList<Comment> replies = new ArrayList<>();
    public Comment(String userID, String username, String commentContent,String docPath) {
        this.userID = userID;
        this.username = username;
        this.commentContent = commentContent;
        this.replies = new ArrayList<>();
        this.docPath = docPath;

        fetchReplies();
        pictureURL = "Profile_Pictures/" + userID;
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
    public void fetchReplies() {
        DocumentReference commentRef = FirebaseFirestore.getInstance().document(docPath);
        commentRef.collection("comments").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                replies.clear();
                Map<String,Object> data;
                for (DocumentSnapshot document : task.getResult()) {
                    data = document.getData();
                    Comment comment = new Comment((String) data.get("userID"), (String) data.get("username"), (String) data.get("commentContent"), document.getReference().getPath());
                    replies.add(comment);
                }
            }
        });
    }

    public String getDocPath() {
        return docPath;
    }

    public ArrayList<Comment> getReplies() {
        return replies;
    }
}

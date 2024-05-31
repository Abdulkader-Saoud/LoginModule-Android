package com.example.loginmodule.Post;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostPage extends AppCompatActivity {
    private Post post;
    private CommentAdapter commentAdapter;
    private RecyclerView commentsRV;
    private ArrayList<Comment> comments = new ArrayList<>();
    private ImageButton addReplyButton;
    private LinearLayout replyLayout;
    private EditText replyET;
    private Button postReplyButton,subBTN;
    private String uid,fname;
    private TextView postTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        uid = sharedPreferences.getString(getString(R.string.prefKey_stdID), "");
        fname = sharedPreferences.getString(getString(R.string.prefKey_fName), "");

        post = (Post) getIntent().getSerializableExtra("post");
        addReplyButton = findViewById(R.id.addReplyButton);
        addReplyButton.setOnClickListener(v -> showAddReply());

        subBTN = findViewById(R.id.subBTN);
        if (post.getIsSub()){
            subBTN.setText("UnSubscribe");
        }
        subBTN.setOnClickListener(e -> toggleSub());

        postTitle = findViewById(R.id.postTitle);
        postTitle.setText(post.getTitle());
        replyLayout = findViewById(R.id.replyLayout);
        replyET = findViewById(R.id.replyET);
        postReplyButton = findViewById(R.id.postReplyButton);
        postReplyButton.setOnClickListener(v -> postReply());
        commentsRV = findViewById(R.id.commentsRV);
        fetchComments();
    }
    private void toggleSub() {
        String postPath = post.getPath();
        DocumentReference postRef = FirebaseFirestore.getInstance().document(postPath);

        Map<String, Object> updates = new HashMap<>();
        if (!post.getIsSub())
            updates.put("subs", FieldValue.arrayUnion(uid));
        else
            updates.put("subs", FieldValue.arrayRemove(uid));

        postRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("TAG", "String appended to array successfully.");
                    post.setIsSub(!post.getIsSub());
                    if (post.getIsSub())
                        subBTN.setText("UnSubscribe");
                    else
                        subBTN.setText("Subscribe");
                })
                .addOnFailureListener(e -> {
                    Log.w("TAG", "Error appending string to array", e);
                });
    }
    private void showAddReply(){
        if (replyLayout.getVisibility() == View.GONE) {
            replyLayout.setVisibility(View.VISIBLE);
        } else {
            replyLayout.setVisibility(View.GONE);
        }
    }
    private void postReply(){
        String reply = replyET.getText().toString();
        if (reply.isEmpty()) {
            Toast.makeText(this, "Reply cannot be empty", Toast.LENGTH_SHORT).show();
        }
        String postPath = post.getPath();
        Map<String, Object> data = new HashMap<>();
        data.put("userID", uid);
        data.put("username", fname);
        data.put("commentContent", reply);
        Log.d("PostPage", "Post Path: " + uid + fname);
        DocumentReference postRef = FirebaseFirestore.getInstance().document(postPath);
        postRef.collection("comments").add(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fetchComments();
                replyET.setText("");
                replyLayout.setVisibility(View.GONE);
            }
        });

    }
    private void fetchComments() {
        commentsRV.setVisibility(View.GONE);
        String postPath = post.getPath();
        DocumentReference postRef = FirebaseFirestore.getInstance().document(postPath);
        postRef.collection("comments").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                comments.clear();
                Map<String,Object> data;
                for (DocumentSnapshot document : task.getResult()) {
                    data = document.getData();
                    Comment comment = new Comment((String) data.get("userID"), (String) data.get("username"), (String) data.get("commentContent"), document.getReference().getPath());
                    comments.add(comment);
                }
                if (comments.isEmpty()) {
                    Toast.makeText(this, "No comments", Toast.LENGTH_SHORT).show();
                    return;
                }
                commentsRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                commentAdapter = new CommentAdapter(comments);
                commentsRV.setAdapter(commentAdapter);
                commentsRV.setVisibility(View.VISIBLE);
            }
        });
    }
}
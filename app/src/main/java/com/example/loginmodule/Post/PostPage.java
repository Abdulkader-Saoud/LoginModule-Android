package com.example.loginmodule.Post;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
    private Button postReplyButton;
    private String uid,fname;
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
        uid = sharedPreferences.getString(String.valueOf(R.string.prefKey_stdID), "");
        fname = sharedPreferences.getString(String.valueOf(R.string.prefKey_fName), "");

        post = (Post) getIntent().getSerializableExtra("post");
        addReplyButton = findViewById(R.id.addReplyButton);
        addReplyButton.setOnClickListener(v -> showAddReply());

        replyLayout = findViewById(R.id.replyLayout);
        replyET = findViewById(R.id.replyET);
        postReplyButton = findViewById(R.id.postReplyButton);
        postReplyButton.setOnClickListener(v -> postReply());
        commentsRV = findViewById(R.id.commentsRV);
        fetchComments();
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
                commentsRV.setLayoutManager(new LinearLayoutManager(this));
                commentAdapter = new CommentAdapter(comments);
                commentsRV.setAdapter(commentAdapter);
                commentsRV.setVisibility(View.VISIBLE);
            }
        });
    }
}
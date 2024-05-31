package com.example.loginmodule.Post;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.loginmodule.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private final ArrayList<Comment> commentModelArrayList;
    private String uid, fname;

    public CommentAdapter(ArrayList<Comment> commentModelArrayList) {
        this.commentModelArrayList = commentModelArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);
        SharedPreferences sharedPreferences = parent.getContext().getSharedPreferences("login", parent.getContext().MODE_PRIVATE);
        uid = sharedPreferences.getString(parent.getContext().getString(R.string.prefKey_stdID), "");
        fname = sharedPreferences.getString(parent.getContext().getString(R.string.prefKey_fName), "");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = commentModelArrayList.get(position);
        holder.showButton.setOnClickListener(v -> setReplies(holder, comment));
        holder.postReplyButton.setOnClickListener(v -> postReply(holder, comment));
        holder.username.setText(comment.getUsername());
        holder.commentContent.setText(comment.getCommentContent());
        setImage(comment.getPictureURL(), holder);
    }

    private void setImage(String picUrl, ViewHolder holder) {
        if (picUrl != null && !picUrl.isEmpty()) {
            try {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference ref = storage.getReference().child(picUrl);
                ref.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(holder.itemView.getContext()).load(uri).into(holder.profileImage))
                        .addOnFailureListener(e -> Log.d("ProfilePage", "Image URL loading failed: " + e.getMessage()));
            } catch (Exception e) {
                Log.d("ProfilePage", "Exception while loading image URL: " + e.getMessage());
            }
        }
    }

    private void setReplies(ViewHolder holder, Comment comment) {
        if (comment == null) {
            Log.d("CommentAdapter", "Comment is null");
            return;
        }
        if (holder.commentsRV.getVisibility() == View.VISIBLE) {
            holder.commentsRV.setVisibility(View.GONE);
            return;
        }
        if (comment.getReplies().isEmpty()) {
            Toast.makeText(holder.itemView.getContext(), "No replies", Toast.LENGTH_SHORT).show();
            return;
        }
        CommentAdapter commentAdapter = new CommentAdapter(comment.getReplies());
        holder.commentsRV.setAdapter(commentAdapter);
        holder.commentsRV.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.commentsRV.setVisibility(View.VISIBLE);
    }

    private void postReply(ViewHolder holder, Comment comment) {
        String reply = holder.replyET.getText().toString();
        holder.commentsRV.setVisibility(View.GONE);

        if (reply.isEmpty()) {
            Toast.makeText(holder.itemView.getContext(), "Reply cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        String postPath = comment.getDocPath();
        Map<String, Object> data = new HashMap<>();
        data.put("userID", uid);
        data.put("username", fname);
        data.put("commentContent", reply);
        Log.d("Replyy", "reply Path: " + uid + fname);
        DocumentReference postRef = FirebaseFirestore.getInstance().document(postPath);
        postRef.collection("comments").add(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                holder.replyET.setText("");
                holder.replyLayout.setVisibility(View.GONE);
                comment.fetchReplies();
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final CircleImageView profileImage, showButton;
        final TextView username, commentContent, replyET;
        final ImageButton addReplyButton;
        final LinearLayout replyLayout;
        final Button postReplyButton;
        final RecyclerView commentsRV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            showButton = itemView.findViewById(R.id.showButton);
            username = itemView.findViewById(R.id.username);
            commentContent = itemView.findViewById(R.id.commentContent);
            replyET = itemView.findViewById(R.id.replyET);
            addReplyButton = itemView.findViewById(R.id.addReplyButton);
            replyLayout = itemView.findViewById(R.id.replyLayout);
            postReplyButton = itemView.findViewById(R.id.postReplyButton);
            commentsRV = itemView.findViewById(R.id.commentsRV);

            addReplyButton.setOnClickListener(v -> addReplyHandler());
        }

        private void addReplyHandler() {
            replyLayout.setVisibility(replyLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        }
    }
}

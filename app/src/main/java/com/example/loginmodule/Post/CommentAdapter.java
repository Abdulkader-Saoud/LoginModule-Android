package com.example.loginmodule.Post;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>  {
    private final ArrayList<Comment> commentModelArrayList;

    public CommentAdapter(ArrayList<Comment> commentModelArrayList) {
        this.commentModelArrayList = commentModelArrayList;
    }
    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        Comment comment = commentModelArrayList.get(position);
        holder.username.setText(comment.getUsername());
        holder.commentContent.setText(comment.getCommentContent());
    }

    @Override
    public int getItemCount() {
        return commentModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage, showButton;
        TextView username, commentContent,replyET;
        ImageButton addReplyButton;
        LinearLayout replyLayout;
        Button postReplyButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            showButton = itemView.findViewById(R.id.showButton);
            username = itemView.findViewById(R.id.username);
            commentContent = itemView.findViewById(R.id.commentContent);
            replyET = itemView.findViewById(R.id.replyET);
            addReplyButton = itemView.findViewById(R.id.addReplyButton);
            addReplyButton.setOnClickListener(v -> addReplyHandler());
            replyLayout = itemView.findViewById(R.id.replyLayout);
            postReplyButton = itemView.findViewById(R.id.postReplyButton);
        }
        private void addReplyHandler(){
            if(replyLayout.getVisibility() == View.GONE){
                replyLayout.setVisibility(View.VISIBLE);
            }else{
                replyLayout.setVisibility(View.GONE);
            }
        }
    }
}

package com.example.loginmodule.Post;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.R;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private final ArrayList<Post> postModelArrayList;

    public PostAdapter(ArrayList<Post> postModelArrayList) {
        this.postModelArrayList = postModelArrayList;
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.post_card, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        Post post = postModelArrayList.get(position);
        holder.postName.setText(post.getTitle());
        holder.postDate.setText(post.getDate().getDay() + "-" + post.getDate().getMonth() + "-" + (post.getDate().getYear() + 1900));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PostPage.class);
            intent.putExtra("post", post);
            v.getContext().startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        return postModelArrayList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView newMessagesImage;
        private TextView postName;
        private TextView postDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            newMessagesImage = itemView.findViewById(R.id.newMessagesImage);
            postName = itemView.findViewById(R.id.postName);
            postDate = itemView.findViewById(R.id.postDate);
        }

    }
}

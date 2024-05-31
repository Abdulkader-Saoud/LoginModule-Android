package com.example.loginmodule.Post;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private final ArrayList<Post> postModelArrayList;

    public PostAdapter(ArrayList<Post> postList, Boolean onlySub) {
        if (onlySub) {
            postModelArrayList = new ArrayList<>();
            for (Post post : postList) {
                if (post.getIsSub()) {
                    postModelArrayList.add(post);
                }
            }
        } else {
            postModelArrayList = postList;
        }
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        Post post = postModelArrayList.get(position);

        holder.postName.setText(post.getTitle());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(post.getDate());
        holder.postDate.setText(formattedDate);

        if (post.getIsSub()) {
            holder.newMessagesImage.setBackgroundColor(ContextCompat.getColor(context, R.color.attending));
        }

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
        private final ImageView newMessagesImage;
        private final TextView postName;
        private final TextView postDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            newMessagesImage = itemView.findViewById(R.id.newMessagesImage);
            postName = itemView.findViewById(R.id.postName);
            postDate = itemView.findViewById(R.id.postDate);
        }
    }
}

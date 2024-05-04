package com.example.loginmodule.Course;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.R;

import java.util.ArrayList;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Course> courseModelArrayList;
    private String uid, accountType;

    public CourseAdapter(Context context, ArrayList<Course> courseModelArrayList) {
        this.context = context;
        this.courseModelArrayList = courseModelArrayList;

        uid = context.getSharedPreferences("login", Context.MODE_PRIVATE).getString(context.getString(R.string.prefKey_stdID), null);
        accountType = context.getSharedPreferences("login", Context.MODE_PRIVATE).getString(context.getString(R.string.prefKey_accType), null);
    }

    @NonNull
    @Override
    public CourseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_card, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courseModelArrayList.get(position);
        holder.courseNameTV.setText(course.getCourseName());
        holder.courseContentTV.setText(course.getCourseCode());

        if (accountType.equals("Instructor")) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (course.isCreator(uid)) {
                        Intent intent = new Intent(context, CourseCreatePage.class);
                        intent.putExtra("docID", course.getCourseCode());
                        context.startActivity(intent);
                    }
                    else {
                        Intent intent = new Intent(context, CourseGroupPage.class);
                        intent.putExtra("docID", course.getCourseCode());
                        context.startActivity(intent);
                    }
                }
            });
        }


    }


    @Override
    public int getItemCount() {
        return courseModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView courseNameTV;
        private TextView courseContentTV;
        private ImageView courseAttendenceIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseNameTV = itemView.findViewById(R.id.courseName);
            courseContentTV = itemView.findViewById(R.id.courseContent);
            courseAttendenceIV = itemView.findViewById(R.id.attendenceImage);
        }
    }
}

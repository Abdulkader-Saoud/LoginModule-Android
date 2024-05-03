package com.example.loginmodule.Course;

import android.annotation.SuppressLint;
import android.content.Context;
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

    public CourseAdapter(Context context, ArrayList<Course> courseModelArrayList) {
        this.context = context;
        this.courseModelArrayList = courseModelArrayList;
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
        holder.courseContentTV.setText(course.getStdNum() + " | " + course.getCourseContent());
//        holder.courseAttendenceIV.setImageResource(course.getCourseAttendence());
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

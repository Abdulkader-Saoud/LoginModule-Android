package com.example.loginmodule.Course;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.R;

import java.util.ArrayList;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Course> courseModelArrayList;
    private String accountType;

    public CourseAdapter(Context context, ArrayList<Course> courseModelArrayList) {
        this.context = context;
        this.courseModelArrayList = courseModelArrayList;
        courseModelArrayList.sort((o1, o2) -> o1.getStartDate().before(o2.getStartDate()) ? -1 : 1);
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
        holder.courseCodeTV.setText(course.getCourseCode());
        holder.courseDateTV.setText(course.getStartandEndDate());


        if (course.getCurrentTimeInfo().equals("Attending")) {
            holder.courseAttendenceIV.setBackgroundColor(context.getResources().getColor(R.color.attending));
        } else if (course.getCurrentTimeInfo().equals("Complete")) {
            holder.courseAttendenceIV.setBackgroundColor(context.getResources().getColor(R.color.complete));
        }

        if (accountType.equals("Instructor")) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.editButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, CourseCreatePage.class);
                intent.putExtra("docID", course.getCourseCode());
                context.startActivity(intent);
            });
        }
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CoursePage.class);
            intent.putExtra("course", course);
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return courseModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageButton editButton;
        private TextView courseNameTV;
        private TextView courseCodeTV;
        private TextView courseDateTV;
        private ImageView courseAttendenceIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseNameTV = itemView.findViewById(R.id.courseName);
            courseCodeTV = itemView.findViewById(R.id.courseCode);
            courseAttendenceIV = itemView.findViewById(R.id.attendenceImage);
            courseDateTV = itemView.findViewById(R.id.courseDate);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }
}

package com.example.loginmodule.AttendanceModule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.Course.Course;
import com.example.loginmodule.R;

import java.util.ArrayList;

public class PastAttendanceAdapter extends RecyclerView.Adapter<PastAttendanceAdapter.ViewHolder> {

    private ArrayList<AttendanceItem> attendanceList;

    public PastAttendanceAdapter(ArrayList<AttendanceItem> attendanceList) {
        this.attendanceList = attendanceList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCourseName;
        TextView textViewCourseCode;
        TextView textViewAttendanceDate;
        ImageButton exportButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCourseName = itemView.findViewById(R.id.textViewCourseName);
            textViewCourseCode = itemView.findViewById(R.id.textViewCourseCode);
            textViewAttendanceDate = itemView.findViewById(R.id.textViewCourseDate);
            exportButton = itemView.findViewById(R.id.exportButton);

            exportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        AttendanceItem clickedItem = attendanceList.get(position);
                        ((PastAttendance) itemView.getContext()).exportAttendance(clickedItem);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_past_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceItem currentItem = attendanceList.get(position);

        holder.textViewCourseName.setText(currentItem.getCourseName());
        holder.textViewCourseCode.setText(currentItem.getCourseCode());
        holder.textViewAttendanceDate.setText(currentItem.getAttendanceDate());
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }
}
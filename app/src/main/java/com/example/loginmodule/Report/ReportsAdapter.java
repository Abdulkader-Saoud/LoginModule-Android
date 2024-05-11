package com.example.loginmodule.Report;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.R;

import java.util.ArrayList;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {
    private ArrayList<String> reportsDatesArray;
    private ArrayList<String> reportsIDSArray;

    public ReportsAdapter(ArrayList<String> reportsDatesArray, ArrayList<String> reportsIDSArray) {
        this.reportsDatesArray = reportsDatesArray;
        this.reportsIDSArray = reportsIDSArray;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_item, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        String date = reportsDatesArray.get(position);
        holder.bind(date);
    }

    @Override
    public int getItemCount() {
        return reportsDatesArray.size();
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {
        private TextView dateTextView;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    String uid = reportsIDSArray.get(position);
                    Log.d("ReportsAdapter", "Report ID: " + uid);
                    Intent intent = new Intent(itemView.getContext(), ReportPage.class);
                    intent.putExtra("reportID", uid);
                    itemView.getContext().startActivity(intent);
                }
            });
        }

        public void bind(String date) {
            dateTextView.setText(date);
        }
    }
}

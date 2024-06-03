package com.example.loginmodule.PollQuesModule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.R;

import java.util.ArrayList;
import java.util.Calendar;

public class PollsAdapter extends RecyclerView.Adapter<PollsAdapter.ViewHolder> {
    private ArrayList<Poll> polls;
    private ItemClickListener itemClickListener;
    private String accountType;
    private Context context;
    private ArrayList<String> states;

    public PollsAdapter(Context context, String accountType, ArrayList<String> states, ArrayList<Poll> polls, ItemClickListener itemClickListener) {
        this.context = context;
        this.accountType = accountType;
        this.states = states;
        this.polls = polls;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public PollsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poll_item_card,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PollsAdapter.ViewHolder holder, int position) {
        String str = reverseString(polls.get(position).getEndDate());
        if (accountType.equals("Instructor")){
            holder.img.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.poll_icon));
            if (str.compareTo(getCurrentDate())>=0){
                holder.stateTxt.setText("Not Finished");
            }else {
                holder.stateTxt.setText("Finished");
            }
        }else {
            if (states.get(position).equals("C")){
                holder.img.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.done_icon));
                holder.stateTxt.setText("Voted");
            }else{
                if (str.compareTo(getCurrentDate())>=0){
                    holder.img.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.notify_icon));
                    holder.stateTxt.setText("Not Voted Yet");
                }else{
                    holder.img.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.undone_icon));
                    holder.stateTxt.setText("Not Voted");
                }
            }
        }
        holder.title.setText(polls.get(position).getTitle());
        holder.sDate.setText("Start Time: " + polls.get(position).getStartDate());
        holder.eDate.setText("End Time: " + polls.get(position).getEndDate());
    }

    @Override
    public int getItemCount() {
        return polls.size();
    }

    private String getCurrentDate(){
        String date;
        Calendar c = Calendar.getInstance();
        date = c.get(Calendar.YEAR) +""+ c.get(Calendar.MONTH) +""+ c.get(Calendar.DAY_OF_MONTH);
        return date;
    }

    private String reverseString(String str){
        String[] segments = str.split("/");
        int i, len = segments.length;
        String nstr="";
        for(i=len-1;i>=0;i--){
            nstr = nstr + segments[i];
        }
        return nstr;
    }

    public interface ItemClickListener{
        void onClick(View v, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title, sDate, eDate, stateTxt;
        private ImageView img;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.pollTitleTVId);
            sDate = itemView.findViewById(R.id.startDateTVId);
            eDate = itemView.findViewById(R.id.endDateTVId);
            stateTxt = itemView.findViewById(R.id.stateTxtId);
            img = itemView.findViewById(R.id.stateImgId);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(itemView,getAdapterPosition());
        }
    }
}

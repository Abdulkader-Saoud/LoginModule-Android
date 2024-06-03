package com.example.loginmodule.PollQuesModule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class PollsListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PollsAdapter adapter;
    private PollsAdapter.ItemClickListener itemClickListener;
    private ArrayList<Poll> polls = new ArrayList<>();
    private ArrayList<String> states = new ArrayList<>();
    private String uid, accType, docID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_polls_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        uid = sharedPreferences.getString(getString(R.string.prefKey_stdID), null);
        accType = sharedPreferences.getString("accType",null);

        docID = getIntent().getStringExtra("docID");

        setComponentsIDs();

        getPolls();

    }

    private void getPolls(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("CourseGroups").document(docID).collection("Polls")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()){
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots){
                                polls.add(doc.toObject(Poll.class));
                            }
                            //Toast.makeText(getApplicationContext(),"Data Retrieved Successfully",
                            //        Toast.LENGTH_SHORT).show();

                            if (!accType.equals("Instructor")){
                                getStdPolls();
                            }
                            setRecyclerViewAdapter();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void getStdPolls(){
        for (Poll p : polls){
            if (p.getResults() != null){
                if (p.getResults().containsKey(uid)){
                    states.add("C");
                }else {
                    states.add("N");
                }
            }else {
                states.add("N");
            }
        }
    }

    private void setRecyclerViewAdapter(){
        rvSetOnItemClickListener();
        adapter = new PollsAdapter(getApplicationContext(),accType,states,polls,itemClickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    // delete selected instructor
    private void rvSetOnItemClickListener(){
        itemClickListener = new PollsAdapter.ItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                Intent intent = new Intent(PollsListActivity.this,PollActivity.class);
                intent.putExtra("poll",polls.get(position));
                intent.putExtra("docID",docID);
                startActivity(intent);
            }
        };
    }

    private void setComponentsIDs(){
        recyclerView = findViewById(R.id.pollsRVId);
    }
}
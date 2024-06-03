package com.example.loginmodule.PollQuesModule;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.loginmodule.Course.DatePickerFragment;
import com.example.loginmodule.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreatePollActivity extends AppCompatActivity implements DatePickerFragment.DateSetListener{
    private EditText titleTxt, questionTxt, option1Txt, option2Txt;
    private TextView timeTxt;
    private CardView sendPollBtn, endTimeBtn, addOptionBtn;
    private LinearLayout optionsContainer;
    private ProgressBar progressBar;
    private Poll poll = new Poll();
    private ArrayList<EditText> editTexts = new ArrayList<>();
    private String docID;
    private String time,title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_poll);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        docID = getIntent().getStringExtra("docId");

        setComponentsIds();

        timeTxt.setText(getCurrentDate());

        addOptionBtn.setOnClickListener(this::addOptionEditTxt);
        sendPollBtn.setOnClickListener(this::sendPoll);
        endTimeBtn.setOnClickListener(this::setEndTime);
    }

    private String getCurrentDate(){
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.DAY_OF_MONTH) + "/" + (c.get(Calendar.MONTH)+1) +
                "/" + (c.get(Calendar.YEAR));
    }

    @Override
    public void onDateSet(int year, int month, int day) {
        String time = day + "/" + (month+1) + "/" + year;
        poll.setEndDate(time);
        timeTxt.setText(time);
    }

    private void setEndTime(View view){
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setDateSetListener(this);
        datePickerFragment.show(getSupportFragmentManager(), "date picker");
    }

    private void sendPoll(View view){
        String question = questionTxt.getText().toString();
        if (!question.isEmpty()){
            if (poll.getEndDate() != null){
                if (!editTexts.get(0).getText().toString().isEmpty()){
                    if (!editTexts.get(1).getText().toString().isEmpty()){
                        poll.setQuestion(question);
                        poll.setOptions(new ArrayList<>());
                        for (EditText txt : editTexts){
                            String opt = txt.getText().toString();
                            if (!opt.isEmpty()){
                                poll.getOptions().add(opt);
                            }
                        }
                        String title = titleTxt.getText().toString();
                        if(title.isEmpty()){
                            poll.setTitle("UnTitled Poll");
                        }else {
                            poll.setTitle(title);
                        }
                        poll.setStartDate(getCurrentDate());
                        countPolls();
                    }else{
                        editTexts.get(1).setError("This field must be field");
                    }
                }else{
                    editTexts.get(0).setError("This field must be field");
                }
            }else {
                Toast.makeText(getApplicationContext(),"Must set end Date",
                        Toast.LENGTH_SHORT).show();
            }

        }else {
            questionTxt.setError("This field must be filled");
        }
    }

    private void countPolls(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("CourseGroups").document(docID).collection("Polls").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            int count = 0;
                            for (QueryDocumentSnapshot doc : task.getResult()){
                                count++;
                            }
                            //Toast.makeText(getApplicationContext(),"Count:" + count,
                            //        Toast.LENGTH_SHORT).show();
                            poll.setId(String.valueOf(count+1));
                            storeData(count);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Read Database Error",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void storeData(int count){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("CourseGroups").document(docID).collection("Polls")
                .document("Poll"+(count+1)).set(poll)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(),"Poll Sent Successfully",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),"Failed sending poll",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void addOptionEditTxt(View view){
        EditText editText = new EditText(getApplicationContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(28,10,28,10);
        editText.setLayoutParams(layoutParams);
        editText.setHint("Option " + (editTexts.size()+1));
        editText.setHintTextColor(getColor(R.color.grey));
        editText.setTextColor(getColor(R.color.black));
        //editText.setTypeface(Typeface.createFromAsset(getAssets(), "font/roboto_regular.ttf"));
        editText.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this,
                        R.drawable.option_icon), null,null,null);
        editText.setCompoundDrawablePadding(10);
        optionsContainer.addView(editText);
        editTexts.add(editText);
    }

    private void setComponentsIds(){
        optionsContainer = findViewById(R.id.optionsContainerId);
        titleTxt = findViewById(R.id.pollTitleEditTxtId);
        questionTxt = findViewById(R.id.questionTxtId);
        timeTxt = findViewById(R.id.timeTxtId);
        sendPollBtn = findViewById(R.id.sendPollBtnId);
        endTimeBtn = findViewById(R.id.endTimeBtnId);
        addOptionBtn = findViewById(R.id.addOptionBtnId);
        progressBar = findViewById(R.id.progressBarId);
        option1Txt = findViewById(R.id.option1TxtId);
        option2Txt = findViewById(R.id.option2TxtId);
        editTexts.add(option1Txt);
        editTexts.add(option2Txt);
    }
}
package com.example.loginmodule.PollQuesModule;

import android.Manifest;
import android.content.ContentValues;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;


import com.example.loginmodule.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class PollActivity extends AppCompatActivity {

    private TextView questionTV, titleTV;
    private RadioGroup radioGroup;
    private ArrayList<RadioButton> radioBtns = new ArrayList<>();
    private ArrayList<TextView> percentagesTV = new ArrayList<>();
    private ImageView chartIcon, saveBtn;
    private BarChart barChart;
    private CardView downloadCSVBtn, voteBtn;
    private LinearLayout percContainer;
    private String uid, accType, docID;
    private Poll poll;
    private BarData barData;
    private BarDataSet barDataSet;
    private ArrayList<BarEntry> barEntriesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_poll);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        uid = sharedPreferences.getString(getString(R.string.prefKey_stdID), null);
        accType = sharedPreferences.getString("accType",null);
        poll = (Poll) getIntent().getSerializableExtra("poll");
        docID = getIntent().getStringExtra("docID");

        setComponentsIds();
        setUIComponents();

        getSelectedOption();
        voteBtn.setOnClickListener(this::storeVote);
        saveBtn.setOnClickListener(this::saveImage);
        downloadCSVBtn.setOnClickListener(this::downloadResultsCSV);
        //toBack();
    }

    private File createCSVFile() {
        String str = "poll result"+poll.getId()+".csv";
        int i;
        File csvFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                str);

        try (FileWriter writer = new FileWriter(csvFile)) {
            str = "Question," + poll.getQuestion()+",\n";
            writer.append(str);
            i=0;
            //writer.append("Analytics,\n");
            //writer.append("Options,Votes,\n");
            int[] arr = calculatePercentages();
            for (String ss: poll.getOptions()){
                str = "Option" + (i+1) + "," + ss + ","+ arr[i] + ",\n";
                writer.append(str);
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return csvFile;
    }

    private void downloadResultsCSV(View view) {
        File csvFile = createCSVFile();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "sample_data.csv");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/CSVFiles");

        Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);

        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri);
                 FileInputStream inputStream = new FileInputStream(csvFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                Toast.makeText(this, "CSV file saved!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to save CSV file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImage(View view){
        if (barChart.getVisibility() == View.VISIBLE){
            checkPermission();
        }else{
            Toast.makeText(getApplicationContext(),"Analytics isn't available",
                    Toast.LENGTH_SHORT).show();
        }
    }


    public void checkPermission(){
        final int REQUEST_PERMISSION = 100;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        } else {
            // Permission is already granted.
            saveChartAsImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveChartAsImage();
            } else {
                Toast.makeText(this, "Permission required to store image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap getBitmap(BarChart chart) {
        chart.setDrawingCacheEnabled(true);
        chart.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(chart.getDrawingCache());
        chart.setDrawingCacheEnabled(false);
        return bitmap;
    }

    private void saveChartAsImage() {
        Bitmap bitmap = getBitmap(barChart);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "bar_chart"+poll.getId()+".png");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BarCharts");

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                    Toast.makeText(this, "Chart saved as image!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save chart as image", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getSelectedOption(){
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = findViewById(checkedId);
                int i;
                boolean found = false;
                if (poll.getResults() == null){
                    poll.setResults(new HashMap<>());
                }
                i=0;
                while (i<radioBtns.size() && !found){
                    if (radioBtns.get(i).getText().equals(radioButton.getText())){
                        found = true;
                    }else{
                        i++;
                    }
                }
                //Toast.makeText(getApplicationContext(),"Option " + (i+1),
                //        Toast.LENGTH_SHORT).show();
                poll.getResults().put(uid,String.valueOf(i+1));
            }
        });
    }

    private void storeVote(View view){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("CourseGroups").document(docID).collection("Polls")
                .document("Poll" + poll.getId()).update("results",poll.getResults())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(),"Voted Successfully",
                                Toast.LENGTH_SHORT).show();

                        int[] arr =calculatePercentages();
                        if (barChart.getVisibility() == View.VISIBLE){
                            updateTVs(arr);
                            updateChartBar(arr);
                        }else{
                            initializeChartBar(arr);
                            updateTVs(arr);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private int[] calculatePercentages(){
        int[] vals = new int[poll.getOptions().size()];
        int i,index;
        for (i=0;i<vals.length;i++){
            vals[i] = 0;
        }
        for (String s : poll.getResults().values()){
            index = Integer.parseInt(s)-1;
            vals[index] +=1;
        }
        return vals;
    }

    private void updateTVs(int[] arr){
        int i=0;
        float val;
        for (TextView tv: percentagesTV){
            val = ((float) arr[i]/poll.getResults().size())*100;
            tv.setText(String.format("%.1f", val)+"%");
            i++;
        }
    }

    private void initializeChartBar(int[] arr){
        // calling method to get bar entries.
        int i;
        barEntriesArrayList = new ArrayList<>();
        for (i=0;i<poll.getOptions().size();i++){
            barEntriesArrayList.add(new BarEntry(i, arr[i]));
        }
        // creating a new bar data set.
        barDataSet = new BarDataSet(barEntriesArrayList, "Options");
        // creating a new bar data and
        // passing our bar data set.

        barData = new BarData(barDataSet);
        // below line is to set data
        // to our bar chart.
        barChart.setData(barData);

        // adding color to our bar data set.
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        // setting text color.
        barDataSet.setValueTextColor(Color.BLACK);

        // setting text size
        barDataSet.setValueTextSize(10f);
        barChart.getDescription().setEnabled(false);

        if (barChart.getVisibility() == View.GONE){
            chartIcon.setVisibility(View.GONE);
            barChart.setVisibility(View.VISIBLE);
        }
    }

    private void updateChartBar(int[] arr){
        // calling method to get bar entries.
        int i;
        barEntriesArrayList.clear();
        for (i=0;i<poll.getOptions().size();i++){
            barEntriesArrayList.add(new BarEntry(i, arr[i]));
        }
        barChart.invalidate();
    }

    private void initializeOptions(){
        int i,len = poll.getOptions().size();
        for (i=0;i<2;i++){
            radioBtns.get(i).setText(poll.getOptions().get(i));
        }
        if (poll.getOptions().size()>2){
            for (i=2;i<len;i++){
                addNewRadioButton(i);
                addNewTextView(i);
            }
        }
    }

    private void addNewRadioButton(int i){
        RadioButton radioButton = new RadioButton(this);
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0,20,0,0);
        radioButton.setLayoutParams(params);
        radioButton.setText(poll.getOptions().get(i));
        radioButton.setTextSize(16);
        radioButton.setTextColor(getColor(R.color.white));
        radioGroup.addView(radioButton);
        radioBtns.add(radioButton);
    }

    private void addNewTextView(int i){
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                80
        );
        params.setMargins(0,10,0,0);
        textView.setLayoutParams(params);
        textView.setText("%0");
        textView.setTextColor(getColor(R.color.red));
        textView.setGravity(Gravity.CENTER);

        percentagesTV.add(textView);
        percContainer.addView(textView);
    }
    private void setUIComponents(){
        titleTV.setText(poll.getTitle());
        questionTV.setText(poll.getQuestion());
        initializeOptions();
        if (!accType.equals("Instructor")){
            saveBtn.setVisibility(View.GONE);
            downloadCSVBtn.setVisibility(View.GONE);
            if (getCurrentDate().compareTo(reverseString(poll.getEndDate()))>0){
                voteBtn.setVisibility(View.GONE);
            }
        }else{
            voteBtn.setVisibility(View.GONE);
        }
        if (poll.getOptions() == null){
            barChart.setVisibility(View.GONE);
        }
        if (poll.getResults() != null){
            if (poll.getResults().containsKey(uid)){
                radioBtns.get(Integer.parseInt(poll.getResults().get(uid))-1).setChecked(true);
            }
            int[] arr = calculatePercentages();
            updateTVs(arr);
            initializeChartBar(arr);
        }
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

    private void toBack() {

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getApplicationContext(),PollsListActivity.class);
                intent.putExtra("docID",docID);
                startActivity(intent);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void setComponentsIds(){
        titleTV = findViewById(R.id.titleTVId);
        questionTV = findViewById(R.id.questionTVId);
        radioGroup = findViewById(R.id.radioGrId);
        radioBtns.add(findViewById(R.id.optBtn1Id));
        radioBtns.add(findViewById(R.id.optBtn2Id));
        percentagesTV.add(findViewById(R.id.percent1TVId));
        percentagesTV.add(findViewById(R.id.percent2TVId));
        percContainer = findViewById(R.id.percentagesContainerId);
        voteBtn = findViewById(R.id.voteBtnId);

        barChart = findViewById(R.id.barChartId);
        chartIcon = findViewById(R.id.iconId);
        saveBtn = findViewById(R.id.saveBtnId);
        downloadCSVBtn = findViewById(R.id.downloadCSVBtnId);

    }
}
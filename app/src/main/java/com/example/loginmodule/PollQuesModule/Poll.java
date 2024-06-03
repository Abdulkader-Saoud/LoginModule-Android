package com.example.loginmodule.PollQuesModule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Poll implements Serializable {
    private String id;
    private String question;
    private ArrayList<String> options;
    private String title, endDate, startDate;
    private HashMap<String,String> results;

    public Poll(){

    }

    public Poll(String question) {
        this.question = question;
        this.options = new ArrayList<>();
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, String> getResults() {
        return results;
    }

    public void setResults(HashMap<String, String> results) {
        this.results = results;
    }
}

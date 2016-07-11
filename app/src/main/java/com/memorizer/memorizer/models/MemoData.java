package com.memorizer.memorizer.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by YS on 2016-06-21.
 */
public class MemoData implements Serializable{
    private int _id = 0;
    private String content;
    private int term;
    private Calendar whileDate;
    private int timeOfHour;
    private int timeOfMinute;
    private String posted = "";

    public MemoData() {
        this.content = "";
        this.term = 1;
        this.whileDate = null;
        Random random = new Random(System.currentTimeMillis());
        this.timeOfHour = random.nextInt(24);
        this.timeOfMinute = random.nextInt(60);
    }

    public MemoData(int _id, String content, Calendar whileDate, int term, int hour, int minute, String posted) {
        this._id = _id;
        this.content = content;
        this.term = term;
        this.whileDate = whileDate;
        this.timeOfHour = hour;
        this.timeOfMinute = minute;
        this.posted = posted;
    }

    public int get_id() {
        return _id;
    }

    public String getContent() {
        return content;
    }

    public int getTerm() {
        return term;
    }

    public Calendar getWhileDate() {
        return whileDate;
    }

    public int getTimeOfHour() {
        return timeOfHour;
    }

    public int getTimeOfMinute() {
        return timeOfMinute;
    }

    public String getPosted() {
        return posted;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public void setWhileDate(Calendar whileDate) {
        this.whileDate = whileDate;
    }

    public void setTimeOfHour(int timeOfHour) {
        this.timeOfHour = timeOfHour;
    }

    public void setTimeOfMinute(int timeOfMinute) {
        this.timeOfMinute = timeOfMinute;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }
}

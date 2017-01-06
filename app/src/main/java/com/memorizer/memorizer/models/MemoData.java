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
    private String label;
    private int labelPos;
    private boolean isRandom;
    private int timeOfHour;
    private int timeOfMinute;
    private String posted = "";

    public MemoData() {
        this.content = "";
        this.term = 1;
        this.whileDate = null;
        this.label = "";
        this.labelPos = 0;
        this.isRandom = true;
        Random random = new Random(System.currentTimeMillis());
        this.timeOfHour = random.nextInt(24);
        this.timeOfMinute = random.nextInt(60);
    }

    public MemoData(int _id, String content, Calendar whileDate, int term, String label, int labelPos, int isRandom, int hour, int minute, String posted) {
        this._id = _id;
        this.content = content;
        this.term = term;
        this.whileDate = whileDate;
        this.label = label;
        this.labelPos = labelPos;
        this.isRandom = Boolean.valueOf(""+isRandom);
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

    public String getLabel() {
        return label;
    }

    public int getLabelPos() {
        return labelPos;
    }

    public boolean isRandom() {
        return isRandom;
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

    public void setLabel(String label) {
        this.label = label;
    }

    public void setLabelPos(int labelPos) {
        this.labelPos = labelPos;
    }

    public void setRandom(boolean isRandom) {
        this.isRandom = isRandom;
    }

    public void setRandom(int isRandom) {
        if (isRandom > 0)
            this.isRandom = true;
        else
            this.isRandom = false;
    }

    public void setTimeOfHour(int timeOfHour) {
        this.isRandom = false;
        this.timeOfHour = timeOfHour;
    }

    public void setTimeOfMinute(int timeOfMinute) {
        this.isRandom = false;
        this.timeOfMinute = timeOfMinute;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }

    public String printItem() {
        String str = "ID: "+_id
                +"\nContent: "+content
                +"\nTerm: "+term
                +"\nLabel: "+label
                +"\nLabelPos: "+labelPos
                +"\nisRandom: "+isRandom
                +"\nHour: "+timeOfHour
                +"\nMinute: "+timeOfMinute
                +"\nPosted: "+posted;

        return str;
    }
}
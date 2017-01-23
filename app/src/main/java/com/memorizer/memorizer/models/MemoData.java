package com.memorizer.memorizer.models;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

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
    private String edited= "";
    private boolean isMarkdown;
    private ArrayList<CheckListData> checkList = new ArrayList<>();

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
        this.isMarkdown = false;
    }

    public MemoData(int _id, String content, Calendar whileDate, int term, String label,
                    int labelPos, int isRandom, int hour, int minute, String posted, String edited,
                    boolean isMarkdown, ArrayList<CheckListData> checkList) {
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
        this.edited = edited;
        this.isMarkdown = isMarkdown;
        this.checkList = checkList;

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
        return changeTimeZone(posted, TimeZone.getTimeZone("GMT"), TimeZone.getDefault());
    }

    public String getRawPosted() {
        return posted;
    }

    public String getEdited() {
        return changeTimeZone(edited, TimeZone.getTimeZone("GMT"), TimeZone.getDefault());
    }

    public String getRawEdited() {
        return edited;
    }

    public boolean isMarkdown() {
        return isMarkdown;
    }

    public ArrayList<CheckListData> getCheckList() {
        return checkList;
    }

    public ArrayList<Boolean> getChecks() {
        ArrayList<Boolean> tempList = new ArrayList<>();
        for (CheckListData each : checkList) {
            tempList.add(each.isCheck());
        }
        return tempList;
    }

    public ArrayList<String> getCheckMessages() {
        ArrayList<String> tempList = new ArrayList<>();
        for (CheckListData each : checkList) {
            tempList.add(each.getCheckMessage());
        }
        return tempList;
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
        this.posted = changeTimeZone(posted, TimeZone.getDefault(), TimeZone.getTimeZone("GMT"));
    }

    public void setEdited(String edited) {
        this.edited = changeTimeZone(edited, TimeZone.getDefault(), TimeZone.getTimeZone("GMT"));
    }

    public void setMarkdown(boolean isMarkdown) {
        this.isMarkdown = isMarkdown;
    }

    public void setCheckList(ArrayList<CheckListData> checkList) {
        this.checkList = checkList;
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
                +"\nPosted: "+posted
                +"\nEdited: "+edited
                +"\nCheckList: "+checkList
                +"\nisMarkdown: "+isMarkdown;

        return str;
    }

    public String changeTimeZone(String dateString, TimeZone originZone, TimeZone TargetZone) {
        // 나라별 시간대 변경
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(originZone);

        try {
            // 날짜 포맷 설정
            dateFormat.parse(dateString);
            dateFormat.setTimeZone(TargetZone);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date date = dateFormat.getCalendar().getTime();

        return dateFormat.format(date);
    }
}

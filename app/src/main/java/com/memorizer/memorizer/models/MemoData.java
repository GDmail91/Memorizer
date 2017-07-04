package com.memorizer.memorizer.models;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;

import com.memorizer.memorizer.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
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

    public MemoData(int _id, String content, Calendar whileDate, int term, String label,
                    int labelPos, boolean isRandom, int hour, int minute, String posted, String edited,
                    boolean isMarkdown, ArrayList<CheckListData> checkList) {
        this._id = _id;
        this.content = content;
        this.term = term;
        this.whileDate = whileDate;
        this.label = label;
        this.labelPos = labelPos;
        this.isRandom = isRandom;
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

    public int getLabelPosDraw() {
        int color = 0;
        switch (getLabelPos()) {
            case Constants.COLOR_BLUE:
                color = R.drawable.color_selector_blue;
                break;
            case Constants.COLOR_RED:
                color = R.drawable.color_selector_red;
                break;
            case Constants.COLOR_ORANGE:
                color = R.drawable.color_selector_orange;
                break;
            case Constants.COLOR_GREEN:
                color = R.drawable.color_selector_green;
                break;
            default:
                color = R.drawable.color_selector;
        }

        return color;
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

    public String getTime() {
        return makeTime(timeOfHour, timeOfMinute);
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
        this.posted = posted;
    }

    public void setPosted(String posted, TimeZone timezone) {
        this.posted = changeTimeZone(posted, timezone, TimeZone.getTimeZone("GMT"));
    }

    public void setEdited(String edited) {
        this.edited = edited;
    }

    public void setEdited(String edited, TimeZone timezone) {
        this.edited = changeTimeZone(edited, timezone, TimeZone.getTimeZone("GMT"));
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

    public static String changeTimeZone(String dateString, TimeZone originZone, TimeZone TargetZone) {
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

    public Long getPostedToLong() {

        // 나라별 시간대 변경
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            // 날짜 포맷 설정
            dateFormat.parse(posted);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateFormat.getCalendar().getTimeInMillis();
    }

    public Long getEditedToLong() {

        // 나라별 시간대 변경
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            // 날짜 포맷 설정
            dateFormat.parse(edited);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateFormat.getCalendar().getTimeInMillis();
    }

    public String getFileName() {
        return posted.replace(":","_") + " " + _id + ".json";
    }

    public File saveFile(Context context) {
        try {
            File tempFile = File.createTempFile(getFileName(), null, context.getCacheDir());
            BufferedWriter fw = new BufferedWriter(new FileWriter(tempFile, true));

            // 파일안에 메모 정보 쓰기
            fw.write("{");
            fw.write("\"id\":"+_id+",");
            fw.write("\"content\":\"" + TextUtils.htmlEncode(content)+ "\",");
            fw.write("\"term\":"+term+",");
            fw.write("\"whileDate\":"+whileDate.getTimeInMillis()+",");
            fw.write("\"label\":\""+ TextUtils.htmlEncode(label)+"\",");
            fw.write("\"labelPos\":"+labelPos+",");
            fw.write("\"isRandom\":"+isRandom+",");
            fw.write("\"timeOfHour\":"+timeOfHour+",");
            fw.write("\"timeOfMinute\":"+timeOfMinute+",");
            fw.write("\"posted\":\""+posted+"\",");
            fw.write("\"edited\":\""+edited+"\",");
            fw.write("\"isMarkdown\":"+isMarkdown+",");
            String isCheck = "";
            String checkMessage = "";
            Iterator<CheckListData> listIter = checkList.iterator();
            while (listIter.hasNext()) {
                CheckListData data = listIter.next();
                isCheck += data.isCheck();
                checkMessage += TextUtils.htmlEncode(data.getCheckMessage());
                if (listIter.hasNext()) {
                    isCheck += ",";
                    checkMessage += ",";
                }
            }
            fw.write("\"isCheck\":\"" + isCheck + "\",");
            fw.write("\"checkMessage\":\"" + checkMessage + "\"");
            fw.write("}");
            fw.flush();

            fw.close();

            return tempFile;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected String makeTime(int hourOfDay, int minute) {
        String hourStr, minStr, ampm = "am";

        if (hourOfDay >= 22) {
            hourStr = String.valueOf(hourOfDay - 12);
            ampm = "pm";
        } else if (hourOfDay > 12) {
            hourStr = "0" + String.valueOf(hourOfDay - 12);
            ampm = "pm";
        } else if (hourOfDay == 0) {
            hourStr = "12";
        } else if (hourOfDay < 10) {
            hourStr = "0" + String.valueOf(hourOfDay);
        } else if (hourOfDay == 12) {
            hourStr = String.valueOf(hourOfDay);
            ampm = "pm";
        } else
            hourStr = String.valueOf(hourOfDay);

        if (minute < 10) {
            minStr = "0" + String.valueOf(minute);
        } else
            minStr = String.valueOf(minute);

        return hourStr +" : "+minStr+ " "+ampm;
    }
}

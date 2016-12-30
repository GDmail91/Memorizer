package com.memorizer.memorizer.models;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by YS on 2016-07-11.
 */
public class ScheduleData implements Serializable {
    private int _id = 0;
    private int memoId;
    private Calendar alarmDate;

    public ScheduleData(int memoId, Calendar alarmDate) {
        this.memoId = memoId;
        this.alarmDate = alarmDate;
    }

    public ScheduleData(int _id, int memoId, Calendar alarmDate) {
        this._id = _id;
        this.memoId = memoId;
        this.alarmDate = alarmDate;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public int getMemoId() {
        return memoId;
    }

    public void setMemoId(int memoId) {
        this.memoId = memoId;
    }

    public Calendar getAlarmDate() {
        return alarmDate;
    }

    public void setAlarmDate(Calendar alarmDate) {
        this.alarmDate = alarmDate;
    }

    public int getDaysNext() {
        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis());
        int term = alarmDate.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR);
        if (term < 0) {
            term += 365;
        }
        return term;
    }

    public String toString() {

        return memoId+"-"+alarmDate.get(Calendar.DAY_OF_MONTH)+"-"+alarmDate.get(Calendar.HOUR_OF_DAY)+"-"+alarmDate.get(Calendar.MINUTE);
    }
}

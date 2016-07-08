package com.memorizer.memorizer.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.memorizer.memorizer.create.MemoData;

import java.util.Calendar;

/**
 * Created by YS on 2016-06-27.
 */
public class Scheduler {
    static Scheduler scheduler = new Scheduler();
    final String TAG = "Scheduler";
    public static final long NEXT = 24 * 3600 * 1000;

    public static Scheduler getScheduler() {return scheduler;}

    public void setSchedule(Context context, MemoData memoData) {
        // 종료날짜가 아닐경우 실행
        if (!checkEndDay(memoData.getWhileDate(), memoData.getTerm())) {
            setAlarm(context, memoData, 0);
        }
    }

    public void setSchedule(Context context, MemoData memoData, int next) {
        // 종료날짜가 아닐경우 실행
        if (!checkEndDay(memoData.getWhileDate(), memoData.getTerm())) {
            setAlarm(context, memoData, next);
        }
    }

    protected void setAlarm(Context context, MemoData memoData, int next) {
        Intent intent = new Intent("com.memorizer.memorizer.alarmTrigger");
        intent.putExtra("memoId", memoData);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, memoData.get_id(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar setDay = Calendar.getInstance();

        // 현재 시간부터 다음 간격시간 후에 알림
        if (next != 0) {
            setDay.setTimeInMillis(System.currentTimeMillis());
        } else {
            setDay.setTimeInMillis(System.currentTimeMillis() + ((long) memoData.getTerm() * NEXT)); // 현재 날짜에서 Term 기간만큼 증가후 저장
            //setDay.setTimeInMillis(System.currentTimeMillis());

            setDay.set(
                    setDay.get(Calendar.YEAR),
                    setDay.get(Calendar.MONTH),
                    setDay.get(Calendar.DAY_OF_MONTH),
                    memoData.getTimeOfHour(),
                    memoData.getTimeOfMinute(),
                    0);
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, setDay.getTimeInMillis(), pIntent);
    }

    protected boolean checkEndDay(Calendar endDay, int term) {
        if (endDay == null
        || endDay.getTimeInMillis() == 0) {
            return false;
        }

        Calendar setDay = Calendar.getInstance();
        setDay.setTimeInMillis(System.currentTimeMillis() + term * 1000);

        return setDay.get(Calendar.DAY_OF_YEAR) == endDay.get(Calendar.DAY_OF_YEAR);
    }
}
